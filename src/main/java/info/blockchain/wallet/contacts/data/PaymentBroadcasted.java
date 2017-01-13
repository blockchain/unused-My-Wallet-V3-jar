package info.blockchain.wallet.contacts.data;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentBroadcasted {

    String facilitated_tx_id;
    String tx_hash;

    public PaymentBroadcasted() {
    }

    public PaymentBroadcasted(String facilitated_tx_id, String tx_hash) {
        this.facilitated_tx_id = facilitated_tx_id;
        this.tx_hash = tx_hash;
    }

    public String getFacilitated_tx_id() {
        return facilitated_tx_id;
    }

    public String getTx_hash() {
        return tx_hash;
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