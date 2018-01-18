package info.blockchain.wallet.test_data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class TestVectorAddress {

    @JsonProperty("receiveLegacy")
    private String receiveLegacy;

    @JsonProperty("changeLegacy")
    private String changeLegacy;

    @JsonProperty("receiveCashAddress")
    private String receiveCashAddress;

    @JsonProperty("changeCashAddress")
    private String changeCashAddress;

    public String getReceiveLegacy() {
        return receiveLegacy;
    }

    public String getChangeLegacy() {
        return changeLegacy;
    }

    public String getReceiveCashAddress() {
        return receiveCashAddress;
    }

    public String getChangeCashAddress() {
        return changeCashAddress;
    }

    public static TestVectorAddress fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, TestVectorAddress.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
