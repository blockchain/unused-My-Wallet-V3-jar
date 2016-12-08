package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class PaymentRequest {

    long amount;
    String note;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long satoshis) {
        this.amount = satoshis;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PaymentRequest fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PaymentRequest.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
