package info.blockchain.wallet.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.api.MetadataEndpoints;
import info.blockchain.api.PersistentUrls;
import info.blockchain.api.WalletEndpoints;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.PaymentRequest;
import info.blockchain.wallet.metadata.data.PaymentRequestResponse;
import info.blockchain.wallet.metadata.data.Trusted;

import org.bitcoinj.core.ECKey;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

import io.jsonwebtoken.lang.Assert;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Integration Test
 */
public class MetadataSharedIT {

    //dev wallets
    //riaanjvos@hotmail.com
    String wallet_A_guid = "014fb9fc-64f9-4cf5-b76b-d927d7619717";
    String wallet_A_sharedKey = "bc73239b-d3d9-4bee-a1f9-80248e179486";
    String wallet_A_seedHex = "20e3939d08ddf727f34a130704cd925e";
    Wallet a_wallet;
    MetadataShared a_Metadata;

    //riaanjvos@gmail.com (verified)
    String wallet_B_guid = "6fbe154a-35e0-46fb-a22b-699dc7cba87c";
    String wallet_B_sharedKey = "49e58bdb-5a66-4353-923a-3b49054603d6";
    String wallet_B_seedHex = "b88d0d894c19ad1d8e7f1563b7455f7c";
    Wallet b_wallet;
    MetadataShared b_Metadata;

    @Before
    public void setup() throws Exception {

        //Set environment
        PersistentUrls.getInstance().setCurrentEnvironment(PersistentUrls.Environment.DEV);
        PersistentUrls.getInstance().setWalletPayloadUrl("https://explorer.dev.blockchain.info/wallet");

        //Logging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        //Http client
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(loggingInterceptor)//Extensive logging
                .build();
        MetadataEndpoints httpClient = RestClient.getClient(okHttpClient);

        //Init wallets
        a_wallet = new WalletFactory().restoreWallet(wallet_A_seedHex,"",1);
        a_Metadata = new MetadataShared(httpClient, a_wallet.getMasterKey());

        System.out.println("--------------Register mdid-----------------");
//        registerMdid(a_Metadata.getNode(), wallet_A_guid, wallet_A_sharedKey);
        System.out.println("mdid - "+a_Metadata.getAddress());

        b_wallet = new WalletFactory().restoreWallet(wallet_B_seedHex,"",1);
        b_Metadata = new MetadataShared(httpClient, b_wallet.getMasterKey());

        System.out.println("--------------Register mdid-----------------");
//        registerMdid(b_Metadata.getNode(), wallet_B_guid, wallet_B_sharedKey);
        System.out.println("mdid should be - "+b_Metadata.getAddress());
        System.out.println("--------------------------------------------");
    }

    private void registerMdid(ECKey key, String guid, String sharedKey) throws Exception {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WalletEndpoints.API_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        WalletEndpoints api = retrofit.create(WalletEndpoints.class);

        System.out.println("key hex: "+Hex.toHexString(key.getPrivKeyBytes()));

        String signedGuid = key.signMessage(guid);

        System.out.println(signedGuid);

        Call<Void> call = api.postMdidRegistration("register-mdid",
                guid,
                sharedKey,
                signedGuid,
                signedGuid.length());

        Response<Void> result = call.execute();

        if(!result.isSuccessful())
            throw new Exception(result.code()+" "+result.message());

        System.out.println("GUID: "+guid);
    }

    @Test
    public void testTrusted() throws Exception {

        String recipientMdid = b_Metadata.getAddress();

        //PUT assert
        boolean result = a_Metadata.putTrusted(recipientMdid);
        Assert.isTrue(result);

        //GET assert
        boolean isTrusted = a_Metadata.getTrusted(recipientMdid);
        Assert.isTrue(isTrusted);

        Trusted list = a_Metadata.getTrustedList();
        Assert.hasText(list.getMdid());
        Assert.isTrue(list.getContacts().length > 0);

        result = a_Metadata.deleteTrusted(recipientMdid);
        Assert.isTrue(result);
    }

