package info.blockchain.wallet.payload;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.subgraph.orchid.encoders.Hex;
import info.blockchain.api.data.UnspentOutput;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.api.WalletApi;
import info.blockchain.wallet.bip44.Address;
import info.blockchain.wallet.bip44.Chain;
import info.blockchain.wallet.bip44.HDWallet;
import info.blockchain.wallet.exceptions.AccountLockedException;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.exceptions.InvalidCredentialsException;
import info.blockchain.wallet.exceptions.ServerConnectionException;
import info.blockchain.wallet.exceptions.UnsupportedVersionException;
import info.blockchain.wallet.metadata.MetadataNodeFactory;
import info.blockchain.wallet.multiaddr.MultiAddrFactory;
import info.blockchain.wallet.payload.data.Account;
import info.blockchain.wallet.payload.data.BlockchainWallet;
import info.blockchain.wallet.payload.data.ImportedAccount;
import info.blockchain.wallet.payload.data.LegacyAddress;
import info.blockchain.wallet.payload.data.Payload;
import info.blockchain.wallet.payment.PaymentBundle;
import info.blockchain.wallet.transaction.Tx;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import info.blockchain.wallet.util.FormatsUtil;
import info.blockchain.wallet.util.PrivateKeyFactory;
import info.blockchain.wallet.util.Util;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

/**
 * PayloadManager.java : singleton class for reading/writing/parsing Blockchain HD JSON payload
 */
public class PayloadManager {

    private final Logger logger = LoggerFactory.getLogger(PayloadManager.class);

    public static final double SUPPORTED_ENCRYPTION_VERSION = 3.0;

    private static PayloadManager instance = null;
    // active payload:
    private Payload payload = null;

    private String strTempPassword = null;
    private boolean isNew = false;
    private String email = null;

    private double version = 2.0;

    private BlockchainWallet bciWallet;

    private HDPayloadBridge hdPayloadBridge;
    private HDWallet wallet;
    private MetadataNodeFactory metadataNodeFactory;

    private PayloadManager() throws IOException {
        hdPayloadBridge = new HDPayloadBridge();
        payload = new Payload();
    }

    /**
     * Return instance for a payload factory.
     *
     * @return PayloadManager
     */
    public static PayloadManager getInstance() throws IOException {

        if (instance == null) {
            instance = new PayloadManager();
        }

        return instance;
    }

    /**
     * Clear all values. This is to prevent issues with DI where two instances can accidentally
     * be created as getInstance() is rarely called
     */
    public void wipe() throws IOException {
        payload = new Payload();
        strTempPassword = null;
        isNew = false;
        email = null;
        version = 2.0;
        bciWallet = null;
        hdPayloadBridge = new HDPayloadBridge();
        wallet = null;
    }

    public interface InitiatePayloadListener {
        void onSuccess();
    }

