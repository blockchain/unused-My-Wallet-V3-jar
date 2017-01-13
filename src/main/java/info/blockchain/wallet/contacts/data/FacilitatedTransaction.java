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

    public static final String STATE_WAITING_FOR_ADDRESS = "waiting_address";
    public static final String STATE_WAITING_FOR_PAYMENT = "waiting_payment";
    public static final String STATE_PAYMENT_BROADCASTED = "payment_broadcasted";

    public static final String ROLE_RPR_INITIATOR = "rpr_initiator";
    public static final String ROLE_RPR_RECEIVER = "rpr_receiver";
    public static final String ROLE_PR_INITIATOR = "pr_initiator";
    public static final String ROLE_PR_RECEIVER = "pr_receiver";

    private String id;
    private String state;
    private long intended_amount;
    private String address;
    private String txHash;
    private String role;

    public FacilitatedTransaction() {
        this.id = new ECKey().getPrivateKeyAsHex();
    }

    public void setId(String id) {
        this.id = id;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
