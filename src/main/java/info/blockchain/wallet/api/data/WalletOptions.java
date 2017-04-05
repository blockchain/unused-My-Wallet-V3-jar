package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletOptions {

    @JsonProperty("showBuySellTab")
    private List<String> buySellCountries = new ArrayList<>();
    @JsonProperty("partners")
    private Partners partners;

    public List<String> getBuySellCountries() {
        return buySellCountries;
    }

    public Partners getPartners() {
        return partners;
    }

}
