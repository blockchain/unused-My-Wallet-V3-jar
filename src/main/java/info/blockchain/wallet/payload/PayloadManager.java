package info.blockchain.wallet.payload;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.Balance;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.WalletApi;
import info.blockchain.wallet.bip44.HDAccount;
import info.blockchain.wallet.exceptions.AccountLockedException;
import info.blockchain.wallet.exceptions.ApiException;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.EncryptionException;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.exceptions.InvalidCredentialsException;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.exceptions.NoSuchAddressException;
import info.blockchain.wallet.exceptions.ServerConnectionException;
import info.blockchain.wallet.exceptions.UnsupportedVersionException;
import info.blockchain.wallet.metadata.MetadataNodeFactory;
import info.blockchain.wallet.multiaddress.MultiAddressFactory;
import info.blockchain.wallet.multiaddress.TransactionSummary;
import info.blockchain.wallet.pairing.Pairing;
import info.blockchain.wallet.payload.data.Account;
import info.blockchain.wallet.payload.data.HDWallet;
import info.blockchain.wallet.payload.data.LegacyAddress;
import info.blockchain.wallet.payload.data.Wallet;
import info.blockchain.wallet.payload.data.WalletBase;
import info.blockchain.wallet.payload.data.WalletWrapper;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import info.blockchain.wallet.util.Tools;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@SuppressWarnings("WeakerAccess")
public class PayloadManager {

    public static final String MULTI_ADDRESS_ALL = "all";
    public static final String MULTI_ADDRESS_ALL_LEGACY = "all_legacy";

    private static Logger log = LoggerFactory.getLogger(PayloadManager.class);

    //Assume we only support 1 hdWallet
    private static final int HD_WALLET_INDEX = 0;

    private WalletBase walletBaseBody;
    private String password;
    private MetadataNodeFactory metadataNodeFactory;
    // This is an explicit dependency and should be injected for easier testing
    private WalletApi walletApi;
    private BlockExplorer blockExplorer;

    private MultiAddressFactory multiAddressFactory;
    private BalanceManager balanceManager;

    private static PayloadManager instance;

    public static PayloadManager getInstance() {
        if (instance == null) {
            instance = new PayloadManager();
        }
        return instance;
    }

    private PayloadManager() {
        log.info("Initializing PayloadManager");
        init();
    }

    private void init() {
        walletApi = new WalletApi();
        blockExplorer = new BlockExplorer(BlockchainFramework.getRetrofitExplorerInstance(), BlockchainFramework.getApiCode());
        multiAddressFactory = new MultiAddressFactory(blockExplorer);
        balanceManager = new BalanceManager(blockExplorer);
    }

    public void wipe() {
        log.info("Wiping PayloadManager");
        walletBaseBody = null;
        password = null;
        metadataNodeFactory = null;
        init();
    }

    public Wallet getPayload() {
        return walletBaseBody != null ? walletBaseBody.getWalletBody() : null;
    }

    public String getPayloadChecksum() {
        return walletBaseBody.getPayloadChecksum();
    }

    public String getTempPassword() {
        return password;
    }

    public void setTempPassword(String password) {
        this.password = password;
    }

    public MetadataNodeFactory getMetadataNodeFactory() {
        return metadataNodeFactory;
    }

    //********************************************************************************************//
    //*                  Wallet initialization, creation, recovery, syncing                      *//
    //********************************************************************************************//

    /**
     * NB! When called from Android - First apply PRNGFixes
     * Creates a new Blockchain wallet and saves it to the server.
     * @param defaultAccountName
     * @param email Used to send GUID link to user
     * @throws Exception
     */
    public Wallet create(@Nonnull String defaultAccountName, @Nonnull String email, @Nonnull String password)
        throws Exception {
        log.info("Creating wallet");

        this.password = password;
        walletBaseBody = new WalletBase();
        walletBaseBody.setWalletBody(new Wallet(defaultAccountName));

        saveNewWallet(email);

        updateAllBalances();

        return walletBaseBody.getWalletBody();
    }

