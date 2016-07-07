package info.blockchain.wallet.payload;

import info.blockchain.bip44.Address;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.wallet.multiaddr.MultiAddrFactory;
import info.blockchain.wallet.util.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class HDPayloadBridge {

    public static final int RECEIVE_CHAIN = 0;

    private WalletFactory walletFactory;
    private Wallet wallet;
    private Wallet watchOnlyWallet;

    public HDPayloadBridge() {
        walletFactory = new WalletFactory();
    }

    public interface InitiatePayloadListener{
        void onInitSuccess();
        void onInitPairFail();
        void onInitCreateFail(String error);
    }

    public interface UpgradePayloadListener{
        void onDoubleEncryptionPasswordError();
        void onUpgradeSuccess();
        void onUpgradeFail();
    }

    public void initiatePayload(String sharedKey, String guid, CharSequenceX password, InitiatePayloadListener listener) {

        PayloadFactory.getInstance().get(guid,
                sharedKey,
                password);

        if (PayloadFactory.getInstance().get() == null || PayloadFactory.getInstance().get().stepNumber != 9) {
            String error = "";
            if (PayloadFactory.getInstance().get() != null) {
                error = error + " Failed at step: " + PayloadFactory.getInstance().get().stepNumber;
                if (PayloadFactory.getInstance().get().lastErrorMessage != null) {
                    error = error + " with message: " + PayloadFactory.getInstance().get().lastErrorMessage;
                }
            }
            listener.onInitCreateFail(error);
        }

        if (PayloadFactory.getInstance().get().getJSON() == null) {
            listener.onInitPairFail();
        }
        listener.onInitSuccess();
    }

    /*
    When called from Android - First apply PRNGFixes
     */
    public void upgradePayload(CharSequenceX password, CharSequenceX secondPassword, boolean isNewlyCreated, String defaultAccountName, UpgradePayloadListener listener) throws IOException, JSONException,
            DecoderException, AddressFormatException, MnemonicException.MnemonicLengthException,
            MnemonicException.MnemonicChecksumException, MnemonicException.MnemonicWordException {

        if (PayloadFactory.getInstance().get().isDoubleEncrypted()) {
            if (StringUtils.isEmpty(secondPassword) || !DoubleEncryptionFactory.getInstance().validateSecondPassword(
                    PayloadFactory.getInstance().get().getDoublePasswordHash(),
                    PayloadFactory.getInstance().get().getSharedKey(),
                    new CharSequenceX(secondPassword),
                    PayloadFactory.getInstance().get().getOptions().getIterations())) {

                listener.onDoubleEncryptionPasswordError();
            }
        }
        //
        // create HD wallet and sync w/ payload
        //
        if (PayloadFactory.getInstance().get().getHdWallets() == null ||
                PayloadFactory.getInstance().get().getHdWallets().size() == 0) {

            String xpub = null;
            int attempts = 0;
            boolean no_tx = false;

            do {

                attempts++;

                wallet = walletFactory.newWallet(12, "", 1);
                HDWallet hdw = new HDWallet();
                String seedHex = wallet.getSeedHex();
                if (!StringUtils.isEmpty(secondPassword)) {
                    seedHex = DoubleEncryptionFactory.getInstance().encrypt(
                            seedHex,
                            PayloadFactory.getInstance().get().getSharedKey(),
                            secondPassword.toString(),
                            PayloadFactory.getInstance().get().getDoubleEncryptionPbkdf2Iterations());
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
                                PayloadFactory.getInstance().get().getSharedKey(),
                                secondPassword.toString(),
                                PayloadFactory.getInstance().get().getDoubleEncryptionPbkdf2Iterations());
                    }
                    accounts.get(0).setXpriv(xpriv);
                }
                hdw.setAccounts(accounts);
                PayloadFactory.getInstance().get().setHdWallets(hdw);
                PayloadFactory.getInstance().get().setUpgraded(true);

                PayloadFactory.getInstance().get().getHdWallet().getAccounts().get(0).setLabel(defaultAccountName);

                try {
                    no_tx = (MultiAddrFactory.getInstance().getXpubTransactionCount(xpub) == 0L);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (!no_tx && attempts < 3);

            if (!no_tx && isNewlyCreated) {
                listener.onUpgradeFail();
            } else {
                if(!PayloadFactory.getInstance().put())
                    listener.onUpgradeFail();
            }
        }

        try {
            updateBalancesAndTransactions(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Account> accounts = PayloadFactory.getInstance().get().getHdWallet().getAccounts();
        PayloadFactory.getInstance().get().getHdWallet().setAccounts(accounts);
        PayloadFactory.getInstance().cache();

        listener.onUpgradeSuccess();
    }

    public void updateBalancesAndTransactions(boolean isNotUpgraded) throws Exception {
        // TODO unify legacy and HD call to one API call
        // TODO getXpub must be called before getLegacy (unify should fix this)
        // TODO do we need to pass isNotUpgraded?

        // xPub balance
        if (!isNotUpgraded) {
            String[] xpubs = getXPUBs(false);
            if (xpubs.length > 0) {
                MultiAddrFactory.getInstance().refreshXPUBData(xpubs);
            }
            List<Account> accounts = PayloadFactory.getInstance().get().getHdWallet().getAccounts();
            for (Account a : accounts) {
                a.setIdxReceiveAddresses(MultiAddrFactory.getInstance().getHighestTxReceiveIdx(a.getXpub()) > a.getIdxReceiveAddresses() ?
                        MultiAddrFactory.getInstance().getHighestTxReceiveIdx(a.getXpub()) : a.getIdxReceiveAddresses());
                a.setIdxChangeAddresses(MultiAddrFactory.getInstance().getHighestTxChangeIdx(a.getXpub()) > a.getIdxChangeAddresses() ?
                        MultiAddrFactory.getInstance().getHighestTxChangeIdx(a.getXpub()) : a.getIdxChangeAddresses());
            }
        }

        // Balance for legacy addresses
        if (PayloadFactory.getInstance().get().getLegacyAddresses().size() > 0) {
            List<String> legacyAddresses = PayloadFactory.getInstance().get().getLegacyAddressStrings();
            String[] addresses = legacyAddresses.toArray(new String[legacyAddresses.size()]);
            MultiAddrFactory.getInstance().refreshLegacyAddressData(addresses, false);
        }
    }

    public String getHDSeed() throws IOException, MnemonicException.MnemonicLengthException {
        return wallet.getSeedHex();
    }

    public String getHDMnemonic() throws IOException, MnemonicException.MnemonicLengthException {
        return wallet.getMnemonic();
    }

    public String getHDPassphrase() throws IOException, MnemonicException.MnemonicLengthException {
        return wallet.getPassphrase();
    }

    public ReceiveAddress getReceiveAddress(int accountIdx) throws DecoderException, IOException, MnemonicException.MnemonicWordException, MnemonicException.MnemonicChecksumException, MnemonicException.MnemonicLengthException, AddressFormatException {

        Address addr = null;
        int idx = PayloadFactory.getInstance().get().getHdWallet().getAccounts().get(accountIdx).getIdxReceiveAddresses();

        if (!PayloadFactory.getInstance().get().isDoubleEncrypted()) {
            addr = wallet.getAccount(accountIdx).getChain(RECEIVE_CHAIN).getAddressAt(idx);
        } else {
            //TODO - getXpubs?
            watchOnlyWallet = new Wallet(MainNetParams.get(), getXPUBs(true));
            addr = watchOnlyWallet.getAccount(accountIdx).getChain(RECEIVE_CHAIN).getAddressAt(idx);
        }

        return new ReceiveAddress(addr.getAddressString(), idx);
    }

    public Payload createHDWallet(int nbWords, String passphrase, int nbAccounts, String defaultAccountName) throws IOException, MnemonicException.MnemonicLengthException {
        wallet = walletFactory.newWallet(12, passphrase, 1);
        return PayloadFactory.getInstance().createBlockchainWallet(defaultAccountName);
    }

    public Payload restoreHDWallet(String seed, String passphrase, int nbAccounts, String defaultAccountName) throws IOException, AddressFormatException, DecoderException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicWordException, MnemonicException.MnemonicChecksumException {
        wallet = walletFactory.restoreWallet(seed, passphrase, 1);
        return PayloadFactory.getInstance().createBlockchainWallet(defaultAccountName);
    }

    private String[] getXPUBs(boolean includeArchives) throws IOException, DecoderException, AddressFormatException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicChecksumException, MnemonicException.MnemonicWordException {

        ArrayList<String> xpubs = new ArrayList<String>();

        if (!PayloadFactory.getInstance().get().isDoubleEncrypted()) {

            Wallet hd_wallet = null;

            //TODO - restoreWallet?
            if (PayloadFactory.getInstance().get().getHdWallet() != null) {
                hd_wallet = walletFactory.restoreWallet(PayloadFactory.getInstance().get().getHdWallet().getSeedHex(),
                        PayloadFactory.getInstance().get().getHdWallet().getPassphrase(),
                        PayloadFactory.getInstance().get().getHdWallet().getAccounts().size());
            }

        }

        //
        // null test added for 'V2' mode
        //
        if (PayloadFactory.getInstance().get().getHdWallet() != null) {
            int nb_accounts = PayloadFactory.getInstance().get().getHdWallet().getAccounts().size();
            for (int i = 0; i < nb_accounts; i++) {
                boolean isArchived = PayloadFactory.getInstance().get().getHdWallet().getAccounts().get(i).isArchived();
                if (isArchived && !includeArchives) {
                    ;
                } else {
                    String s = PayloadFactory.getInstance().get().getHdWallet().getAccounts().get(i).getXpub();
                    if (s != null && s.length() > 0) {
                        xpubs.add(s);
                    }
                }
            }
        }

        return xpubs.toArray(new String[xpubs.size()]);
    }

    public Account addAccount(String label) throws IOException, MnemonicException.MnemonicLengthException {

        String xpub = null;
        String xpriv = null;

        if(!PayloadFactory.getInstance().get().isDoubleEncrypted()) {

            wallet.addAccount();

            xpub = wallet.getAccounts().get(wallet.getAccounts().size() - 1).xpubstr();
            xpriv = wallet.getAccounts().get(wallet.getAccounts().size() - 1).xprvstr();
        }
        else {
            watchOnlyWallet.addAccount();

            xpub = watchOnlyWallet.getAccounts().get(watchOnlyWallet.getAccounts().size() - 1).xpubstr();
            xpriv = watchOnlyWallet.getAccounts().get(watchOnlyWallet.getAccounts().size() - 1).xprvstr();
        }

        //Initialize newly created xpub's tx list and balance
        List<Tx> txs = new ArrayList<Tx>();
        MultiAddrFactory.getInstance().getXpubTxs().put(xpub, txs);
        MultiAddrFactory.getInstance().getXpubAmounts().put(xpub, 0L);

        //Get account list from payload (not in sync with wallet from WalletFactory)
        List<Account> accounts = PayloadFactory.getInstance().get().getHdWallet().getAccounts();

        //Create new account (label, xpub, xpriv)
        Account account = new Account(label);
        account.setXpub(xpub);
        if(!PayloadFactory.getInstance().get().isDoubleEncrypted()) {
            account.setXpriv(xpriv);
        }
        else {
            String encrypted_xpriv = DoubleEncryptionFactory.getInstance().encrypt(
                    xpriv,
                    PayloadFactory.getInstance().get().getSharedKey(),
                    PayloadFactory.getInstance().getTempDoubleEncryptPassword().toString(),
                    PayloadFactory.getInstance().get().getDoubleEncryptionPbkdf2Iterations());
            account.setXpriv(encrypted_xpriv);
        }

        //Add new account to payload
        if(accounts.get(accounts.size() - 1) instanceof ImportedAccount) {
            accounts.add(accounts.size() - 1, account);
        }
        else {
            accounts.add(account);
        }
        PayloadFactory.getInstance().get().getHdWallet().setAccounts(accounts);

        //After this, remember to save payload remotely
        return account;
    }

    /*
    Generate V2 legacy address
    When called from Android - First apply PRNGFixes
     */
    public ECKey newLegacyAddress() {

        String result = null;
        byte[] data = null;
        try {
            result = WebUtil.getInstance().getURL(WebUtil.EXTERNAL_ENTROPY_URL);
            if (!result.matches("^[A-Fa-f0-9]{64}$")) {
                return null;
            }
            data = Hex.decode(result);
        } catch (Exception e) {
            return null;
        }

        ECKey ecKey = null;
        if (data != null) {
            byte[] rdata = new byte[32];
            SecureRandom random = new SecureRandom();
            random.nextBytes(rdata);
            byte[] privbytes = Util.getInstance().xor(data, rdata);
            if (privbytes == null) {
                return null;
            }
            ecKey = ECKey.fromPrivate(privbytes, true);
            // erase all byte arrays:
            random.nextBytes(privbytes);
            random.nextBytes(rdata);
            random.nextBytes(data);
        } else {
            return null;
        }

        return ecKey;
    }

    public void createWatchOnlyWalet(String[] xpubs) throws AddressFormatException {
        watchOnlyWallet = new Wallet(MainNetParams.get(), xpubs);
    }

    public void setWatchOnlyWalletWithPrivateKeys(String decrypted_hex, int accountSize){

        Wallet hdw = null;
        try {
            hdw = walletFactory.restoreWallet(decrypted_hex, "", accountSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.watchOnlyWallet = hdw;
    }

    public Wallet getWallet(){
        return wallet;
    }

    public Wallet getWatchOnlyWallet(){
        return watchOnlyWallet;
    }

    public String getChangeAddress(int accountIndex, int changeIdx, boolean isDoubleEncrypted) throws Exception {
        if (!isDoubleEncrypted) {
            return wallet.getAccount(accountIndex).getChange().getAddressAt(changeIdx).getAddressString();
        } else {
            return watchOnlyWallet.getAccount(accountIndex).getChange().getAddressAt(changeIdx).getAddressString();
        }
    }

    public ECKey getECKey(int accountIndex, String path, boolean isDoubleEncrypted) throws Exception{
        String[] s = path.split("/");
        Address hd_address = null;
        if (!isDoubleEncrypted) {
            hd_address = wallet.getAccount(accountIndex).getChain(Integer.parseInt(s[1])).getAddressAt(Integer.parseInt(s[2]));
        } else {
            hd_address = watchOnlyWallet.getAccount(accountIndex).getChain(Integer.parseInt(s[1])).getAddressAt(Integer.parseInt(s[2]));
        }
        return PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.WIF_COMPRESSED, hd_address.getPrivateKeyString());
    }
}