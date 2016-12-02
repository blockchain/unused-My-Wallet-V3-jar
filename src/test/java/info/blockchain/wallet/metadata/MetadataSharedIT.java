package info.blockchain.wallet.metadata;

import com.google.gson.Gson;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.api.MetadataEndpoints;
import info.blockchain.api.PersistentUrls;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.PaymentRequest;
import info.blockchain.wallet.metadata.data.PaymentRequestResponse;
import info.blockchain.wallet.metadata.data.Trusted;
import info.blockchain.wallet.payload.PayloadManager;
import info.blockchain.wallet.util.CharSequenceX;

import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.jsonwebtoken.lang.Assert;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Integration Test
 */
public class MetadataSharedIT {

    private MetadataShared senderMetadata;
    private MetadataShared recipientMetadata;
    private PayloadManager recipientPayloadManager;
    private String senderToken;
    private String recipientToken;

    @Before
    public void setup() throws Exception {
        PersistentUrls.getInstance().setCurrentEnvironment(PersistentUrls.Environment.DEV);
        PersistentUrls.getInstance().setCurrentServerUrl("https://explorer.dev.blockchain.info/");

        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {
                return new Retrofit.Builder()
                        .baseUrl(MetadataEndpoints.API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return null;
            }

            @Override
            public Gson getGsonInstance() {
                return null;
            }
        });

        //Instantiate existing wallets
        //Sender metadata
        PayloadManager payloadManager = PayloadManager.getInstance();
        payloadManager.initiatePayload("bc73239b-d3d9-4bee-a1f9-80248e179486", "014fb9fc-64f9-4cf5-b76b-d927d7619717", new CharSequenceX("MyTestWallet"), new PayloadManager.InitiatePayloadListener() {
            public void onSuccess() {
                System.out.println("Sender onSuccess");
            }
        });
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        DeterministicKey senderKey = payloadManager.getMasterKey();
        System.out.println("senderKey: "+senderKey);
        senderMetadata = new MetadataShared();
        senderMetadata.setMetadataNode(senderKey);
        senderToken = senderMetadata.getToken();
//        payloadManager.registerMdid(senderMetadata.getNode());

