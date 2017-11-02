package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class DustResponse {

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("tx_hash_big_endian")
    private String txHashBigEndian;

    @JsonProperty("tx_index")
    private long txIndex;

    @JsonProperty("tx_output_n")
    private long txOutputN;

    @JsonProperty("script")
    private String script;

    @JsonProperty("value")
    private long value;

    @JsonProperty("value_hex")
    private String valueHex;

    @JsonProperty("confirmations")
    private long confirmations;

    @JsonProperty("output_script")
    private String outputScript;

    @JsonProperty("lock_secret")
    private String lockSecret;

    public String getTxHash() {
        return txHash;
    }

    public String getTxHashBigEndian() {
        return txHashBigEndian;
    }

    public long getTxIndex() {
        return txIndex;
    }

    public long getTxOutputN() {
        return txOutputN;
    }

    public String getScript() {
        return script;
    }

    public long getValue() {
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

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