    @Test
    public void testInvitation() throws Exception {

        //Sender - Create invitation
        Invitation invitation = a_Metadata.createInvitation(null);
        Assert.notNull(invitation.getId());
        Assert.notNull(invitation.getMdid());

        //Recipient - Accept invitation and check if sender mdid is included
        Invitation acceptedInvitation = b_Metadata.acceptInvitation(invitation.getId());
        System.out.println(acceptedInvitation);
        Assert.isTrue(invitation.getId().equals(acceptedInvitation.getId()));
        Assert.isTrue(a_Metadata.getAddress().equals(acceptedInvitation.getMdid()));

        //Sender - Check if invitation was accepted
        //If it has been accepted the recipient mdid will be included in invitation contact
        Invitation checkInvitation = a_Metadata.readInvitation(invitation.getId());
        System.out.println(checkInvitation.toString());
        Assert.isTrue(invitation.getId().equals(checkInvitation.getId()));
        Assert.isTrue(b_Metadata.getAddress().equals(checkInvitation.getContact()));

        //delete one-time UUID
        System.out.println("deleting "+invitation.getId());
        boolean success = a_Metadata.deleteInvitation(invitation.getId());
        Assert.isTrue(success);

        //make sure one-time UUID is deleted
        Invitation invitationDel = a_Metadata.readInvitation(invitation.getId());
        Assert.isNull(invitationDel);
    }

    @Test
    public void testSendPayment() throws Exception {

        Invitation invitation = a_Metadata.createInvitation(null);
        System.out.println("Creating invite with id: " + invitation.getId());

        //'contact' is recipient address (not available until accepted)
        System.out.println("\n--Sender--");
        invitation = a_Metadata.readInvitation(invitation.getId());
        System.out.println("Check if accepted...");
        Assert.isNull(invitation.getContact());
        System.out.println("not yet");

        System.out.println("\n--Recipient--");
        //Accept one time url invite - 'mdid' is sender address
        invitation = b_Metadata.acceptInvitation(invitation.getId());
        System.out.println("Accepting invite from " + invitation.getMdid());
        System.out.println("Attaching my address to invite" + b_Metadata.getAddress());
        //Add sender address to trusted list
        System.out.println("Adding sender to my trusted list...");
        b_Metadata.putTrusted(invitation.getMdid());

        System.out.println("\n--Sender--");
        //contact is recipient address (now available)
        invitation = a_Metadata.readInvitation(invitation.getId());
        System.out.println("Check if accepted...");
        System.out.println(invitation.getContact() + " accepted the invite");
        //Add recipient address to trusted list
        System.out.println("Adding recipient to my trusted list...");
        a_Metadata.putTrusted(invitation.getContact());

        //Payment request test
        System.out.println("\n--Sender--");
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setNote("I owe you £15.50 for the Honest burger.");
        paymentRequest.setAmount(2637310);

        System.out.println("Sending payment request: " +new ObjectMapper().writeValueAsString(paymentRequest));
        a_Metadata.sendPaymentRequest(invitation.getContact(), paymentRequest);

        System.out.println("\n--Recipient--");
        List<PaymentRequest> paymentRequests = b_Metadata.getPaymentRequests(true);
        String receivingAddress = b_wallet.getAccount(0).getReceive().getAddressAt(0).getAddressString();
        System.out.println("Checking payment requests and found " + paymentRequests.size() + " new request.");
        System.out.println("Received payment request: '" + paymentRequests.get(0).getNote() + "'");
        System.out.println("Accepting payment request and responding with address '"+receivingAddress+"'");
        b_Metadata.acceptPaymentRequest(invitation.getMdid(), paymentRequests.get(0), "Send coins here please.", receivingAddress);

        System.out.println("\n--Sender--");
        List<PaymentRequestResponse> paymentRequestResponses = a_Metadata.getPaymentRequestResponses(true);
        System.out.println("Checking payment requests responses and found " + paymentRequestResponses.size() + " new responses.");
        System.out.println("Received payment request response with address: '" + paymentRequestResponses.get(0).getAddress() + "'");

        //Use this URI for SendActivity
        System.out.println("Bitcoin URL: '" + paymentRequestResponses.get(0).toBitcoinURI() + "'");

        System.out.println("Marking payment as processed...");
    }
}