package info.blockchain.wallet.contacts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.contacts.data.FacilitatedTransaction;
import info.blockchain.wallet.contacts.data.PaymentBroadcasted;
import info.blockchain.wallet.contacts.data.PaymentCancelledResponse;
import info.blockchain.wallet.contacts.data.PaymentDeclinedResponse;
import info.blockchain.wallet.contacts.data.PaymentRequest;
import info.blockchain.wallet.contacts.data.PublicContactDetails;
import info.blockchain.wallet.contacts.data.RequestForPaymentRequest;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.exceptions.SharedMetadataException;
import info.blockchain.wallet.exceptions.ValidationException;
import info.blockchain.wallet.metadata.Metadata;
import info.blockchain.wallet.metadata.SharedMetadata;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.Message;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import org.bitcoinj.crypto.DeterministicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Base64;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Contacts {

    /**
     * Payment request types
     */
    private static final int TYPE_PAYMENT_REQUEST = 0;
    private static final int TYPE_PAYMENT_REQUEST_RESPONSE = 1;
    private static final int TYPE_PAYMENT_BROADCASTED = 2;
    private static final int TYPE_DECLINE_REQUEST = 3;
    private static final int TYPE_CANCEL_REQUEST = 4;

    /**
     * Metadata node type
     */
    private static final int METADATA_TYPE_EXTERNAL = 4;

    private static final Logger log = LoggerFactory.getLogger(Contacts.class);

    private Metadata metadata;
    private SharedMetadata sharedMetadata;
    private HashMap<String, Contact> contactList;
    private ObjectMapper mapper = new ObjectMapper();

    public Contacts() {
        //Empty constructor for dagger injection client side
    }

    public void init(DeterministicKey metaDataHDNode, DeterministicKey sharedMetaDataHDNode) throws
            IOException,
            MetadataException {
        log.info("Initialising Contacts");
        metadata = new Metadata.Builder(metaDataHDNode, METADATA_TYPE_EXTERNAL).build();
        sharedMetadata = new SharedMetadata.Builder(sharedMetaDataHDNode).build();
        contactList = new HashMap<>();
    }

    public Contacts(DeterministicKey metaDataHDNode, DeterministicKey sharedMetaDataHDNode) throws
            IOException,
            MetadataException {
        init(metaDataHDNode, sharedMetaDataHDNode);
    }

    /**
     * Retrieves contact list from metadata service
     */
    public void fetch() throws MetadataException, IOException, InvalidCipherTextException {
        log.info("Fetching contact list");
        String data = metadata.getMetadata();
        if (data != null) {
            contactList = mapper.readValue(data, new TypeReference<Map<String, Contact>>() {
            });
        } else {
            contactList = new HashMap<>();
        }

        log.info("Fetching contact list. Size = {}", contactList.size());
    }

    /**
     * Saves contact list to metadata service
     */
    public void save() throws IOException, MetadataException, InvalidCipherTextException {
        log.info("Saving contact list");
        if (contactList != null) {
            metadata.putMetadata(mapper.writeValueAsString(contactList));
        }
    }

    /**
     * Wipes contact list on metadata service as well as local contact list
     */
    public void wipe() throws IOException, MetadataException, InvalidCipherTextException {
        log.info("Wiping contact list");
        metadata.putMetadata(mapper.writeValueAsString(new HashMap<>()));
        contactList = new HashMap<>();
    }

    /**
     * Nulls out all Metadata nodes to allow proper reset when logging in/out.
     */
    public void destroy() {
        metadata = null;
        sharedMetadata = null;
        contactList = null;
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
    public void setContactList(List<Contact> contacts) throws
            MetadataException,
            IOException,
            InvalidCipherTextException {
        contactList.clear();

        for (Contact contact : contacts) {
            contactList.put(contact.getId(), contact);
        }

        save();
    }

    /**
     * Adds contact to contact list.
     */
    public void addContact(Contact contact) throws
            MetadataException,
            IOException,
            InvalidCipherTextException {
        log.info("Adding contact {}", contact.getId());
        contactList.put(contact.getId(), contact);
        save();
    }

    /**
     * Removes contact from contact list.
     */
    public void removeContact(Contact contact) throws
            IOException,
            SharedMetadataException,
            MetadataException,
            InvalidCipherTextException {
        log.info("Removing contact {}", contact.getId());
        contactList.remove(contact.getId());
        if (contact.getMdid() != null) {
            sharedMetadata.deleteTrusted(contact.getMdid());
        }
        save();
    }

    /**
     * Removes contact from contact list using mdid.
     */
    public void removeContact(String mdid) throws
            IOException,
            SharedMetadataException,
            MetadataException,
            InvalidCipherTextException {
        Contact contact = getContactFromMdid(mdid);

        log.info("Removing contact {}", contact.getId());
        contactList.remove(contact.getId());
        sharedMetadata.deleteTrusted(contact.getMdid());
        save();
    }

    /**
     * Renames a {@link Contact} based on their ID. Saves changes to server.
     *
     * @param contactId The Contact's ID (Note: not MDID)
     * @param name      The new name for the Contact
     */
    public void renameContact(String contactId, String name) throws
            MetadataException,
            IOException,
            InvalidCipherTextException {
        Contact contact = getContactList().get(contactId);
        if (contact != null) {
            contact.setName(name);
            save();
        } else {
            throw new NullPointerException("Contact not found");
        }
    }

    /**
     * Deletes a {@link FacilitatedTransaction} from a {@link Contact} and saves to the server.
     * You'll want to sync the contacts list after failure if an exception is propagated.
     *
     * @param mdid   A {@link Contact#getMdid()}
     * @param fctxId A {@link FacilitatedTransaction#getId()}
     */
    public void deleteFacilitatedTransaction(String mdid, String fctxId) throws
            MetadataException,
            IOException,
            InvalidCipherTextException {
        log.info("Deleting facilitated transaction {}", fctxId);
        Contact contact = getContactFromMdid(mdid);
        if (contact != null) {
            contact.deleteFacilitatedTransaction(fctxId);
            save();
        } else {
            throw new NullPointerException("Contact not found");
        }
    }

    /**
     * Publishes your mdid-xpub pair unencrypted to metadata service
     */
    public void publishXpub() throws MetadataException, IOException, InvalidCipherTextException {
        log.info("Publishing mdid-xpub pair");
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
    public String fetchXpub(String mdid) throws MetadataException, IOException,
            InvalidCipherTextException {
        log.info("Fetching mdid's xpub");
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
    public Contact createInvitation(Contact myDetails, Contact recipientDetails) throws
            IOException,
            SharedMetadataException,
            MetadataException,
            InvalidCipherTextException {
        log.info("Creating inter-wallet-comms invitation");
        myDetails.setMdid(sharedMetadata.getAddress());

        Invitation i = sharedMetadata.createInvitation();

        myDetails.setInvitationSent(i.getId());

        Invitation received = new Invitation();
        received.setId(i.getId());

        recipientDetails.setInvitationSent(i.getId());

        addContact(recipientDetails);

        return myDetails;
    }

    /**
     * Parses invitation uri to {@link Contact}
     */
    public Contact readInvitationLink(String link) throws UnsupportedEncodingException {
        log.info("Reading inter-wallet-comms invitation link");
        Map<String, String> queryParams = getQueryParams(link);
        //link will contain contact info, but not mdid
        return new Contact().fromQueryParameters(queryParams);
    }

    /**
     * Accepts invitation link and returns {@link Contact}.
     */
    public Contact acceptInvitationLink(String link) throws
        IOException,
        SharedMetadataException,
        MetadataException,
        InvalidCipherTextException,
        NoSuchElementException {
        log.info("Accepting inter-wallet-comms invitation link");
        Map<String, String> queryParams = getQueryParams(link);

        String id = queryParams.get("id");

        Invitation accepted = null;
        try {
            accepted = sharedMetadata.acceptInvitation(queryParams.get("id"));
        } catch (SharedMetadataException e) {
            //Invitation doesn't exist
            throw new NoSuchElementException("Invitation already accepted");
        }

        Contact contact = new Contact().fromQueryParameters(queryParams);
        contact.setMdid(accepted.getMdid());
        contact.setInvitationReceived(accepted.getId());

        publishXpub();
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
    public boolean readInvitationSent(Contact invite) throws
            IOException,
            SharedMetadataException,
            MetadataException,
            InvalidCipherTextException {
        boolean accepted = false;

        String recipientMdid = sharedMetadata.readInvitation(invite.getInvitationSent());

        if (recipientMdid != null) {

            Contact contact = getContactFromSentInviteId(invite.getInvitationSent());
            contact.setMdid(recipientMdid);

            sharedMetadata.addTrusted(recipientMdid);
            addContact(contact);
            contact.setXpub(fetchXpub(recipientMdid));

            //Contact accepted invite, we can update and delete invite now
            sharedMetadata.deleteInvitation(invite.getInvitationSent());

            accepted = true;

            save();
        }
        log.info("Checking if invitation has been accepted - {}",accepted);

        return accepted;
    }

    private Contact getContactFromSentInviteId(String id) {
        for (Contact contact : contactList.values()) {
            if (contact.getInvitationSent() != null && contact.getInvitationSent().equals(id)) {
                return contact;
            }
        }
        return null;
    }

    /**
     * Send message
     */
    public void sendMessage(String mdid, String message, int type, boolean encrypted) throws
            SharedMetadataException,
            IOException,
            InvalidCipherTextException {
        log.info("Sending inter-wallet-comms message");
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
    public List<Message> getMessages(boolean onlyNew) throws
            SharedMetadataException,
            ValidationException,
            SignatureException,
            IOException {
        log.info("Fetching inter-wallet-comms messages");
        List<Message> messages = sharedMetadata.getMessages(onlyNew);

        Iterator<Message> i = messages.iterator();
        while (i.hasNext()) {
            Message message = i.next();
            final Contact contactFromMdid = getContactFromMdid(message.getSender());
            if (contactFromMdid != null && contactFromMdid.getXpub() != null) {
                try {
                    decryptMessageFrom(message, contactFromMdid.getXpub());
                } catch (IOException | InvalidCipherTextException | MetadataException e) {
                    e.printStackTrace();
                }
            } else {
                //Edge case since Android will not allow contact invitation without a published xpub
                log.warn("Unable to decrypt message - Sender's xpub might not be published");
                markMessageAsRead(message.getId(), true);
                i.remove();
            }
        }

        return messages;
    }

    /**
     * Returns {@link Message}
     */
    public Message readMessage(String messageId) throws
            SharedMetadataException,
            ValidationException,
            SignatureException,
            IOException {
        return sharedMetadata.getMessage(messageId);
    }

    /**
     * Flag message as read
     */
    public void markMessageAsRead(String messageId, boolean markAsRead) throws
            IOException,
            SharedMetadataException {
        sharedMetadata.processMessage(messageId, markAsRead);
    }

    private Message decryptMessageFrom(Message message, String xpub) throws
            IOException,
            InvalidCipherTextException,
            MetadataException {
        log.info("Decrypting inter-wallet-comms message");
        String decryptedPayload = sharedMetadata.decryptFrom(xpub, message.getPayload());
        message.setPayload(decryptedPayload);
        return message;
    }

    private Map<String, String> getQueryParams(String uri) throws UnsupportedEncodingException {
        URI a = URI.create(uri);

        Map<String, String> params = new HashMap<>();

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
            if (contact.getMdid() != null && contact.getMdid().equals(mdid)) {
                return contact;
            }
        }

        return null;
    }

    /**
     * Send request for payment request. (Ask recipient to send a bitcoin receive address)
     */
    public void sendRequestForPaymentRequest(final String mdid, RequestForPaymentRequest request) throws
            IOException,
            MetadataException,
            InvalidCipherTextException,
            SharedMetadataException {

        log.info("Sending inter-wallet-comms request for payment request");
        FacilitatedTransaction tx = new FacilitatedTransaction();
        tx.setIntendedAmount(request.getIntendedAmount());
        tx.setCurrency(request.getCurrency());
        tx.setState(FacilitatedTransaction.STATE_WAITING_FOR_ADDRESS);
        tx.setRole(FacilitatedTransaction.ROLE_RPR_INITIATOR);
        tx.setNote(request.getNote());
        tx.updateCompleted();

        request.setId(tx.getId());

        sendMessage(mdid, request.toJson(), TYPE_PAYMENT_REQUEST, true);

        Contact contact = getContactFromMdid(mdid);
        contact.addFacilitatedTransaction(tx);
        save();
    }

    /**
     * Sends new payment request without need to ask for receive address.
     */
    public void sendPaymentRequest(final String mdid, PaymentRequest request) throws
            IOException,
            MetadataException,
            InvalidCipherTextException,
            SharedMetadataException {

        log.info("Sending inter-wallet-comms payment request");
        FacilitatedTransaction facilitatedTransaction = new FacilitatedTransaction();
        request.setId(facilitatedTransaction.getId());

        sendMessage(mdid, request.toJson(), TYPE_PAYMENT_REQUEST_RESPONSE, true);

        Contact contact = getContactFromMdid(mdid);
        facilitatedTransaction.setNote(request.getNote());
        facilitatedTransaction.setIntendedAmount(request.getIntendedAmount());
        facilitatedTransaction.setCurrency(request.getCurrency());
        facilitatedTransaction.setState(FacilitatedTransaction.STATE_WAITING_FOR_PAYMENT);
        facilitatedTransaction.setRole(FacilitatedTransaction.ROLE_PR_INITIATOR);
        facilitatedTransaction.updateCompleted();
        contact.addFacilitatedTransaction(facilitatedTransaction);
        save();
    }

    /**
     * Send payment request response
     */
    public void sendPaymentRequest(final String mdid, PaymentRequest request, String fTxId) throws
            IOException,
            SharedMetadataException,
            InvalidCipherTextException,
            MetadataException {

        log.info("Sending inter-wallet-comms payment request response");
        sendMessage(mdid, request.toJson(), TYPE_PAYMENT_REQUEST_RESPONSE, true);

        Contact contact = getContactFromMdid(mdid);

        FacilitatedTransaction ftx = contact.getFacilitatedTransactions().get(fTxId);
        ftx.setState(FacilitatedTransaction.STATE_WAITING_FOR_PAYMENT);
        ftx.updateCompleted();

        save();
    }

    /**
     * Sends notification that transaction has been processed.
     */
    public void sendPaymentBroadcasted(String mdid, String txHash, String fTxId) throws
            IOException,
            MetadataException,
            InvalidCipherTextException,
            SharedMetadataException {

        log.info("Sending inter-wallet-comms notification that transaction has been processed.");
        Contact contact = getContactFromMdid(mdid);
        FacilitatedTransaction ftx = contact.getFacilitatedTransactions().get(fTxId);

        sendMessage(mdid,
                new PaymentBroadcasted(fTxId, txHash).toJson(),
                TYPE_PAYMENT_BROADCASTED,
                true);

        ftx.setState(FacilitatedTransaction.STATE_PAYMENT_BROADCASTED);
        ftx.setTxHash(txHash);
        ftx.updateCompleted();

        save();
    }

    /**
     * Sends notification that the payment request has been declined.
     */
    public void sendPaymentDeclined(String mdid, String fTxId) throws
            IOException,
            SharedMetadataException,
            InvalidCipherTextException,
            MetadataException {

        log.info("Sending inter-wallet-comms notification that transaction has been declined.");
        Contact contact = getContactFromMdid(mdid);
        FacilitatedTransaction ftx = contact.getFacilitatedTransactions().get(fTxId);

        sendMessage(mdid,
                new PaymentDeclinedResponse(fTxId).toJson(),
                TYPE_DECLINE_REQUEST,
                true);

        ftx.setState(FacilitatedTransaction.STATE_DECLINED);
        ftx.updateCompleted();

        save();
    }

    /**
     * Sends notification that the payment request has been cancelled.
     */
    public void sendPaymentCancelled(String mdid, String fTxId) throws
            IOException,
            SharedMetadataException,
            InvalidCipherTextException,
            MetadataException {

        log.info("Sending inter-wallet-comms notification that transaction has been cancelled.");
        Contact contact = getContactFromMdid(mdid);
        FacilitatedTransaction ftx = contact.getFacilitatedTransactions().get(fTxId);

        sendMessage(mdid,
                new PaymentCancelledResponse(fTxId).toJson(),
                TYPE_CANCEL_REQUEST,
                true);

        ftx.setState(FacilitatedTransaction.STATE_CANCELLED);
        ftx.updateCompleted();

        save();
    }

    /**
     * Digests unread payment requests and returns a list of {@link Contact} with {@link
     * FacilitatedTransaction} that need responding to.
     */
    @Nonnull
    public List<Contact> digestUnreadPaymentRequests() throws
            SharedMetadataException,
            IOException,
            SignatureException,
            ValidationException,
            MetadataException,
            InvalidCipherTextException {
        return digestUnreadPaymentRequests(getMessages(true), true);
    }

    List<Contact> digestUnreadPaymentRequests(List<Message> messages, boolean markAsRead) throws
            IOException,
            SharedMetadataException,
            MetadataException,
            InvalidCipherTextException {
        List<Contact> unread = new ArrayList<>();

        log.info("Digesting inter-wallet-comms payment requests.");

        for (Message message : messages) {
            switch (message.getType()) {
                case TYPE_PAYMENT_REQUEST:
                    RequestForPaymentRequest rpr = new RequestForPaymentRequest()
                            .fromJson(message.getPayload());
                    FacilitatedTransaction tx = new FacilitatedTransaction();
                    tx.setId(rpr.getId());
                    tx.setIntendedAmount(rpr.getIntendedAmount());
                    tx.setCurrency(rpr.getCurrency());
                    tx.setState(FacilitatedTransaction.STATE_WAITING_FOR_ADDRESS);
                    tx.setRole(FacilitatedTransaction.ROLE_RPR_RECEIVER);
                    tx.setNote(rpr.getNote());
                    tx.updateCompleted();

                    Contact contact = getContactFromMdid(message.getSender());
                    contact.addFacilitatedTransaction(tx);
                    unread.add(contact);
                    if (markAsRead) markMessageAsRead(message.getId(), true);
                    break;
                case TYPE_PAYMENT_REQUEST_RESPONSE:
                    PaymentRequest pr = new PaymentRequest().fromJson(message.getPayload());
                    contact = getContactFromMdid(message.getSender());
                    tx = contact.getFacilitatedTransactions().get(pr.getId());

                    boolean newlyCreated = false;
                    if (tx == null) {
                        tx = new FacilitatedTransaction();
                        tx.setId(pr.getId());
                        tx.setIntendedAmount(pr.getIntendedAmount());
                        tx.setCurrency(pr.getCurrency());
                        tx.setRole(FacilitatedTransaction.ROLE_PR_RECEIVER);
                        tx.setNote(pr.getNote());
                        newlyCreated = true;
                    }

                    tx.setState(FacilitatedTransaction.STATE_WAITING_FOR_PAYMENT);
                    tx.setAddress(pr.getAddress());
                    tx.updateCompleted();

                    unread.add(contact);
                    if (markAsRead) markMessageAsRead(message.getId(), true);
                    if (newlyCreated) {
                        contact.addFacilitatedTransaction(tx);
                    }
                    break;
                case TYPE_PAYMENT_BROADCASTED:
                    PaymentBroadcasted pb = new PaymentBroadcasted().fromJson(message.getPayload());
                    contact = getContactFromMdid(message.getSender());
                    tx = contact.getFacilitatedTransactions().get(pb.getId());
                    tx.setState(FacilitatedTransaction.STATE_PAYMENT_BROADCASTED);
                    tx.setTxHash(pb.getTxHash());
                    tx.updateCompleted();

                    unread.add(contact);
                    if (markAsRead) markMessageAsRead(message.getId(), true);
                    break;
                case TYPE_DECLINE_REQUEST:
                    PaymentDeclinedResponse declined =
                            new PaymentDeclinedResponse().fromJson(message.getPayload());
                    contact = getContactFromMdid(message.getSender());
                    tx = contact.getFacilitatedTransactions().get(declined.getFctxId());
                    tx.setState(FacilitatedTransaction.STATE_DECLINED);
                    tx.updateCompleted();

                    unread.add(contact);
                    if (markAsRead) markMessageAsRead(message.getId(), true);
                    break;
                case TYPE_CANCEL_REQUEST:
                    PaymentCancelledResponse cancelled =
                            new PaymentCancelledResponse().fromJson(message.getPayload());
                    contact = getContactFromMdid(message.getSender());
                    tx = contact.getFacilitatedTransactions().get(cancelled.getFctxId());
                    tx.setState(FacilitatedTransaction.STATE_CANCELLED);
                    tx.updateCompleted();

                    unread.add(contact);
                    if (markAsRead) markMessageAsRead(message.getId(), true);
                    break;
            }
        }

        if (!messages.isEmpty()) {
            save();
        }

        return unread;
    }
}
