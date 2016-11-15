package info.blockchain.api.metadata;

import info.blockchain.api.WalletPayload;
import info.blockchain.api.metadata.data.Message;
import info.blockchain.api.metadata.data.Share;
import info.blockchain.api.metadata.data.Trusted;
import info.blockchain.wallet.payload.Payload;
import info.blockchain.wallet.payload.PayloadManager;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Base64;

import java.security.SecureRandom;
import java.util.List;

import io.jsonwebtoken.lang.Assert;

/**
 * Integration Test
 */
public class MetadataTest {

    Metadata mds;

    ECKey senderKey;//extended public senderKey derived from master senderKey
    String senderMdid;

    ECKey recipientKey;
    String recipientMdid;

    @Before
    public void setup() throws Exception {

        mds = new Metadata();

        senderKey = getRandomECKey();
        senderMdid = senderKey.toAddress(MainNetParams.get()).toString();

        recipientKey = getRandomECKey();
        recipientMdid = recipientKey.toAddress(MainNetParams.get()).toString();
    }

    private ECKey getRandomECKey() throws Exception {

        byte[] rdata = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(rdata);
        return ECKey.fromPrivate(rdata, true);
    }

    @Test
    public void testGetNonce() throws Exception {
        String nonce = mds.getNonce();
        Assert.hasText(nonce);
    }

    @Test
    public void testGetToken() throws Exception {
        String token = mds.getToken(senderKey);
        Assert.hasText(token);
    }

    @Test
    public void testPutGetTrusted() throws Exception {

        ECKey otherKey = getRandomECKey();
        String otherMdid = otherKey.toAddress(MainNetParams.get()).toString();

        //PUT assert
        String token = mds.getToken(senderKey);
        boolean result = mds.putTrusted(token, otherMdid);
        Assert.isTrue(result);

        //GET assert
        boolean isTrusted = mds.getTrusted(token, otherMdid);
        Assert.isTrue(isTrusted);

        Trusted list = mds.getTrustedList(token);
        Assert.hasText(list.getMdid());
        Assert.isTrue(list.getContacts().length > 0);
    }

    @Test
    public void testDeleteTrusted() throws Exception {
        ECKey otherKey = getRandomECKey();
        String otherMdid = otherKey.toAddress(MainNetParams.get()).toString();

        String token = mds.getToken(senderKey);
        mds.putTrusted(token, otherMdid);

        boolean result = mds.deleteTrusted(token, otherMdid);
        Assert.isTrue(result);
    }

    @Test
    public void testPostGetMessage() throws Exception {

        String recipientToken = mds.getToken(recipientKey);
        ECKey senderKey = this.senderKey;
        String senderToken = mds.getToken(this.senderKey);
        String senderMdid = this.senderMdid;

        //Add senderMdid to trust list
        mds.putTrusted(recipientToken, senderMdid);
        boolean isTrusted = mds.getTrusted(recipientToken, senderMdid);
        Assert.isTrue(isTrusted);

        //Send that senderMdid a message
        String messageString = "Any fool can paint a picture, but it takes a wise person to be able to sell it.";
        Message messageId = mds.postMessage(senderToken, senderKey, recipientMdid, messageString, 1);

        //Get message
        Message message = mds.getMessage(recipientToken, messageId.getId());
        String returnedMessage = new String(Base64.decode(message.getPayload()));
        Assert.isTrue(returnedMessage.equals(messageString));

        //Get messages
        List<Message> messages = mds.getMessages(recipientToken, messageId.getId());
        returnedMessage = new String(Base64.decode(messages.get(0).getPayload()));
        Assert.isTrue(returnedMessage.equals(messageString));

        //Get unprocessed messages
        messages = mds.getMessages(recipientToken, false);
        returnedMessage = new String(Base64.decode(messages.get(0).getPayload()));
        Assert.isTrue(returnedMessage.equals(messageString));

    }

