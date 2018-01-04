package info.blockchain.wallet.api.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sfox {

    @JsonProperty("countries")
    private List<String> countries = new ArrayList<>();
    @JsonProperty("states")
    private List<String> states = new ArrayList<>();
    @JsonProperty("inviteFormFraction")
    private Float inviteFormFraction;
    @JsonProperty("showCheckoutFraction")
    private Long showCheckoutFraction;
    @JsonProperty("apiKey")
    private String apiKey;
    @JsonProperty("plaid")
    private String plaid;
    @JsonProperty("plaidEnv")
    private String plaidEnv;
    @JsonProperty("siftScience")
    private String siftScience;

    public List<String> getCountries() {
        return countries;
    }

    public List<String> getStates() {
        return states;
    }

    public Float getInviteFormFraction() {
        return inviteFormFraction;
    }

    public Long getShowCheckoutFraction() {
        return showCheckoutFraction;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getPlaid() {
        return plaid;
    }

    public String getPlaidEnv() {
        return plaidEnv;
    }

    public String getSiftScience() {
        return siftScience;
    }

}

