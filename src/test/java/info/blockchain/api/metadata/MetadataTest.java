package info.blockchain.api.metadata;

import info.blockchain.wallet.metadata.Metadata;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.Message;
import info.blockchain.wallet.metadata.data.Trusted;
import info.blockchain.wallet.payload.PayloadManager;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Base64;

import java.util.List;

import io.jsonwebtoken.lang.Assert;

/**
 * Integration Test
 */
public class MetadataTest {

    Metadata senderMetadata;
    Metadata recipientMetadata;

    @Before
    public void setup() throws Exception {

        //Sender metadata
        PayloadManager payloadManager = PayloadManager.getInstance();
        payloadManager.createHDWallet("", "Account 1");
        DeterministicKey senderKey = payloadManager.getMasterKey();
        senderMetadata = new Metadata(senderKey);
        payloadManager.wipe();

        //Recipient metadata
        payloadManager = PayloadManager.getInstance();
        payloadManager.createHDWallet("", "Account 1");
        DeterministicKey recipientKey = payloadManager.getMasterKey();
        recipientMetadata = new Metadata(recipientKey);
        payloadManager.wipe();
    }

    private DeterministicKey getRandomECKey() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();
        payloadManager.createHDWallet("", "Account 1");
        return payloadManager.getMasterKey();
    }

    @Test
    public void testMetadata() throws Exception{

//        String web_mnemonic = "bicycle balcony prefer kid flower pole goose crouch century lady worry flavor";
//        PayloadManager payloadManager = PayloadManager.getInstance();
//        payloadManager.restoreHDWallet("", web_mnemonic, "Account 1");
//        DeterministicKey key = payloadManager.getMasterKey();

        DeterministicKey key = getRandomECKey();

        String message = "{hello: 'world'}";

        Metadata metadata5 = new Metadata(key, 5);
        metadata5.putMetadata(message);

        String result1 = metadata5.getMetadata();
        Assert.isTrue(message.equals(result1));

        message = "{hello: 'mars'}";
        metadata5.putMetadata(message);

        String result2 = metadata5.getMetadata();
        Assert.isTrue(message.equals(result2));

        metadata5.deleteMetadata(message);

        try {
            metadata5.getMetadata();
            Assert.isTrue(false);
        }catch (Exception e){
            Assert.isTrue(true);
        }
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
    public void testGetMetadataNode() throws Exception {

        String web_mnemonic = "bicycle balcony prefer kid flower pole goose crouch century lady worry flavor";
        String web_seedHex = "15e23aa73d25994f1921a1256f93f72c";
        String web_address = "12sC9tqHzAhdoukhCbTnyx2MjYXNXBGHnF";

        PayloadManager payloadManager = PayloadManager.getInstance();
        payloadManager.restoreHDWallet("", web_mnemonic, "Account 1");

        //Ensure web_wallet and this restore wallet is same
        Assert.isTrue(web_seedHex.equals(payloadManager.getHDSeedHex()));
        String web_priv = "xprv9s21ZrQH143K2qnxcoP1RnRkxYvHT5ZDamV4B4UYTmAuANBnyWwVP7e3GYmEkt1chPWq264tiUxo21FiRKx3kVTpHLkkP65NRzHSAjS8nHA";
        Assert.isTrue(web_priv.equals(payloadManager.getMasterKey().serializePrivB58(MainNetParams.get())));

        senderMetadata = new Metadata(payloadManager.getMasterKey(), 2);
        Assert.isTrue(senderMetadata.getAddress().equals(web_address));
        payloadManager.wipe();
    }
}