package info.blockchain.wallet.metadata;

import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.Message;
import info.blockchain.wallet.metadata.data.Trusted;
import info.blockchain.wallet.payload.PayloadManager;

import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Base64;

import java.util.List;

import io.jsonwebtoken.lang.Assert;

/**
 * Integration Test
 */
public class MetadataSharedTest {

    MetadataShared senderMetadata;
    MetadataShared recipientMetadata;

    @Before
    public void setup() throws Exception {

        //Sender metadata
        PayloadManager payloadManager = PayloadManager.getInstance();
        payloadManager.createHDWallet("", "Account 1");
        DeterministicKey senderKey = payloadManager.getMasterKey();
        senderMetadata = new MetadataShared(senderKey);
        payloadManager.wipe();

        //Recipient metadata
        payloadManager = PayloadManager.getInstance();
        payloadManager.createHDWallet("", "Account 1");
        DeterministicKey recipientKey = payloadManager.getMasterKey();
        recipientMetadata = new MetadataShared(recipientKey);
        payloadManager.wipe();
    }

    private DeterministicKey getRandomECKey() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();
        payloadManager.createHDWallet("", "Account 1");
        return payloadManager.getMasterKey();
    }

    @Test
    public void testTrusted() throws Exception {

        String recipientMdid = recipientMetadata.getAddress();

        //PUT assert
        boolean result = senderMetadata.putTrusted(recipientMdid);
        Assert.isTrue(result);

        //GET assert
        boolean isTrusted = senderMetadata.getTrusted(recipientMdid);
        Assert.isTrue(isTrusted);

        Trusted list = senderMetadata.getTrustedList();
        Assert.hasText(list.getMdid());
        Assert.isTrue(list.getContacts().length > 0);

        result = senderMetadata.deleteTrusted(recipientMdid);
        Assert.isTrue(result);
    }

    @Test
    public void testMessage() throws Exception {

        String recipientMdid = recipientMetadata.getAddress();

        //Add both to each other's trust lists
        senderMetadata.putTrusted(recipientMdid);
        recipientMetadata.putTrusted(senderMetadata.getAddress());

        //Send that senderMdid a message
        String messageString = "Any fool can paint a picture, but it takes a wise person to be able to sell it.";
        Message messageId = senderMetadata.postMessage(recipientMdid, messageString, 1);

        //Get message
        Message message = recipientMetadata.getMessage(messageId.getId());
        String returnedMessage = new String(Base64.decode(message.getPayload()));
        Assert.isTrue(returnedMessage.equals(messageString));

        //Get messages
        List<Message> messages = recipientMetadata.getMessages(messageId.getId());
        returnedMessage = new String(Base64.decode(messages.get(0).getPayload()));
        Assert.isTrue(returnedMessage.equals(messageString));

        //Get unprocessed messages
        messages = recipientMetadata.getMessages(false);
        returnedMessage = new String(Base64.decode(messages.get(0).getPayload()));
        Assert.isTrue(returnedMessage.equals(messageString));
    }

    @Test
    public void testInvitation() throws Exception {

        //Sender - Create invitation
        Invitation invitation = senderMetadata.createInvitation();
        Assert.notNull(invitation.getId());
        Assert.notNull(invitation.getMdid());

        //Recipient - Accept invitation and check if sender mdid is included
        Invitation acceptedInvitation = recipientMetadata.acceptInvitation(invitation.getId());
        System.out.println(acceptedInvitation.toString());
        Assert.isTrue(invitation.getId().equals(acceptedInvitation.getId()));
        Assert.isTrue(senderMetadata.getAddress().equals(acceptedInvitation.getMdid()));

        //Sender - Check if invitation was accepted
        //If it has been accepted the recipient mdid will be included in invitation contact
        Invitation checkInvitation = senderMetadata.readInvitation(invitation.getId());
        System.out.println(checkInvitation.toString());
        Assert.isTrue(invitation.getId().equals(checkInvitation.getId()));
        Assert.isTrue(recipientMetadata.getAddress().equals(checkInvitation.getContact()));

        //delete one-time UUID
        boolean success = senderMetadata.deleteInvitation(invitation.getId());
        Assert.isTrue(success);

        //make sure one-time UUID is deleted
        Invitation invitationDel = senderMetadata.readInvitation(invitation.getId());
        Assert.isNull(invitationDel);
    }

    @Test
    public void testMe() throws Exception {

        System.out.println("--Sender--");
        //Create invite
        Invitation invitation = senderMetadata.createInvitation();
        System.out.println("Creating invite with my address " + senderMetadata.getAddress());

        //contact is recipient address (not available until accepted)
        invitation = senderMetadata.readInvitation(invitation.getId());
        Assert.isNull(invitation.getContact());
        System.out.println("Check not accepted yet");


        System.out.println("\n--Recipient--");
        //Accept one time url invite - mdid is sender address
        invitation = recipientMetadata.acceptInvitation(invitation.getId());
        System.out.println("Accepting invite from " + invitation.getMdid() + "--");
        System.out.println("attaching my address " + recipientMetadata.getAddress() + "--");
        //Add sender address to trusted list
        System.out.println("Adding to trusted list...");
        recipientMetadata.putTrusted(invitation.getMdid());


        System.out.println("\n--Sender--");
        //contact is recipient address (now available)
        invitation = senderMetadata.readInvitation(invitation.getId());
        System.out.println(invitation.getContact() + " accepted the invite");
        //Add recipient address to trusted list
        System.out.println("Adding to trusted list...");
        senderMetadata.putTrusted(invitation.getContact());

        String messageString = "Any fool can paint a picture, but it takes a wise person to be able to sell it.";
        System.out.println("Encrypting and sending message: '" + messageString + "'");
        senderMetadata.postMessage(invitation.getContact(), messageString, 1);


        System.out.println("\n--Recipient--");
        List<Message> messages = recipientMetadata.getMessages(false);
        System.out.println("Checking messages and found " + messages.size() + " new message.");
        System.out.println("It says: '" + new String(Base64.decode(messages.get(0).getPayload())) + "'");
    }
}