package info.blockchain.wallet.contacts.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestForPaymentRequest {

    String id;
    long intended_amount;
    String note;

    public RequestForPaymentRequest() {
    }

    public RequestForPaymentRequest(long amount, String note) {
        this.intended_amount = amount;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getIntended_amount() {
        return intended_amount;
    }

    public void setIntended_amount(long satoshis) {
        this.intended_amount = satoshis;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @JsonIgnore
    public RequestForPaymentRequest fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, RequestForPaymentRequest.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
