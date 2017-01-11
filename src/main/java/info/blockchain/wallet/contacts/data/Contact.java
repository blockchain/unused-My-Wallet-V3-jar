package info.blockchain.wallet.contacts.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.blockchain.wallet.metadata.data.Invitation;
import io.mikael.urlbuilder.UrlBuilder;
import io.mikael.urlbuilder.util.UrlParameterMultimap;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.bitcoinj.core.ECKey;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
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
    private List<FacilitatedTransaction> facilitatedTransaction;

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

    public List<FacilitatedTransaction> getFacilitatedTransaction() {
        return facilitatedTransaction;
    }

    @JsonIgnore
    public void addFacilitatedTransaction(FacilitatedTransaction facilitatedTransaction) {
        this.facilitatedTransaction.add(facilitatedTransaction);
    }

    public void setFacilitatedTransaction(
        List<FacilitatedTransaction> facilitatedTransaction) {
        this.facilitatedTransaction = facilitatedTransaction;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    private UrlParameterMultimap toQueryParameters() {
        UrlParameterMultimap queryParams = UrlParameterMultimap.newMultimap();
        if (id != null) queryParams.add("id", id);
        if (name != null) queryParams.add("name", name);
        if (surname != null) queryParams.add("surname", surname);
        if (company != null) queryParams.add("company", company);
        if (email != null) queryParams.add("email", email);
        if (note != null) queryParams.add("note", note);
        if (xpub != null) queryParams.add("xpub", xpub);
        if (mdid != null) queryParams.add("mdid", mdid);

        return queryParams;
    }

    public Contact fromQueryParameters(Map<String, String> queryParams) {

        Contact contact = new Contact();
        contact.id = queryParams.get("id");
        contact.name = queryParams.get("name");
        contact.surname = queryParams.get("surname");

        return contact;
    }

    public String createURI() throws URISyntaxException {

        UrlParameterMultimap urlParameterMultimap = toQueryParameters();

        UrlBuilder urlBuilder = UrlBuilder.empty()
                .withScheme("https")
                .withHost("blockchain.info")
                .withPath("/invite")
                .withParameters(urlParameterMultimap);

        return urlBuilder.toUri().toString();
    }
}
