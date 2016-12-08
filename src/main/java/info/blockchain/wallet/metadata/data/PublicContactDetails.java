package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class PublicContactDetails {

    String xpub;

    public PublicContactDetails() {
        //noop
    }

    public PublicContactDetails(String xpub) {
        this.xpub = xpub;
    }

    public String getXpub() {
        return xpub;
    }

    public void setXpub(String xpub) {
        this.xpub = xpub;
    }

    public PublicContactDetails fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PublicContactDetails.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
