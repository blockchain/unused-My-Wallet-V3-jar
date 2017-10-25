package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class ShapeShiftOptions {

    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("countriesBlacklist")
    private List<String> countriesBlacklist = new ArrayList<>();

    @JsonProperty("statesWhitelist")
    private List<String> statesWhitelist = new ArrayList<>();

    @JsonProperty("rolloutFraction")
    private double rolloutFraction;

    @JsonProperty("upperLimit")
    private int upperLimit;

    public String getApiKey() {
        return apiKey;
    }

    public List<String> getCountriesBlacklist() {
        return countriesBlacklist;
    }

    public List<String> getStatesWhitelist() {
        return statesWhitelist;
    }

    public double getRolloutFraction() {
        return rolloutFraction;
    }

    public int getUpperLimit() {
        return upperLimit;
    }
}
