package info.blockchain.wallet.payload;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import info.blockchain.api.ExternalEntropy;
import info.blockchain.api.PersistentUrls;
import info.blockchain.api.WalletPayload;
import info.blockchain.bip44.Address;
import info.blockchain.bip44.Chain;
import info.blockchain.bip44.Wallet;
import info.blockchain.wallet.exceptions.AccountLockedException;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.exceptions.InvalidCredentialsException;
import info.blockchain.wallet.exceptions.ServerConnectionException;
import info.blockchain.wallet.exceptions.UnsupportedVersionException;
import info.blockchain.wallet.metadata.MetadataNodeFactory;
import info.blockchain.wallet.multiaddr.MultiAddrFactory;
import info.blockchain.wallet.payment.data.SpendableUnspentOutputs;
import info.blockchain.wallet.send.MyTransactionOutPoint;
import info.blockchain.wallet.transaction.Tx;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import info.blockchain.wallet.util.PrivateKeyFactory;
import info.blockchain.wallet.util.Util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * PayloadManager.java : singleton class for reading/writing/parsing Blockchain HD JSON payload
 */
public class PayloadManager {

    public static final double SUPPORTED_ENCRYPTION_VERSION = 3.0;

    private static PayloadManager instance = null;
    // active payload:
    private Payload payload = null;
    // cached payload, compare to this payload to determine if changes have been made. Used to avoid needless remote saves to server
    private String cached_payload = null;

    private CharSequenceX strTempPassword = null;
    private boolean isNew = false;
    private String email = null;

    private double version = 2.0;

    private BlockchainWallet bciWallet;

    private HDPayloadBridge hdPayloadBridge;
    private info.blockchain.bip44.Wallet wallet;
    private PrivateKeyFactory privateKeyFactory;
    private WalletPayload walletApi;
    private MetadataNodeFactory metadataNodeFactory;

    private PayloadManager() {
        hdPayloadBridge = new HDPayloadBridge();
        payload = new Payload();
        cached_payload = "";
        privateKeyFactory = new PrivateKeyFactory();
        walletApi = new WalletPayload();
    }

    /**
     * Return instance for a payload factory.
     *
     * @return PayloadManager
     */
    public static PayloadManager getInstance() {

        if (instance == null) {
            instance = new PayloadManager();
        }

        return instance;
    }

    /**
     * Clear all values. This is to prevent issues with DI where two instances can accidentally
     * be created as getInstance() is rarely called
     */
    public void wipe() {
        payload = new Payload();
        cached_payload = "";
        strTempPassword = null;
        isNew = false;
        email = null;
        version = 2.0;
        bciWallet = null;
        hdPayloadBridge = new HDPayloadBridge();
        wallet = null;
        privateKeyFactory = new PrivateKeyFactory();
    }

    public interface InitiatePayloadListener {
        void onSuccess();
    }

    /**
     * Downloads payload from server, decrypts, and stores as local var {@link Payload}
     */
    public void initiatePayload(@Nonnull String sharedKey, @Nonnull String guid, @Nonnull CharSequenceX password, @Nonnull InitiatePayloadListener listener) throws Exception {

        String walletData;
        try {
            walletData = walletApi.fetchWalletData(guid, sharedKey);
        } catch (Exception e) {

            e.printStackTrace();

            if (e.getMessage() != null && e.getMessage().contains("Invalid GUID")) {
                throw new InvalidCredentialsException();
            } else if (e.getMessage() != null && e.getMessage().contains("locked")) {
                throw new AccountLockedException(e.getMessage(), e);
            } else {
                throw new ServerConnectionException(e.getMessage(), e);
            }
        }
        bciWallet = new BlockchainWallet(walletData, password);
        payload = bciWallet.getPayload();

        if (getVersion() > PayloadManager.SUPPORTED_ENCRYPTION_VERSION) {

            payload = null;
            throw new UnsupportedVersionException(getVersion() + "");
        }

        syncWallet();

        listener.onSuccess();
    }

