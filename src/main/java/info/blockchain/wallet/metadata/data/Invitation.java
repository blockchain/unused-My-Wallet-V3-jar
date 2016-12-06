package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @Override
    public String toString() {
        return "Invitation{" +
                "id='" + id + '\'' +
                ", mdid='" + mdid + '\'' +
                ", contact='" + contact + '\'' +
                '}';
    }
}
