package info.blockchain.wallet.payload;

import com.google.common.annotations.VisibleForTesting;
import info.blockchain.api.ExternalEntropy;
import info.blockchain.api.WalletPayload;
import info.blockchain.bip44.Address;
import info.blockchain.bip44.Chain;
import info.blockchain.bip44.Wallet;
import info.blockchain.wallet.exceptions.*;
import info.blockchain.wallet.multiaddr.MultiAddrFactory;
import info.blockchain.wallet.payment.data.SpendableUnspentOutputs;
import info.blockchain.wallet.send.MyTransactionOutPoint;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import info.blockchain.wallet.util.PrivateKeyFactory;
import info.blockchain.wallet.util.Util;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * PayloadManager.java : singleton class for reading/writing/parsing Blockchain HD JSON payload
 */
public class PayloadManager {

    public static final double SUPPORTED_ENCRYPTION_VERSION = 3.0;

    private static PayloadManager instance = null;
    // active payload:
    private static Payload payload = null;
    // cached payload, compare to this payload to determine if changes have been made. Used to avoid needless remote saves to server
    private static String cached_payload = null;

    private static CharSequenceX strTempPassword = null;
    private static boolean isNew = false;
    private static String email = null;

    private static double version = 2.0;

    private static BlockchainWallet bciWallet;

    private static HDPayloadBridge hdPayloadBridge;
    private static info.blockchain.bip44.Wallet wallet;

    private PayloadManager() {
        ;
    }

    /**
     * Return instance for a payload factory.
     *
     * @return PayloadManager
     */
    public static PayloadManager getInstance() {

        if (instance == null) {
            instance = new PayloadManager();
            payload = new Payload();
            cached_payload = "";
            hdPayloadBridge = new HDPayloadBridge();
        }

        return instance;
    }

    /**
     * Reset PayloadManager to null instance.
     */
    public void wipe() {
        instance = null;
    }

    public interface InitiatePayloadListener {
        void onSuccess();
    }

    /**
     * Downloads payload from server, decrypts, and stores as local var {@link Payload}
     *
     * @param sharedKey
     * @param guid
     * @param password
     * @param listener
     */
    public void initiatePayload(@Nonnull String sharedKey, @Nonnull String guid, @Nonnull CharSequenceX password, @Nonnull InitiatePayloadListener listener) throws InvalidCredentialsException, ServerConnectionException, UnsupportedVersionException, PayloadException, DecryptionException, HDWalletException {

        String walletData = null;
        try {
            walletData = new WalletPayload().fetchWalletData(guid, sharedKey);
        } catch (Exception e) {

            e.printStackTrace();

            if (e.getMessage().contains("Invalid GUID")) {
                throw new InvalidCredentialsException();
            } else {
                throw new ServerConnectionException();
            }
        }

        bciWallet = new BlockchainWallet(walletData, password);
        payload = bciWallet.getPayload();

        if (getVersion() > PayloadManager.SUPPORTED_ENCRYPTION_VERSION) {

            payload = null;
            throw new UnsupportedVersionException(getVersion()+"");
        }

        syncWallet();

        listener.onSuccess();
    }

    /**
     * Syncs payload wallet and bip44 wallet
     * @throws HDWalletException
     */
    private void syncWallet() throws HDWalletException {
        if (payload.getHdWallet() != null && !payload.isDoubleEncrypted()) {
            try {
                wallet = hdPayloadBridge.getHDWalletFromPayload(payload);
            } catch (Exception e) {
                throw new HDWalletException("Bip44 wallet error: " + e.getMessage());
            }
        } else {
            //V2 wallet - no need to keep in sync with bp44 wallet
        }
    }

    /**
     * Get temporary password for user. Read password from here rather than reprompting user.
     *
     * @return CharSequenceX
     */
    public CharSequenceX getTempPassword() {
        return strTempPassword;
    }

    /**
     * Set temporary password for user once it has been validated. Read password from here rather than reprompting user.
     *
     * @param temp_password Validated user password
     */
    public void setTempPassword(CharSequenceX temp_password) {
        strTempPassword = temp_password;
        clearCachedPayload();
    }

    /**
     * Get checksum for this payload.
     *
     * @return String
     */
    public String getCheckSum() {
        return bciWallet.getPayloadChecksum();
    }

    /**
     * Check if this payload is for a new Blockchain account.
     *
     * @return boolean
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * Set if this payload is for a new Blockchain account.
     *
     * @param isNew
     */
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Local get(). Returns current payload from the client.
     *
     * @return Payload
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     * Local set(). Sets current payload on the client.
     *
     * @param p Payload to be assigned
     */
    public void setPayload(Payload p) {
        payload = p;
    }

