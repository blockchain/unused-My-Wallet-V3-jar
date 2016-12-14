package info.blockchain.api;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.util.RestClient;

import org.junit.Test;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import static org.hamcrest.MatcherAssert.assertThat;

public class AccessTest {

    final String guid = "a09910d9-1906-4ea1-a956-2508c3fe0661";

    @Test
    public void getEncryptedPayload_whenBadString_shouldPass() throws Exception {

        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {
                return RestClient.getRetrofitInstance(new OkHttpClient());
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return null;
            }
        });

        WalletPayload access = new WalletPayload();
        String sessionId = access.getSessionId(guid);
        String payload = access.getEncryptedPayload(guid, sessionId);
        assertThat("Encrypted payload should not be null", payload != null);
    }
}
