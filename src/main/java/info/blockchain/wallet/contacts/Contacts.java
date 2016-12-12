package info.blockchain.wallet.contacts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.exceptions.SharedMetadataConnectionException;
import info.blockchain.wallet.metadata.Metadata;
import info.blockchain.wallet.metadata.SharedMetadata;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.Message;
import info.blockchain.wallet.metadata.data.PublicContactDetails;
import info.blockchain.wallet.util.MetadataUtil;

import org.bitcoinj.crypto.DeterministicKey;
import org.spongycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contacts {

    private final int TYPE_PAYMENT_REQUEST = 1;
    private final int TYPE_PAYMENT_REQUEST_RESPONSE = 2;

    private final static int METADATA_TYPE_EXTERNAL = 4;
    private Metadata metadata;
    private SharedMetadata sharedMetadata;
    private List<Contact> contacts;
    private ObjectMapper mapper = new ObjectMapper();

    public Contacts(DeterministicKey masterKey) throws Exception {

        metadata = new Metadata.Builder(masterKey, METADATA_TYPE_EXTERNAL).build();
        sharedMetadata = new SharedMetadata.Builder(masterKey).build();
        contacts = new ArrayList<>();
    }

    public void fetch() throws Exception {

        String data = metadata.getMetadata();
        if(data != null) {
            contacts = mapper.readValue(data, new TypeReference<List<Contact>>(){});
        } else {
            contacts = new ArrayList<>();
        }
    }

    public void save() throws Exception {

        if(contacts != null) {
            metadata.putMetadata(mapper.writeValueAsString(contacts));
        }
    }

    public void wipe() throws Exception {
        metadata.putMetadata(mapper.writeValueAsString(new ArrayList<Contact>()));
    }

    public List<Contact> getContactList() {
        return contacts;
    }

    public void setContactList(List<Contact> contacts){
        this.contacts = contacts;
    }

    public void addContact(Contact contact){
        contacts.add(contact);
    }

    public void publishXpub() throws Exception {
        metadata.putMetadata(sharedMetadata.getAddress(), sharedMetadata.getXpub(), false);
    }

    public String fetchXpub(String mdid) throws Exception {

        String data = metadata.getMetadata(mdid, false);

        if(data != null) {
            PublicContactDetails publicXpub = new PublicContactDetails().fromJson(data);
            return publicXpub.getXpub();
        } else {
            return null;
        }
    }

    /**
     * returns a promise with the invitation and updates my contact list
     */
    public Contact createInvitation(Contact myDetails, Contact recipientDetails) throws Exception {

        //myInfoToShare could be info that will be encoded on the QR
        Invitation invitationSent = sharedMetadata.createInvitation();
        myDetails.setOutgoingInvitation(invitationSent);

        //contactInfo comes from a form that is filled before pressing invite (I am inviting James bla bla)
        addContact(recipientDetails);

        return myDetails;
    }

    public Contact readInvitationLink(String link) throws Exception{

        Map<String, String> queryParams = getQueryParams(link);

        //link will contain contact info, but not mdid
        Contact contact = new Contact().fromQueryParameters(queryParams);

        return contact;
    }

    public Contact acceptInvitationLink(String link) throws Exception{

        Map<String, String> queryParams = getQueryParams(link);

        Invitation accepted = sharedMetadata.acceptInvitation(queryParams.get("id"));

        Contact contact = new Contact().fromQueryParameters(queryParams);
        contact.setMdid(accepted.getMdid());

        sharedMetadata.addTrusted(accepted.getMdid());

        return contact;
    }

    public boolean readInvitationSent(Contact contact) throws SharedMetadataConnectionException, IOException {

        boolean accepted = false;

        String contactMdid = sharedMetadata.readInvitation(contact.getOutgoingInvitation().getId());

        if(contactMdid != null){
            //Contact accepted invite, we can update and delete invite now
            contact.setMdid(contactMdid);
            sharedMetadata.deleteInvitation(contact.getOutgoingInvitation().getId());
            accepted = true;
        }

        return accepted;
    }

    public void addTrusted(String mdid) throws SharedMetadataConnectionException, IOException {
        sharedMetadata.addTrusted(mdid);
    }

    public void deleteTrusted(String mdid) throws SharedMetadataConnectionException, IOException {
        sharedMetadata.deleteTrusted(mdid);
    }

    public void sendMessage(String mdid, String message, int type, boolean encrypted) throws Exception {

        if(encrypted) {
            String recipientXpub = fetchXpub(mdid);
            if (recipientXpub == null) throw new Exception("No public xpub for mdid.");
            byte[] encryptedMessage = MetadataUtil.encryptFor(sharedMetadata.getNode(), recipientXpub, message);
            message = new String(encryptedMessage);
        } else {
            message = new String(Base64.encode(message.getBytes("utf-8")));
        }

        sharedMetadata.postMessage(mdid, message, type);
    }

    public List<Message> getMessages(boolean onlyNew) throws Exception {
        return sharedMetadata.getMessages(onlyNew);
    }

    public Message readMessage(String messageId) throws Exception {
        return sharedMetadata.getMessage(messageId);
    }

    public void markMessageAsRead(Message message) throws Exception {
        sharedMetadata.processMessage(message.getId());// TODO: 12/12/2016 This API call hasn't been working
    }

    public Message decryptMessageFrom(Message message, String mdid) throws Exception {

        String xpub = fetchXpub(mdid);
        String payload = new String(Base64.decode(message.getPayload()));
        message.setPayload(MetadataUtil.decryptFrom(sharedMetadata.getNode(), xpub, payload));

        return message;
    }

    private Map<String, String> getQueryParams(String uri) throws UnsupportedEncodingException {

        URI a = URI.create(uri);

        Map<String, String> params = new HashMap<String, String>();

        for (String param : a.getQuery().split("&")) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = URLDecoder.decode(pair[1], "UTF-8");
            params.put(key, value);
        }

        return params;
    }

}
