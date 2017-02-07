package info.blockchain.wallet.api;

import static org.hamcrest.MatcherAssert.assertThat;

import info.blockchain.util.RestClient;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.FrameworkInterface;
import okhttp3.OkHttpClient;
import org.junit.Test;
import retrofit2.Retrofit;

public class AccessTest {

    final String guid = "a09910d9-1906-4ea1-a956-2508c3fe0661";

    @Test
    public void getEncryptedPayload_whenBadString_shouldPass() throws Exception {

        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {
                return RestClient.getRetrofitApiInstance(new OkHttpClient());
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

        WalletPayload access = new WalletPayload();
        String sessionId = access.getSessionId(guid);
        String payload = access.getEncryptedPayload(guid, sessionId);
        assertThat("Encrypted payload should not be null", payload != null);
    }
}
