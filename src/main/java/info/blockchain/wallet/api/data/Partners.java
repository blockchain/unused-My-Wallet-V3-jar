package info.blockchain.wallet.api.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Partners {

    @JsonProperty("coinify")
    private Coinify coinify;
    @JsonProperty("sfox")
    private Sfox sfox;
    @JsonProperty("unocoin")
    private Unocoin unocoin;

    public Coinify getCoinify() {
        return coinify;
    }

    public Sfox getSfox() {
        return sfox;
    }

    public Unocoin getUnocoin() {
        return unocoin;
    }
}

