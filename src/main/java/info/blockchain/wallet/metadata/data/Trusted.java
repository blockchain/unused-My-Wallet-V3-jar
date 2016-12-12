package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Trusted {

    String mdid;
    String[] contacts;
    String contact;

    public String getMdid() {
        return mdid;
    }

    public String[] getContacts() {
        return contacts;
    }

    public String getContact() {
        return contact;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
