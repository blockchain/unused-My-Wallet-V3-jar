
package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class EthAccount {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("txn_count")
    private Integer txnCount;
    @JsonProperty("account")
    private String account;
    @JsonProperty("accountType")
    private Integer accountType;
    @JsonProperty("balance")
    private String balance;
    @JsonProperty("nonce")
    private Integer nonce;
    @JsonProperty("firstTime")
    private Long firstTime;
    @JsonProperty("numNormalTxns")
    private Integer numNormalTxns;
    @JsonProperty("numInternalTxns")
    private Integer numInternalTxns;
    @JsonProperty("totalReceived")
    private String totalReceived;
    @JsonProperty("totalSent")
    private String totalSent;
    @JsonProperty("totalFee")
    private String totalFee;
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

    public String getBalance() {
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

    public String getTotalReceived() {
        return totalReceived;
    }

    public String getTotalSent() {
        return totalSent;
    }

    public String getTotalFee() {
        return totalFee;
    }

    @Nonnull
    public List<EthTransaction> getTransactions() {
        return txns;
    }

    public Integer getTransactionOffset() {
        return txnOffset;
    }

}
