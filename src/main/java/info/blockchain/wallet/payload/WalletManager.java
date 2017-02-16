package info.blockchain.wallet.payload;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.WalletApi;
import info.blockchain.wallet.exceptions.AccountLockedException;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.EncryptionException;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.exceptions.InvalidCredentialsException;
import info.blockchain.wallet.exceptions.NoSuchAddressException;
import info.blockchain.wallet.exceptions.ServerConnectionException;
import info.blockchain.wallet.exceptions.UnsupportedVersionException;
import info.blockchain.wallet.payload.data2.LegacyAddressBody;
import info.blockchain.wallet.payload.data2.WalletBaseBody;
import info.blockchain.wallet.payload.data2.WalletBody;
import info.blockchain.wallet.payload.data2.WalletWrapperBody;
import info.blockchain.wallet.util.Tools;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import okhttp3.ResponseBody;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.spongycastle.crypto.InvalidCipherTextException;
import retrofit2.Call;
import retrofit2.Response;

public class WalletManager {

    private WalletBaseBody walletBaseBody;

    private String tempPassword;

    private static WalletManager instance = new WalletManager();

    public static WalletManager getInstance() {
        return instance;
    }

    private WalletManager() {
        //no-op
    }

    public void setTempPassword(String password) {
        this.tempPassword = password;
    }

    public void wipe() {
        walletBaseBody = null;
        tempPassword = null;
    }

    public void create(@Nonnull String defaultAccountName, @Nonnull String email)
        throws Exception {

        walletBaseBody = new WalletBaseBody();
        walletBaseBody.setWalletBody(new WalletBody(defaultAccountName));

        saveNewWallet(email);
    }

    public void initializeAndDecrypt(@Nonnull String sharedKey, @Nonnull String guid)
        throws IOException, InvalidCredentialsException, AccountLockedException, ServerConnectionException,
        DecryptionException, InvalidCipherTextException, UnsupportedVersionException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, DecoderException {

        Call<ResponseBody> call = WalletApi.fetchWalletData(guid, sharedKey);
        Response<ResponseBody> exe = call.execute();

        if(exe.isSuccessful()){
            walletBaseBody = WalletBaseBody.fromJson(exe.body().string());
            walletBaseBody.decryptPayload(tempPassword);
        } else {
            // TODO: 14/02/2017 Catching error messages bad
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

    private void validateSave() throws HDWalletException {
        if (walletBaseBody == null) {
            throw new HDWalletException("Save aborted - HDWallet not initialized.");
        } else if (!walletBaseBody.getWalletBody().isEncryptionConsistent()) {
            throw new HDWalletException("Save aborted - Payload corrupted. Key encryption not consistent.");
        } else if (BlockchainFramework.getDevice() == null) {
            throw new HDWalletException("Save aborted - Device name not specified in FrameWork.");
        }
    }

    private boolean saveNewWallet(String email) throws Exception {

        validateSave();

        //Encrypt and wrap payload
        Pair pair = walletBaseBody.encryptAndWrapPayload(tempPassword);
        WalletWrapperBody payloadWrapper = (WalletWrapperBody) pair.getRight();
        String newPayloadChecksum = (String) pair.getLeft();

        //Save to server
        Call<Void> call = WalletApi.insertWallet(
            walletBaseBody.getWalletBody().getGuid(),
            walletBaseBody.getWalletBody().getSharedKey(),
            null,
            payloadWrapper.toJson(),
            newPayloadChecksum,
            email,
            BlockchainFramework.getDevice());

        Response<Void> exe = call.execute();
        if(exe.isSuccessful()) {
            //set new checksum
            walletBaseBody.setPayloadChecksum(newPayloadChecksum);

            return true;
        } else{
            return false;
        }
    }

    public boolean save()
        throws HDWalletException, NoSuchAlgorithmException,
        EncryptionException, IOException {

        validateSave();

        //Encrypt and wrap payload
        Pair pair = walletBaseBody.encryptAndWrapPayload(tempPassword);
        WalletWrapperBody payloadWrapper = (WalletWrapperBody) pair.getRight();
        String newPayloadChecksum = (String) pair.getLeft();
        String oldPayloadChecksum = walletBaseBody.getPayloadChecksum();

        //Save to server
        List<String> syncAddresses = null;
        if(walletBaseBody.isSyncPubkeys()) {
            syncAddresses = Tools.filterLegacyAddress(
                LegacyAddressBody.NORMAL_ADDRESS,
                walletBaseBody.getWalletBody().getLegacyAddressList());
        }
        Call<Void> call = WalletApi.updateWallet(
            walletBaseBody.getWalletBody().getGuid(),
            walletBaseBody.getWalletBody().getSharedKey(),
            syncAddresses,
            payloadWrapper.toJson(),
            newPayloadChecksum,
            oldPayloadChecksum,
            BlockchainFramework.getDevice());

        Response<Void> exe = call.execute();
        if(exe.isSuccessful()) {
            //set new checksum
            walletBaseBody.setPayloadChecksum(newPayloadChecksum);

            return true;
        } else{
            return false;
        }
    }

    public void validateSecondPassword(String secondPassword) throws DecryptionException {
        walletBaseBody.getWalletBody().validateSecondPassword(secondPassword);
    }

    /*
    Used to check if wallet has HD wallet - Prompt user for upgrade if not
     */
    public boolean isNotUpgraded() {
        return walletBaseBody.getWalletBody() != null && !walletBaseBody.getWalletBody().isUpgraded();
    }

    /*
    When called from Android - First apply PRNGFixes
     */
    public void upgradeV2PayloadToV3(String secondPassword, String defaultAccountName) throws Exception {
        walletBaseBody.getWalletBody().upgradeV2PayloadToV3(secondPassword, defaultAccountName);
    }

    public void getNextChangeAddress(int accountIndex) {
        // TODO: 13/02/2017 Multi_address needs to set this first
//        walletBaseBody.getWalletBody().getHdWallet().getAccounts().get(accountIndex).ge
    }

    public String getXpubFromAccountIndex(int accountIdx) {
        return walletBaseBody.getWalletBody().getHdWallet().getAccounts().get(accountIdx).getXpub();
    }

    /**
     * Return non-archived xpubs
     * @return
     */
    public ArrayList<String> getActiveXpubs() {

        if(walletBaseBody.getWalletBody().getHdWallet() == null) {
            return new ArrayList<>();
        }

        return walletBaseBody.getWalletBody().getHdWallet().getActive();
    }

    public void addAccount(String label, @Nullable String secondPassword)
        throws Exception {
        walletBaseBody.getWalletBody().addAccount(label, secondPassword);
    }

    public void addLegacyAddress(String label, @Nullable String secondPassword) throws Exception {
        walletBaseBody.getWalletBody().addLegacyAddress(label, secondPassword);
    }

    public boolean setKeyForLegacyAddress(ECKey key, @Nullable String secondPassword)
        throws EncryptionException, IOException, DecryptionException, NoSuchAddressException,
        NoSuchAlgorithmException, HDWalletException {

        LegacyAddressBody matchingLegacyAddress = walletBaseBody.getWalletBody()
            .setKeyForLegacyAddress(key, secondPassword);

        boolean success = save();

        if (!success) {
            //Revert on save fail
            matchingLegacyAddress.setPrivateKey(null);
        }

        return success;

    }

    public WalletBody getWalletBody() {
        return walletBaseBody.getWalletBody();
    }
}