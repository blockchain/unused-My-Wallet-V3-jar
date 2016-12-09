package info.blockchain.wallet.contacts.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Contact {

    private String name;
    private String surname;
    private String company;
    private String email;
    private String xpub;
    private String note;
    private String mdid;

    public Contact() {
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

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public List<NameValuePair> toQueryParameters(){

        List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
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
        contact.name = queryParams.get("name");
        contact.surname = queryParams.get("surname");
        contact.company = queryParams.get("company");
        contact.email = queryParams.get("email");
        contact.note = queryParams.get("note");
        contact.xpub = queryParams.get("xpub");
        contact.mdid = queryParams.get("mdid");

        return contact;
    }
}
