package info.blockchain.wallet.shapeshift.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class MarketInfo {

    @JsonProperty("pair")
    private String pair;

    @JsonProperty("rate")
    private Double rate;

    @JsonProperty("limit")
    private Double limit;

    @JsonProperty("minimum")
    private Double minimum;

    @JsonProperty("maxLimit")
    private Double maxLimit;

    @JsonProperty("minerFee")
    private Double minerFee;

    public String getPair() {
        return pair;
    }

    public Double getRate() {
        return rate;
    }

    public Double getLimit() {
        return limit;
    }

    public Double getMinimum() {
        return minimum;
    }

    public Double getMaxLimit() {
        return maxLimit;
    }

    public Double getMinerFee() {
        return minerFee;
    }

    public static MarketInfo fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, MarketInfo.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
