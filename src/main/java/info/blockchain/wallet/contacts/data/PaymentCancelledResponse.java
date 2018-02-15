package info.blockchain.wallet.contacts.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentCancelledResponse {

    @JsonProperty("id") private String fctxId;

    public PaymentCancelledResponse() {
        // Empty constructor
    }

    public PaymentCancelledResponse(String fctxId) {
        this.fctxId = fctxId;
    }

    public String getFctxId() {
        return fctxId;
    }

    public void setFctxId(String fctxId) {
        this.fctxId = fctxId;
    }

    @JsonIgnore
    public PaymentCancelledResponse fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PaymentCancelledResponse.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

}

