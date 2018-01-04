package info.blockchain.wallet.api.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coinify {

    @JsonProperty("countries")
    private List<String> countries = new ArrayList<>();
    @JsonProperty("partnerId")
    private Long partnerId;
    @JsonProperty("iSignThisDomain")
    private String iSignThisDomain;

    public List<String> getCountries() {
        return countries;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public String getISignThisDomain() {
        return iSignThisDomain;
    }

}
