package info.blockchain.wallet.ethereum.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class EthTxDetails {

    @JsonProperty("hash")
    private String hash;
    @JsonProperty("nonce")
    private Long nonce;
    @JsonProperty("blockHash")
    private String blockHash;
    @JsonProperty("blockNumber")
    private Long blockNumber;
    @JsonProperty("transactionIndex")
    private Long transactionIndex;
    @JsonProperty("from")
    private String from;
    @JsonProperty("to")
    private String to;
    @JsonProperty("value")
    private BigInteger value;
    @JsonProperty("gasPrice")
    private BigInteger gasPrice;
    @JsonProperty("gas")
    private BigInteger gas;
    @JsonProperty("input")
    private String input;
    @JsonProperty("creates")
    private String creates;
    @JsonProperty("publicKey")
    private String publicKey;
    @JsonProperty("raw")
    private String raw;
    @JsonProperty("r")
    private String r;
    @JsonProperty("s")
    private String s;
    @JsonProperty("v")
    private Long v;
    @JsonProperty("nonceRaw")
    private String nonceRaw;
    @JsonProperty("blockNumberRaw")
    private String blockNumberRaw;
    @JsonProperty("transactionIndexRaw")
    private String transactionIndexRaw;
    @JsonProperty("valueRaw")
    private String valueRaw;
    @JsonProperty("gasPriceRaw")
    private String gasPriceRaw;
    @JsonProperty("gasRaw")
    private String gasRaw;

    public String getHash() {
        return hash;
    }

    public Long getNonce() {
        return nonce;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public Long getTransactionIndex() {
        return transactionIndex;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public BigInteger getGas() {
        return gas;
    }

    public String getInput() {
        return input;
    }

    public String getCreates() {
        return creates;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getRaw() {
        return raw;
    }

    public String getR() {
        return r;
    }

    public String getS() {
        return s;
    }

    public Long getV() {
        return v;
    }

    public String getNonceRaw() {
        return nonceRaw;
    }

    public String getBlockNumberRaw() {
        return blockNumberRaw;
    }

    public String getTransactionIndexRaw() {
        return transactionIndexRaw;
    }

    public String getValueRaw() {
        return valueRaw;
    }

    public String getGasPriceRaw() {
        return gasPriceRaw;
    }

    public String getGasRaw() {
        return gasRaw;
    }

}