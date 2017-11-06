package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.params.AbstractBitcoinNetParams;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class DustServiceInput {

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("tx_hash_big_endian")
    private String txHashBigEndian;

    @JsonProperty("tx_index")
    private long txIndex;

    @JsonProperty("tx_output_n")
    private int txOutputCount;

    @JsonProperty("script")
    private String script;

    @JsonProperty("value")
    private BigInteger value;

    @JsonProperty("value_hex")
    private String valueHex;

    @JsonProperty("confirmations")
    private long confirmations;

    @JsonProperty("output_script")
    private String outputScript;

    @JsonProperty("lock_secret")
    private String lockSecret;

    public DustServiceInput() {
    }

    public String getTxHash() {
        return txHash;
    }

    public String getTxHashBigEndian() {
        return txHashBigEndian;
    }

    public long getTxIndex() {
        return txIndex;
    }

    public int getTxOutputCount() {
        return txOutputCount;
    }

    public String getScript() {
        return script;
    }

    public BigInteger getValue() {
        return value;
    }

    public String getValueHex() {
        return valueHex;
    }

    public long getConfirmations() {
        return confirmations;
    }

    public String getOutputScript() {
        return outputScript;
    }

    public String getLockSecret() {
        return lockSecret;
    }

    public TransactionOutPoint getTransactionOutPoint(AbstractBitcoinNetParams params) {
        return new TransactionOutPoint(
            params,
            txIndex,
            Sha256Hash.wrap(txHashBigEndian)
        );
    }

    @JsonIgnore
    public static DustServiceInput fromJson(String json) throws IOException {
        return (DustServiceInput) (new ObjectMapper()).readValue(json, DustServiceInput.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return (new ObjectMapper()).writeValueAsString(this);
    }
}