    /**
     * Creates a new Blockchain wallet based on provided mnemonic and saves it to the server.
     * @param mnemonic 12 word recovery phrase - space separated
     * @param defaultAccountName
     * @param email Used to send GUID link to user
     * @throws Exception
     */
    public Wallet recoverFromMnemonic(@Nonnull String mnemonic, @Nonnull String defaultAccountName,
        @Nonnull String email, @Nonnull String password) throws Exception {
        log.info("Recovering wallet");

        this.password = password;
        walletBaseBody = new WalletBase();

        Wallet walletBody = new Wallet();
        HDWallet hdWallet = HDWallet.recoverFromMnemonic(mnemonic, defaultAccountName);
        walletBody.setHdWallets(Collections.singletonList(hdWallet));

        walletBaseBody.setWalletBody(walletBody);

        saveNewWallet(email);

        updateAllBalances();

        return walletBaseBody.getWalletBody();
    }

    /**
     * Upgrades a V2 wallet to a V3 HD wallet and saves it to the server
     * NB! When called from Android - First apply PRNGFixes
     * @param secondPassword
     * @param defaultAccountName
     * @return
     * @throws Exception
     */
    public boolean upgradeV2PayloadToV3(String secondPassword, String defaultAccountName) throws Exception {
        log.info("Upgrading to v3 wallet");

        walletBaseBody.getWalletBody().upgradeV2PayloadToV3(secondPassword, defaultAccountName);

        boolean success = save();

        if (!success) {
            //Revert on save fail
            walletBaseBody.getWalletBody().setHdWallets(null);
        }

        updateAllBalances();

        return success;
    }

    /**
     * Initializes a wallet from provided credentials.
     * Calls balance api to show wallet balances on wallet load.
     * @param sharedKey
     * @param guid
     * @throws IOException
     * @throws InvalidCredentialsException GUID might be incorrect
     * @throws AccountLockedException Account has been locked, contact support
     * @throws ServerConnectionException Unknown server error
     * @throws DecryptionException Password not able to decrypt payload
     * @throws InvalidCipherTextException Decryption issue
     * @throws UnsupportedVersionException Payload version newer than current supported
     * @throws MnemonicLengthException Initializing HD issue
     * @throws MnemonicWordException Initializing HD issue
     * @throws MnemonicChecksumException Initializing HD issue
     * @throws DecoderException Decryption issue
     */
    public void initializeAndDecrypt(@Nonnull String sharedKey, @Nonnull String guid, @Nonnull String password)
        throws IOException, InvalidCredentialsException, AccountLockedException, ServerConnectionException,
        DecryptionException, InvalidCipherTextException, UnsupportedVersionException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, DecoderException,
        ApiException, HDWalletException {
        log.info("Initializing and decrypting wallet from credentials");

        this.password = password;

        Call<ResponseBody> call = walletApi.fetchWalletData(guid, sharedKey);
        Response<ResponseBody> exe = call.execute();

        if(exe.isSuccessful()){
            final WalletBase walletBase = WalletBase.fromJson(exe.body().string());
            walletBase.decryptPayload(this.password);
            walletBaseBody = walletBase;
        } else {
            log.warn("Fetching wallet data failed with provided credentials");
            String errorMessage = exe.errorBody().string();
            log.warn("", errorMessage);
            if (errorMessage != null && errorMessage.contains("Unknown Wallet Identifier")) {
                throw new InvalidCredentialsException();
            } else if (errorMessage != null && errorMessage.contains("locked")) {
                throw new AccountLockedException(errorMessage);
            } else {
                throw new ServerConnectionException(errorMessage);
            }
        }

        updateAllBalances();
    }

    public void initializeAndDecryptFromQR(@Nonnull String qrData) throws Exception {
        log.info("Initializing and decrypting wallet from scanned QR");

        Pair qrComponents = Pairing.getQRComponentsFromRawString(qrData);
        Call<ResponseBody> call = walletApi.fetchPairingEncryptionPasswordCall((String)qrComponents.getLeft());

        Response<ResponseBody> exe = call.execute();

        if(exe.isSuccessful()) {
            String encryptionPassword = exe.body().string();
            String encryptionPairingCode = (String)qrComponents.getRight();
            String guid = (String)qrComponents.getLeft();

            String[] sharedKeyAndPassword = Pairing.getSharedKeyAndPassword(encryptionPairingCode, encryptionPassword);
            String sharedKey = sharedKeyAndPassword[0];
            String hexEncodedPassword = sharedKeyAndPassword[1];
            String password = new String(Hex.decode(hexEncodedPassword), "UTF-8");

            initializeAndDecrypt(sharedKey, guid, password);

        } else {
            log.error("", exe.code()+" - "+exe.errorBody().string());
            throw new ServerConnectionException(exe.code()+" - "+exe.errorBody().string());
        }

        updateAllBalances();
    }

