package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.core.Coin;
import org.bitcoinj.uri.BitcoinURI;

import java.io.IOException;

public class PaymentRequestResponse {

    long amount;
    String label = null;//unused
    String note;
    String address;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toBitcoinURI() {
        return BitcoinURI.convertToBitcoinURI(address, Coin.valueOf(amount), label, note);
    }

    public PaymentRequestResponse fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PaymentRequestResponse.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
