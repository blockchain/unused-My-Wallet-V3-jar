package info.blockchain.wallet.payload;

import info.blockchain.api.data.MultiAddress;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.WalletApi;
import info.blockchain.wallet.bip44.HDWallet;
import info.blockchain.wallet.exceptions.AccountLockedException;
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
import info.blockchain.wallet.payload.data2.AccountBody;
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
    private MetadataNodeFactory metadataNodeFactory;

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

    public WalletBody getWalletBody() {
        return walletBaseBody.getWalletBody();
    }

    public MetadataNodeFactory getMetadataNodeFactory() {
        return metadataNodeFactory;
    }

    //********************************************************************************************//
    //*                  Wallet initialization, creation, recovery, syncing                      *//
    //********************************************************************************************//

    /*
    NB! When called from Android - First apply PRNGFixes
     */
    public void create(@Nonnull String defaultAccountName, @Nonnull String email)
        throws Exception {

        walletBaseBody = new WalletBaseBody();
        walletBaseBody.setWalletBody(new WalletBody(defaultAccountName));

        boolean success = saveNewWallet(email);

        if(!success) {
            //revert on save fail
            walletBaseBody = null;
            throw new ServerConnectionException("Failed to save new wallet to server.");
        }
    }

    public void recoverFromMnemonic(@Nonnull String mnemonic, @Nonnull String defaultAccountName,
        @Nonnull String email) throws Exception {

        walletBaseBody = new WalletBaseBody();
        walletBaseBody.setWalletBody(WalletBody.recoverFromMnemonic(mnemonic, defaultAccountName));

        boolean success = saveNewWallet(email);

        if(!success) {
            //revert on save fail
            walletBaseBody = null;
            throw new ServerConnectionException("Failed to save new wallet to server.");
        }
    }

    /*
    NB! When called from Android - First apply PRNGFixes
     */
    public boolean upgradeV2PayloadToV3(String secondPassword, String defaultAccountName) throws Exception {

        walletBaseBody.getWalletBody().upgradeV2PayloadToV3(secondPassword, defaultAccountName);

        boolean success = save();

        if (!success) {
            //Revert on save fail
            walletBaseBody.getWalletBody().setHdWallets(null);
        }

        return success;
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
            String errorMessage = exe.errorBody().string();
            if (errorMessage != null && errorMessage.contains("Unknown Wallet Identifier")) {
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

    //********************************************************************************************//
    //*                         Account and Legacy Address creation                              *//
    //********************************************************************************************//

    public boolean addAccount(String label, @Nullable String secondPassword)
        throws Exception {
        AccountBody accountBody = walletBaseBody.getWalletBody().addAccount(label, secondPassword);

        boolean success = save();

        if (!success) {
            //Revert on save fail
            walletBaseBody.getWalletBody().getHdWallet().getAccounts().remove(accountBody);
        }

        return success;
    }

    /**
     * NB! When called from Android - First apply PRNGFixes
     * @param label
     * @param secondPassword
     * @return
     * @throws Exception
     */
    public boolean addLegacyAddress(String label, @Nullable String secondPassword) throws Exception {

        LegacyAddressBody legacyAddressBody = walletBaseBody.getWalletBody()
            .addLegacyAddress(label, secondPassword);

        boolean success = save();

        if (!success) {
            //Revert on save fail
            walletBaseBody.getWalletBody().getLegacyAddressList().remove(legacyAddressBody);
        }

        return success;
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

    //********************************************************************************************//
    //*                 Shortcut methods(Remove from Android first then delete)                  *//
    //********************************************************************************************//

    @Deprecated
    //Shouldn't really have to call this - validation already happens in methods that need it
    public void validateSecondPassword(String secondPassword) throws DecryptionException {
        walletBaseBody.getWalletBody().validateSecondPassword(secondPassword);
    }

    @Deprecated
    //old way - try to use walletBody directly
    public boolean isNotUpgraded() {
        return walletBaseBody.getWalletBody() != null && !walletBaseBody.getWalletBody().isUpgraded();
    }

    public String getXpubFromAccountIndex(int accountIdx) {
        return walletBaseBody.getWalletBody().getHdWallet().getAccounts().get(accountIdx).getXpub();
    }

    @Deprecated
    //old way - just get directly from hdWallet
    public ArrayList<String> getActiveXpubs() {
        return walletBaseBody.getWalletBody().getHdWallet().getActive();
    }

    //********************************************************************************************//
    //*                                        Metadata                                          *//
    //********************************************************************************************//

    /**
     *
     * @param node used to sign GUID. This will deactivate push notifications.
     * @return
     * @throws Exception
     */
    public Call<ResponseBody> unregisterMdid(ECKey node) throws Exception {
        String signedGuid = node.signMessage(walletBaseBody.getWalletBody().getGuid());
        return WalletApi.unregisterMdid(walletBaseBody.getWalletBody().getGuid(),
            walletBaseBody.getWalletBody().getSharedKey(),
            signedGuid);
    }

    /**
     *
     * @param node used to sign GUID. This will activate push notifications.
     * @return
     * @throws Exception
     */
    public Call<ResponseBody> registerMdid(ECKey node) throws Exception {
        String signedGuid = node.signMessage(walletBaseBody.getWalletBody().getGuid());
        return WalletApi.registerMdid(walletBaseBody.getWalletBody().getGuid(),
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
                walletBaseBody.getWalletBody().getSharedKey(), tempPassword);
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

        boolean success = metadataNodeFactory.saveMetadataHdNodes(
            walletBaseBody.getWalletBody().getMasterKey(secondPassword));
        if (!success) {
            throw new MetadataException("All Metadata nodes might not have saved.");
        }
    }
}