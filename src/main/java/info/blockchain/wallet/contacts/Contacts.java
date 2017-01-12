package info.blockchain.wallet.contacts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.contacts.data.FacilitatedTransaction;
import info.blockchain.wallet.contacts.data.RequestPaymentRequest;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.exceptions.SharedMetadataException;
import info.blockchain.wallet.exceptions.ValidationException;
import info.blockchain.wallet.metadata.Metadata;
import info.blockchain.wallet.metadata.SharedMetadata;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.Message;
import info.blockchain.wallet.contacts.data.PublicContactDetails;

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

import javax.annotation.Nonnull;

public class Contacts {

    private final int TYPE_PAYMENT_REQUEST = 1;
    private final int TYPE_PAYMENT_REQUEST_RESPONSE = 2;

    private final static int METADATA_TYPE_EXTERNAL = 4;
    private Metadata metadata;
    private SharedMetadata sharedMetadata;
    private List<Contact> contactList;
    private ObjectMapper mapper = new ObjectMapper();

    public Contacts() {
        //Empty constructor for dagger injection client side
    }

    public void init(DeterministicKey metaDataHDNode, DeterministicKey sharedMetaDataHDNode) throws IOException,
            MetadataException {
        metadata = new Metadata.Builder(metaDataHDNode, METADATA_TYPE_EXTERNAL).build();
        sharedMetadata = new SharedMetadata.Builder(sharedMetaDataHDNode).build();
        contactList = new ArrayList<>();
    }

    public Contacts(DeterministicKey metaDataHDNode, DeterministicKey sharedMetaDataHDNode) throws IOException,
            MetadataException {
        init(metaDataHDNode, sharedMetaDataHDNode);
    }

    public void fetch() throws MetadataException, IOException, InvalidCipherTextException {

        String data = metadata.getMetadata();
        if (data != null) {
            contactList = mapper.readValue(data, new TypeReference<List<Contact>>() {
            });
        } else {
            contactList = new ArrayList<>();
        }
    }

    public void save() throws IOException, MetadataException, InvalidCipherTextException {

        if (contactList != null) {
            metadata.putMetadata(mapper.writeValueAsString(contactList));
        }
    }

    public void wipe() throws IOException, MetadataException, InvalidCipherTextException {
        metadata.putMetadata(mapper.writeValueAsString(new ArrayList<Contact>()));
        contactList = new ArrayList<>();
    }

    public void invalidateToken() {
        sharedMetadata.setToken(null);
    }

    public String getMdid() {
        return sharedMetadata.getAddress();
    }

    @Nonnull
    public List<Contact> getContactList() {
        return contactList != null ? contactList : new ArrayList<Contact>();
    }

    public void setContactList(List<Contact> contacts) {
        this.contactList = contacts;
    }

    public void addContact(Contact contact) {
        contactList.add(contact);
    }

    public void publishXpub() throws MetadataException, IOException, InvalidCipherTextException {

        PublicContactDetails details = new PublicContactDetails(sharedMetadata.getXpub());

        Metadata publicMet = new Metadata();
        publicMet.setEncrypted(false);
        publicMet.setAddress(sharedMetadata.getAddress());
        publicMet.setNode(sharedMetadata.getNode());
        publicMet.setType(METADATA_TYPE_EXTERNAL);
        publicMet.fetchMagic();
        publicMet.putMetadata(details.toJson());
    }

    public String fetchXpub(String mdid) throws MetadataException, IOException, InvalidCipherTextException {

        String data = metadata.getMetadata(mdid, false);

        if (data != null) {
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

        myDetails.setMdid(sharedMetadata.getAddress());

        Invitation i = sharedMetadata.createInvitation();

        Invitation sent = new Invitation();
        sent.setId(i.getId());
        sent.setDetails(recipientDetails);
        myDetails.setInvitationSent(sent);

        Invitation received = new Invitation();
        received.setId(i.getId());
        received.setDetails(myDetails);
        myDetails.setInvitationReceived(received);

        return myDetails;
    }

    public Contact readInvitationLink(String link) throws UnsupportedEncodingException {

        Map<String, String> queryParams = getQueryParams(link);

        //link will contain contact info, but not mdid
        Contact contact = new Contact().fromQueryParameters(queryParams);

        return contact;
    }

    public Contact acceptInvitationLink(String link) throws Exception {

        Map<String, String> queryParams = getQueryParams(link);

        Invitation accepted = sharedMetadata.acceptInvitation(queryParams.get("id"));

        Contact contact = new Contact().fromQueryParameters(queryParams);
        contact.setMdid(accepted.getMdid());

        sharedMetadata.addTrusted(accepted.getMdid());

        return contact;
    }

    public boolean readInvitationSent(Contact invite) throws SharedMetadataException, IOException {

        boolean accepted = false;

        String contactMdid = sharedMetadata.readInvitation(invite.getInvitationSent().getId());

        if (contactMdid != null) {
            //Contact accepted invite, we can update and delete invite now
            sharedMetadata.deleteInvitation(invite.getInvitationSent().getId());

            Contact cc = invite.getInvitationSent().getDetails();
            cc.setMdid(contactMdid);
            addContact(cc);

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

        if (encrypted) {
            String recipientXpub = fetchXpub(mdid);
            if (recipientXpub == null)
                throw new SharedMetadataException("No public xpub for mdid.");

            b64Message = sharedMetadata.encryptFor(recipientXpub, message);
        } else {
            b64Message = new String(Base64.encode(message.getBytes("utf-8")));
        }

        sharedMetadata.postMessage(mdid, b64Message, type);
    }

    public List<Message> getMessages(boolean onlyNew)
        throws SharedMetadataException, ValidationException,
        SignatureException, IOException {

        List<Message> messages =  sharedMetadata.getMessages(onlyNew);

        for(Message message : messages) {
            try {
                decryptMessageFrom(message, message.getSender());
            } catch (IOException | InvalidCipherTextException | MetadataException e) {
                e.printStackTrace();//Unable to decrypt message - Sender's xpub might not be published
            }
        }

        return messages;
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

    public String sendRequestPaymentRequest(final String mdid, long satoshis) throws IOException,
            SharedMetadataException, InvalidCipherTextException, MetadataException {

        RequestPaymentRequest request = new RequestPaymentRequest();
        request.setIntendedAmount(satoshis);
        sendMessage(mdid, request.toJson(), TYPE_PAYMENT_REQUEST, true);

        FacilitatedTransaction tx = new FacilitatedTransaction();
        tx.setIntendedAmount(satoshis);
        tx.setState(FacilitatedTransaction.STATE_WAITING_FOR_ADDRESS);
        tx.setRole(FacilitatedTransaction.ROLE_RPR_INITIATOR);

        // TODO: 12/01/2017 - iteration not so great
        for(Contact c : contactList) {
            System.out.println(c.toJson());
            if(c.getMdid() != null && c.getMdid().equals(mdid)) {
                c.addFacilitatedTransaction(tx);
                break;
            }
        }

        return tx.getId();
    }
}
