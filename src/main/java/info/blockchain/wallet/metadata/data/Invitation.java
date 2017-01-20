package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.blockchain.wallet.contacts.data.Contact;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Invitation {

    String id;//one-time UUID
    String mdid;//me
    String contact;//other user

    public Invitation() {
    }

    public String getId() {
        return id;
    }

    public String getMdid() {
        return mdid;
    }

    public String getContact() {
        return contact;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMdid(String mdid) {
        this.mdid = mdid;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
