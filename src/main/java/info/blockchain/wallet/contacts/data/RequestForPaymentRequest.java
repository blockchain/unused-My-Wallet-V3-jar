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
public class RequestForPaymentRequest {

    private String id;
    private long intendedAmount;
    private String note;
    private String currency;

    public RequestForPaymentRequest() {
    }

    public RequestForPaymentRequest(long amount, String note, String currency) {
        this.intendedAmount = amount;
        this.note = note;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("intended_amount")
    public long getIntendedAmount() {
        return intendedAmount;
    }

    @JsonProperty("intended_amount")
    public void setIntendedAmount(long satoshis) {
        this.intendedAmount = satoshis;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
