package info.blockchain.wallet.contacts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.exceptions.SharedMetadataException;
import info.blockchain.wallet.exceptions.ValidationException;
import info.blockchain.wallet.metadata.Metadata;
import info.blockchain.wallet.metadata.SharedMetadata;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.Message;
import info.blockchain.wallet.metadata.data.PaymentRequest;
import info.blockchain.wallet.metadata.data.PaymentRequestResponse;
import info.blockchain.wallet.metadata.data.PublicContactDetails;

import org.bitcoinj.crypto.DeterministicKey;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.security.SignatureException;
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
    private Metadata publicContactDetailsMetadata;
    private List<Contact> contacts;
    private ObjectMapper mapper = new ObjectMapper();

    public Contacts() {
        //Empty constructor for dagger injection client side
    }

    public void init(DeterministicKey metaDataHDNode, DeterministicKey sharedMetaDataHDNode) throws IOException,
            MetadataException {
        metadata = new Metadata.Builder(metaDataHDNode, METADATA_TYPE_EXTERNAL).build();
        sharedMetadata = new SharedMetadata.Builder(sharedMetaDataHDNode).build();
        publicContactDetailsMetadata = new Metadata.Builder(sharedMetaDataHDNode, METADATA_TYPE_EXTERNAL)
                .setEncrypted(false)
                .build();
        contacts = new ArrayList<>();
    }

    public Contacts(DeterministicKey metaDataHDNode, DeterministicKey sharedMetaDataHDNode) throws IOException,
            MetadataException {
        init(metaDataHDNode, sharedMetaDataHDNode);
    }

    public void fetch() throws MetadataException, IOException, InvalidCipherTextException {

        String data = metadata.getMetadata();
        if(data != null) {
            contacts = mapper.readValue(data, new TypeReference<List<Contact>>(){});
        } else {
            contacts = new ArrayList<>();
        }
    }

    public void save() throws IOException, MetadataException, InvalidCipherTextException {

        if(contacts != null) {
            metadata.putMetadata(mapper.writeValueAsString(contacts));
        }
    }

    public void wipe() throws IOException, MetadataException, InvalidCipherTextException {
        metadata.putMetadata(mapper.writeValueAsString(new ArrayList<Contact>()));
        contacts = new ArrayList<>();
    }

    public void invalidateToken() {
        sharedMetadata.setToken(null);
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

    public void publishXpub() throws MetadataException, IOException, InvalidCipherTextException {

        PublicContactDetails details = new PublicContactDetails(sharedMetadata.getXpub());
        publicContactDetailsMetadata.putMetadata(details.toJson());
    }

    public String fetchXpub(String mdid) throws MetadataException, IOException, InvalidCipherTextException {

        String data = metadata.getMetadata(mdid, false);

        if(data != null) {
            PublicContactDetails publicXpub = new PublicContactDetails().fromJson(data);
            return publicXpub.getXpub();
        } else {
            throw new MetadataException("Xpub not found");
        }
    }

    /**
     * returns a promise with the invitation and updates my contact list
     */
    public Contact createInvitation(Contact myDetails, Contact recipientDetails) throws IOException, SharedMetadataException {

        //myInfoToShare could be info that will be encoded on the QR
        Invitation invitationSent = sharedMetadata.createInvitation();
        myDetails.setOutgoingInvitation(invitationSent);

        //contactInfo comes from a form that is filled before pressing invite (I am inviting James bla bla)
        addContact(recipientDetails);

        return myDetails;
    }

    public Contact readInvitationLink(String link) throws UnsupportedEncodingException {

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

    public boolean readInvitationSent(Contact contact) throws SharedMetadataException, IOException {

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

    public boolean addTrusted(String mdid) throws SharedMetadataException, IOException {
        return sharedMetadata.addTrusted(mdid);
    }

    public boolean deleteTrusted(String mdid) throws SharedMetadataException, IOException {
        return sharedMetadata.deleteTrusted(mdid);
    }

    public void sendMessage(String mdid, String message, int type, boolean encrypted) throws IOException,
            SharedMetadataException, InvalidCipherTextException, MetadataException {

        String b64Message;

        if(encrypted) {
            String recipientXpub = fetchXpub(mdid);
            if (recipientXpub == null) throw new SharedMetadataException("No public xpub for mdid.");

            b64Message = sharedMetadata.encryptFor(recipientXpub, message);
        } else {
            b64Message = new String(Base64.encode(message.getBytes("utf-8")));
        }

        sharedMetadata.postMessage(mdid, b64Message, type);
    }

    public List<Message> getMessages(boolean onlyNew) throws SharedMetadataException, ValidationException,
            SignatureException, IOException {
        return sharedMetadata.getMessages(onlyNew);
    }

    public Message readMessage(String messageId) throws SharedMetadataException, ValidationException,
            SignatureException, IOException {
        return sharedMetadata.getMessage(messageId);
    }

    public void markMessageAsRead(String messageId, boolean markAsRead) throws IOException, SharedMetadataException {
        sharedMetadata.processMessage(messageId, markAsRead);
    }

    public Message decryptMessageFrom(Message message, String mdid) throws IOException,
            InvalidCipherTextException, MetadataException {

        String xpub = fetchXpub(mdid);
        String decryptedPayload = sharedMetadata.decryptFrom(xpub, message.getPayload());
        message.setPayload(decryptedPayload);
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

    public void sendPaymentRequest(String mdid, PaymentRequest paymentRequest) throws IOException,
            SharedMetadataException, InvalidCipherTextException, MetadataException {
        sendMessage(mdid, paymentRequest.toJson(), TYPE_PAYMENT_REQUEST, true);
    }

    public List<PaymentRequest> getPaymentRequests() throws SharedMetadataException,
            IOException, SignatureException, ValidationException {

        List<PaymentRequest> result = new ArrayList<>();

        List<Message> messages = getMessages(true);

        for(Message message : messages) {
            if(message.getType() == TYPE_PAYMENT_REQUEST){
                result.add(new PaymentRequest().fromJson(message.getPayload()));
            }
        }

        return result;
    }

    public List<PaymentRequestResponse> getPaymentRequestResponses(boolean onlyNew) throws
            SharedMetadataException, IOException, SignatureException, ValidationException {

        List<PaymentRequestResponse> responses = new ArrayList<>();

        List<Message> messages = getMessages(onlyNew);

        for (Message message : messages) {

            if (message.getType() == TYPE_PAYMENT_REQUEST_RESPONSE) {
                responses.add(new PaymentRequestResponse().fromJson(message.getPayload()));
            }
        }

        return responses;
    }

    public Message acceptPaymentRequest(String mdid, PaymentRequest paymentRequest,
                                        String note, String receiveAddress) throws IOException,
            SharedMetadataException {

        PaymentRequestResponse response = new PaymentRequestResponse();
        response.setAmount(paymentRequest.getAmount());
        response.setNote(note);
        response.setAddress(receiveAddress);

        return sharedMetadata.postMessage(mdid, response.toJson(), TYPE_PAYMENT_REQUEST_RESPONSE);
    }
}
