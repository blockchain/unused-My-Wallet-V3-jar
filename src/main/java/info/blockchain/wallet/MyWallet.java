//package info.blockchain.wallet;
//
//import info.blockchain.wallet.api.WalletApi;
//import info.blockchain.wallet.exceptions.AccountLockedException;
//import info.blockchain.wallet.exceptions.DecryptionException;
//import info.blockchain.wallet.exceptions.HDWalletException;
//import info.blockchain.wallet.exceptions.InvalidCredentialsException;
//import info.blockchain.wallet.exceptions.ServerConnectionException;
//import info.blockchain.wallet.exceptions.UnsupportedVersionException;
//import info.blockchain.wallet.payload.data2.AccountBody;
//import info.blockchain.wallet.payload.data2.LegacyAddressBody;
//import info.blockchain.wallet.payload.data2.HDWalletBody;
//import info.blockchain.wallet.payload.data2.WalletBaseBody;
//import info.blockchain.wallet.payload.data2.WalletBody;
//import info.blockchain.wallet.payload.data2.WalletWrapperBody;
//import info.blockchain.wallet.util.DoubleEncryptionFactory;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import javax.annotation.Nonnull;
//import okhttp3.ResponseBody;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.tuple.Pair;
//import org.spongycastle.crypto.InvalidCipherTextException;
//import retrofit2.Call;
//import retrofit2.Response;
//
//public class MyWallet {
//
//    public static final int CURRENT_VERSION = 3;
//    public static final int DEFAULT_PBKDF2_ITERATIONS_V2 = 5000;
//
//    private WalletBaseBody walletBaseBody;
//
//    private String tempPassword;
//
//    private static MyWallet instance = new MyWallet();
//
//    public static MyWallet getInstance() {
//        return instance;
//    }
//
//    private MyWallet() {
//        //no-op
//    }
//
//    public void setTempPassword(String password) {
//        this.tempPassword = password;
//    }
//
//    public void create(@Nonnull String defaultAccountName, @Nonnull String email)
//        throws Exception {
//
//        walletBaseBody = new WalletBaseBody(defaultAccountName);
//
//        saveNewWallet(email);
//    }
//
//    public void initializeAndDecrypt(@Nonnull String sharedKey, @Nonnull String guid)
//        throws IOException, InvalidCredentialsException, AccountLockedException, ServerConnectionException,
//        DecryptionException, InvalidCipherTextException, UnsupportedVersionException {
//
//        Call<ResponseBody> call = WalletApi.fetchWalletData(guid, sharedKey);
//        Response<ResponseBody> exe = call.execute();
//
//        if(exe.isSuccessful()){
//            walletBaseBody = WalletBaseBody.fromJson(exe.body().string());
//            WalletBody walletBody = walletBaseBody.decryptPayload(tempPassword);
////            walletBody.get
//        } else {
//            // TODO: 14/02/2017 Catching error messages bad
//            String errorMessage = exe.errorBody().string();
//            if (errorMessage != null && errorMessage.contains("Invalid GUID")) {
//                throw new InvalidCredentialsException();
//            } else if (errorMessage != null && errorMessage.contains("locked")) {
//                throw new AccountLockedException(errorMessage);
//            } else {
//                throw new ServerConnectionException(errorMessage);
//            }
//        }
//    }
//
//    public void wipe() {
//        walletBaseBody = null;
//        tempPassword = null;
//    }
//
//    private void validateSave() throws HDWalletException {
//        if (walletBaseBody == null) {
//            throw new HDWalletException("Save aborted - HDWallet not initialized.");
//        } else if (!walletBaseBody.getWalletBody().isEncryptionConsistent()) {
//            throw new HDWalletException("Save aborted - Payload corrupted. Key encryption not consistent.");
//        } else if (BlockchainFramework.getDevice() == null) {
//            throw new HDWalletException("Save aborted - Device name not specified in FrameWork.");
//        }
//    }
//
//    private boolean saveNewWallet(String email) throws Exception {
//
//        validateSave();
//
//        //Encrypt and wrap payload
//        Pair pair = walletBaseBody.encryptAndWrapPayload(tempPassword);
//        WalletWrapperBody payloadWrapper = (WalletWrapperBody) pair.getRight();
//        String newPayloadChecksum = (String) pair.getLeft();
//
//        //Save to server
//        Call<Void> call = WalletApi.insertWallet(
//            walletBaseBody.getWalletBody().getGuid(),
//            walletBaseBody.getWalletBody().getSharedKey(),
//            null,
//            payloadWrapper.toJson(),
//            newPayloadChecksum,
//            email,
//            BlockchainFramework.getDevice());
//
//        Response<Void> exe = call.execute();
//        if(exe.isSuccessful()) {
//            //set new checksum
//            walletBaseBody.setPayloadChecksum(newPayloadChecksum);
//
//            return true;
//        } else{
//            return false;
//        }
//    }
//
//    public boolean save() throws Exception {
//
//        validateSave();
//
//        //Encrypt and wrap payload
//        Pair pair = walletBaseBody.encryptAndWrapPayload(tempPassword);
//        WalletWrapperBody payloadWrapper = (WalletWrapperBody) pair.getRight();
//        String newPayloadChecksum = (String) pair.getLeft();
//        String oldPayloadChecksum = walletBaseBody.getPayloadChecksum();
//
//        //Save to server
//        List<String> syncAddresses = null;
//        if(walletBaseBody.isSyncPubkeys()) {
//            syncAddresses = LegacyAddressBody.filterAddressString(LegacyAddressBody.NORMAL_ADDRESS, walletBaseBody.getWalletBody().getKeys());
//        }
//        Call<Void> call = WalletApi.updateWallet(
//            walletBaseBody.getWalletBody().getGuid(),
//            walletBaseBody.getWalletBody().getSharedKey(),
//            syncAddresses,
//            payloadWrapper.toJson(),
//            newPayloadChecksum,
//            oldPayloadChecksum,
//            BlockchainFramework.getDevice());
//
//        Response<Void> exe = call.execute();
//        if(exe.isSuccessful()) {
//            //set new checksum
//            walletBaseBody.setPayloadChecksum(newPayloadChecksum);
//
//            return true;
//        } else{
//            return false;
//        }
//    }
//
//    public boolean validateSecondPassword(String secondPassword) {
//        return DoubleEncryptionFactory.getInstance().validateSecondPassword(
//            walletBaseBody.getWalletBody().getDpasswordhash(),
//            walletBaseBody.getWalletBody().getSharedKey(),
//            secondPassword,
//            walletBaseBody.getWalletBody().getPbkdf2Iterations());
//    }
//
//    /*
//    When called from Android - First apply PRNGFixes
//     */
//    public void upgradeV2PayloadToV3(String secondPassword, String defaultAccountName) throws Exception {
//
//        //Check if payload has 2nd password
//        if (walletBaseBody.getWalletBody().isDoubleEncryption() && (
//            StringUtils.isEmpty(secondPassword) || !validateSecondPassword(secondPassword))) {
//            throw new DecryptionException("Double encryption password error!");
//        }
//
//        //Upgrade
//        walletBaseBody.getWalletBody().upgradeV2PayloadToV3(secondPassword, defaultAccountName);
//    }
//
//    /*
//    Used to check if wallet has HD wallet - Prompt user for upgrade if not
//     */
//    public boolean isNotUpgraded() {
//        return walletBaseBody.getWalletBody() != null && !walletBaseBody.getWalletBody().isUpgraded();
//    }
//
//    public void getNextChangeAddress(int accountIndex) {
//        //Currently only 1 hdWallet possible in payload
//        // TODO: 13/02/2017 Multi_address needs to set this first
////        walletBaseBody.getWalletBody().getHdWallets().get(0).getAccounts().get(accountIndex).ge
//    }
//
//    public String getXpubFromAccountIndex(int accountIdx) {
//        return walletBaseBody.getWalletBody().getHdWallet().getAccounts().get(accountIdx).getXpub();
//    }
//
//    /**
//     * Return non-archived xpubs
//     * @return
//     */
//    public ArrayList<String> getActiveXpubs() {
//
//        HDWalletBody hdWallet = walletBaseBody.getWalletBody()
//            .getHdWallet();
//
//        ArrayList<String> xpubs = new ArrayList<>();
//
//        if(hdWallet == null || hdWallet.getAccounts() == null) {
//            return xpubs;
//        }
//
//        int nb_accounts = hdWallet.getAccounts().size();
//        for (int i = 0; i < nb_accounts; i++) {
//
//            AccountBody account = hdWallet.getAccounts().get(i);
//            boolean isArchived = account.isArchived();
//            if (!isArchived) {
//                String xpub = account.getXpub();
//                if (xpub != null && xpub.length() > 0) {
//                    xpubs.add(xpub);
//                }
//            }
//        }
//        return xpubs;
//    }
//
//    public void addAccount(String label) {
//        walletBaseBody.getWalletBody().getHdWallet().addAccount(label);
//    }
//
//    public LegacyAddressBody generateLegacyAddress(String secondPassword) throws Exception {
//
//        LegacyAddressBody addressBody = LegacyAddressBody.generateNewLegacy();
//
//        if (walletBaseBody.getWalletBody().isDoubleEncryption() && validateSecondPassword(secondPassword)) {
//
//            //Double encryption
//            String unencryptedKey = addressBody.getPrivateKey();
//
//            String encryptedKey = DoubleEncryptionFactory.getInstance().encrypt(unencryptedKey,
//                walletBaseBody.getWalletBody().getSharedKey(),
//                secondPassword,
//                walletBaseBody.getWalletBody().getPbkdf2Iterations());
//
//            addressBody.setPrivateKey(encryptedKey);
//
//        }else {
//            throw new DecryptionException("Second password validation error.");
//        }
//
//        return addressBody;
//    }
//
//    public boolean addLegacyAddress(LegacyAddressBody addressBody) throws Exception {
//        walletBaseBody.getWalletBody().getKeys().add(addressBody);
//        return save();
//    }
//}
