package info.blockchain.wallet.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.util.RestClient;
import info.blockchain.wallet.util.MetadataUtil;

import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class MetadataTest {

    boolean isEncrypted = false;

    MockInterceptor mockInterceptor;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {

        mockInterceptor = new MockInterceptor();

        //Set environment
        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {

                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(mockInterceptor)//Mock responses
                        .addInterceptor(loggingInterceptor)//Extensive logging
                        .build();

                return RestClient.getRetrofitInstance(okHttpClient);
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return null;
            }
        });
    }

    private Wallet getWallet() throws Exception {

        return new WalletFactory().restoreWallet("15e23aa73d25994f1921a1256f93f72c",
                "",
                1);
    }

    @Test
    public void testAddressDerivation() throws Exception {

        String address = "12sC9tqHzAhdoukhCbTnyx2MjYXNXBGHnF";

        mockInterceptor.setResponse_404();//New metadata response

        DeterministicKey metaDataHDNode = MetadataUtil.deriveMetadataNode(getWallet().getMasterKey());

        Metadata metadata = new Metadata.Builder(metaDataHDNode, 2)
                .setEncrypted(isEncrypted)
                .build();

        Assert.assertTrue(metadata.getAddress().equals(address));
    }

    @Test
    public void testMetadata() throws Exception{

        mockInterceptor.setResponse_404();//New metadata response

        DeterministicKey metaDataHDNode = MetadataUtil.deriveMetadataNode(getWallet().getMasterKey());

        Metadata metadata = new Metadata.Builder(metaDataHDNode, 2)
                .setEncrypted(isEncrypted)
                .build();

        String msg = "Rage rage";
        mockInterceptor.setResponse_PUT_rage();
        metadata.putMetadata(mapper.writeValueAsString(msg));

        mockInterceptor.setResponse_GET_rage();
        String result1 = metadata.getMetadata();

        Assert.assertTrue(msg.equals(result1));

        mockInterceptor.setResponse_PUT_more_rage();
        msg = "Rage rage some more";
        metadata.putMetadata(mapper.writeValueAsString(msg));

        mockInterceptor.setResponse_GET_more_rage();
        String result2 = metadata.getMetadata();
        Assert.assertTrue(msg.equals(result2));

        mockInterceptor.setResponse_DELETE_ok();
        metadata.deleteMetadata(msg);

        mockInterceptor.setResponse_404();
        Assert.assertNull(metadata.getMetadata());
    }
}