    /**
     * Initializes a wallet from a Payload string from manual pairing. Should decode both V3 and V1 wallets successfully.
     *
     * @param payload  The Payload in String format that you wish to decrypt and initialise
     * @param password The password for the payload
     * @throws HDWalletException   Thrown for a variety of reasons, wraps actual exception and is fatal
     * @throws DecryptionException Thrown if the password is incorrect
     */
    public void initializeAndDecryptFromPayload(String payload,
                                                String password) throws HDWalletException, DecryptionException {
        log.info("Initializing and decrypting wallet from manual pairing");

        try {
            walletBaseBody = WalletBase.fromJson(payload);
            walletBaseBody.decryptPayload(password);
            setTempPassword(password);

            updateAllBalances();
        } catch (DecryptionException decryptionException) {
            log.warn("", decryptionException);
            throw decryptionException;
        } catch (Exception e) {
            log.error("", e);
            throw new HDWalletException(e);
        }
    }

    private void validateSave() throws HDWalletException {
        log.info("Checking if wallet is safe to save");
        if (walletBaseBody == null) {
            throw new HDWalletException("Save aborted - HDWallet not initialized.");
        } else if (!walletBaseBody.getWalletBody().isEncryptionConsistent()) {
            throw new HDWalletException("Save aborted - Payload corrupted. Key encryption not consistent.");
        } else if (BlockchainFramework.getDevice() == null) {
            throw new HDWalletException("Save aborted - Device name not specified in FrameWork.");
        }
    }

    private void saveNewWallet(String email) throws Exception {
        validateSave();
        log.info("Saving wallet");

        //Encrypt and wrap payload
        Pair pair = walletBaseBody.encryptAndWrapPayload(password);
        WalletWrapper payloadWrapper = (WalletWrapper) pair.getRight();
        String newPayloadChecksum = (String) pair.getLeft();

        //Save to server
        Call<ResponseBody> call = walletApi.insertWallet(
            walletBaseBody.getWalletBody().getGuid(),
            walletBaseBody.getWalletBody().getSharedKey(),
            null,
            payloadWrapper.toJson(),
            newPayloadChecksum,
            email,
            BlockchainFramework.getDevice());

        Response<ResponseBody> exe = call.execute();
        if(exe.isSuccessful()) {
            //set new checksum
            walletBaseBody.setPayloadChecksum(newPayloadChecksum);
        } else{
            log.error("", exe.code()+" - "+exe.errorBody().string());
            throw new ServerConnectionException(exe.code()+" - "+exe.errorBody().string());
        }
    }

    /**
     * Saves wallet to server and forces the upload of the user's addresses to allow notifications
     * to work correctly.
     *
     * @return True if save successful
     */
    public boolean saveAndSyncPubKeys() throws
            HDWalletException,
            EncryptionException,
            NoSuchAlgorithmException,
            IOException {
        return save(true);
    }

    /**
     * Saves wallet to server.
     *
     * @return True if save successful
     */
    public boolean save() throws
            HDWalletException,
            EncryptionException,
            NoSuchAlgorithmException,
            IOException {
        return save(false);
    }

