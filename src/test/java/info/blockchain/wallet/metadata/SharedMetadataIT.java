package info.blockchain.wallet.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.api.PersistentUrls;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.util.RestClient;
import info.blockchain.wallet.contacts.Contacts;
import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.PaymentRequest;
import info.blockchain.wallet.metadata.data.PaymentRequestResponse;
import info.blockchain.wallet.util.MetadataUtil;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Integration Test
 */
@Ignore
public class SharedMetadataIT {

    //dev wallets
    private String wallet_A_guid = "014fb9fc-64f9-4cf5-b76b-d927d7619717";
    private String wallet_A_sharedKey = "bc73239b-d3d9-4bee-a1f9-80248e179486";
    private String wallet_A_seedHex = "20e3939d08ddf727f34a130704cd925e";

    private String wallet_B_guid = "6fbe154a-35e0-46fb-a22b-699dc7cba87c";
    private String wallet_B_sharedKey = "49e58bdb-5a66-4353-923a-3b49054603d6";
    private String wallet_B_seedHex = "b88d0d894c19ad1d8e7f1563b7455f7c";

    @Before
    public void setup() throws Exception {

        //Set environment
        PersistentUrls.getInstance().setCurrentEnvironment(PersistentUrls.Environment.DEV);
        PersistentUrls.getInstance().setCurrentApiUrl("https://api.dev.blockchain.info/");
        PersistentUrls.getInstance().setCurrentServerUrl("https://explorer.dev.blockchain.info/");

        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {

                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                        .addInterceptor(loggingInterceptor)//Extensive logging
                        .build();

                return RestClient.getRetrofitInstance(okHttpClient);
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return null;
            }
        });
    }

    @Test
    public void testSendPayment() throws Exception {

        Pair<SharedMetadata, Wallet> pair = setupWallet(wallet_A_seedHex, wallet_A_guid, wallet_A_sharedKey);
        SharedMetadata a_shared_Metadata = pair.getLeft();
        System.out.println("Sender mdid = "+a_shared_Metadata.getAddress());
        Wallet a_wallet = pair.getRight();

        pair = setupWallet(wallet_B_seedHex, wallet_B_guid, wallet_B_sharedKey);
        SharedMetadata b_shared_Metadata = pair.getLeft();
        System.out.println("Recipient mdid = "+b_shared_Metadata.getAddress());
        Wallet b_wallet = pair.getRight();


        System.out.println("\n--Sender--");
        System.out.println("createInvitation");
        Invitation invitationSent = a_shared_Metadata.createInvitation();
        a_shared_Metadata.readInvitation(invitationSent.getId());

        //Prompt to fill in your name
        Contact recipientContact = new Contact();
        recipientContact.setName("John");
        String oneTimeUri = MetadataUtil.createURI(recipientContact, invitationSent);
        System.out.println("createURI: "+oneTimeUri);
        addToContactList(a_wallet, recipientContact);


        System.out.println("\n--Recipient--");
        Contact senderDetails = acceptAndTrustInvite(b_shared_Metadata, oneTimeUri);
        addToContactList(b_wallet, senderDetails);


        System.out.println("\n--Sender--");
        readAndTrustAcceptedInvite(a_shared_Metadata, invitationSent);
        sendPaymentRequest(a_shared_Metadata, invitationSent);


        System.out.println("\n--Recipient--");
        PaymentRequest paymentRequest = checkPaymentRequests(b_shared_Metadata);
        acceptPaymentRequest(b_shared_Metadata, paymentRequest);


        System.out.println("\n--Sender--");
        checkPaymentResponse(a_shared_Metadata);
    }

    private Pair<SharedMetadata, Wallet> setupWallet(String hex, String guid, String sharedKey) throws Exception {
        System.out.println("\n--Start Wallet "+guid+"--");
        Wallet wallet = new WalletFactory().restoreWallet(hex,"",1);
        SharedMetadata sharedMetadata =  new SharedMetadata.Builder(
                wallet.getMasterKey())
                .build();

        //DEV is down, a lot, so skip this
//        PayloadManager.getInstance().registerMdid(guid, sharedKey, sharedMetadata.getNode());

        return Pair.of(sharedMetadata, wallet);
    }

    private String createInvite(SharedMetadata sharedMetadata) throws Exception{

        //Prompt to fill in your name
        Contact contact = new Contact();
        contact.setName("John");

        Invitation invitation = sharedMetadata.createInvitation();
        String oneTimeUri = MetadataUtil.createURI(contact, invitation);
        System.out.println("createInvite: "+oneTimeUri);
        return oneTimeUri;
    }

    private Contact acceptAndTrustInvite(SharedMetadata sharedMetadata, String link) throws Exception{

        Contact inviteContactDetails = sharedMetadata.acceptInvitation(link);
        sharedMetadata.putTrusted(inviteContactDetails.getMdid());
        System.out.println("acceptAndTrustInvite: " + inviteContactDetails.toJson());
        return inviteContactDetails;
    }

    private void readAndTrustAcceptedInvite(SharedMetadata sharedMetadata, Invitation invitation) throws Exception{
        System.out.println("Check if accepted");
        Contact recipientDetails = sharedMetadata.readInvitation(invitation.getId());

        System.out.println("Fill in some details manually...");
        recipientDetails.setName("Dave");
        System.out.println(recipientDetails.toJson() + " accepted the invite");

        //Add recipient address to trusted list
        System.out.println("Adding recipient to my trusted list...");
        sharedMetadata.putTrusted(recipientDetails.getMdid());
    }

    private void sendPaymentRequest(SharedMetadata sharedMetadata, Invitation invitation) throws Exception{
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setMdid(sharedMetadata.getAddress());
        paymentRequest.setNote("I owe you £15.50 for the Honest burger.");
        paymentRequest.setAmount(2637310);

        System.out.println("Sending payment request: " + new ObjectMapper().writeValueAsString(paymentRequest));
        sharedMetadata.sendPaymentRequest(invitation.getMdid(), paymentRequest);
    }

    private PaymentRequest checkPaymentRequests(SharedMetadata sharedMetadata) throws Exception{

        boolean onlyProcessed = false;

        List<PaymentRequest> paymentRequests = sharedMetadata.getPaymentRequests(onlyProcessed);
        System.out.println("Checking payment requests and found " + paymentRequests.size() + " unprocessed requests.");

        //For test purpose let's just return the 1 we created.
        return paymentRequests.get(0);
    }

    private void acceptPaymentRequest(SharedMetadata sharedMetadata, PaymentRequest paymentRequest) throws Exception{

        // TODO: 09/12/2016 - send reserved address
        String receivingAddress = "1GYkgRtJmEp355xUtVFfHSFjFdbqjiwKmb";
        System.out.println("Received payment request: '" + paymentRequest.toJson() + "'");
        System.out.println("Accepting and attaching receive address '" + receivingAddress + "'");
        sharedMetadata.acceptPaymentRequest(paymentRequest.getMdid(), paymentRequest, "Send coins here please.", receivingAddress);
    }

    private void checkPaymentResponse(SharedMetadata sharedMetadata) throws Exception{
        List<PaymentRequestResponse> paymentRequestResponses = sharedMetadata.getPaymentRequestResponses(true);
        System.out.println("Checking payment requests responses and found " + paymentRequestResponses.size() + " new responses.");
        System.out.println("Received payment request response with address: '" + paymentRequestResponses.get(0).getAddress() + "'");

        //Use this URI for SendActivity
        System.out.println("Bitcoin URL: '" + paymentRequestResponses.get(0).toBitcoinURI() + "'");

        System.out.println("Marking payment as processed...");
    }

    private void addToContactList(Wallet wallet, Contact contact) throws Exception{
        System.out.println("Adding to contact list");

        Contacts contacts = new Contacts(wallet.getMasterKey());
        contacts.add(contact);
        contacts.save();

    }
}