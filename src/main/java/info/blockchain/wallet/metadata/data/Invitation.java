package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Invitation {

    String id;//one-time UUID
    String mdid;//mdid of sender
    String contact;//mdid of recipient

    Contact contactInfo;

    public String getId() {
        return id;
    }

    public String getMdid() {
        return mdid;
    }

    public String getContact() {
        return contact;
    }

    public Contact getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(Contact contactInfo) {
        this.contactInfo = contactInfo;
    }

    // TODO: 05/12/2016 This needs changing to a fully-formed URL
    public String getURI() {

        String scheme = "blockchain://invite?from=Matt&mdid_hash=123";
        return scheme;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
