package info.blockchain.wallet.contacts.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.uri.BitcoinURI;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacilitatedTransaction {

    public static final String STATE_WAITING_FOR_ADDRESS = "sender_waiting_for_receiver_to_provide_address";
    public static final String STATE_WAITING_FOR_PAYMENT = "receiver_waiting_for_sender_to_send_payment";
    public static final String STATE_PAYMENT_BROADCASTED = "payment_broadcasted";

    String id;
    String state;
    long intended_amount;
    String address;
    String txHash;

    public FacilitatedTransaction() {
        this.id = new ECKey().getPrivateKeyAsHex();
    }

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getIntendedAmount() {
        return intended_amount;
    }

    public void setIntendedAmount(long satoshis) {
        this.intended_amount = satoshis;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    @JsonIgnore
    public String toBitcoinURI() {
        return BitcoinURI.convertToBitcoinURI(address, Coin.valueOf(intended_amount), null, null);
    }

    @JsonIgnore
    public FacilitatedTransaction fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, FacilitatedTransaction.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
