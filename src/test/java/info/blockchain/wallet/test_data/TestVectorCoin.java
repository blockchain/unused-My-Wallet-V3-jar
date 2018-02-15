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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class TestVectorCoin {

    @JsonProperty("coinPath")
    private String coinPath;

    @JsonProperty("coinUriScheme")
    private String coinUriScheme;

    @JsonProperty("accounts")
    private ArrayList<TestVectorAccount> accountList;

    public String getCoinPath() {
        return coinPath;
    }

    public String getCoinUriScheme() {
        return coinUriScheme;
    }

    public ArrayList<TestVectorAccount> getAccountList() {
        return accountList;
    }

    public static TestVectorCoin fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, TestVectorCoin.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
