package info.blockchain.wallet.contacts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.contacts.data.FacilitatedTransaction;
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

    /**
     * Retrieves contact list from metadata service
     * @throws MetadataException
     * @throws IOException
     * @throws InvalidCipherTextException
     */
    public void fetch() throws MetadataException, IOException, InvalidCipherTextException {

        String data = metadata.getMetadata();
        if (data != null) {
            contactList = mapper.readValue(data, new TypeReference<List<Contact>>() {
            });
        } else {
            contactList = new ArrayList<>();
        }
    }

    /**
     * Saves contact list to metadata service
     * @throws IOException
     * @throws MetadataException
     * @throws InvalidCipherTextException
     */
    public void save() throws IOException, MetadataException, InvalidCipherTextException {

        if (contactList != null) {
            metadata.putMetadata(mapper.writeValueAsString(contactList));
        }
    }

    /**
     * Wipes contact list on metadata service as well as local contact list
     * @throws IOException
     * @throws MetadataException
     * @throws InvalidCipherTextException
     */
    public void wipe() throws IOException, MetadataException, InvalidCipherTextException {
        metadata.putMetadata(mapper.writeValueAsString(new ArrayList<Contact>()));
        contactList = new ArrayList<>();
    }

    /**
     * Invalidates auth token
     */
    public void invalidateToken() {
        sharedMetadata.setToken(null);
    }

    /**
     * Returns your shared metadata mdid
     * @return
     */
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

    /**
     * Publishes your mdid-xpub pair unencrypted to metadata service
     * @throws MetadataException
     * @throws IOException
     * @throws InvalidCipherTextException
     */
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

    /**
     * Fetches unencrypted xpub associated with mdid
     * @param mdid
     * @return
     * @throws MetadataException
     * @throws IOException
     * @throws InvalidCipherTextException
     */
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
     * Creates an invitation {@link Contact}
     * @param myDetails
     * @param recipientDetails
     * @return
     * @throws IOException
     * @throws SharedMetadataException
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

    /**
     * Parses invitation uri to {@link Contact}
     * @param link
     * @return
     * @throws UnsupportedEncodingException
     */
    public Contact readInvitationLink(String link) throws UnsupportedEncodingException {

        Map<String, String> queryParams = getQueryParams(link);

        //link will contain contact info, but not mdid
        Contact contact = new Contact().fromQueryParameters(queryParams);

        return contact;
    }

    /**
     * Accepts invitation link and returns {@link Contact}.
     * @param link
     * @return
     * @throws Exception
     */
    public Contact acceptInvitationLink(String link) throws Exception {

        Map<String, String> queryParams = getQueryParams(link);

        Invitation accepted = sharedMetadata.acceptInvitation(queryParams.get("id"));

        Contact contact = new Contact().fromQueryParameters(queryParams);
        contact.setMdid(accepted.getMdid());

        sharedMetadata.addTrusted(accepted.getMdid());

        return contact;
    }

    /**
     * Checks if sent invitation has been accepted. If accepted, the invitee is added to contact list.
     * @param invite
     * @return
     * @throws SharedMetadataException
     * @throws IOException
     */
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

    /**
     * Send message
     * @param mdid
     * @param message
     * @param type
     * @param encrypted
     * @throws IOException
     * @throws SharedMetadataException
     * @throws InvalidCipherTextException
     * @throws MetadataException
     */
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

    /**
     * Retrieves received messages
     * @param onlyNew
     * @return
     * @throws SharedMetadataException
     * @throws ValidationException
     * @throws SignatureException
     * @throws IOException
     */
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

    /**
     * Returns {@link Message}
     * @param messageId
     * @return
     * @throws SharedMetadataException
     * @throws ValidationException
     * @throws SignatureException
     * @throws IOException
     */
    public Message readMessage(String messageId) throws SharedMetadataException, ValidationException,
            SignatureException, IOException {
        return sharedMetadata.getMessage(messageId);
    }

    /**
     * Process message
     * @param messageId
     * @param markAsRead
     * @throws IOException
     * @throws SharedMetadataException
     */
    public void markMessageAsRead(String messageId, boolean markAsRead) throws IOException, SharedMetadataException {
        sharedMetadata.processMessage(messageId, markAsRead);
    }

    private Message decryptMessageFrom(Message message, String mdid) throws IOException,
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

    /**
     * Send request for payment request.
     * @param mdid
     * @param satoshis
     * @return
     * @throws IOException
     * @throws SharedMetadataException
     * @throws InvalidCipherTextException
     * @throws MetadataException
     */
    public String sendRequestPaymentRequest(final String mdid, long satoshis) throws IOException,
            SharedMetadataException, InvalidCipherTextException, MetadataException {

        String intendedAmount = "{intended_amount: "+satoshis+" }";
        sendMessage(mdid, intendedAmount, TYPE_PAYMENT_REQUEST, true);

        FacilitatedTransaction tx = new FacilitatedTransaction();
        tx.setIntendedAmount(satoshis);
        tx.setState(FacilitatedTransaction.STATE_WAITING_FOR_ADDRESS);
        tx.setRole(FacilitatedTransaction.ROLE_RPR_INITIATOR);

        // TODO: 12/01/2017 - iteration not so great
        for(Contact c : contactList) {
            if(c.getMdid() != null && c.getMdid().equals(mdid)) {
                c.addFacilitatedTransaction(tx);
                break;
            }
        }

        return tx.getId();
    }
}
