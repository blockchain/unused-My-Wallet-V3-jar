package info.blockchain.wallet.payload;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.Address;
import info.blockchain.wallet.bip44.HDAccount;
import info.blockchain.wallet.bip44.HDWallet;
import info.blockchain.wallet.bip44.WalletFactory;
import info.blockchain.wallet.payload.data.Account;
import info.blockchain.wallet.payload.data.Payload;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.AddressFormatException;
import retrofit2.Response;

public class HDPayloadBridge {

    private final int DEFAULT_MNEMONIC_LENGTH = 12;
    private final int DEFAULT_NEW_WALLET_SIZE = 1;
    private final String DEFAULT_PASSPHRASE = "";

    private final WalletFactory bip44WalletFactory;
    private BlockExplorer blockExplorer;

    public HDPayloadBridge() throws IOException {
        this.bip44WalletFactory = new WalletFactory(PersistentUrls.getInstance().getCurrentNetworkParams());
        this.blockExplorer = new BlockExplorer(BlockchainFramework.getRetrofitServerInstance(), BlockchainFramework.getApiCode());
    }

    public class HDWalletPayloadPair {
        Payload payload;
        HDWallet wallet;
    }

    public HDWalletPayloadPair createHDWallet(String defaultAccountName) throws Exception {

        HDWalletPayloadPair result = new HDWalletPayloadPair();
        result.wallet = bip44WalletFactory.newWallet(DEFAULT_MNEMONIC_LENGTH, DEFAULT_PASSPHRASE, DEFAULT_NEW_WALLET_SIZE);
        result.payload = createBlockchainWallet(defaultAccountName, result.wallet);

        return result;
    }

    public HDWalletPayloadPair restoreHDWallet(String seed, String defaultAccountName) throws Exception {

        return restoreWallet(seed, defaultAccountName, DEFAULT_PASSPHRASE);
    }

    public HDWalletPayloadPair restoreHDWallet(String seed, String defaultAccountName, String passphrase) throws Exception {

        return restoreWallet(seed, defaultAccountName, passphrase);
    }

    private HDWalletPayloadPair restoreWallet(String seed, String defaultAccountName, String passphrase) throws Exception {

        HDWalletPayloadPair result = new HDWalletPayloadPair();
        result.wallet = bip44WalletFactory.restoreWallet(seed, passphrase, DEFAULT_NEW_WALLET_SIZE);

        int index = 0;
        int walletSize = 1;
        final int lookAheadTotal = 10;
        int lookAhead = lookAheadTotal;

        while (lookAhead > 0) {

            String xpub = result.wallet.getAccount(index).getXpub();
            if (hasTransactions(blockExplorer, xpub)) {
                lookAhead = lookAheadTotal;
                walletSize++;
            }

            result.wallet.addAccount();
            index++;
            lookAhead--;
        }

        result.wallet = bip44WalletFactory.restoreWallet(seed, passphrase, walletSize);
        result.payload = createBlockchainWallet(defaultAccountName, result.wallet);

        return result;
    }

    private boolean hasTransactions(BlockExplorer blockExplorer, String xpub) throws Exception {

        Response<HashMap<String, info.blockchain.api.data.Balance>> exe = blockExplorer
            .getBalance(Arrays.asList(xpub), BlockExplorer.TX_FILTER_ALL).execute();

        if(!exe.isSuccessful()) {
            throw new Exception(exe.code()+" "+exe.errorBody().string());
        }

        HashMap<String, info.blockchain.api.data.Balance> body = exe.body();

        return body.get(xpub).getNTx() > 0L;
    }

    public HDWallet getHDWalletFromPayload(Payload payload) throws Exception {
        return bip44WalletFactory.restoreWallet(payload.getHdWallet().getSeedHex(),
                DEFAULT_PASSPHRASE,
                payload.getHdWallet().getAccounts().size());
    }

    public HDWallet decryptWatchOnlyWallet(Payload payload, String decrypted_hex) throws Exception {
        return bip44WalletFactory.restoreWallet(decrypted_hex, DEFAULT_PASSPHRASE, payload.getHdWallet().getAccounts().size());
    }