        //Recipient metadata
        recipientPayloadManager = PayloadManager.getInstance();
        recipientPayloadManager.initiatePayload("49e58bdb-5a66-4353-923a-3b49054603d6", "6fbe154a-35e0-46fb-a22b-699dc7cba87c", new CharSequenceX("MyTestWallet"), new PayloadManager.InitiatePayloadListener() {
            public void onSuccess() {
                System.out.println("Recipient onSuccess");
            }
        });
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        DeterministicKey recipientKey = recipientPayloadManager.getMasterKey();
        recipientMetadata = new MetadataShared();
        recipientMetadata.setMetadataNode(recipientKey);
        recipientToken = recipientMetadata.getToken();
//        recipientPayloadManager.registerMdid(recipientMetadata.getNode());
    }

    @Test
    public void testTrusted() throws Exception {

        String recipientMdid = recipientMetadata.getAddress();

        //PUT assert
        boolean result = senderMetadata.putTrusted(senderToken, recipientMdid);
        Assert.isTrue(result);

        //GET assert
        boolean isTrusted = senderMetadata.getTrusted(senderToken, recipientMdid);
        Assert.isTrue(isTrusted);

        Trusted list = senderMetadata.getTrustedList(senderToken);
        Assert.hasText(list.getMdid());
        Assert.isTrue(list.getContacts().length > 0);

        result = senderMetadata.deleteTrusted(senderToken, recipientMdid);
        Assert.isTrue(result);
    }

    @Test
    public void testInvitation() throws Exception {

        //Sender - Create invitation
        Invitation invitation = senderMetadata.createInvitation(senderToken);
        Assert.notNull(invitation.getId());
        Assert.notNull(invitation.getMdid());

        //Recipient - Accept invitation and check if sender mdid is included
        Invitation acceptedInvitation = recipientMetadata.acceptInvitation(recipientToken, invitation.getId());
        System.out.println(acceptedInvitation.toString());
        Assert.isTrue(invitation.getId().equals(acceptedInvitation.getId()));
        Assert.isTrue(senderMetadata.getAddress().equals(acceptedInvitation.getMdid()));

        //Sender - Check if invitation was accepted
        //If it has been accepted the recipient mdid will be included in invitation contact
        Invitation checkInvitation = senderMetadata.readInvitation(senderToken, invitation.getId());
        System.out.println(checkInvitation.toString());
        Assert.isTrue(invitation.getId().equals(checkInvitation.getId()));
        Assert.isTrue(recipientMetadata.getAddress().equals(checkInvitation.getContact()));

        //delete one-time UUID
        boolean success = senderMetadata.deleteInvitation(senderToken, invitation.getId());
        Assert.isTrue(success);

        //make sure one-time UUID is deleted
        Invitation invitationDel = senderMetadata.readInvitation(senderToken, invitation.getId());
        Assert.isNull(invitationDel);
    }

    @Test
    public void testSendPayment() throws Exception {

        System.out.println("--Sender--");
        //Create invite
        Invitation invitation = senderMetadata.createInvitation(senderToken);
        System.out.println("Creating invite with my address " + senderMetadata.getAddress());

        //'contact' is recipient address (not available until accepted)
        invitation = senderMetadata.readInvitation(senderToken, invitation.getId());
        System.out.println("Check if accepted...");
        Assert.isNull(invitation.getContact());
        System.out.println("not yet");


        System.out.println("\n--Recipient--");
        //Accept one time url invite - 'mdid' is sender address
        invitation = recipientMetadata.acceptInvitation(recipientToken, invitation.getId());
        System.out.println("Accepting invite from " + invitation.getMdid());
        System.out.println("Attaching my address to invite" + recipientMetadata.getAddress());
        //Add sender address to trusted list
        System.out.println("Adding sender to my trusted list...");
        recipientMetadata.putTrusted(recipientToken, invitation.getMdid());


        System.out.println("\n--Sender--");
        //contact is recipient address (now available)
        invitation = senderMetadata.readInvitation(senderToken, invitation.getId());
        System.out.println("Check if accepted...");
        System.out.println(invitation.getContact() + " accepted the invite");
        //Add recipient address to trusted list
        System.out.println("Adding recipient to my trusted list...");
        senderMetadata.putTrusted(senderToken, invitation.getContact());

        //Payment request test
        System.out.println("\n--Sender--");
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setNote("I owe you £15.50 for the Honest burger.");
        paymentRequest.setAmount(2637310);

        System.out.println("Sending payment request: " +new Gson().toJson(paymentRequest));
        senderMetadata.sendPaymentRequest(senderToken, invitation.getContact(), paymentRequest);

        System.out.println("\n--Recipient--");
        List<PaymentRequest> paymentRequests = recipientMetadata.getPaymentRequests(recipientToken, true);
        String receivingAddress = recipientPayloadManager.getNextReceiveAddress(0);
        System.out.println("Checking payment requests and found " + paymentRequests.size() + " new request.");
        System.out.println("Received payment request: '" + paymentRequests.get(0).getNote() + "'");
        System.out.println("Accepting payment request and responding with address '"+receivingAddress+"'");
        recipientMetadata.acceptPaymentRequest(recipientToken, invitation.getMdid(), paymentRequests.get(0), "Send coins here please.", receivingAddress);

        System.out.println("\n--Sender--");
        List<PaymentRequestResponse> paymentRequestResponses = senderMetadata.getPaymentRequestResponses(senderToken, true);
        System.out.println("Checking payment requests responses and found " + paymentRequestResponses.size() + " new responses.");
        System.out.println("Received payment request response with address: '" + paymentRequestResponses.get(0).getAddress() + "'");

        //Use this URI for SendActivity
        System.out.println("Bitcoin URL: '" + paymentRequestResponses.get(0).toBitcoinURI() + "'");

        System.out.println("Marking payment as processed...");
    }
}