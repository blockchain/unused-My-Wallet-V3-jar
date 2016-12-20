package info.blockchain.wallet.contacts.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import info.blockchain.wallet.metadata.data.Invitation;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.bitcoinj.core.ECKey;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Contact {

    private String id;
    private String name;
    private String surname;
    private String company;
    private String email;
    private String xpub;
    private String note;
    private String mdid;
    private Invitation outgoingInvitation; // I invited somebody
    private Invitation incomingInvitation;// Somebody invited me

    public Contact() {
        this.id = new ECKey().getPrivateKeyAsHex();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getXpub() {
        return xpub;
    }

    public void setXpub(String xpub) {
        this.xpub = xpub;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getMdid() {
        return mdid;
    }

    public void setMdid(String mdid) {
        this.mdid = mdid;
    }

    public Invitation getOutgoingInvitation() {
        return outgoingInvitation;
    }

    public void setOutgoingInvitation(Invitation outgoingInvitation) {
        this.outgoingInvitation = outgoingInvitation;
    }

    public Invitation getIncomingInvitation() {
        return incomingInvitation;
    }

    public void setIncomingInvitation(Invitation incomingInvitation) {
        this.incomingInvitation = incomingInvitation;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public List<NameValuePair> toQueryParameters(){

        List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
        if (id != null) queryParams.add(new BasicNameValuePair("id", id));
        if (name != null) queryParams.add(new BasicNameValuePair("name", name));
        if (surname != null) queryParams.add(new BasicNameValuePair("surname", surname));
        if (company != null) queryParams.add(new BasicNameValuePair("company", company));
        if (email != null) queryParams.add(new BasicNameValuePair("email", email));
        if (note != null) queryParams.add(new BasicNameValuePair("note", note));
        if (xpub != null) queryParams.add(new BasicNameValuePair("xpub", xpub));
        if (mdid != null) queryParams.add(new BasicNameValuePair("mdid", mdid));

        return queryParams;
    }

    public Contact fromQueryParameters(Map<String, String> queryParams){

        Contact contact = new Contact();
        contact.id = queryParams.get("id");
        contact.name = queryParams.get("name");
        contact.surname = queryParams.get("surname");
        contact.company = queryParams.get("company");
        contact.email = queryParams.get("email");
        contact.note = queryParams.get("note");
        contact.xpub = queryParams.get("xpub");
        contact.mdid = queryParams.get("mdid");

        return contact;
    }

    public String createURI() throws URISyntaxException {

        List<NameValuePair> qparams = toQueryParameters();

        URIBuilder builder = new URIBuilder()
                .setScheme("http")
                .setHost("blockchain.info")
                .setPath("/invite")
                .setParameters(qparams);

        return builder.build().toString();
    }
}
