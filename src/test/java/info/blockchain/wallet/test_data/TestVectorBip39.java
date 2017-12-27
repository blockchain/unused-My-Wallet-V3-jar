package info.blockchain.wallet.test_data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class TestVectorBip39 {

    @JsonProperty("entropy")
    private String entropy;

    @JsonProperty("mnemonic")
    private String mnemonic;


    @JsonProperty("passphrase")
    private String passphrase;


    @JsonProperty("seed")
    private String seed;

    @JsonProperty("coins")
    private ArrayList<TestVectorCoin> coinList;

    public String getEntropy() {
        return entropy;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public String getSeed() {
        return seed;
    }

    public static TestVectorBip39 fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, TestVectorBip39.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public TestVectorCoin getCoinTestVectors(String path) throws Exception {

        for(TestVectorCoin coin : coinList) {
            if(coin.getCoinPath().equals(path)) {
                return coin;
            }
        }
        throw new Exception("Coin path "+path+" not found. Add test vectors for this coin to it to test_EN_BIP39.json");
    }
}
