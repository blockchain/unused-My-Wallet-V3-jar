package info.blockchain.wallet.metadata;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.util.RestClient;

import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class SharedMetadataTest {

    DeterministicKey key;

    MockInterceptor mockInterceptor;

    @Before
    public void setUp() throws Exception {

        mockInterceptor = new MockInterceptor();

        //Set environment
//        PersistentUrls.getInstance().setCurrentEnvironment(PersistentUrls.Environment.DEV);
//        PersistentUrls.getInstance().setCurrentApiUrl("https://api.staging.blockchain.info/");
//        PersistentUrls.getInstance().setCurrentServerUrl("https://explorer.staging.blockchain.info/");

        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {

                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                        .addInterceptor(mockInterceptor)//Mock responses
//                        .addInterceptor(loggingInterceptor)//Extensive logging
                        .build();

                return RestClient.getRetrofitInstance(okHttpClient);
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return null;
            }
        });

        key = new WalletFactory().restoreWallet("15e23aa73d25994f1921a1256f93f72c","",1).getMasterKey();
    }

    @Test
    public void getNode() throws Exception {
        SharedMetadata sharedMetadata = new SharedMetadata.Builder(key).build();
        Assert.assertTrue(sharedMetadata.getNode().getPrivateKeyAsHex().equals("b3c830cde7bac6b5d2cad754ea523fb6ff51fc59da49d28ac98268d87f23b89b"));
    }

    @Test
    public void getAddress() throws Exception {
        SharedMetadata sharedMetadata = new SharedMetadata.Builder(key).build();
        Assert.assertTrue(sharedMetadata.getAddress().equals("1B6ptXjmNHimrWNMzKSDhxTmdjhWvZE3vV"));
    }

    @Test
    public void getTrustedList() throws Exception {
        mockInterceptor.setResponseString("{\"mdid\":\"1Q8wTYwXRfEW9qKfpwbZKo7kAXFdRhR9s9\",\"contacts\":[{\"name\" : \"John1\"} , {\"name\" : \"John2\"}]}");
        SharedMetadata sharedMetadata = new SharedMetadata.Builder(key).build();
        System.out.println(sharedMetadata.getTrustedList().getContacts().length);
//        Assert.assertTrue(sharedMetadata.getTrustedList().equals("1Q8wTYwXRfEW9qKfpwbZKo7kAXFdRhR9s9"));
    }

    @Test
    public void getTrusted() throws Exception {
        SharedMetadata sharedMetadata = new SharedMetadata.Builder(key).build();
        sharedMetadata.getTrustedList();
    }

    @Test
    public void putTrusted() throws Exception {

    }

    @Test
    public void deleteTrusted() throws Exception {

    }

    @Test
    public void sendPaymentRequest() throws Exception {

    }

    @Test
    public void acceptPaymentRequest() throws Exception {

    }

    @Test
    public void getPaymentRequests() throws Exception {

    }

    @Test
    public void getPaymentRequestResponses() throws Exception {

    }

    @Test
    public void createInvitation() throws Exception {

    }

    @Test
    public void acceptInvitation() throws Exception {

    }

    @Test
    public void readInvitation() throws Exception {

    }

    @Test
    public void deleteInvitation() throws Exception {

    }

    @Test
    public void publishXpub() throws Exception {

    }

    @Test
    public void getPublicXpubFromMdid() throws Exception {

    }

    @Test
    public void decryptFrom() throws Exception {

        DeterministicKey a_key = new WalletFactory().restoreWallet("15e23aa73d25994f1921a1256f93f72c","",1).getMasterKey();
        SharedMetadata a_sharedMetadata = new SharedMetadata.Builder(a_key).build();

        DeterministicKey b_key = new WalletFactory().restoreWallet("20e3939d08ddf727f34a130704cd925e","",1).getMasterKey();
        SharedMetadata b_sharedMetadata = new SharedMetadata.Builder(b_key).build();

        String encryptedMessage = a_sharedMetadata.encryptFor(b_sharedMetadata.getXpub(), "Water is wet");
        String decryptedMessage = b_sharedMetadata.decryptFrom(a_sharedMetadata.getXpub(), encryptedMessage);
        System.out.println(decryptedMessage);
    }
}