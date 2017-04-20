package info.blockchain.wallet.contacts.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicContactDetails {

    private String xpub;

    public PublicContactDetails() {
        // Empty constructor
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

    @JsonIgnore
    public PublicContactDetails fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PublicContactDetails.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
