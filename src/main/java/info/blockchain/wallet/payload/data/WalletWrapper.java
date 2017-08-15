package info.blockchain.wallet.payload.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.exceptions.UnsupportedVersionException;
import info.blockchain.wallet.util.FormatsUtil;
import java.io.IOException;
import org.apache.commons.codec.DecoderException;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.spongycastle.crypto.InvalidCipherTextException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class WalletWrapper {

    public static final int CURRENT_VERSION = 3;
    public static final int DEFAULT_PBKDF2_ITERATIONS_V2 = 5000;

    @JsonProperty("version")
    private int version;

    @JsonProperty("pbkdf2_iterations")
    private int pbkdf2_iterations;

    @JsonProperty("payload")
    private String payload;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getPbkdf2Iterations() {
        return pbkdf2_iterations;
    }

    public void setPbkdf2Iterations(int pbkdf2_iterations) {
        this.pbkdf2_iterations = pbkdf2_iterations;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public static WalletWrapper fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, WalletWrapper.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    private void validateVersion() throws UnsupportedVersionException {
        if(getVersion() > CURRENT_VERSION) {
            throw new UnsupportedVersionException(getVersion() + "");
        }
    }

    /**
     * Set iterations to default if need
     */
    private void validatePbkdf2Iterations() {
        if (pbkdf2_iterations <= 0) {
            pbkdf2_iterations = DEFAULT_PBKDF2_ITERATIONS_V2;
        }
    }

    public Wallet decryptPayload(String password)
        throws UnsupportedVersionException, IOException, DecryptionException, InvalidCipherTextException,
        MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, DecoderException, HDWalletException {
        validateVersion();
        validatePbkdf2Iterations();

        String decryptedPayload = null;
        try {
            decryptedPayload = AESUtil.decrypt(getPayload(), password,
                getPbkdf2Iterations());
        } catch (Exception e) {
            throw new DecryptionException(e);
        }

        if(decryptedPayload == null || !FormatsUtil.isValidJson(decryptedPayload)) {
            throw new DecryptionException("Decryption failed.");
        }

        return Wallet.fromJson(decryptedPayload);
    }

    public static WalletWrapper wrap(String encryptedPayload, int iterations) {
        WalletWrapper walletWrapperBody = new WalletWrapper();
        walletWrapperBody.setVersion(CURRENT_VERSION);
        walletWrapperBody.setPbkdf2Iterations(iterations);
        walletWrapperBody.setPayload(encryptedPayload);
        return walletWrapperBody;
    }
}
