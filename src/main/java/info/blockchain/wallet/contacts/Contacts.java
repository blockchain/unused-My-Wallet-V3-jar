package info.blockchain.wallet.contacts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.api.TransactionDetails;
import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.contacts.data.FacilitatedTransaction;
import info.blockchain.wallet.contacts.data.PaymentBroadcasted;
import info.blockchain.wallet.contacts.data.PaymentRequest;
import info.blockchain.wallet.contacts.data.RequestForPaymentRequest;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.exceptions.MismatchValueException;
import info.blockchain.wallet.exceptions.SharedMetadataException;
import info.blockchain.wallet.exceptions.ValidationException;
import info.blockchain.wallet.metadata.Metadata;
import info.blockchain.wallet.metadata.SharedMetadata;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.Message;
import info.blockchain.wallet.contacts.data.PublicContactDetails;

import info.blockchain.wallet.transaction.Transaction;
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

    private final int TYPE_PAYMENT_REQUEST = 0;
    private final int TYPE_PAYMENT_REQUEST_RESPONSE = 1;
    private final int TYPE_PAYMENT_BROADCASTED = 2;

    private final static int METADATA_TYPE_EXTERNAL = 4;
    private Metadata metadata;
    private SharedMetadata sharedMetadata;
    private HashMap<String, Contact> contactList;
    private ObjectMapper mapper = new ObjectMapper();

    public Contacts() {
        //Empty constructor for dagger injection client side
    }

    public void init(DeterministicKey metaDataHDNode, DeterministicKey sharedMetaDataHDNode)
        throws IOException,
        MetadataException {
        metadata = new Metadata.Builder(metaDataHDNode, METADATA_TYPE_EXTERNAL).build();
        sharedMetadata = new SharedMetadata.Builder(sharedMetaDataHDNode).build();
        contactList = new HashMap<>();
    }

    public Contacts(DeterministicKey metaDataHDNode, DeterministicKey sharedMetaDataHDNode)
        throws IOException,
        MetadataException {
        init(metaDataHDNode, sharedMetaDataHDNode);
    }

    /**
     * Retrieves contact list from metadata service
     */
    public void fetch() throws MetadataException, IOException, InvalidCipherTextException {

        String data = metadata.getMetadata();
        if (data != null) {
            ArrayList<Contact> list = mapper.readValue(data, new TypeReference<List<Contact>>() {
            });

            contactList.clear();

            for (Contact contact : list) {
                contactList.put(contact.getId(), contact);
            }

        } else {
            contactList = new HashMap<>();
        }
    }

    /**
     * Saves contact list to metadata service
     */
    public void save() throws IOException, MetadataException, InvalidCipherTextException {

        if (contactList != null) {
            metadata.putMetadata(mapper.writeValueAsString(contactList.values().toArray()));
        }
    }

    /**
     * Wipes contact list on metadata service as well as local contact list
     */
    public void wipe() throws IOException, MetadataException, InvalidCipherTextException {
        metadata.putMetadata(mapper.writeValueAsString(new ArrayList<Contact>()));
        contactList = new HashMap<>();
    }

    /**
     * Invalidates auth token
     */
    public void invalidateToken() {
        sharedMetadata.setToken(null);
    }

    /**
     * Returns your shared metadata mdid
     */
    public String getMdid() {
        return sharedMetadata.getAddress();
    }

    @Nonnull
    public HashMap<String, Contact> getContactList() {
        return contactList != null ? contactList : new HashMap<String, Contact>();
    }

    /**
     * Overwrites contact list.
     */
    public void setContactList(List<Contact> contacts)
        throws MetadataException, IOException, InvalidCipherTextException {
        for (Contact contact : contacts) {
            contactList.put(contact.getId(), contact);
        }

        save();
    }

    /**
     * Adds contact to contact list.
     */
    public void addContact(Contact contact)
        throws MetadataException, IOException, InvalidCipherTextException {
        contactList.put(contact.getMdid(), contact);
        save();
    }

    /**
     * Removes contact from contact list.
     */
    public void removeContact(Contact contact)
        throws MetadataException, IOException, InvalidCipherTextException, SharedMetadataException {

        contactList.remove(contact.getId());
        if (contact.getMdid() != null) {
            sharedMetadata.deleteTrusted(contact.getMdid());
        }
        save();
    }

    /**
     * Removes contact from contact list using mdid.
     */
    public void removeContact(String mdid)
        throws MetadataException, IOException, InvalidCipherTextException, SharedMetadataException {

        Contact contact = getContactFromMdid(mdid);

        contactList.remove(contact.getId());
        sharedMetadata.deleteTrusted(contact.getMdid());
        save();
    }

    /**
     * Publishes your mdid-xpub pair unencrypted to metadata service
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
     */
    public String fetchXpub(String mdid)
        throws MetadataException, IOException, InvalidCipherTextException {

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
     */
    public Contact createInvitation(Contact myDetails, Contact recipientDetails)
        throws IOException, SharedMetadataException, MetadataException, InvalidCipherTextException {

        myDetails.setMdid(sharedMetadata.getAddress());

        Invitation i = sharedMetadata.createInvitation();

        Invitation sent = new Invitation();
        sent.setId(i.getId());
        myDetails.setInvitationSent(sent);

        Invitation received = new Invitation();
        received.setId(i.getId());
        myDetails.setInvitationReceived(received);

        recipientDetails.setInvitationSent(sent);

        addContact(recipientDetails);

        return myDetails;
    }

    /**
     * Parses invitation uri to {@link Contact}
     */
    public Contact readInvitationLink(String link) throws UnsupportedEncodingException {

        Map<String, String> queryParams = getQueryParams(link);

        //link will contain contact info, but not mdid
        Contact contact = new Contact().fromQueryParameters(queryParams);

        return contact;
    }

    /**
     * Accepts invitation link and returns {@link Contact}.
     */
    public Contact acceptInvitationLink(String link) throws Exception {

        Map<String, String> queryParams = getQueryParams(link);

        Invitation accepted = sharedMetadata.acceptInvitation(queryParams.get("id"));

        Contact contact = new Contact().fromQueryParameters(queryParams);
        contact.setMdid(accepted.getMdid());
        contact.setInvitationReceived(accepted);

        sharedMetadata.addTrusted(accepted.getMdid());
        addContact(contact);
        contact.setXpub(fetchXpub(accepted.getMdid()));
        save();

        return contact;
    }

    /**
     * Checks if sent invitation has been accepted. If accepted, the invitee is added to contact
     * list.
     */
    public boolean readInvitationSent(Contact invite)
        throws SharedMetadataException, IOException, MetadataException, InvalidCipherTextException {

        boolean accepted = false;

        String contactMdid = sharedMetadata.readInvitation(invite.getInvitationSent().getId());

        if (contactMdid != null) {

            Contact contact = getContactFromContactId(invite.getInvitationSent().getId());
            contact.setMdid(contactMdid);

            sharedMetadata.addTrusted(contactMdid);
            addContact(contact);
            contact.setXpub(fetchXpub(contactMdid));

            //Contact accepted invite, we can update and delete invite now
            sharedMetadata.deleteInvitation(invite.getInvitationSent().getId());

            accepted = true;

            save();
        }

        return accepted;
    }

    private Contact getContactFromContactId(String id) {
        for (Contact item : contactList.values()) {
            if (item.getInvitationSent().getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Send message
     */
    public void sendMessage(String mdid, String message, int type, boolean encrypted)
        throws IOException,
        SharedMetadataException, InvalidCipherTextException, MetadataException {

        String b64Message;

        if (encrypted) {
            String recipientXpub = getContactFromMdid(mdid).getXpub();
            if (recipientXpub == null) {
                throw new SharedMetadataException("No public xpub for mdid.");
            }

            b64Message = sharedMetadata.encryptFor(recipientXpub, message);
        } else {
            b64Message = new String(Base64.encode(message.getBytes("utf-8")));
        }

        sharedMetadata.postMessage(mdid, b64Message, type);
    }

    /**
     * Retrieves received messages
     */
    public List<Message> getMessages(boolean onlyNew)
        throws SharedMetadataException, ValidationException,
        SignatureException, IOException {

        List<Message> messages = sharedMetadata.getMessages(onlyNew);

        for (Message message : messages) {
            try {
                decryptMessageFrom(message, getContactFromMdid(message.getSender()).getXpub());
            } catch (IOException | InvalidCipherTextException | MetadataException e) {
                e.printStackTrace();//Unable to decrypt message - Sender's xpub might not be published
            }
        }

        return messages;
    }

    /**
     * Returns {@link Message}
     */
    public Message readMessage(String messageId)
        throws SharedMetadataException, ValidationException,
        SignatureException, IOException {
        return sharedMetadata.getMessage(messageId);
    }

    /**
     * Flag message as read
     */
    public void markMessageAsRead(String messageId, boolean markAsRead)
        throws IOException, SharedMetadataException {
        sharedMetadata.processMessage(messageId, markAsRead);
    }

    private Message decryptMessageFrom(Message message, String xpub) throws IOException,
        InvalidCipherTextException, MetadataException {

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

    private Contact getContactFromMdid(String mdid) {

        for (Contact contact : contactList.values()) {
            if (contact.getMdid().equals(mdid)) {
                return contact;
            }
        }

        return null;
    }

    /**
     * Send request for payment request. (Ask recipient to send a bitcoin receive address)
     */
    public void sendRequestForPaymentRequest(final String mdid, RequestForPaymentRequest request)
        throws IOException,
        SharedMetadataException, InvalidCipherTextException, MetadataException {

        FacilitatedTransaction tx = new FacilitatedTransaction();
        tx.setIntended_amount(request.getIntended_amount());
        tx.setState(FacilitatedTransaction.STATE_WAITING_FOR_ADDRESS);
        tx.setRole(FacilitatedTransaction.ROLE_RPR_INITIATOR);

        request.setId(tx.getId());

        sendMessage(mdid, request.toJson(), TYPE_PAYMENT_REQUEST, true);

        Contact contact = getContactFromMdid(mdid);
        contact.addFacilitatedTransaction(tx);
        save();
    }

    /**
     * Sends new payment request without need to ask for receive address.
     */
    public void sendPaymentRequest(final String mdid, PaymentRequest request,
        FacilitatedTransaction ftx) throws IOException,
        SharedMetadataException, InvalidCipherTextException, MetadataException {

        sendMessage(mdid, request.toJson(), TYPE_PAYMENT_REQUEST_RESPONSE, true);

        Contact contact = getContactFromMdid(mdid);
        contact.addFacilitatedTransaction(ftx);
        ftx.setState(FacilitatedTransaction.STATE_WAITING_FOR_PAYMENT);
        save();
    }

    /**
     * Send payment request response
     */
    public void sendPaymentRequest(final String mdid, PaymentRequest request, String fTxId)
        throws IOException,
        SharedMetadataException, InvalidCipherTextException, MetadataException {

        sendMessage(mdid, request.toJson(), TYPE_PAYMENT_REQUEST_RESPONSE, true);

        Contact contact = getContactFromMdid(mdid);

        FacilitatedTransaction ftx = contact.getFacilitatedTransaction().get(fTxId);
        ftx.setState(FacilitatedTransaction.STATE_WAITING_FOR_PAYMENT);

        save();
    }

    /**
     * Sends notification that transaction has been processed.
     */
    public void sendPaymentBroadcasted(final String mdid, final String txHash, final String fTxId)
        throws IOException,
        SharedMetadataException, InvalidCipherTextException, MetadataException, MismatchValueException {

        Contact contact = getContactFromMdid(mdid);
        FacilitatedTransaction ftx = contact.getFacilitatedTransaction().get(fTxId);

        if (!assertTransactionValue(ftx, txHash)) {
            throw new MismatchValueException(
                "Transaction value does not match intended payment value.");
        }

        sendMessage(mdid, new PaymentBroadcasted(fTxId, txHash).toJson(),
            TYPE_PAYMENT_BROADCASTED,
            true);

        ftx.setState(FacilitatedTransaction.STATE_PAYMENT_BROADCASTED);
        ftx.setTx_hash(txHash);

        save();
    }

    private boolean assertTransactionValue(FacilitatedTransaction ftx, String txHash)
        throws IOException {

        long result = 0;
        try {
            Transaction tx = new TransactionDetails()
                .getTransactionDetails(txHash);
            if (tx == null) {
                return false;
            } else {
                result = tx.getResult();
            }
        } catch (Exception e) {
            throw new IOException(
                e);// TODO: 13/01/2017 TransactionDetails should throw better exception
        }

        return ftx.getIntended_amount() == result;
    }

    /**
     * Digests unread payment requests and returns a list of {@link Contact} with {@link
     * FacilitatedTransaction} that need responding to.
     */
    public List<Contact> digestUnreadPaymentRequests()
        throws SharedMetadataException, IOException, SignatureException, ValidationException, MetadataException, InvalidCipherTextException {
        List<Message> messages = getMessages(true);

        List<Contact> unread = new ArrayList<>();

        for (Message message : messages) {

            switch (message.getType()) {
                case TYPE_PAYMENT_REQUEST:

                    RequestForPaymentRequest rpr = new RequestForPaymentRequest()
                        .fromJson(message.getPayload());

                    FacilitatedTransaction tx = new FacilitatedTransaction();
                    tx.setId(rpr.getId());
                    tx.setIntended_amount(rpr.getIntended_amount());
                    tx.setState(FacilitatedTransaction.STATE_WAITING_FOR_ADDRESS);
                    tx.setRole(FacilitatedTransaction.ROLE_RPR_RECEIVER);

                    Contact contact = getContactFromMdid(message.getSender());
                    contact.addFacilitatedTransaction(tx);
                    unread.add(contact);
                    markMessageAsRead(message.getId(), true);
                    save();
                    break;

                case TYPE_PAYMENT_REQUEST_RESPONSE:

                    PaymentRequest pr = new PaymentRequest().fromJson(message.getPayload());

                    contact = getContactFromMdid(message.getSender());
                    tx = contact.getFacilitatedTransaction()
                        .get(pr.getId());

                    tx.setState(FacilitatedTransaction.STATE_WAITING_FOR_PAYMENT);
                    tx.setAddress(pr.getAddress());

                    unread.add(contact);
                    markMessageAsRead(message.getId(), true);
                    save();
                    break;

                case TYPE_PAYMENT_BROADCASTED:

                    PaymentBroadcasted pb = new PaymentBroadcasted().fromJson(message.getPayload());

                    contact = getContactFromMdid(message.getSender());

                    tx = contact.getFacilitatedTransaction().get(pb.getId());

                    tx.setState(FacilitatedTransaction.STATE_PAYMENT_BROADCASTED);
                    tx.setTx_hash(pb.getTx_hash());

                    unread.add(contact);
                    markMessageAsRead(message.getId(), true);
                    save();
                    break;
            }
        }

        return unread;
    }
}
