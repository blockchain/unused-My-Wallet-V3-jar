package info.blockchain.wallet.payload;

import info.blockchain.api.Balance;
import info.blockchain.bip44.Address;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.DoubleEncryptionFactory;

import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.params.MainNetParams;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HDPayloadBridge {

    private final int DEFAULT_MNEMONIC_LENGTH = 12;
    private final int DEFAULT_NEW_WALLET_SIZE = 1;
    private final String DEFAULT_PASSPHRASE = "";

    private final WalletFactory bip44WalletFactory;

    public HDPayloadBridge() {
        this.bip44WalletFactory = new WalletFactory();
    }

    public class HDWalletPayloadPair {
        Payload payload;
        Wallet wallet;
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

            String xpub = result.wallet.getAccount(index).xpubstr();

            boolean hasTransactions = (new Balance().getXpubTransactionCount(xpub) > 0L);
            if (hasTransactions) {
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

    public Wallet getHDWalletFromPayload(Payload payload) throws Exception {
        return bip44WalletFactory.restoreWallet(payload.getHdWallet().getSeedHex(),
                DEFAULT_PASSPHRASE,
                payload.getHdWallet().getAccounts().size());
    }

    public Wallet getHDWatchOnlyWalletFromXpubs(String[] xpubs) throws Exception {
        return new Wallet(MainNetParams.get(), xpubs);
    }

    public Wallet decryptWatchOnlyWallet(Payload payload, String decrypted_hex) throws Exception {
        return bip44WalletFactory.restoreWallet(decrypted_hex, DEFAULT_PASSPHRASE, payload.getHdWallet().getAccounts().size());
    }

    private Payload createBlockchainWallet(String defaultAccountName, Wallet hdw) {

        String guid = UUID.randomUUID().toString();
        String sharedKey = UUID.randomUUID().toString();

        Payload payload = new Payload();
        payload.setGuid(guid);
        payload.setSharedKey(sharedKey);

        HDWallet payloadHDWallet = new HDWallet();
        payloadHDWallet.setSeedHex(hdw.getSeedHex());

        List<info.blockchain.bip44.Account> hdAccounts = hdw.getAccounts();
        List<info.blockchain.wallet.payload.Account> payloadAccounts = new ArrayList<Account>();

        int accountNumber = 1;
        for (int i = 0; i < hdAccounts.size(); i++) {

            String label = defaultAccountName;
            if (accountNumber > 1) {
                label = defaultAccountName + " " + accountNumber;
            }
            info.blockchain.wallet.payload.Account account = new info.blockchain.wallet.payload.Account(label);
            accountNumber++;

            String xpub = hdw.getAccounts().get(i).xpubstr();
            account.setXpub(xpub);
            String xpriv = hdw.getAccounts().get(i).xprvstr();
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
    public boolean upgradeV2PayloadToV3(Payload payload, CharSequenceX secondPassword, boolean isNewlyCreated, String defaultAccountName) throws Exception {

        //
        // create HD wallet and sync w/ payload
        //
        if (payload.getHdWalletList() == null || payload.getHdWalletList().size() == 0) {

            String xpub;
            int attempts = 0;
            boolean no_tx = false;

            do {

                attempts++;

                Wallet wallet = bip44WalletFactory.newWallet(DEFAULT_MNEMONIC_LENGTH, DEFAULT_PASSPHRASE, DEFAULT_NEW_WALLET_SIZE);
                HDWallet hdw = new HDWallet();
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
                xpub = wallet.getAccount(0).xpubstr();
                if (isNewlyCreated) {
                    accounts.add(new Account());
                    accounts.get(0).setXpub(xpub);
                    String xpriv = wallet.getAccount(0).xprvstr();
                    if (!StringUtils.isEmpty(secondPassword)) {
                        xpriv = DoubleEncryptionFactory.getInstance().encrypt(
                                xpriv,
                                payload.getSharedKey(),
                                secondPassword.toString(),
                                payload.getDoubleEncryptionPbkdf2Iterations());
                    }
                    accounts.get(0).setXpriv(xpriv);
                }
                hdw.setAccounts(accounts);
                payload.setHdWallets(hdw);
                payload.setUpgraded(true);

                payload.getHdWallet().getAccounts().get(0).setLabel(defaultAccountName);

                try {
                    no_tx = (new Balance().getXpubTransactionCount(xpub) == 0L);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (!no_tx && attempts < 3);

            return !(!no_tx && isNewlyCreated);
        }

        List<Account> accounts = payload.getHdWallet().getAccounts();
        payload.getHdWallet().setAccounts(accounts);

        return true;
    }

    public Address getAddressAt(String xpub, int chain, int addressIndex) throws AddressFormatException {

        info.blockchain.bip44.Account account = new info.blockchain.bip44.Account(MainNetParams.get(), xpub);
        return account.getChain(chain).getAddressAt(addressIndex);
    }
}