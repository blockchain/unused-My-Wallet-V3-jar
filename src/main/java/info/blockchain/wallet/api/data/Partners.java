package info.blockchain.wallet.api.data;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Partners {

    @JsonProperty("coinify")
    private Coinify coinify;
    @JsonProperty("sfox")
    private Sfox sfox;

    public Coinify getCoinify() {
        return coinify;
    }

    public Sfox getSfox() {
        return sfox;
    }

}

