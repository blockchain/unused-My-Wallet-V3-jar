package info.blockchain.wallet.metadata;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.util.RestClient;

import org.bitcoinj.crypto.DeterministicKey;
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

        key = new WalletFactory().restoreWallet("009e63e95eeabdbe080ead5a707bdac2","",1).getMasterKey();
    }

    @Test
    public void getNode() throws Exception {

    }

    @Test
    public void getAddress() throws Exception {

    }

    @Test
    public void getToken() throws Exception {

    }

    @Test
    public void getTrustedList() throws Exception {

    }

    @Test
    public void getTrusted() throws Exception {

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

}