    @Test
    public void testPostShare() throws Exception {

        //Get one-use uuid
        String recipientToken = mds.getToken(recipientKey);
        Share share = mds.postShare(recipientToken);
        Assert.notNull(share.getId());
        Assert.notNull(share.getMdid());

        //set the MDID of the recipient
        Share toShare = mds.postToShare(recipientToken, share.getId());
        Assert.isTrue(share.getId().equals(toShare.getId()));

        //make sure MDID of the recipient was set
        Share recipientShare = mds.getShare(recipientToken, share.getId());
        Assert.isTrue(share.getId().equals(recipientShare.getId()));

        //delete one-time UUID
        boolean success = mds.deleteShare(recipientToken, share.getId());
        Assert.isTrue(success);

        //make sure one-time UUID is deleted
        Share shareDel = mds.getShare(recipientToken, share.getId());
        Assert.isNull(shareDel);
    }

    @Test
    public void testCompleteScenario() throws Exception {

        System.out.println("Creating recipient wallet...");
        PayloadManager payloadManager = PayloadManager.getInstance();
        Payload recipientWallet = payloadManager.createHDWallet("password", "Account 1");

        System.out.println("Registering recipient mdid-guid...");
        DeterministicKey recipientKey = HDKeyDerivation.createMasterPrivateKey(payloadManager.getHDSeed());
        boolean success = new WalletPayload().registerMdid(recipientKey, recipientWallet.getGuid(), recipientWallet.getSharedKey());
        Assert.isTrue(success);

        System.out.println("Authenticating recipient...");
        String recipientToken = mds.getToken(recipientKey);
        String recipientMdid = recipientKey.toAddress(MainNetParams.get()).toString();

        System.out.println("Share one time uuid...");
        Share recipientShareData = mds.postShare(recipientToken);
        String uuid = recipientShareData.getId();
        Assert.notNull(uuid);

        System.out.println("Wiping recipient payload instance");
        payloadManager.wipe();

        //
        //Share above one-time uuid over QR code with sender
        //

        System.out.println("Creating sender wallet...");
        Payload senderWallet = payloadManager.createHDWallet("password", "Account 1");

        System.out.println("Registering sender mdid-guid...");
        DeterministicKey senderKey = HDKeyDerivation.createMasterPrivateKey(payloadManager.getHDSeed());
        success = new WalletPayload().registerMdid(senderKey, senderWallet.getGuid(), senderWallet.getSharedKey());
        Assert.isTrue(success);

        System.out.println("Authenticating sender...");
        String senderToken = mds.getToken(senderKey);

        System.out.println("Getting recipient mdid and uuid from scanned QR...");
        Share senderShareData = mds.postToShare(senderToken, uuid);
        String recipientMdidFromOTUUID = senderShareData.getMdid();//is recipient mdid
        Assert.isTrue(recipientMdidFromOTUUID.equals(recipientMdid), "Mdid from share data should match original recipient mdid.");

        System.out.println("Adding recipient's mdid to sender's trusted list...");
        mds.putTrusted(senderToken, recipientMdidFromOTUUID);
        boolean isTrusted = mds.getTrusted(senderToken, recipientMdidFromOTUUID);
        Assert.isTrue(isTrusted);

        //
        // TODO: 14/11/16 How to get sender mdid to recipient?
        System.out.println("CHEATING!!! - Adding sender's mdid to recipient's trusted list. (the recipient will probably get a notification with sender's mdid.)");
        String senderMdid = senderKey.toAddress(MainNetParams.get()).toString();
        mds.putTrusted(recipientToken, senderMdid);
        //

        //Send recipient a message
        System.out.println("Sender is sending a message to recipient...");
        String messageString = "Hey fool.";
        Message messageId = mds.postMessage(senderToken, senderKey, recipientMdidFromOTUUID, messageString, 1);
        //If all goes well and notifications work - recipient should get a notification

        System.out.println("Done.");
    }
}