    /**
     * Syncs payload wallet and bip44 wallet
     */
    private void syncWallet() throws HDWalletException {
        if (payload.getHdWallet() != null && !payload.isDoubleEncrypted()) {
            try {
                wallet = hdPayloadBridge.getHDWalletFromPayload(payload);
            } catch (Exception e) {
                throw new HDWalletException("Bip44 wallet error: " + e.getMessage());
            }
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
     * Set temporary password for user once it has been validated. Read password from here rather
     * than reprompting user.
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
     */
    @SuppressWarnings("SameParameterValue")
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

        try {
            if (cached_payload != null && cached_payload.equals(payload.toJson().toString())) {
                return true;
            }

            String method = isNew ? "insert" : "update";


            Pair pair = bciWallet.encryptPayload(payload.toJson().toString(), new CharSequenceX(strTempPassword), bciWallet.getPbkdf2Iterations(), getVersion());

            JSONObject encryptedPayload = (JSONObject) pair.getRight();
            String newPayloadChecksum = (String) pair.getLeft();
            String oldPayloadChecksum = bciWallet.getPayloadChecksum();

            new WalletPayload().savePayloadToServer(method,
                    payload.getGuid(),
                    payload.getSharedKey(),
                    payload.getLegacyAddressList(),
                    encryptedPayload,
                    bciWallet.isSyncPubkeys(),
                    newPayloadChecksum,
                    oldPayloadChecksum,
                    email);

            bciWallet.setPayloadChecksum(newPayloadChecksum);

            isNew = false;
            cachePayload(payload);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Write to current client payload to cache.
     */
    public void cachePayload(Payload payload) throws Exception {
        cached_payload = payload.toJson().toString();
    }

    private void clearCachedPayload() {
        cached_payload = null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Wallet getDecryptedWallet(String secondPassword) throws DecryptionException {

        if (validateSecondPassword(secondPassword)) {

            try {
                String encrypted_hex = payload.getHdWallet().getSeedHex();
                String decrypted_hex = DoubleEncryptionFactory.getInstance().decrypt(
                        encrypted_hex,
                        payload.getSharedKey(),
                        secondPassword,
                        payload.getDoubleEncryptionPbkdf2Iterations());

                return hdPayloadBridge.decryptWatchOnlyWallet(payload, decrypted_hex);
            } catch (Exception e) {
                throw new DecryptionException(e.getMessage());
            }

        } else {
            throw new DecryptionException("Second password validation error.");
        }
    }

    public Payload createHDWallet(String payloadPassword, String defaultAccountName) throws Exception {

        setTempPassword(new CharSequenceX(payloadPassword));
        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.createHDWallet(defaultAccountName);
        wallet = pair.wallet;
        payload = pair.payload;
        setNew(true);

        bciWallet = new BlockchainWallet(payload);

        savePayloadToServer();

        return payload;
    }

    public Payload restoreHDWallet(String payloadPassword, String seed, String defaultAccountName) throws Exception {

        setTempPassword(new CharSequenceX(payloadPassword));
        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.restoreHDWallet(seed, defaultAccountName);
        wallet = pair.wallet;
        payload = pair.payload;
        setNew(true);

        bciWallet = new BlockchainWallet(payload);

        savePayloadToServer();

        return payload;
    }

    public Payload restoreHDWallet(String payloadPassword, String seed, String defaultAccountName, String passphrase) throws Exception {

        setTempPassword(new CharSequenceX(payloadPassword));
        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.restoreHDWallet(seed, defaultAccountName, passphrase);
        wallet = pair.wallet;
        payload = pair.payload;
        setNew(true);

        bciWallet = new BlockchainWallet(payload);

        savePayloadToServer();

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

            try {
                savePayloadToServer();

                syncWallet();
                updateBalancesAndTransactions();
                cachePayload(payload);
                listener.onUpgradeSuccess();

            } catch (Exception e) {
                e.printStackTrace();
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

        Account account = payload.getHdWallet().getAccounts().get(accountIndex);
        int receiveAddressIndex = findNextUnreservedReceiveAddressIndex(account, account.getIdxReceiveAddresses());

        String xpub = getXpubFromAccountIndex(accountIndex);
        return hdPayloadBridge.getAddressAt(xpub, Chain.RECEIVE_CHAIN, receiveAddressIndex).getAddressString();
    }

    /**
     * Allows you to generate a receive address at an arbitrary number of positions on the chain
     * from the next valid unused address. For example, the passing 5 as the position will generate
     * an address which correlates with the next available address + 5 positions.
     *
     * @param accountIndex The index of the account you wish to generate addresses from
     * @param position     Represents how many positions on the chain beyond what is already used
     *                     that you wish to generate
     * @return A bitcoin address
     */
    public String getReceiveAddressAtPosition(int accountIndex, int position) throws AddressFormatException {

        Account account = payload.getHdWallet().getAccounts().get(accountIndex);
        int receiveAddressIndex = findNextUnreservedReceiveAddressIndex(account, account.getIdxReceiveAddresses() + position);

        String xpub = getXpubFromAccountIndex(accountIndex);
        return hdPayloadBridge.getAddressAt(xpub, Chain.RECEIVE_CHAIN, receiveAddressIndex).getAddressString();
    }

    private int findNextUnreservedReceiveAddressIndex(Account account, int addressPosition) {
        return account.getAddressLabels().containsKey(addressPosition)
                ? findNextUnreservedReceiveAddressIndex(account, addressPosition + 1) : addressPosition;
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
        if (payload.getLegacyAddressList().size() > 0) {
            List<String> legacyAddresses = payload.getLegacyAddressStringList();
            String[] addresses = legacyAddresses.toArray(new String[legacyAddresses.size()]);
            MultiAddrFactory.getInstance().refreshLegacyAddressData(addresses, false);
        }
    }

    public boolean isNotUpgraded() {
        return payload != null && !payload.isUpgraded();
    }

    @SuppressWarnings("SameParameterValue")
    public String[] getXPUBs(boolean includeArchives) {

        ArrayList<String> xpubs = new ArrayList<String>();

        if (payload.getHdWallet() != null) {
            int nb_accounts = payload.getHdWallet().getAccounts().size();
            for (int i = 0; i < nb_accounts; i++) {
                boolean isArchived = payload.getHdWallet().getAccounts().get(i).isArchived();
                if (isArchived && !includeArchives) {
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

    public Account addAccount(String accountLabel, @Nullable String secondPassword) throws Exception {

        //Add account
        String xpub;
        String xpriv;

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
                throw new DecryptionException();
            }
        }

        //Initialize newly created xpub's tx list and balance
        MultiAddrFactory.getInstance().getXpubTxs().put(xpub, new ArrayList<Tx>());
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
        savePayloadToServer();

        return account;
    }

    /*
    Generate V2 legacy address
    When called from Android - First apply PRNGFixes
     */
    public LegacyAddress generateLegacyAddress(String deviceName, String deviceVersion, String secondPassword) throws Exception {

        if (payload.isDoubleEncrypted() && !validateSecondPassword(secondPassword)) {
            return null;//second password validation failed
        }

        ECKey ecKey = getRandomECKey();

        String encryptedKey = Base58.encode(ecKey.getPrivKeyBytes());
        if (payload.isDoubleEncrypted()) {
            encryptedKey = DoubleEncryptionFactory.getInstance().encrypt(encryptedKey,
                    payload.getSharedKey(),
                    secondPassword,
                    payload.getOptions().getIterations());
        }

        final LegacyAddress legacyAddress = new LegacyAddress();
        legacyAddress.setEncryptedKey(encryptedKey);
        legacyAddress.setAddress(ecKey.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString());
        legacyAddress.setCreatedDeviceName(deviceName);
        legacyAddress.setCreated(System.currentTimeMillis());
        legacyAddress.setCreatedDeviceVersion(deviceVersion);

        return legacyAddress;
    }

    public boolean addLegacyAddress(LegacyAddress legacyAddress) throws Exception {

        List<LegacyAddress> updatedLegacyAddresses = payload.getLegacyAddressList();
        updatedLegacyAddresses.add(legacyAddress);
        payload.setLegacyAddressList(updatedLegacyAddresses);
        
        boolean success = savePayloadToServer();

        if(!success){
            //revert on sync fail
            updatedLegacyAddresses.remove(legacyAddress);
            payload.setLegacyAddressList(updatedLegacyAddresses);
        }

        return success;
    }

    /**
     * Sets a private key for a {@link LegacyAddress}
     *
     * @param key            The {@link ECKey} for the address
     * @param secondPassword An optional double encryption password
     */
    public boolean setKeyForLegacyAddress(ECKey key, @Nullable CharSequenceX secondPassword) throws Exception {

        String address = key.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString();
        int index = payload.getLegacyAddressStringList().indexOf(address);

        LegacyAddress legacyAddress = payload.getLegacyAddressList().get(index);

        // If double encrypted, save encrypted in payload
        if (!payload.isDoubleEncrypted()) {
            legacyAddress.setEncryptedKeyBytes(key.getPrivKeyBytes());
        } else {
            String encryptedKey = Base58.encode(key.getPrivKeyBytes());
            String encrypted2 = DoubleEncryptionFactory.getInstance().encrypt(encryptedKey,
                    payload.getSharedKey(),
                    secondPassword != null ? secondPassword.toString() : null,
                    payload.getOptions().getIterations());

            legacyAddress.setEncryptedKey(encrypted2);
        }

        legacyAddress.setWatchOnly(false);

        setPayload(payload);

        boolean success =  savePayloadToServer();

        if(!success){
            //revert on sync fail
            legacyAddress.setEncryptedKey(null);
            legacyAddress.setWatchOnly(true);
        }

        return success;
    }

    ECKey getRandomECKey() throws Exception {

        byte[] data = new ExternalEntropy().getRandomBytes();

        if (data == null) throw new Exception("ExternalEntropy.getRandomBytes failed.");

        byte[] rdata = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(rdata);
        byte[] privbytes = Util.getInstance().xor(data, rdata);
        if (privbytes == null) {
            return null;
        }
        ECKey ecKey = ECKey.fromPrivate(privbytes, true);
        // erase all byte arrays:
        random.nextBytes(privbytes);
        random.nextBytes(rdata);
        random.nextBytes(data);

        return ecKey;
    }

    public byte[] getHDSeed() {
        return wallet.getSeed();
    }

    public String getHDSeedHex() {
        return wallet.getSeedHex();
    }

    public DeterministicKey getMasterKey() {
        return wallet.getMasterKey();
    }

    public String[] getMnemonic(String secondPassword) throws Exception {

        Wallet wallet = getDecryptedWallet(secondPassword);

        if (wallet == null) throw new Exception("getDecryptedWallet returned null.");

        String mnemonic = wallet.getMnemonic();

        if (mnemonic != null && mnemonic.length() > 0) {
            return mnemonic.split("\\s+");
        } else {
            throw new Exception("Invalid mnemonic.");
        }
    }

    public String[] getMnemonic() {
        return wallet.getMnemonic().split("\\s+");
    }

    public String getHDPassphrase() {
        return wallet.getPassphrase();
    }

    /**
     * Debugging purposes
     */
    public BlockchainWallet getBciWallet() {
        return bciWallet;
    }

    public List<ECKey> getHDKeys(String secondPassword, Account account, SpendableUnspentOutputs unspentOutputBundle) throws Exception {

        List<ECKey> keys = new ArrayList<ECKey>();

        for (MyTransactionOutPoint a : unspentOutputBundle.getSpendableOutputs()) {
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
            ECKey walletKey = privateKeyFactory.getKey(PrivateKeyFactory.WIF_COMPRESSED, hd_address.getPrivateKeyString());
            keys.add(walletKey);
        }

        return keys;
    }

    public BiMap<String, Integer> getXpubToAccountIndexMap() {

        BiMap<String, Integer> xpubToAccountIndexMap = HashBiMap.create();

        List<Account> accountList = payload.getHdWallet().getAccounts();

        for (Account account : accountList) {
            xpubToAccountIndexMap.put(account.getXpub(), account.getRealIdx());
        }

        return xpubToAccountIndexMap;
    }

    public Map<Integer, String> getAccountIndexToXpubMap() {
        return getXpubToAccountIndexMap().inverse();
    }

    public void unregisterMdid(String guid, String sharedKey, ECKey node) throws Exception {
        walletApi.unregisterMdid(node, guid, sharedKey);
    }

    public void registerMdid(String guid, String sharedKey, ECKey node) throws Exception {
        walletApi.registerMdid(node, guid, sharedKey);
    }

    public void loadNodes(String guid, String sharedKey, String walletPassword, @Nullable String secondPassword) throws Exception {

        metadataNodeFactory = new MetadataNodeFactory(guid, sharedKey, walletPassword);

        boolean usable = metadataNodeFactory.isMetadataUsable();
        if(!usable){

            Wallet wallet;
            if (payload.isDoubleEncrypted()) {
                wallet = getDecryptedWallet(secondPassword);
            } else {
                wallet = this.wallet;
            }

            boolean success = metadataNodeFactory.saveMetadataHdNodes(wallet.getMasterKey());
            if(!success){
                throw new Exception("All Metadata nodes might not have saved.");
            }
        }
    }

    public MetadataNodeFactory getMetadataNodeFactory() {
        return metadataNodeFactory;
    }
}