    /**
     * Downloads payload from server, decrypts, and stores as local var {@link Payload}
     */
    // TODO: 08/02/2017 This should be improved - just roughly changed fetchWalletData() to call from WalletApi
    public void initiatePayload(@Nonnull String sharedKey, @Nonnull String guid, @Nonnull String password, @Nonnull InitiatePayloadListener listener) throws Exception {

        if (version > PayloadManager.SUPPORTED_ENCRYPTION_VERSION) {
            payload = null;
            throw new UnsupportedVersionException(version + "");
        }

        Call<ResponseBody> call = WalletApi.fetchWalletData(guid, sharedKey);

        Response<ResponseBody> exe = call.execute();

        if(exe.isSuccessful()){
            String walletData = exe.body().string();
            bciWallet = new BlockchainWallet(walletData, password);
            payload = bciWallet.getPayload();

            syncWallet();

            listener.onSuccess();
        }else{
            // TODO: 08/02/2017 Don't catch error messages like this
            String errorMessage = exe.errorBody().string();
            if (errorMessage != null && errorMessage.contains("Invalid GUID")) {
                throw new InvalidCredentialsException();
            } else if (errorMessage != null && errorMessage.contains("locked")) {
                throw new AccountLockedException(errorMessage);
            } else {
                throw new ServerConnectionException(errorMessage);
            }
        }
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
    public String getTempPassword() {
        return strTempPassword;
    }

    /**
     * Set temporary password for user once it has been validated. Read password from here rather
     * than reprompting user.
     *
     * @param temp_password Validated user password
     */
    public void setTempPassword(String temp_password) {
        strTempPassword = temp_password;
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

    // TODO: 07/02/2017 should return response and not block
    @Deprecated
    public boolean savePayloadToServer() {

        if (payload == null || !isEncryptionConsistent()) {
            return false;
        }

        try {
            Pair pair = bciWallet.encryptPayload(payload.toJson().toString(), strTempPassword, bciWallet.getPbkdf2Iterations(), getVersion());

            JSONObject encryptedPayload = (JSONObject) pair.getRight();
            String newPayloadChecksum = (String) pair.getLeft();
            String oldPayloadChecksum = bciWallet.getPayloadChecksum();

            Call<Void> call = WalletApi.saveWallet(isNew,
                payload.getGuid(),
                payload.getSharedKey(),
                payload.getLegacyAddressStringList(0L),
                encryptedPayload,
                bciWallet.isSyncPubkeys(),
                newPayloadChecksum,
                oldPayloadChecksum,
                email,
                "android");

            Response<Void> exe = call.execute();

            if(exe.isSuccessful()) {

                bciWallet.setPayloadChecksum(newPayloadChecksum);

                isNew = false;

                return true;
            } else{
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    @Deprecated
    public boolean validateSecondPassword(String secondPassword) {
        try {
            DoubleEncryptionFactory.validateSecondPassword(
                    payload.getDoublePasswordHash(),
                    payload.getSharedKey(),
                    secondPassword,
                    payload.getDoubleEncryptionPbkdf2Iterations());
            return true;
        } catch (DecryptionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Deprecated
    public HDWallet getDecryptedWallet(String secondPassword) throws DecryptionException {

        if (validateSecondPassword(secondPassword)) {

            try {
                String encrypted_hex = payload.getHdWallet().getSeedHex();
                String decrypted_hex = DoubleEncryptionFactory.decrypt(
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

    @Deprecated
    public Payload createHDWallet(String payloadPassword, String defaultAccountName) throws Exception {

        setTempPassword(payloadPassword);
        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.createHDWallet(defaultAccountName);
        wallet = pair.wallet;
        payload = pair.payload;
        setNew(true);

        bciWallet = new BlockchainWallet(payload);
        System.out.println(payload.toJson().toString());
        savePayloadToServer();

        return payload;
    }

    @Deprecated
    public Payload restoreHDWallet(String payloadPassword, String seed, String defaultAccountName) throws Exception {

        setTempPassword(payloadPassword);
        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.restoreHDWallet(seed, defaultAccountName);
        wallet = pair.wallet;
        payload = pair.payload;
        setNew(true);

        bciWallet = new BlockchainWallet(payload);

        savePayloadToServer();

        return payload;
    }

    @Deprecated
    public Payload restoreHDWallet(String payloadPassword, String seed, String defaultAccountName, String passphrase) throws Exception {

        setTempPassword(payloadPassword);
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
    @Deprecated
    public void upgradeV2PayloadToV3(String secondPassword, boolean isNewlyCreated, String defaultAccountName, final UpgradePayloadListener listener) throws Exception {

        //Check if payload has 2nd password
        if (payload.isDoubleEncrypted()) {

            //Validate 2nd password
            if (StringUtils.isEmpty(secondPassword) || !validateSecondPassword(secondPassword)) {
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
                listener.onUpgradeSuccess();

            } catch (Exception e) {
                e.printStackTrace();
                listener.onUpgradeFail();//failed to save
            }
        } else {
            listener.onUpgradeFail();//failed to create
        }

    }

    @Deprecated
    //get from multi address
    public String getNextChangeAddress(int accountIndex) throws AddressFormatException {

        int changeAddressIndex = payload.getHdWallet().getAccounts().get(accountIndex).getIdxChangeAddresses();

        String xpub = getXpubFromAccountIndex(accountIndex);
        return hdPayloadBridge.getAddressAt(xpub, Chain.CHANGE_CHAIN, changeAddressIndex).getAddressString();
    }

    @Deprecated
    //get from multi address
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

    @Deprecated
    public String getXpubFromAccountIndex(int accountIdx) {
        return payload.getHdWallet().getAccounts().get(accountIdx).getXpub();
    }

    public void updateBalancesAndTransactions() throws Exception {
        // TODO unify legacy and HD call to one API call
        // TODO getXpub must be called before getLegacy (unify should fix this)

        boolean isNotUpgraded = isNotUpgraded();

        // xPub balance
        if (!isNotUpgraded) {
            ArrayList<String> xpubs = getXPUBs(false);
            if (xpubs.size() > 0) {
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
            MultiAddrFactory.getInstance().refreshLegacyAddressData(legacyAddresses, false);
        }
    }

    @Deprecated
    public boolean isNotUpgraded() {
        return payload != null && !payload.isUpgraded();
    }

    @SuppressWarnings("SameParameterValue")
    @Deprecated
    public ArrayList<String> getXPUBs(boolean includeArchives) {

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

        return xpubs;
    }

    @Deprecated
    public Account addAccount(String accountLabel, @Nullable String secondPassword) throws Exception {

        //Add account
        String xpub;
        String xpriv;

        if (!payload.isDoubleEncrypted()) {

            wallet.addAccount();

            xpub = wallet.getAccounts().get(wallet.getAccounts().size() - 1).getXpub();
            xpriv = wallet.getAccounts().get(wallet.getAccounts().size() - 1).getXPriv();
        } else {

            HDWallet wallet = getDecryptedWallet(secondPassword);
            if (wallet != null) {

                wallet.addAccount();

                xpub = wallet.getAccounts().get(wallet.getAccounts().size() - 1).getXpub();
                xpriv = wallet.getAccounts().get(wallet.getAccounts().size() - 1).getXPriv();


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
            String encrypted_xpriv = DoubleEncryptionFactory.encrypt(
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
        if (!savePayloadToServer()) {
            //Revert
            accounts.remove(account);
            throw new Exception("Payload failed to save to server.");
        }

        return account;
    }

    /*
    Generate V2 legacy address
    When called from Android - First apply PRNGFixes
     */
    @Deprecated
    public LegacyAddress generateLegacyAddress(String deviceName, String deviceVersion, String secondPassword) throws Exception {

        if (payload.isDoubleEncrypted() && !validateSecondPassword(secondPassword)) {
            return null;//second password validation failed
        }

        ECKey ecKey = getRandomECKey();

        String encryptedKey = Base58.encode(ecKey.getPrivKeyBytes());
        if (payload.isDoubleEncrypted()) {
            encryptedKey = DoubleEncryptionFactory.encrypt(encryptedKey,
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

    @Deprecated
    public boolean addLegacyAddress(LegacyAddress legacyAddress) throws Exception {

        List<LegacyAddress> updatedLegacyAddresses = payload.getLegacyAddressList();
        updatedLegacyAddresses.add(legacyAddress);
        payload.setLegacyAddressList(updatedLegacyAddresses);

        boolean success = savePayloadToServer();

        if (!success) {
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
    @Deprecated
    public boolean setKeyForLegacyAddress(ECKey key, @Nullable String secondPassword) throws Exception {

        String address = key.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString();
        int index = payload.getLegacyAddressStringList().indexOf(address);

        LegacyAddress legacyAddress = payload.getLegacyAddressList().get(index);

        // If double encrypted, save encrypted in payload
        if (!payload.isDoubleEncrypted()) {
            legacyAddress.setEncryptedKeyBytes(key.getPrivKeyBytes());
        } else {
            String encryptedKey = Base58.encode(key.getPrivKeyBytes());
            String encrypted2 = DoubleEncryptionFactory.encrypt(encryptedKey,
                    payload.getSharedKey(),
                    secondPassword != null ? secondPassword : null,
                    payload.getOptions().getIterations());

            legacyAddress.setEncryptedKey(encrypted2);
        }

        legacyAddress.setWatchOnly(false);

        setPayload(payload);

        boolean success = savePayloadToServer();

        if (!success) {
            //revert on sync fail
            legacyAddress.setEncryptedKey(null);
            legacyAddress.setWatchOnly(true);
        }

        return success;
    }

    @Deprecated
    ECKey getRandomECKey() throws Exception {

        Call<ResponseBody> call = WalletApi.getRandomBytes();
        Response<ResponseBody> exe = call.execute();

        if(!exe.isSuccessful()){
            throw new Exception("ExternalEntropy.getRandomBytes failed.");
        }

        byte[] data = Hex.decode(exe.body().string());

        if (data == null) throw new Exception("ExternalEntropy.getRandomBytes failed.");

        byte[] rdata = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(rdata);
        byte[] privbytes = Util.xor(data, rdata);
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

    @Deprecated
    public byte[] getHDSeed() {
        return wallet.getSeed();
    }

    @Deprecated
    public String getHDSeedHex() {
        return wallet.getSeedHex();
    }

    @Deprecated
    public DeterministicKey getMasterKey() {
        return wallet.getMasterKey();
    }

    @Deprecated
    public String[] getMnemonic(String secondPassword) throws Exception {

        HDWallet wallet = getDecryptedWallet(secondPassword);

        if (wallet == null) throw new Exception("getDecryptedWallet returned null.");

        String mnemonic = wallet.getMnemonicOld();

        if (mnemonic != null && mnemonic.length() > 0) {
            return mnemonic.split("\\s+");
        } else {
            throw new Exception("Invalid mnemonic.");
        }
    }

    @Deprecated
    public String[] getMnemonic() {
        return wallet.getMnemonicOld().split("\\s+");
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

    @Deprecated
    public List<ECKey> getHDKeys(String secondPassword, Account account, PaymentBundle unspentOutputBundle) throws Exception {

        List<ECKey> keys = new ArrayList<ECKey>();

        for (UnspentOutput a : unspentOutputBundle.getSpendableOutputs()) {
            String[] split = a.getXpub().getPath().split("/");
            int chain = Integer.parseInt(split[1]);
            int addressIndex = Integer.parseInt(split[2]);

            HDWallet wallet;

            if (payload.isDoubleEncrypted()) {
                wallet = getDecryptedWallet(secondPassword);
            } else {
                wallet = this.wallet;
            }

            Address hd_address = wallet.getAccount(account.getRealIdx()).getChain(chain).getAddressAt(addressIndex);
            ECKey walletKey = PrivateKeyFactory.getKey(PrivateKeyFactory.WIF_COMPRESSED, hd_address.getPrivateKeyString());
            keys.add(walletKey);
        }

        return keys;
    }

    @Deprecated
    public BiMap<String, Integer> getXpubToAccountIndexMap() {

        BiMap<String, Integer> xpubToAccountIndexMap = HashBiMap.create();

        List<Account> accountList = payload.getHdWallet().getAccounts();

        for (Account account : accountList) {
            xpubToAccountIndexMap.put(account.getXpub(), account.getRealIdx());
        }

        return xpubToAccountIndexMap;
    }

    @Deprecated
    public Map<Integer, String> getAccountIndexToXpubMap() {
        return getXpubToAccountIndexMap().inverse();
    }

    @Deprecated
    public Call<ResponseBody> unregisterMdid(String guid, String sharedKey, ECKey node) throws Exception {
        String signedGuid = node.signMessage(guid);
        return WalletApi.unregisterMdid(guid,
            sharedKey,
            signedGuid);
    }

    @Deprecated
    public Call<ResponseBody> registerMdid(String guid, String sharedKey, ECKey node) throws Exception {
        String signedGuid = node.signMessage(guid);
        return WalletApi.registerMdid(guid,
            sharedKey,
            signedGuid);
    }

    /**
     * Loads the metadata nodes from the metadata service. If this fails, the function returns false
     * and they must be generated and saved using this#generateNodes(String). This allows us
     * to generate and prompt for a second password only once.
     *
     * @param guid           The user's GUID
     * @param sharedKey      The user's shared key
     * @param walletPassword The wallet password
     * @return Returns true if the metadata nodes can be loaded from the service
     * @throws Exception Can throw an Exception if there's an issue with the credentials or network
     */
    @Deprecated
    public boolean loadNodes(String guid, String sharedKey, String walletPassword) throws Exception {
        if (metadataNodeFactory == null) {
            metadataNodeFactory = new MetadataNodeFactory(guid, sharedKey, walletPassword);
        }
        return metadataNodeFactory.isMetadataUsable();
    }

    /**
     * Generates the nodes for the shared metadata service and saves them on the service. Takes an
     * optional second password if set by the user. this#loadNodes(String, String, String)
     * must be called first to avoid a {@link NullPointerException}.
     *
     * @param secondPassword An optional second password, if applicable
     * @throws Exception Can throw a {@link DecryptionException} if the second password is wrong, or
     *                   a generic Exception if saving the nodes fails
     */
    @Deprecated
    public void generateNodes(@Nullable String secondPassword) throws Exception {
        HDWallet wallet;
        if (payload.isDoubleEncrypted()) {
            wallet = getDecryptedWallet(secondPassword);
        } else {
            wallet = this.wallet;
        }
        boolean success = metadataNodeFactory.saveMetadataHdNodes(wallet.getMasterKey());
        if (!success) {
            throw new Exception("All Metadata nodes might not have saved.");
        }
    }

    @Deprecated
    public MetadataNodeFactory getMetadataNodeFactory() {
        return metadataNodeFactory;
    }

    @Deprecated
    HDWallet getWallet() {
        return wallet;
    }

    /**
     * Checks imported address and hd keys for possible double encryption corruption
     */
    @Deprecated
    public boolean isEncryptionConsistent() {

        ArrayList<String> keyList = new ArrayList<>();

        if (payload.getLegacyAddressList() != null) {
            List<LegacyAddress> legacyAddresses = payload.getLegacyAddressList();
            for (LegacyAddress legacyAddress : legacyAddresses) {
                if (!legacyAddress.isWatchOnly()) {
                    keyList.add(legacyAddress.getEncryptedKey());
                }
            }
        }

        if (payload.getHdWallet() != null && payload.getHdWallet().getAccounts() != null) {
            List<Account> accounts = payload.getHdWallet().getAccounts();
            for (Account account : accounts) {
                keyList.add(account.getXpriv());
            }
        }

        return isEncryptionConsistent(payload.isDoubleEncrypted(), keyList);
    }

    @Deprecated
    boolean isEncryptionConsistent(boolean isDoubleEncrypted, List<String> keyList) {

        boolean consistent = true;

        for (String key : keyList) {

            if (isDoubleEncrypted) {
                consistent = FormatsUtil.isKeyEncrypted(key);
            } else {
                consistent = FormatsUtil.isKeyUnencrypted(key);
            }

            if (!consistent) {
                break;
            }
        }

        return consistent;
    }
}