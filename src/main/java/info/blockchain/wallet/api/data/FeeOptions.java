package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeOptions {

    @JsonProperty("gasLimit")
    private long gasLimit;

    @JsonProperty("regular")
    private long regularFee;

    @JsonProperty("priority")
    private long priorityFee;

    @JsonProperty("limits")
    private FeeLimits limits;

    /**
     * Returns a "gasLimit" for Ethereum
     */
    public long getGasLimit() {
        return gasLimit;
    }

    /**
     * Returns a "regular" fee, which should result in a transaction being included in a block
     * within the next 4-6 hours. The fee is in Satoshis per byte.
     */
    public long getRegularFee() {
        return regularFee;
    }

    /**
     * Returns a "priority" fee, which should result in a transaction being included in a block in
     * an hour or so. The fee is in Satoshis per byte.
     */
    public long getPriorityFee() {
        return priorityFee;
    }

    /**
     * Returns a "priority" fee, which should result in a transaction being included in a block in
     * an hour or so. The fee is in Satoshis per byte.
     */
    public FeeLimits getLimits() {
        return limits;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setRegularFee(long regularFee) {
        this.regularFee = regularFee;
    }

    public void setPriorityFee(long priorityFee) {
        this.priorityFee = priorityFee;
    }

    public void setLimits(FeeLimits limits) {
        this.limits = limits;
    }
}
