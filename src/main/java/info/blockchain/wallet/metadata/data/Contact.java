package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Contact {

    public String name;
    public String surname;
    public String company;
    public String email;
    public String xpub;
    public String note;
    public String mdid;

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
