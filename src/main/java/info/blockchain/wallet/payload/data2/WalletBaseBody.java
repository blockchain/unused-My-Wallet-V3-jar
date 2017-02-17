package info.blockchain.wallet.payload.data2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.EncryptionException;
import info.blockchain.wallet.exceptions.UnsupportedVersionException;
import info.blockchain.wallet.util.FormatsUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.annotation.Nonnull;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.json.JSONObject;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.paddings.BlockCipherPadding;
import org.spongycastle.crypto.paddings.ISO10126d2Padding;
import org.spongycastle.crypto.paddings.ISO7816d4Padding;
import org.spongycastle.crypto.paddings.ZeroBytePadding;
import org.spongycastle.util.encoders.Hex;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class WalletBaseBody {

    private static final int DEFAULT_PBKDF2_ITERATIONS_V1_A = 1;
    private static final int DEFAULT_PBKDF2_ITERATIONS_V1_B = 10;

    //payload could be string in V1
    //V2 and up is WalletWrapperBody
    @JsonProperty("payload")
    private String payload;

    //V3
    @JsonProperty("guid")
    private String guid;

    @JsonProperty("extra_seed")
    private String extraSeed;

    @JsonProperty("payload_checksum")
    private String payloadChecksum;

    @JsonProperty("war_checksum")
    private String warChecksum;

    @JsonProperty("language")
    private String language;

    @JsonProperty("storage_token")
    private String storageToken;

    @JsonProperty("sync_pubkeys")
    private boolean syncPubkeys;

    public WalletBaseBody() {
        //Empty constructor needed for Jackson
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void decryptPayload(@Nonnull String password)
        throws DecryptionException, IOException, InvalidCipherTextException, UnsupportedVersionException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, DecoderException {

        if (!isV1Wallet()) {
            walletBody = decryptV3Wallet(password);
        } else {
            walletBody =  decryptV1Wallet(password);
        }
    }

    private WalletBody decryptV3Wallet(String password)
        throws IOException, DecryptionException, InvalidCipherTextException, UnsupportedVersionException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, DecoderException {

        WalletWrapperBody walletWrapperBody = WalletWrapperBody.fromJson(payload);
        WalletBody walletBody = walletWrapperBody.decryptPayload(password);
        //In case iterations weren't set in wallet options
        walletBody.getOptions().setPbkdf2Iterations(walletWrapperBody.getPbkdf2Iterations());
        return walletBody;
    }

    /*
    No need to encrypt V1 wallet again. We will force user to upgrade to V3
     */
    private WalletBody decryptV1Wallet(String password)
        throws DecryptionException, IOException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, DecoderException, InvalidCipherTextException {

        String decrypted = null;
        int succeededIterations = -1000;

        int iterations[] = {DEFAULT_PBKDF2_ITERATIONS_V1_A, DEFAULT_PBKDF2_ITERATIONS_V1_B};
        int modes[] = {AESUtil.MODE_CBC, AESUtil.MODE_OFB};
        BlockCipherPadding[] paddings = {
            new ISO10126d2Padding(),
            new ISO7816d4Padding(),
            new ZeroBytePadding(),
            null};//NoPadding

        outerloop:
        for (int iteration : iterations) {
            for (int mode : modes) {
                for (BlockCipherPadding padding : paddings) {
                    try {
                        decrypted = AESUtil
                            .decryptWithSetMode(payload, password, iteration, mode,
                                padding);
                        //Ensure it's parsable
                        new JSONObject(decrypted);

                        succeededIterations = iteration;
                        break outerloop;

                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                }
            }
        }

        if (decrypted == null || succeededIterations < 0) {
            throw new DecryptionException("Failed to decrypt");
        }

        String decryptedPayload = decrypted;
        walletBody = WalletBody.fromJson(decryptedPayload);
        return walletBody;
    }

    public String getExtraSeed() {
        return extraSeed;
    }

    public void setExtraSeed(String extraSeed) {
        this.extraSeed = extraSeed;
    }

    public String getPayloadChecksum() {
        return payloadChecksum;
    }

    public void setPayloadChecksum(String payloadChecksum) {
        this.payloadChecksum = payloadChecksum;
    }

    public String getWarChecksum() {
        return warChecksum;
    }

    public void setWarChecksum(String warChecksum) {
        this.warChecksum = warChecksum;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStorageToken() {
        return storageToken;
    }

    public void setStorageToken(String storageToken) {
        this.storageToken = storageToken;
    }

    public boolean isSyncPubkeys() {
        return syncPubkeys;
    }

    public void setSyncPubkeys(boolean syncPubkeys) {
        this.syncPubkeys = syncPubkeys;
    }

    public boolean isV1Wallet() {
        return !FormatsUtil.isValidJson(payload);
    }

    public static WalletBaseBody fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, WalletBaseBody.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public Pair encryptAndWrapPayload(String password)
        throws JsonProcessingException, UnsupportedEncodingException, EncryptionException, NoSuchAlgorithmException {

        int iterations = walletBody.getOptions().getPbkdf2Iterations();
        String encryptedPayload = AESUtil.encrypt(walletBody.toJson(), password, iterations);
        WalletWrapperBody wrapperBody = WalletWrapperBody.wrap(encryptedPayload, iterations);

        String checkSum = new String(Hex.encode(MessageDigest.getInstance("SHA-256").digest(wrapperBody.toJson().getBytes("UTF-8"))));

        return Pair.of(checkSum, wrapperBody);
    }

    /**********************************************************************************************/
    /*                          HDWallet body containing private keys                               */
    /**********************************************************************************************/
    private WalletBody walletBody;

    public WalletBody getWalletBody() {
        return walletBody;
    }

    public void setWalletBody(WalletBody walletBody) {
        this.walletBody = walletBody;
    }
}
