package info.blockchain.wallet.api.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Unocoin {

    @JsonProperty("countries")
    private List<String> countries = new ArrayList<>();

    @JsonProperty("production")
    private boolean production;

    public List<String> getCountries() {
        return countries;
    }

    public boolean isProduction() {
        return production;
    }
}