    public boolean savePayloadToServer() {

        if (payload == null) return false;

        if (cached_payload != null && cached_payload.equals(payload.dumpJSON().toString())) {
            return true;
        }

        String method = isNew ? "insert" : "update";

        try {
            Pair pair = payload.encryptPayload(payload.dumpJSON().toString(), new CharSequenceX(strTempPassword), bciWallet.getPbkdf2Iterations(), getVersion());

            JSONObject encryptedPayload = (JSONObject) pair.getRight();
            String newPayloadChecksum = (String)pair.getLeft();
            String oldPayloadChecksum = bciWallet.getPayloadChecksum();

            boolean success = new WalletPayload().savePayloadToServer(method,
                    payload.getGuid(),
                    payload.getSharedKey(),
                    payload.getLegacyAddresses(),
                    encryptedPayload,
                    bciWallet.isSyncPubkeys(),
                    newPayloadChecksum,
                    oldPayloadChecksum,
                    email);

            bciWallet.setPayloadChecksum(newPayloadChecksum);

            if (success){
                isNew = false;
                cachePayload();
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Write to current client payload to cache.
     */
    public void cachePayload() {
        try {
            cached_payload = payload.dumpJSON().toString();
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    private void clearCachedPayload() {
        cached_payload = null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        PayloadManager.email = email;
    }

    public double getVersion() {
        return version;
    }

    @VisibleForTesting
    double setVersion(double version) {
        return this.version = version;
    }

    public boolean validateSecondPassword(String secondPassword) {
        return DoubleEncryptionFactory.getInstance().validateSecondPassword(
                payload.getDoublePasswordHash(),
                payload.getSharedKey(),
                new CharSequenceX(secondPassword),
                payload.getDoubleEncryptionPbkdf2Iterations());
    }

    public Wallet getDecryptedWallet(String secondPassword) throws Exception{

        if (validateSecondPassword(secondPassword)) {

            String encrypted_hex = payload.getHdWallet().getSeedHex();
            String decrypted_hex = DoubleEncryptionFactory.getInstance().decrypt(
                    encrypted_hex,
                    payload.getSharedKey(),
                    secondPassword,
                    payload.getDoubleEncryptionPbkdf2Iterations());

            return hdPayloadBridge.decryptWatchOnlyWallet(payload, decrypted_hex);

        } else {
            return null;
        }
    }

    public Payload createHDWallet(String payloadPassword, String defaultAccountName) throws Exception {

        setTempPassword(new CharSequenceX(payloadPassword));
        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.createHDWallet(defaultAccountName);
        wallet = pair.wallet;
        payload = pair.payload;
        setNew(true);

        bciWallet = new BlockchainWallet(payload);

        if (!savePayloadToServer()) {
            //if save failed don't return payload
            payload = null;
        }

        return payload;
    }

    public Payload restoreHDWallet(String payloadPassword, String seed, String defaultAccountName) throws Exception {

        setTempPassword(new CharSequenceX(payloadPassword));
        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.restoreHDWallet(seed, defaultAccountName);
        wallet = pair.wallet;
        payload = pair.payload;
        setNew(true);

        bciWallet = new BlockchainWallet(payload);

        if (!savePayloadToServer()) {
            //if save failed don't return payload
            payload = null;
        }

        return payload;
    }

    public Payload restoreHDWallet(String payloadPassword, String seed, String defaultAccountName, String passphrase) throws Exception {

        setTempPassword(new CharSequenceX(payloadPassword));
        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.restoreHDWallet(seed, defaultAccountName, passphrase);
        wallet = pair.wallet;
        payload = pair.payload;
        setNew(true);

        bciWallet = new BlockchainWallet(payload);

        if (!savePayloadToServer()) {
            //if save failed don't return payload
            payload = null;
        }

        return payload;
    }

    public interface UpgradePayloadListener {
        void onDoubleEncryptionPasswordError();

        void onUpgradeSuccess();

        void onUpgradeFail();
    }

    /*
    When called from Android - First apply PRNGFixes
     */
    public void upgradeV2PayloadToV3(CharSequenceX secondPassword, boolean isNewlyCreated, String defaultAccountName, final UpgradePayloadListener listener) throws Exception {

        //Check if payload has 2nd password
        if (payload.isDoubleEncrypted()) {

            //Validate 2nd password
            if (StringUtils.isEmpty(secondPassword) || !validateSecondPassword(secondPassword.toString())) {
                listener.onDoubleEncryptionPasswordError();
            }
        }

        //Upgrade
        boolean isUpgradeSuccessful = hdPayloadBridge.upgradeV2PayloadToV3(payload, secondPassword, isNewlyCreated, defaultAccountName);
        if (isUpgradeSuccessful) {

            if (savePayloadToServer()) {

                syncWallet();
                updateBalancesAndTransactions();
                cachePayload();
                listener.onUpgradeSuccess();
            } else {
                listener.onUpgradeFail();//failed to save
            }
        } else {
            listener.onUpgradeFail();//failed to create
        }

    }

    public String getNextChangeAddress(int accountIndex) throws AddressFormatException {

        int changeAddressIndex = payload.getHdWallet().getAccounts().get(accountIndex).getIdxChangeAddresses();

        String xpub = getXpubFromAccountIndex(accountIndex);
        return hdPayloadBridge.getAddressAt(xpub, Chain.CHANGE_CHAIN, changeAddressIndex).getAddressString();
    }

    public String getNextReceiveAddress(int accountIndex) throws AddressFormatException {

        int receiveAddressIndex = payload.getHdWallet().getAccounts().get(accountIndex).getIdxReceiveAddresses();

        String xpub = getXpubFromAccountIndex(accountIndex);
        return hdPayloadBridge.getAddressAt(xpub, Chain.RECEIVE_CHAIN, receiveAddressIndex).getAddressString();
    }

    public String getXpubFromAccountIndex(int accountIdx) {
        return payload.getHdWallet().getAccounts().get(accountIdx).getXpub();
    }

    public void updateBalancesAndTransactions() throws Exception {
        // TODO unify legacy and HD call to one API call
        // TODO getXpub must be called before getLegacy (unify should fix this)

        boolean isNotUpgraded = isNotUpgraded();

        // xPub balance
        if (!isNotUpgraded) {
            String[] xpubs = getXPUBs(false);
            if (xpubs.length > 0) {
                MultiAddrFactory.getInstance().refreshXPUBData(xpubs);
            }
            List<Account> accounts = payload.getHdWallet().getAccounts();
            for (Account a : accounts) {
                a.setIdxReceiveAddresses(MultiAddrFactory.getInstance().getHighestTxReceiveIdx(a.getXpub()) > a.getIdxReceiveAddresses() ?
                        MultiAddrFactory.getInstance().getHighestTxReceiveIdx(a.getXpub()) : a.getIdxReceiveAddresses());
                a.setIdxChangeAddresses(MultiAddrFactory.getInstance().getHighestTxChangeIdx(a.getXpub()) > a.getIdxChangeAddresses() ?
                        MultiAddrFactory.getInstance().getHighestTxChangeIdx(a.getXpub()) : a.getIdxChangeAddresses());
            }
        }

        // Balance for legacy addresses
        if (payload.getLegacyAddresses().size() > 0) {
            List<String> legacyAddresses = payload.getLegacyAddressStrings();
            String[] addresses = legacyAddresses.toArray(new String[legacyAddresses.size()]);
            MultiAddrFactory.getInstance().refreshLegacyAddressData(addresses, false);
        }
    }

    public boolean isNotUpgraded() {
        return payload != null && !payload.isUpgraded();
    }

    public String[] getXPUBs(boolean includeArchives) throws IOException, DecoderException, AddressFormatException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicChecksumException, MnemonicException.MnemonicWordException {

        ArrayList<String> xpubs = new ArrayList<String>();

        if (payload.getHdWallet() != null) {
            int nb_accounts = payload.getHdWallet().getAccounts().size();
            for (int i = 0; i < nb_accounts; i++) {
                boolean isArchived = payload.getHdWallet().getAccounts().get(i).isArchived();
                if (isArchived && !includeArchives) {
                    ;
                } else {
                    String s = payload.getHdWallet().getAccounts().get(i).getXpub();
                    if (s != null && s.length() > 0) {
                        xpubs.add(s);
                    }
                }
            }
        }

        return xpubs.toArray(new String[xpubs.size()]);
    }

    public interface AccountAddListener {
        void onAccountAddSuccess(Account account);

        void onSecondPasswordFail();

        void onPayloadSaveFail();
    }

    public void addAccount(String accountLabel, @Nullable String secondPassword, AccountAddListener listener) throws Exception {

        //Add account
        String xpub = null;
        String xpriv = null;

        if (!payload.isDoubleEncrypted()) {

            wallet.addAccount();

            xpub = wallet.getAccounts().get(wallet.getAccounts().size() - 1).xpubstr();
            xpriv = wallet.getAccounts().get(wallet.getAccounts().size() - 1).xprvstr();
        } else {

            Wallet wallet = getDecryptedWallet(secondPassword);
            if (wallet != null) {

                wallet.addAccount();

                xpub = wallet.getAccounts().get(wallet.getAccounts().size() - 1).xpubstr();
                xpriv = wallet.getAccounts().get(wallet.getAccounts().size() - 1).xprvstr();


            } else {
                listener.onSecondPasswordFail();
                return;
            }
        }

        //Initialize newly created xpub's tx list and balance
        List<Tx> txs = new ArrayList<Tx>();
        MultiAddrFactory.getInstance().getXpubTxs().put(xpub, txs);
        MultiAddrFactory.getInstance().getXpubAmounts().put(xpub, 0L);

        //Get account list from payload (not in sync with wallet from WalletFactory)
        List<Account> accounts = payload.getHdWallet().getAccounts();

        //Create new account (label, xpub, xpriv)
        Account account = new Account(accountLabel);
        account.setXpub(xpub);
        if (!payload.isDoubleEncrypted()) {
            account.setXpriv(xpriv);
        } else {
            String encrypted_xpriv = DoubleEncryptionFactory.getInstance().encrypt(
                    xpriv,
                    payload.getSharedKey(),
                    secondPassword,
                    payload.getDoubleEncryptionPbkdf2Iterations());
            account.setXpriv(encrypted_xpriv);
        }

        //Add new account to payload
        if (accounts.get(accounts.size() - 1) instanceof ImportedAccount) {
            accounts.add(accounts.size() - 1, account);
        } else {
            accounts.add(account);
        }
        payload.getHdWallet().setAccounts(accounts);

        //Save payload
        if (savePayloadToServer()) {
            if (listener != null) listener.onAccountAddSuccess(account);
        } else {
            if (listener != null) listener.onPayloadSaveFail();
        }
    }

    /*
    Generate V2 legacy address
    When called from Android - First apply PRNGFixes
     */
    public LegacyAddress generateLegacyAddress(String deviceName, String deviceVersion, String secondPassword) {

        if (payload.isDoubleEncrypted() && !validateSecondPassword(secondPassword)) {
            return null;//second password validation failed
        }

        ECKey ecKey = getRandomECKey();

        String encryptedKey = new String(Base58.encode(ecKey.getPrivKeyBytes()));
        if (payload.isDoubleEncrypted()) {
            encryptedKey = DoubleEncryptionFactory.getInstance().encrypt(encryptedKey,
                    payload.getSharedKey(),
                    secondPassword,
                    payload.getOptions().getIterations());
        }

        final LegacyAddress legacyAddress = new LegacyAddress();
        legacyAddress.setEncryptedKey(encryptedKey);
        legacyAddress.setAddress(ecKey.toAddress(MainNetParams.get()).toString());
        legacyAddress.setCreatedDeviceName(deviceName);
        legacyAddress.setCreated(System.currentTimeMillis());
        legacyAddress.setCreatedDeviceVersion(deviceVersion);

        return legacyAddress;
    }

    public boolean addLegacyAddress(LegacyAddress legacyAddress) {
        List<LegacyAddress> updatedLegacyAddresses = payload.getLegacyAddresses();
        updatedLegacyAddresses.add(legacyAddress);
        payload.setLegacyAddresses(updatedLegacyAddresses);
        return savePayloadToServer();
    }

    ECKey getRandomECKey() {

        byte[] data = null;
        try {
            data = new ExternalEntropy().getRandomBytes();
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

    public String getHDSeed() throws IOException, MnemonicException.MnemonicLengthException {
        return wallet.getSeedHex();
    }

    public String[] getMnemonic(String secondPassword) throws IOException, MnemonicException.MnemonicLengthException {
        try {
            Wallet wallet = getDecryptedWallet(secondPassword);

            if(wallet != null) {
                String mnemonic = wallet.getMnemonic();

                if (mnemonic != null && mnemonic.length() > 0) {
                    return mnemonic.split("\\s+");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String[] getMnemonic() throws IOException, MnemonicException.MnemonicLengthException {
        return wallet.getMnemonic().split("\\s+");
    }

    public String getHDPassphrase() throws IOException, MnemonicException.MnemonicLengthException {
        return wallet.getPassphrase();
    }

    /**
     * Debugging purposes
     * @return
     */
    public BlockchainWallet getBciWallet(){
        return bciWallet;
    }

    public List<ECKey> getHDKeys(String secondPassword, Account account, SpendableUnspentOutputs unspentOutputBundle) throws Exception {

        List<ECKey> keys = new ArrayList<ECKey>();

        for(MyTransactionOutPoint a : unspentOutputBundle.getSpendableOutputs()){
            String[] split = a.getPath().split("/");
            int chain = Integer.parseInt(split[1]);
            int addressIndex = Integer.parseInt(split[2]);

            Wallet wallet;

            if (payload.isDoubleEncrypted()) {
                wallet = getDecryptedWallet(secondPassword);
            } else {
                wallet = this.wallet;
            }

            Address hd_address = wallet.getAccount(account.getRealIdx()).getChain(chain).getAddressAt(addressIndex);
            ECKey walletKey =  PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.WIF_COMPRESSED, hd_address.getPrivateKeyString());
            keys.add(walletKey);
        }

        return keys;
    }
}
