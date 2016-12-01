package info.blockchain.wallet.metadata;

import com.google.gson.Gson;

import info.blockchain.api.MetadataEndpoints;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;

import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Before;
import org.junit.Test;

import io.jsonwebtoken.lang.Assert;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MetadataTest {

    boolean isEncrypted = false;

    MetadataEndpoints httpClient;
    MockInterceptor mockInterceptor;

    @Before
    public void setup() throws Exception {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        mockInterceptor = new MockInterceptor();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(mockInterceptor)//Mock responses
//                .addInterceptor(loggingInterceptor)//Extensive logging
                .build();

        httpClient = RestClient.getClient(okHttpClient);
    }

    private Wallet getWallet() throws Exception {

        return new WalletFactory().restoreWallet("15e23aa73d25994f1921a1256f93f72c",
                "",
                1);
    }

    @Test
    public void testAddressDerivation() throws Exception {

        String address = "12sC9tqHzAhdoukhCbTnyx2MjYXNXBGHnF";

        Wallet wallet = getWallet();
        DeterministicKey key = wallet.getMasterKey();

        mockInterceptor.setResponse_404();//New metadata response
        Metadata metadata = new Metadata(httpClient, key, 2, isEncrypted);
        Assert.isTrue(metadata.getAddress().equals(address));
    }

    @Test
    public void testMetadata() throws Exception{

        Wallet wallet = getWallet();
        DeterministicKey key = wallet.getMasterKey();

        mockInterceptor.setResponse_404();//New metadata response
        Metadata metadata = new Metadata(httpClient, key, Metadata.PAYLOAD_TYPE_RESERVED, isEncrypted);

        String message = new Gson().toJson("{hello: 'world'}");

        mockInterceptor.setResponse_PUT_hello_world();
        metadata.putMetadata(message);

        mockInterceptor.setResponse_GET_hello_world();
        String result1 = metadata.getMetadata();
        Assert.isTrue(message.equals(result1));

        mockInterceptor.setResponse_PUT_hello_mars();
        message = new Gson().toJson("{hello: 'mars'}");
        metadata.putMetadata(message);

        mockInterceptor.setResponse_GET_hello_mars();
        String result2 = metadata.getMetadata();
        Assert.isTrue(message.equals(result2));

        mockInterceptor.setResponse_DELETE_ok();
        metadata.deleteMetadata(message);

        mockInterceptor.setResponse_404();
        try {
            metadata.getMetadata();
            Assert.isTrue(false);
        }catch (Exception e){
            Assert.isTrue(true);
        }
    }
}