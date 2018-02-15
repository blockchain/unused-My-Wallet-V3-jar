package info.blockchain.wallet.ethereum.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;

/**
 * We don't currently parse the transactions included in the block in this object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class EthLatestBlock {

    @JsonProperty("number")
    private long number;
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("parentHash")
    private String parentHash;
    @JsonProperty("nonce")
    private BigInteger nonce;
    @JsonProperty("sha3Uncles")
    private String sha3Uncles;
    @JsonProperty("logsBloom")
    private String logsBloom;
    @JsonProperty("transactionsRoot")
    private String transactionsRoot;
    @JsonProperty("stateRoot")
    private String stateRoot;
    @JsonProperty("receiptsRoot")
    private String receiptsRoot;
    @JsonProperty("miner")
    private String miner;
    @JsonProperty("mixHash")
    private String mixHash;
    @JsonProperty("difficulty")
    private BigInteger difficulty;
    @JsonProperty("totalDifficulty")
    private BigInteger totalDifficulty;
    @JsonProperty("extraData")
    private String extraData;
    @JsonProperty("size")
    private long size;
    @JsonProperty("gasLimit")
    private BigInteger gasLimit;
    @JsonProperty("gasUsed")
    private BigInteger gasUsed;
    @JsonProperty("timestamp")
    private long timestamp;

    public Long getBlockHeight() {
        return number;
    }

    public String getHash() {
        return hash;
    }

    public String getParentHash() {
        return parentHash;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public String getSha3Uncles() {
        return sha3Uncles;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public String getTransactionsRoot() {
        return transactionsRoot;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public String getReceiptsRoot() {
        return receiptsRoot;
    }

    public String getMiner() {
        return miner;
    }

    public String getMixHash() {
        return mixHash;
    }

    public BigInteger getDifficulty() {
        return difficulty;
    }

    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    public String getExtraData() {
        return extraData;
    }

    public Long getSize() {
        return size;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public Long getTimestamp() {
        return timestamp;
    }

}
