package info.blockchain.wallet.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.api.MetadataEndpoints;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;

import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Before;
import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Integration Test
 */
public class MetadataIT {

    boolean isEncrypted = false;

    MetadataEndpoints httpClient;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
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
    public void testFetchExistingMagicHash() throws Exception {

        /*
        Magic hash need to be computed correctly otherwise consecutive PUT will fail
         */

        Wallet wallet = getWallet();
        DeterministicKey key = wallet.getMasterKey();

        Metadata metadata = new Metadata(httpClient, key, 2, isEncrypted);
        metadata.putMetadata(mapper.writeValueAsString("Yolo1"));

        metadata = new Metadata(httpClient, key, 2, isEncrypted);
        metadata.putMetadata(mapper.writeValueAsString("Yolo2"));

        metadata = new Metadata(httpClient, key, 2, isEncrypted);
        metadata.putMetadata(mapper.writeValueAsString("Yolo3"));
    }

//    @Test
//    public void test() throws Exception {
//
//        String mnemonic = "iron permit matter upset various access scorpion drip tree best viable chuckle";
//        PayloadManager payloadManager = PayloadManager.getInstance();
//        payloadManager.restoreHDWallet("", mnemonic, "Account 1");
//        DeterministicKey key = payloadManager.getMasterKey();
//
////        DeterministicKey key = getRandomECKey();
//
//        Metadata metadata = new Metadata(httpClient, key, 2048, true);
//        metadata.putMetadata(mapper.writeValueAsString("{\"whats up\":\"my ninja\"}"));
//
//        System.out.println(metadata.getMetadata());
//    }
}