    private Payload createBlockchainWallet(String defaultAccountName, HDWallet hdw) {

        String guid = UUID.randomUUID().toString();
        String sharedKey = UUID.randomUUID().toString();

        Payload payload = new Payload();
        payload.setGuid(guid);
        payload.setSharedKey(sharedKey);

        info.blockchain.wallet.payload.data.HDWallet payloadHDWallet = new info.blockchain.wallet.payload.data.HDWallet();
        payloadHDWallet.setSeedHex(hdw.getSeedHex());

        List<HDAccount> hdAccounts = hdw.getAccounts();
        List<info.blockchain.wallet.payload.data.Account> payloadAccounts = new ArrayList<Account>();

        int accountNumber = 1;
        for (int i = 0; i < hdAccounts.size(); i++) {

            String label = defaultAccountName;
            if (accountNumber > 1) {
                label = defaultAccountName + " " + accountNumber;
            }
            info.blockchain.wallet.payload.data.Account account = new info.blockchain.wallet.payload.data.Account(label);
            accountNumber++;

            String xpub = hdw.getAccounts().get(i).getXpub();
            account.setXpub(xpub);
            String xpriv = hdw.getAccounts().get(i).getXPriv();
            account.setXpriv(xpriv);

            payloadAccounts.add(account);
        }
        payloadHDWallet.setAccounts(payloadAccounts);

        payload.setHdWallets(payloadHDWallet);

        payload.setUpgraded(true);

        return payload;
    }

    /*
    When called from Android - First apply PRNGFixes
     */
    public boolean upgradeV2PayloadToV3(Payload payload, String secondPassword, boolean isNewlyCreated, String defaultAccountName) throws Exception {

        //
        // create HD wallet and sync w/ payload
        //
        if (payload.getHdWalletList() == null || payload.getHdWalletList().size() == 0) {

            String xpub;
            int attempts = 0;
            boolean isEmpty;

            do {

                attempts++;

                HDWallet wallet = bip44WalletFactory.newWallet(DEFAULT_MNEMONIC_LENGTH, DEFAULT_PASSPHRASE, DEFAULT_NEW_WALLET_SIZE);
                info.blockchain.wallet.payload.data.HDWallet hdw = new info.blockchain.wallet.payload.data.HDWallet();
                String seedHex = wallet.getSeedHex();
                if (!StringUtils.isEmpty(secondPassword)) {
                    seedHex = DoubleEncryptionFactory.getInstance().encrypt(
                            seedHex,
                            payload.getSharedKey(),
                            secondPassword.toString(),
                            payload.getDoubleEncryptionPbkdf2Iterations());
                }

                hdw.setSeedHex(seedHex);
                List<Account> accounts = new ArrayList<Account>();
                xpub = wallet.getAccount(0).getXpub();
                if (isNewlyCreated) {
                    accounts.add(new Account());
                    accounts.get(0).setXpub(xpub);
                    String xpriv = wallet.getAccount(0).getXPriv();
                    if (!StringUtils.isEmpty(secondPassword)) {
                        xpriv = DoubleEncryptionFactory.getInstance().encrypt(
                                xpriv,
                                payload.getSharedKey(),
                                secondPassword,
                                payload.getDoubleEncryptionPbkdf2Iterations());
                    }
                    accounts.get(0).setXpriv(xpriv);
                }
                hdw.setAccounts(accounts);
                payload.setHdWallets(hdw);
                payload.setUpgraded(true);

                payload.getHdWallet().getAccounts().get(0).setLabel(defaultAccountName);

                isEmpty = !hasTransactions(blockExplorer, xpub);

            } while (!isEmpty && attempts < 3);

            return !(!isEmpty && isNewlyCreated);
        }

        List<Account> accounts = payload.getHdWallet().getAccounts();
        payload.getHdWallet().setAccounts(accounts);

        return true;
    }

    public Address getAddressAt(String xpub, int chain, int addressIndex) throws AddressFormatException {

        HDAccount account = new HDAccount(PersistentUrls.getInstance().getCurrentNetworkParams(), xpub);
        return account.getChain(chain).getAddressAt(addressIndex);
    }
}