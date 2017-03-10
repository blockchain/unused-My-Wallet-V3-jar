package info.blockchain.wallet.payload;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.Balance;
import info.blockchain.api.data.MultiAddress;
import info.blockchain.api.data.Transaction;
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

import java.util.Collection;
import java.util.Map.Entry;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    //Assume we only support 1 hdWallet
    private static final int HD_WALLET_INDEX = 0;

    private WalletBase walletBaseBody;
    private String password;
    private MetadataNodeFactory metadataNodeFactory;
    // This is an explicit dependency and should be injected for easier testing
    private WalletApi walletApi;
    private BlockExplorer blockExplorer;
    //Key = address/xpub, Value = Multiaddress response from endpoint
    private HashMap<String, MultiAddress> multiAddressMap;
    //Key = address, Value = list of summarized transactions
    private HashMap<String, List<TransactionSummary>> transactionSummaryMap;
    private HashMap<String, BigInteger> balanceMap;

    private static PayloadManager instance = new PayloadManager();

    public static PayloadManager getInstance() {
        return instance;
    }

    private PayloadManager() {
        walletApi = new WalletApi();
        blockExplorer = new BlockExplorer(BlockchainFramework.getRetrofitServerInstance(), BlockchainFramework.getApiCode());
        transactionSummaryMap = new HashMap<>();
        multiAddressMap = new HashMap<>();
        balanceMap = new HashMap<>();
    }

    public void wipe() {
        walletBaseBody = null;
        password = null;
    }

    public Wallet getPayload() {
        return walletBaseBody.getWalletBody();
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

        this.password = password;
        walletBaseBody = new WalletBase();

        Wallet walletBody = new Wallet();
        HDWallet hdWallet = HDWallet.recoverFromMnemonic(mnemonic, defaultAccountName);
        walletBody.setHdWallets(Arrays.asList(hdWallet));

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
        ApiException {

        this.password = password;

        Call<ResponseBody> call = walletApi.fetchWalletData(guid, sharedKey);
        Response<ResponseBody> exe = call.execute();

        if(exe.isSuccessful()){
            walletBaseBody = WalletBase.fromJson(exe.body().string());
            walletBaseBody.decryptPayload(this.password);
        } else {
            String errorMessage = exe.errorBody().string();
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
            throw new ServerConnectionException(exe.errorBody().string());
        }

        updateAllBalances();
    }

    private void validateSave() throws HDWalletException {
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
            throw new ServerConnectionException(exe.code()+" - "+exe.errorBody().string());
        }
    }

    /**
     * Saves wallet to server
     * @return True if save successful
     * @throws HDWalletException
     * @throws NoSuchAlgorithmException
     * @throws EncryptionException
     * @throws IOException
     */
    public boolean save()
        throws HDWalletException, NoSuchAlgorithmException,
        EncryptionException, IOException {

        validateSave();

        //Encrypt and wrap payload
        Pair pair = walletBaseBody.encryptAndWrapPayload(password);
        WalletWrapper payloadWrapper = (WalletWrapper) pair.getRight();
        String newPayloadChecksum = (String) pair.getLeft();
        String oldPayloadChecksum = walletBaseBody.getPayloadChecksum();

        //Save to server
        List<String> syncAddresses = null;
        if(walletBaseBody.isSyncPubkeys()) {
            syncAddresses = Tools.filterLegacyAddress(
                LegacyAddress.NORMAL_ADDRESS,
                walletBaseBody.getWalletBody().getLegacyAddressList());
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
        if(exe.isSuccessful()) {
            //set new checksum
            walletBaseBody.setPayloadChecksum(newPayloadChecksum);

            return true;
        } else{
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

        return all;
    }

    public boolean validateSecondPassword(String secondPassword) {

        try{
            walletBaseBody.getWalletBody().validateSecondPassword(secondPassword);
            return true;
        } catch (Exception e){
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

        walletBaseBody.getWalletBody().validateSecondPassword(secondPassword);

        String decryptedPrivateKey = legacyAddress.getPrivateKey();

        if(secondPassword != null) {
            decryptedPrivateKey = DoubleEncryptionFactory
                .decrypt(legacyAddress.getPrivateKey(),
                    walletBaseBody.getWalletBody().getSharedKey(),
                    secondPassword,
                    walletBaseBody.getWalletBody().getOptions().getPbkdf2Iterations());
        }

        return Tools.getECKeyFromKeyAndAddress(decryptedPrivateKey, legacyAddress.getAddress());
    }

    public String getNextReceiveAddress(Account account) throws IOException, HDWalletException {

        MultiAddress multiAddress = multiAddressMap.get(account.getXpub());

        int nextIndex = MultiAddressFactory.getNextReceiveAddress(multiAddress, account.getXpub(), account.getAddressLabels());

        HDAccount hdAccount = getPayload().getHdWallets().get(0)
            .getHDAccountFromAccountBody(account);

        return hdAccount.getReceive().getAddressAt(nextIndex).getAddressString();
    }

    public String getNextChangeAddress(Account account) throws IOException, HDWalletException {


        MultiAddress multiAddress = multiAddressMap.get(account.getXpub());

        int nextIndex = MultiAddressFactory.getNextChangeAddress(multiAddress, account.getXpub());

        HDAccount hdAccount = getPayload().getHdWallets().get(0)
            .getHDAccountFromAccountBody(account);

        return hdAccount.getChange().getAddressAt(nextIndex).getAddressString();
    }

    public List<TransactionSummary> getWalletTransactions() {
        return getAddressTransactions(MULTI_ADDRESS_ALL);
    }

    public List<TransactionSummary> getImportedAddressesTransactions() {
        return getAddressTransactions(MULTI_ADDRESS_ALL_LEGACY);
    }

    /**
     *
     * @param address or xpub
     * @return
     */
    public List<TransactionSummary> getAddressTransactions(String address) {
        if(transactionSummaryMap.containsKey(address)) {
            return transactionSummaryMap.get(address);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Updates address balance as well as wallet balance.
     * This is used to immediately update balances after a successful transaction which speeds
     * up the balance the UI reflects without the need to wait for incoming websocket notification.
     * @param amount
     * @throws Exception
     */
    public void subtractAmountFromAddressBalance(String address, BigInteger amount) throws Exception {

        //Update individual address
        MultiAddress addressMultiAddress = multiAddressMap.get(address);
        if(addressMultiAddress == null) {
            throw new Exception("No info for this address. updateMultiAddress should be called first.");
        }
        BigInteger newBalance = getAddressBalance(address).subtract(amount);
        addressMultiAddress.getWallet().setFinalBalance(newBalance);

        //Update wallet balance
        MultiAddress allMultiAddress = multiAddressMap.get(MULTI_ADDRESS_ALL);
        if(allMultiAddress == null) {
            throw new Exception("No info for wallet. updateMultiAddress should be called first.");
        }
        newBalance = getWalletBalance().subtract(amount);
        allMultiAddress.getWallet().setFinalBalance(newBalance);
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
    public Observable<ResponseBody> unregisterMdid(ECKey node) throws Exception {
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
    public Observable<ResponseBody> registerMdid(ECKey node) throws Exception {
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

    private MultiAddress getMultiAddress(String context, int limit, int offset) throws IOException, ApiException{

        if (context.equals(MULTI_ADDRESS_ALL)) {
            return getPayload().getWalletBalanceAndTransactions(limit, offset);
        } else {
            return getPayload().getAccountBalanceAndTransactions(context, limit, offset);
        }
    }

    /**
     * Updates internal balance and transaction list for all wallet accounts/addresses
     * @param limit Amount of transactions per page
     * @param offset Page offset
     * @return List of tx summaries for all wallet transactions
     * @throws IOException
     * @throws ApiException
     */
    public List<TransactionSummary> updateAllTransactions(int limit, int offset) throws IOException, ApiException {
        return updateAccountTransaction(MULTI_ADDRESS_ALL, limit, offset);
    }

    /**
     * Updates internal balance and transaction list
     * @param xpub
     * @param limit Amount of transactions per page
     * @param offset Page offset
     * @return List of tx summaries for specified xpubs transactions
     * @throws IOException
     * @throws ApiException
     */
    public List<TransactionSummary> updateAccountTransaction(String xpub, int limit, int offset)
        throws IOException, ApiException {

        LinkedHashSet<String> all = getAllAccountsAndAddresses();
        List<String> watchOnly = getPayload().getWatchOnlyAddressStringList();

        MultiAddress multiAddress = getMultiAddress(xpub, limit, offset);
        if(multiAddress == null || multiAddress.getTxs() == null) {
            return new ArrayList<>();
        }

        MultiAddressFactory.sort(multiAddress.getTxs());

        MultiAddress existing = multiAddressMap.get(xpub);
        if(existing != null && existing.getTxs() != null) {

            multiAddress.getTxs().addAll(existing.getTxs());

            //Remove duplicates
            Set<Transaction> hs = new HashSet<>();
            hs.addAll(multiAddress.getTxs());
            multiAddress.getTxs().clear();
            multiAddress.getTxs().addAll(hs);

            multiAddressMap.put(xpub, multiAddress);
        } else {
            multiAddressMap.put(xpub, multiAddress);
        }

        List<TransactionSummary> summaryList = MultiAddressFactory.summarize(all, watchOnly, multiAddress);
        transactionSummaryMap.put(xpub, summaryList);

        return summaryList;
    }

    /**
     * Updates internal balance and transaction list for imported addresses
     * @param address
     * @param limit Amount of transactions per page
     * @param offset Page offset
     * @return Consolidated list of tx summaries for specified imported transactions
     * @throws IOException
     * @throws ApiException
     */
    // TODO: 09/03/2017 Imported addresses needs sorting out
    public List<TransactionSummary> updateAddressTransactions(String address, int limit, int offset)
        throws IOException, ApiException {

        LinkedHashSet<String> all = getAllAccountsAndAddresses();
        List<String> watchOnly = getPayload().getWatchOnlyAddressStringList();

        MultiAddress multiAddress = getMultiAddress(address, limit, offset);
        if(multiAddress == null || multiAddress.getTxs() == null) {
            return new ArrayList<>();
        }

        MultiAddressFactory.sort(multiAddress.getTxs());
        multiAddressMap.put(address, multiAddress);
        List<TransactionSummary> summaryList = MultiAddressFactory.summarize(all, watchOnly, multiAddress);
        transactionSummaryMap.put(address, summaryList);

        //Consolidate for 'Imported addresses'
        MultiAddress existing = multiAddressMap.get(MULTI_ADDRESS_ALL_LEGACY);
        if(existing != null && existing.getTxs() != null) {
            existing.getTxs().addAll(multiAddress.getTxs());
            existing.getWallet().setFinalBalance(existing.getWallet().getFinalBalance()
                .add(multiAddress.getWallet().getFinalBalance()));
        } else {
            existing = multiAddress;
        }
        multiAddressMap.put(MULTI_ADDRESS_ALL_LEGACY, existing);
        summaryList = MultiAddressFactory.summarize(all, watchOnly, existing);
        transactionSummaryMap.put(MULTI_ADDRESS_ALL_LEGACY, summaryList);

        return summaryList;
    }

    /**
     * Calculates if an address belongs to any xpubs in wallet.
     * Make sure multi address is up to date before executing this method.
     * @param address
     * @return
     */
    public boolean isOwnHDAddress(String address) {
        MultiAddress multiAddress = multiAddressMap.get(MULTI_ADDRESS_ALL);
        return MultiAddressFactory.isOwnHDAddress(multiAddress, address);
    }

    /**
     * Calculates which xpub an address belongs to in wallet.
     * Make sure multi address is up to date before executing this method.
     * @param address
     * @return
     */
    public String getXpubFromAddress(String address) {
        MultiAddress multiAddress = multiAddressMap.get(MULTI_ADDRESS_ALL);
        return MultiAddressFactory.getXpubFromAddress(multiAddress, address);
    }

    /**
     * Converts any address to a label.
     * @param address Accepts account receive or change chain address, as well as legacy address.
     * @return Account or legacy address label
     */
    public String getLabelFromAddress(String address) {

        String label;
        String xpub = getXpubFromAddress(address);

        if(xpub != null) {
            label = getPayload().getHdWallets().get(HD_WALLET_INDEX).getLabelFromXpub(xpub);
        } else {
            label = getPayload().getLabelFromLegacyAddress(address);
        }

        if(label == null || label.isEmpty()) {
            label = address;
        }

        return label;
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
        return balanceMap.get(address);
    }

    /**
     * Balance API - Final balance for all accounts + addresses.
     * @return
     */
    public BigInteger getWalletBalance() {
        return balanceMap.get(MULTI_ADDRESS_ALL);
    }

    /**
     * Balance API - Final balance imported addresses.
     * @return
     */
    public BigInteger getImportedAddressesBalance() {
        return balanceMap.get(MULTI_ADDRESS_ALL_LEGACY);
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
        Call<HashMap<String, Balance>> call = blockExplorer.getBalance(new ArrayList<>(getAllAccountsAndAddresses()),
            BlockExplorer.TX_FILTER_ALL);

        List<String> legacyAddressList = getPayload().getLegacyAddressStringList();

        BigInteger walletFinalBalance = BigInteger.ZERO;
        BigInteger importedFinalBalance = BigInteger.ZERO;

        Response<HashMap<String, Balance>> exe = call.execute();
        if(exe.isSuccessful()) {

            Set<Entry<String, Balance>> set = exe.body().entrySet();
            for(Entry<String, Balance> item : set) {
                String address = item.getKey();
                Balance balance = item.getValue();

                balanceMap.put(address, balance.getFinalBalance());

                //Consolidate 'All'
                walletFinalBalance = walletFinalBalance.add(balance.getFinalBalance());

                //Consolidate 'Imported'
                if(legacyAddressList.contains(address)) {
                    importedFinalBalance = importedFinalBalance.add(balance.getFinalBalance());
                }
            }

            balanceMap.put(MULTI_ADDRESS_ALL, walletFinalBalance);
            balanceMap.put(MULTI_ADDRESS_ALL_LEGACY, importedFinalBalance);

        } else {
            throw new ServerConnectionException(exe.errorBody().string());
        }
    }
}