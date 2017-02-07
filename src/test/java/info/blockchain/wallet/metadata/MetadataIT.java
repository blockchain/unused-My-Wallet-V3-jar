package info.blockchain.wallet.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.blockchain.util.RestClient;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.FrameworkInterface;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.Wallet;
import info.blockchain.wallet.bip44.WalletFactory;
import info.blockchain.wallet.contacts.data.PublicContactDetails;
import info.blockchain.wallet.util.MetadataUtil;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import retrofit2.Retrofit;

/**
 * Integration Test
 */
@Ignore
public class MetadataIT {

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {

        //Set environment
//        PersistentUrls.getInstance().setCurrentEnvironment(PersistentUrls.Environment.DEV);
//        PersistentUrls.getInstance().setCurrentApiUrl("https://api.dev.blockchain.info/");
//        PersistentUrls.getInstance().setCurrentServerUrl("https://explorer.dev.blockchain.info/");

        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {

                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                        .addInterceptor(loggingInterceptor)//Extensive logging
                        .build();

                return RestClient.getRetrofitApiInstance(okHttpClient);
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return null;
            }

            @Override
            public String getApiCode() {
                return null;
            }
        });
    }

    private Wallet getWallet() throws Exception {

        return new WalletFactory(PersistentUrls.getInstance().getCurrentNetworkParams()).restoreWallet("15e23aa73d25994f1921a1256f93f72c",
                "",
                1);
    }

    @Test
    public void testFetchExistingMagicHash() throws Exception {

        /*
        Magic hash need to be computed correctly otherwise consecutive PUT will fail
         */
        DeterministicKey metaDataHDNode = MetadataUtil.deriveMetadataNode(getWallet().getMasterKey());

        Metadata metadata = new Metadata.Builder(metaDataHDNode, 2)
                .build();
        metadata.putMetadata(new PublicContactDetails("Yolo1").toJson());

        metadata = new Metadata.Builder(metaDataHDNode, 2)
                .build();
        metadata.putMetadata(new PublicContactDetails("Yolo2").toJson());

        metadata = new Metadata.Builder(metaDataHDNode, 2)
                .build();
        metadata.putMetadata(new PublicContactDetails("Yolo3").toJson());


    }
}