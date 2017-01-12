package info.blockchain.wallet.contacts.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestPaymentRequest {
    long intended_amount;

    @JsonIgnore
    public RequestPaymentRequest fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, RequestPaymentRequest.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public void setIntendedAmount(long satoshis) {
        this.intended_amount = satoshis;
    }
}