    private synchronized boolean save(boolean forcePubKeySync)
            throws HDWalletException, NoSuchAlgorithmException,
            EncryptionException, IOException {

        validateSave();
        log.info("Saving wallet");

        //Encrypt and wrap payload
        Pair pair = walletBaseBody.encryptAndWrapPayload(password);
        WalletWrapper payloadWrapper = (WalletWrapper) pair.getRight();
        String newPayloadChecksum = (String) pair.getLeft();
        String oldPayloadChecksum = walletBaseBody.getPayloadChecksum();

        //Save to server
        List<String> syncAddresses = null;
        if (walletBaseBody.isSyncPubkeys() || forcePubKeySync) {
            syncAddresses = new ArrayList<>();

            /*
              This matches what iOS is doing, but it seems to be massive overkill for mobile
              devices. I'm also filtering out archived accounts here because I don't see the point
              in sending them.
             */
            for (Account account : getPayload().getHdWallets().get(0).getAccounts()) {
                if (!account.isArchived()) {
                    HDAccount hdAccount =
                            getPayload().getHdWallets().get(0).getHDAccountFromAccountBody(account);
                    int nextIndex = getNextReceiveAddressIndex(account);

                    syncAddresses.addAll(
                            Tools.getReceiveAddressList(hdAccount, nextIndex, nextIndex + 20));
                }
            }

            syncAddresses.addAll(
                    Tools.filterLegacyAddress(
                            LegacyAddress.NORMAL_ADDRESS,
                            walletBaseBody.getWalletBody().getLegacyAddressList()));
        }

        Call<ResponseBody> call = walletApi.updateWallet(
                walletBaseBody.getWalletBody().getGuid(),
                walletBaseBody.getWalletBody().getSharedKey(),
                syncAddresses,
                payloadWrapper.toJson(),
                newPayloadChecksum,
                oldPayloadChecksum,
                BlockchainFramework.getDevice());

        Response<ResponseBody> exe = call.execute();
        if (exe.isSuccessful()) {
            //set new checksum
            walletBaseBody.setPayloadChecksum(newPayloadChecksum);

            return true;
        } else {
            log.error("Save unsuccessful: " + exe.errorBody().string());
            return false;
        }
    }

    //********************************************************************************************//
    //*                         Account and Legacy HDAddress creation                              *//
    //********************************************************************************************//

    /**
     * Adds a new account to hd wallet and saves to server.
     * Reverts on save failure.
     * @param label
     * @param secondPassword
     * @return
     * @throws Exception
     */
    public Account addAccount(String label, @Nullable String secondPassword)
        throws Exception {
        log.info("Adding account");
        Account accountBody = walletBaseBody.getWalletBody().addAccount(HD_WALLET_INDEX, label, secondPassword);

        boolean success = save();

        if (!success) {
            //Revert on save fail
            walletBaseBody.getWalletBody().getHdWallets().get(0).getAccounts().remove(accountBody);
            throw new Exception("Failed to save added account.");
        }

        updateAllBalances();

        return accountBody;
    }

    /**
     * NB! When called from Android - First apply PRNGFixes
     * Generates new legacy address and saves to server.
     * Reverts on save failure.
     * @param label
     * @param secondPassword
     * @return
     * @throws Exception
     */
    public boolean addLegacyAddress(String label, @Nullable String secondPassword) throws Exception {
        log.info("Adding legacy address");

        LegacyAddress legacyAddressBody = walletBaseBody.getWalletBody()
            .addLegacyAddress(label, secondPassword);

        boolean success = save();

        if (!success) {
            //Revert on save fail
            walletBaseBody.getWalletBody().getLegacyAddressList().remove(legacyAddressBody);
        }

        updateAllBalances();

        return success;
    }

    /**
     * Inserts a {@link LegacyAddress} into the user's {@link Wallet} and then syncs the wallet with
     * the server. Will remove/revert the LegacyAddress if the sync was unsuccessful.
     *
     * @param legacyAddress The {@link LegacyAddress} to be added
     * @throws Exception Possible if saving the Wallet fails
     */
    public void addLegacyAddress(LegacyAddress legacyAddress) throws Exception {
        log.info("Adding legacy address");
        // TODO: 02/03/2017  second password

        List<LegacyAddress> currentAddresses = walletBaseBody.getWalletBody().getLegacyAddressList();
        walletBaseBody.getWalletBody().getLegacyAddressList().add(legacyAddress);

        if (!save()) {
            // Revert on sync fail
            walletBaseBody.getWalletBody().setLegacyAddressList(currentAddresses);
            throw new Exception("Failed to save added Legacy Address.");
        }

        updateAllBalances();
    }

