package info.blockchain.wallet.metadata;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.util.RestClient;

import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class MetadataNodeFactoryTest {

    String masterKeyHex = "cbf9a5a77a913ee6cbdd560510c8d40020431479edb582e81262cc5e14fdc921";
    String guid = "5381b28a-290b-4ce5-bc7f-11bb40a3600c";
    String sharedKey = "f0a6557e-9421-43c7-8e0c-6aa025110afb";
    String walletPass = "password";

    MockInterceptor mockInterceptor;

    MetadataNodeFactory metadataNodeFactory;

    @Before
    public void setup() throws Exception {

        mockInterceptor = new MockInterceptor();

        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {

                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(mockInterceptor)//Mock responses
                        .build();

                return RestClient.getRetrofitInstance(okHttpClient);
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return null;
            }
        });

        mockInterceptor.setResponseString("{\"version\":1,\"payload\":\"mUVohkLFB4m2vZy4PjtupD4Yuk6smwWp7FfIyvWnzDOMieiEXOejVvbQ3oPBl/Ln4aQp6ZuLt5y0zL8XUaI0FDleazn4hzeiv34fZGj9xzvj0WMISlcNJfR0qU3e4pfK5HmsM5HMwlxri2Ozb8/mZXJFnr5CfibGsNwqYhn1v+bmcOam3B79jtRJ8sINRC0jJYkqaF5yOow74YYcKaKEgeumDf/cZVl1Kcd2i/n6i63ujPWt1gqfzeOi6naIWyq508U5hduwtkFpG5ONLajXjRf13I0lipClIUSo13ZfKUdqd2zMdDTgPkLhF5hjWYxybEMA7BRSzptaSaVj4IbikQHmdAKQID4WmEbqPMDpUb0=\",\"signature\":\"HxiSlotRSXVGF8rnwkHYgBsiFlCNFTQ4Y1Y+xaufODKLJGv+JFsdttvqNqPNCNX8aqWgq9+z0fL1/5DXkr4fnGI=\",\"prev_magic_hash\":\"7327d31adae585c360ed746b3626f32c4ed9db8675f1ff468448c5d6081b3f34\",\"type_id\":-1,\"created_at\":1481716675000,\"updated_at\":1481724489000,\"address\":\"1G8NqfMPq8WWHgrTNfnAWr1gSwCmYn4XaS\"}");
        metadataNodeFactory = new MetadataNodeFactory(guid, sharedKey, walletPass);
    }

    @Test
    public void testSaveMetadataHdNodes() throws Exception {

        String metadataB58 = "xprv9twfkEhG8UPRWCrX3HnowyQk7vMCVeGY5ZHWsLANZqxqBaiAHPN3MvuuFedVNBhixki7WBvYW8gcg2mCemfV6XNNpCZ31JusEHUhdmdhsvp";
        String sharedMetadataB58 = "xprv9twfkEhLiMZnQ5Gjf8w2f5wn5ASrQ9qMVwLo458shQmfMWhus8oo9aecvsAmSDXfgGbX2xQgeXi9Luj9ao5xGLKGjCj7gKiwvBGnjhdVEji";

        metadataNodeFactory.saveMetadataHdNodes(HDKeyDerivation.createMasterPrivateKey(Hex.decode(masterKeyHex)));

        Assert.assertTrue(metadataNodeFactory.getMetadataNode().serializePrivB58(MainNetParams.get()).equals(metadataB58));
        Assert.assertTrue(metadataNodeFactory.getSharedMetadataNode().serializePrivB58(MainNetParams.get()).equals(sharedMetadataB58));
    }

    @Test
    public void testIsMetadataUsable() throws Exception {
        mockInterceptor.setResponseString("{\"version\":1,\"payload\":\"VpyH1H4n7dyb0JZGp1ymVkxIJMupa8Y5DP2c+2hH24qMNvjxmufJixWviTES0NNANvUnQr+Eb7V1WYe4QH0UKljnOEGahz8UCYQ1rGiteNAELeiSijHBFUdlxQAzAUsa57EMrBW4twDZ5Af0SCfMy6mhMCrUWMAq/qUfkBlxEvtNBN3plLuRG3F8K428UYe8yPWnmIOZ5qD5dBgs/L+b2COLuJQfhUK7mkyurRhk5aKWkxDVx8r2rA/u0d8lX89+ChndIYqqZh46sqwelAZ21IpvhjNuW7cDgtAIJokRt44ihZ7lQ9A7MM9CotRUtN+TqUPWt7g8VI1wqRmqkZCiby01djqM8pd9ZekuFJCVztw=\",\"signature\":\"H/ESHR40uap12zOHMlKH6Qdl8JQ68ZJNjp2DGo+I6YMDUyZ4CLc/QWUbli7Rvm0QUJytrWxffVKLa7FsoBvoLTA=\",\"prev_magic_hash\":\"b5f22894644afaa6c82edbe513c45a7af0489e6bf808fa9b2bff16b2ca9f9524\",\"type_id\":-1,\"created_at\":1481716675000,\"updated_at\":1481724546000,\"address\":\"1G8NqfMPq8WWHgrTNfnAWr1gSwCmYn4XaS\"}");
        Assert.assertTrue(metadataNodeFactory.isMetadataUsable());
    }
}