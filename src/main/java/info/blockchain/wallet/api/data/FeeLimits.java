package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeLimits {

    @JsonProperty("min")
    private long min;

    @JsonProperty("max")
    private long max;

    public FeeLimits(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public FeeLimits() {
    }

    /**
     * Returns a "min" fee limit, which should result in a transaction being included in the 6th block.
     * The fee is in Satoshis per byte.
     */
    public long getMin() {
        return min;
    }

    /**
     * Returns a "max" fee limit, which should result in a transaction being included in the 1st block.
     * The fee is in Satoshis per byte.
     */
    public long getMax() {
        return max;
    }

}