    /**
     * Replaces an old {@link LegacyAddress} with a newer one if found and then syncs the wallet
     * with the server. Will remove/revert the LegacyAddress if the sync was unsuccessful.
     *
     * @param legacyAddress The {@link LegacyAddress} to be added
     * @throws Exception Possible if saving the Wallet fails
     * @throws NullPointerException Thrown if the address to be updated is not found
     */
    public void updateLegacyAddress(LegacyAddress legacyAddress) throws Exception {
        log.info("Updating legacy address");
        // TODO: 02/03/2017  second password
        boolean found = false;

        final List<LegacyAddress> legacyAddressList = walletBaseBody.getWalletBody().getLegacyAddressList();
        for (int i = 0; i < legacyAddressList.size(); i++) {
            final LegacyAddress address = legacyAddressList.get(i);
            if (address.getAddress().equals(legacyAddress.getAddress())) {
                // Replace object with updated version
                walletBaseBody.getWalletBody().getLegacyAddressList().set(i, legacyAddress);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new NullPointerException("Legacy address not found");
        }

        if (!save()) {
            // Revert on sync fail
            walletBaseBody.getWalletBody().setLegacyAddressList(legacyAddressList);
            throw new Exception("Failed to save added Legacy Address.");
        }

        updateAllBalances();
    }

    /**
     * Sets private key to existing matching legacy address. If no match is found the key will be added
     * to the wallet non the less.
     * @param key ECKey for existing legacy address
     * @param secondPassword Double encryption password if applicable.
     * @return
     * @throws EncryptionException
     * @throws IOException
     * @throws DecryptionException
     * @throws NoSuchAddressException
     * @throws NoSuchAlgorithmException
     * @throws HDWalletException
     */
    public LegacyAddress setKeyForLegacyAddress(ECKey key, @Nullable String secondPassword)
        throws Exception {
        log.info("Setting key for legacy address");

        LegacyAddress matchingLegacyAddress = null;
        try {
            matchingLegacyAddress = walletBaseBody.getWalletBody()
                .setKeyForLegacyAddress(key, secondPassword);
        } catch (NoSuchAddressException e) {
            e.printStackTrace();
            //If no match found, save as new
            return addLegacyAddressFromKey(key, secondPassword);
        }

        boolean success = save();

        if (!success) {
            //Revert on save fail
            matchingLegacyAddress.setPrivateKey(null);
        }

        return matchingLegacyAddress;

    }

    public LegacyAddress addLegacyAddressFromKey(ECKey key, @Nullable String secondPassword)
        throws Exception {
        log.info("Adding legacy address from ECKey");

        LegacyAddress newlyAdded = walletBaseBody.getWalletBody()
            .addLegacyAddressFromKey(key, secondPassword);

        boolean success = save();

        if (!success) {
            //Revert on save fail
            newlyAdded.setPrivateKey(null);
        }

        updateAllBalances();

        return newlyAdded;

    }

    //********************************************************************************************//
    //*                                 Shortcut methods                                         *//
    //********************************************************************************************//

    private LinkedHashSet<String> getAllAccountsAndAddresses() {
        LinkedHashSet<String> all = new LinkedHashSet<>();

        //Add all accounts
        if(getPayload().getHdWallets() != null) {
            List<String> xpubs = getPayload().getHdWallets().get(0).getActiveXpubs();
            all.addAll(xpubs);
        }

        //Add all addresses, archived or not
        all.addAll(getPayload().getLegacyAddressStringList());

        log.info("Getting account and address list: List size = {}", all.size());
        return all;
    }

    public boolean validateSecondPassword(String secondPassword) {
        log.info("Validating second password");

        try{
            walletBaseBody.getWalletBody().validateSecondPassword(secondPassword);
            return true;
        } catch (Exception e){
            log.warn("",e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean isNotUpgraded() {
        return walletBaseBody.getWalletBody() != null && !walletBaseBody.getWalletBody().isUpgraded();
    }

    public String getXpubFromAccountIndex(int accountIdx) {
        return walletBaseBody.getWalletBody().getHdWallets().get(0).getAccounts().get(accountIdx).getXpub();
    }

    public ECKey getAddressECKey(@Nonnull LegacyAddress legacyAddress, @Nullable String secondPassword)
        throws DecryptionException, UnsupportedEncodingException, InvalidCipherTextException {
        log.info("Get address ECKey");

        walletBaseBody.getWalletBody().validateSecondPassword(secondPassword);

        String decryptedPrivateKey = legacyAddress.getPrivateKey();

        if(secondPassword != null) {
            log.info("Decrypting address private key");
            decryptedPrivateKey = DoubleEncryptionFactory
                .decrypt(legacyAddress.getPrivateKey(),
                    walletBaseBody.getWalletBody().getSharedKey(),
                    secondPassword,
                    walletBaseBody.getWalletBody().getOptions().getPbkdf2Iterations());
        }

        return Tools.getECKeyFromKeyAndAddress(decryptedPrivateKey, legacyAddress.getAddress());
    }

    /**
     * Returns a {@link LinkedHashMap} of {@link Balance} objects keyed to their respective addresses.
     *
     * @param addresses A List of addresses as Strings
     * @return A {@link LinkedHashMap} where they key is the address String, and the value is a {@link Balance} object
     * @throws IOException  Thrown if there are network issues
     * @throws ApiException Thrown if the call isn't successful
     */
    public LinkedHashMap<String, Balance> getBalanceOfAddresses(List<String> addresses) throws IOException, ApiException {
        LinkedHashMap<String, Balance> map = new LinkedHashMap<>();

        final Response<HashMap<String, Balance>> response = balanceManager.getBalanceOfAddresses(addresses).execute();
        if (response.isSuccessful()) {
            final HashMap<String, Balance> balanceHashMap = response.body();
            // Place into map to maintain order, as API may return them in a random order
            for (String address : addresses) {
                map.put(address, balanceHashMap.get(address));
            }

            log.info("Get map for address balances: Map size = {}", map.size());
            return map;
        } else {
            throw new ApiException(response.code() + ": " + response.errorBody().string());
        }
    }

    //********************************************************************************************//
    //*                                        Metadata                                          *//
    //********************************************************************************************//

    /**
     * This will deactivate push notifications.
     * @param node used to sign GUID.
     * @return
     * @throws Exception
     */
    public Observable<ResponseBody> unregisterMdid(ECKey node) {
        log.info("Unregister mdid - deactivate push notifications");
        String signedGuid = node.signMessage(walletBaseBody.getWalletBody().getGuid());
        return walletApi.unregisterMdid(walletBaseBody.getWalletBody().getGuid(),
            walletBaseBody.getWalletBody().getSharedKey(),
            signedGuid);
    }

    /**
     * This will activate push notifications.
     * @param node used to sign GUID.
     * @return
     * @throws Exception
     */
    public Observable<ResponseBody> registerMdid(ECKey node) {
        log.info("Register mdid - activate push notifications");
        String signedGuid = node.signMessage(walletBaseBody.getWalletBody().getGuid());
        return walletApi.registerMdid(walletBaseBody.getWalletBody().getGuid(),
            walletBaseBody.getWalletBody().getSharedKey(),
            signedGuid);
    }

    /**
     *
     * Loads the metadata nodes from the metadata service. If this fails, the function returns false
     * and they must be generated and saved using this#generateNodes(String). This allows us
     * to generate and prompt for a second password only once.
     *
     * @return Returns true if the metadata nodes can be loaded from the service
     * @throws Exception Can throw an Exception if there's an issue with the credentials or network
     */
    public boolean loadNodes() throws Exception {
        log.info("Loading metadata nodes");
        if (metadataNodeFactory == null) {
            metadataNodeFactory = new MetadataNodeFactory(walletBaseBody.getWalletBody().getGuid(),
                walletBaseBody.getWalletBody().getSharedKey(), password);
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
    public void generateNodes(@Nullable String secondPassword) throws Exception{
        log.info("Generating metadata nodes");
        walletBaseBody.getWalletBody().decryptHDWallet(HD_WALLET_INDEX, secondPassword);

        boolean success = metadataNodeFactory.saveMetadataHdNodes(
            walletBaseBody.getWalletBody().getHdWallets().get(HD_WALLET_INDEX).getMasterKey());
        if (!success) {
            throw new MetadataException("All Metadata nodes might not have saved.");
        }
    }

    //********************************************************************************************//
    //*                                     Multi_address                                        *//
    //********************************************************************************************//

    /**
     * Gets transaction list for all wallet accounts/addresses
     * @param limit Amount of transactions per page
     * @param offset Page offset
     * @return List of tx summaries for all wallet transactions
     * @throws IOException
     * @throws ApiException
     */
    public List<TransactionSummary> getAllTransactions(int limit, int offset) throws IOException, ApiException {
        return getAccountTransactions(MULTI_ADDRESS_ALL, limit, offset);
    }

    /**
     * Updates internal balance and transaction list for imported addresses
     * @param limit Amount of transactions per page
     * @param offset Page offset
     * @return Consolidated list of tx summaries for specified imported transactions
     * @throws IOException
     * @throws ApiException
     */
    public List<TransactionSummary> getImportedAddressesTransactions(int limit, int offset)
        throws IOException, ApiException {
        List<String> activeXpubs = getPayload().getHdWallets().get(0).getActiveXpubs();
        List<String> watchOnly = getPayload().getWatchOnlyAddressStringList();
        List<String> activeLegacy = getPayload().getLegacyAddressStringList(LegacyAddress.NORMAL_ADDRESS);

        ArrayList<String> all = new ArrayList<>(activeXpubs);
        all.addAll(activeLegacy);

        return multiAddressFactory.getAccountTransactions(all, watchOnly, activeLegacy, null, limit, offset);
    }

    /**
     * Gets transaction list for account
     * @param xpub
     * @param limit Amount of transactions per page
     * @param offset Page offset
     * @return List of tx summaries for specified xpubs transactions
     * @throws IOException
     * @throws ApiException
     */
    public List<TransactionSummary> getAccountTransactions(String xpub, int limit, int offset)
        throws IOException, ApiException {

        List<String> activeXpubs = getPayload().getHdWallets().get(0).getActiveXpubs();
        List<String> watchOnly = getPayload().getWatchOnlyAddressStringList();
        List<String> activeLegacy = getPayload().getLegacyAddressStringList(LegacyAddress.NORMAL_ADDRESS);

        ArrayList<String> all = new ArrayList<>(activeXpubs);
        all.addAll(activeLegacy);

        return multiAddressFactory.getAccountTransactions(all, watchOnly, null, xpub, limit, offset);
    }

    /**
     * Calculates if an address belongs to any xpubs in wallet.
     * Make sure multi address is up to date before executing this method.
     * @param address
     * @return
     */
    public boolean isOwnHDAddress(String address) {
        return multiAddressFactory.isOwnHDAddress(address);
    }

    /**
     * Converts any address to a label.
     *
     * @param address Accepts account receive or change chain address, as well as legacy address.
     * @return Account or legacy address label
     */
    public String getLabelFromAddress(String address) {
        String label;
        String xpub = multiAddressFactory.getXpubFromAddress(address);

        if (xpub != null) {
            label = getPayload().getHdWallets().get(HD_WALLET_INDEX).getLabelFromXpub(xpub);
        } else {
            label = getPayload().getLabelFromLegacyAddress(address);
        }

        if (label == null || label.isEmpty()) {
            label = address;
        }

        return label;
    }

    /**
     * Returns an xPub from an address if the address belongs to this wallet.
     * @param address The address you want to query
     * @return  An xPub as a String
     */
    @Nullable
    public String getXpubFromAddress(String address) {
        return multiAddressFactory.getXpubFromAddress(address);
    }

    /**
     * Gets next receive address. Excludes reserved addresses.
     * @param account
     * @return
     * @throws IOException
     * @throws HDWalletException
     */
    public String getNextReceiveAddress(Account account) throws IOException, HDWalletException {

        int nextIndex = getNextReceiveAddressIndex(account);

        HDAccount hdAccount = getPayload().getHdWallets().get(0)
            .getHDAccountFromAccountBody(account);

        return hdAccount.getReceive().getAddressAt(nextIndex).getAddressString();
    }

    /**
     * Allows you to generate a receive address at an arbitrary number of positions on the chain
     * from the next valid unused address. For example, the passing 5 as the position will generate
     * an address which correlates with the next available address + 5 positions.
     *
     * @param account  The {@link Account} you wish to generate an address from
     * @param position Represents how many positions on the chain beyond what is already used
     *                 that you wish to generate
     * @return A bitcoin address
     */
    @Nullable
    public String getReceiveAddressAtPosition(Account account, int position) {
        try {
            HDAccount hdAccount = getPayload().getHdWallets().get(0).getHDAccountFromAccountBody(account);
            int nextIndex = getNextReceiveAddressIndex(account);
            return hdAccount.getReceive().getAddressAt(nextIndex + position).getAddressString();
        } catch (HDWalletException e) {
            return null;
        }
    }

    /**
     * Allows you to get an address from any given point on the receive chain.
     *
     * @param account  The {@link Account} you wish to generate an address from
     * @param position What position on the chain the address you wish to create is
     * @return A bitcoin address
     */
    @Nullable
    public String getReceiveAddressAtArbitraryPosition(Account account, int position) {
        try {
            HDAccount hdAccount = getPayload().getHdWallets().get(0).getHDAccountFromAccountBody(account);
            return hdAccount.getReceive().getAddressAt(position).getAddressString();
        } catch (HDWalletException e) {
            return null;
        }
    }

    private int getNextReceiveAddressIndex(Account account)  {
        return multiAddressFactory.getNextReceiveAddressIndex(account.getXpub(), account.getAddressLabels());
    }

    private int getNextChangeAddressIndex(Account account)  {
        return multiAddressFactory.getNextChangeAddressIndex(account.getXpub());
    }

    /**
     * Gets next change address.
     * @param account
     * @return
     * @throws IOException
     * @throws HDWalletException
     */
    public String getNextChangeAddress(Account account) throws IOException, HDWalletException {

        int nextIndex = getNextChangeAddressIndex(account);

        HDAccount hdAccount = getPayload().getHdWallets().get(0)
            .getHDAccountFromAccountBody(account);

        return hdAccount.getChange().getAddressAt(nextIndex).getAddressString();
    }

    public void incrementNextReceiveAddress(Account account) {
        multiAddressFactory.incrementNextReceiveAddress(account.getXpub(), account.getAddressLabels());
    }

    public void incrementNextChangeAddress(Account account) {
        multiAddressFactory.incrementNextChangeAddress(account.getXpub());
    }

    @Nullable
    public String getNextReceiveAddressAndReserve(Account account, String reserveLabel)
        throws HDWalletException, EncryptionException, NoSuchAlgorithmException, IOException, ServerConnectionException {

        int nextIndex = getNextReceiveAddressIndex(account);

        reserveAddress(account, nextIndex, reserveLabel);

        HDAccount hdAccount = getPayload().getHdWallets().get(0)
            .getHDAccountFromAccountBody(account);

        return hdAccount.getReceive().getAddressAt(nextIndex).getAddressString();
    }

    public void reserveAddress(Account account, int index, String label)
        throws HDWalletException, EncryptionException, NoSuchAlgorithmException, IOException, ServerConnectionException {

        account.addAddressLabel(index, label);
        if(!save()) {
            throw new ServerConnectionException("Unable to reserve address.");
        }
    }

    //********************************************************************************************//
    //*                                        Balance                                           *//
    //********************************************************************************************//

    /**
     * Balance API - Final balance for address.
     * @param address
     * @return
     */
    public BigInteger getAddressBalance(String address) {
        BigInteger result = balanceManager.getAddressBalance(address);
        if(result == null)result = BigInteger.ZERO;
        return result;
    }

    /**
     * Balance API - Final balance for all accounts + addresses.
     * @return
     */
    public BigInteger getWalletBalance() {
        BigInteger result = balanceManager.getWalletBalance();
        if(result == null)result = BigInteger.ZERO;
        return result;
    }

    /**
     * Balance API - Final balance imported addresses.
     * @return
     */
    public BigInteger getImportedAddressesBalance() {
        BigInteger result = balanceManager.getImportedAddressesBalance();
        if(result == null)result = BigInteger.ZERO;
        return result;
    }

    /**
     * Updates all account and address balances and transaction counts.
     * API call uses the Balance endpoint and is much quicker than multiaddress.
     * This will allow the wallet to display wallet/account totals while transactions are still being fetched.
     * This also stores the amount of transactions per address which we can use to limit the calls to multiaddress
     * when the limit is reached.
     * @throws ServerConnectionException
     * @throws IOException
     */
    public void updateAllBalances() throws ServerConnectionException, IOException {

        List<String> legacyAddressList = getPayload().getLegacyAddressStringList();
        ArrayList<String> all = new ArrayList<>(getAllAccountsAndAddresses());

        balanceManager.updateAllBalances(legacyAddressList, all);
    }

    /**
     * Updates address balance as well as wallet balance.
     * This is used to immediately update balances after a successful transaction which speeds
     * up the balance the UI reflects without the need to wait for incoming websocket notification.
     * @param amount
     * @throws Exception
     */
    public void subtractAmountFromAddressBalance(String address, BigInteger amount) throws Exception {
        balanceManager.subtractAmountFromAddressBalance(address, amount);
    }
}