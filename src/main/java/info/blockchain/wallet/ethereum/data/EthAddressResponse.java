
package info.blockchain.wallet.ethereum.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class EthAddressResponse {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("txn_count")
    private Integer txnCount;
    @JsonProperty("account")
    private String account;
    @JsonProperty("accountType")
    private Integer accountType;
    @JsonProperty("balance")
    private BigInteger balance;
    @JsonProperty("nonce")
    private Integer nonce;
    @JsonProperty("firstTime")
    private Long firstTime;
    @JsonProperty("numNormalTxns")
    private Integer numNormalTxns;
    @JsonProperty("numInternalTxns")
    private Integer numInternalTxns;
    @JsonProperty("totalReceived")
    private BigInteger totalReceived;
    @JsonProperty("totalSent")
    private BigInteger totalSent;
    @JsonProperty("totalFee")
    private BigInteger totalFee;
    @JsonProperty("txns")
    private List<EthTransaction> txns = new ArrayList<>();
    @JsonProperty("txnOffset")
    private Integer txnOffset;

    public Integer getId() {
        return id;
    }

    public Integer getTransactionCount() {
        return txnCount;
    }

    public String getAccount() {
        return account;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public Integer getNonce() {
        return nonce;
    }

    public Long getFirstTime() {
        return firstTime;
    }

    public Integer getNumberOfNormalTransactions() {
        return numNormalTxns;
    }

    public Integer getNumberOfInternalTransactions() {
        return numInternalTxns;
    }

    public BigInteger getTotalReceived() {
        return totalReceived;
    }

    public BigInteger getTotalSent() {
        return totalSent;
    }

    public BigInteger getTotalFee() {
        return totalFee;
    }

    @Nonnull
    public List<EthTransaction> getTransactions() {
        return txns;
    }

    public Integer getTransactionOffset() {
        return txnOffset;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
