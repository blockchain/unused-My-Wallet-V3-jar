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
public class PaymentBroadcasted {

    private String id;
    private String txHash;

    public PaymentBroadcasted() {
        // Empty constructor
    }

    public PaymentBroadcasted(String id, String txHash) {
        this.id = id;
        this.txHash = txHash;
    }

    public String getId() {
        return id;
    }

    @JsonProperty("tx_hash")
    public String getTxHash() {
        return txHash;
    }

    @JsonProperty("tx_hash")
    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    @JsonIgnore
    public PaymentBroadcasted fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PaymentBroadcasted.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}