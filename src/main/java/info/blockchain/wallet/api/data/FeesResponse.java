package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
public class FeesResponse {

    @JsonProperty("mempool")
    private int mempool;

    @JsonProperty("default")
    private FeesItem defaultFee;

    @JsonProperty("estimate")
    private ArrayList<FeesItem> estimate;

    public int getMempool() {
        return mempool;
    }

    public FeesItem getDefaultFee() {
        return defaultFee;
    }

    public ArrayList<FeesItem> getEstimate() {
        return estimate;
    }
}
