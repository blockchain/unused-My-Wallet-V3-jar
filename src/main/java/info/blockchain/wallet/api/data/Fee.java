package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class Fee {

    @JsonProperty("fee")
    private double fee;

    @JsonProperty("surge")
    private boolean surge;

    @JsonProperty("ok")
    private boolean ok;

    public double getFee() {
        return fee;
    }

    public boolean isSurge() {
        return surge;
    }

    public boolean isOk() {
        return ok;
    }

    @JsonIgnore
    public static Fee fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, Fee.class);
    }
}
