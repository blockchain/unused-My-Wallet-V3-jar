package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class FeesListBody {

    @JsonProperty("mempool")
    private int mempool;

    @JsonProperty("default")
    private FeesBody defaultFee;

    @JsonProperty("estimate")
    private ArrayList<FeesBody> estimate;

    public int getMempool() {
        return mempool;
    }

    public FeesBody getDefaultFee() {
        return defaultFee;
    }

    public ArrayList<FeesBody> getEstimate() {
        return estimate;
    }
}
