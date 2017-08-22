
package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class EthTransaction {

    @JsonProperty("blockNumber")
    private Long blockNumber;
    @JsonProperty("timeStamp")
    private Long timeStamp;
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("failFlag")
    private Boolean failFlag;
    @JsonProperty("errorDescription")
    private String errorDescription;
    @JsonProperty("nonce")
    private String nonce;
    @JsonProperty("blockHash")
    private String blockHash;
    @JsonProperty("transactionIndex")
    private Integer transactionIndex;
    @JsonProperty("from")
    private String from;
    @JsonProperty("to")
    private String to;
    @JsonProperty("value")
    private String value;
    @JsonProperty("gas")
    private Long gas;
    @JsonProperty("gasPrice")
    private Long gasPrice;
    @JsonProperty("gasUsed")
    private Long gasUsed;
    @JsonProperty("input")
    private String input;
    @JsonProperty("internalFlag")
    private Boolean internalFlag;

    public Long getBlockNumber() {
        return blockNumber;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public String getHash() {
        return hash;
    }

    public Boolean getFailFlag() {
        return failFlag;
    }

    public Object getErrorDescription() {
        return errorDescription;
    }

    public String getNonce() {
        return nonce;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public Integer getTransactionIndex() {
        return transactionIndex;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getValue() {
        return value;
    }

    public Long getGas() {
        return gas;
    }

    public Long getGasPrice() {
        return gasPrice;
    }

    public Long getGasUsed() {
        return gasUsed;
    }

    public String getInput() {
        return input;
    }

    public Boolean getInternalFlag() {
        return internalFlag;
    }

}
