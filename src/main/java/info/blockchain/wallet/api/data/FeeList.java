package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class FeeList {

    @JsonProperty("mempool")
    private int mempool;

    @JsonProperty("default")
    private Fee defaultFee;

    @JsonProperty("estimate")
    private ArrayList<Fee> estimate;

    public int getMempool() {
        return mempool;
    }

    public Fee getDefaultFee() {
        return defaultFee;
    }

    public ArrayList<Fee> getEstimate() {
        return estimate;
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
