package info.blockchain.wallet.multiaddr;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.util.RestClient;

import org.junit.Before;
import org.junit.Test;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MultiAddrFactoryTest {

    //Address with lost private key, so multiaddress response won't change
    private final String dormantAddress = "15sAyHb9zBsZbVnaSXz2UivTZYxnjjrEkX";
    private final long dormantAddressBalance = 10000L;

    private final String dormantXpub = "xpub6CFgfYG9chNp7rzZ7ByXyAJruku5JSVhtGmGqR9tmeLRwu3jtioyBZpXC6GAnpMQPBQg5rviqTwMN4EwgMCZNVT3N22sSnM1yEfBQzjHXJt";
    private final long dormantXpubBalance = 10000L;

    @Before
    public void setup() {
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
    }

    @Test
    public void refreshLegacyAddressData() throws Exception {
        MultiAddrFactory.getInstance().refreshLegacyAddressData(new String[]{dormantAddress}, false);
        assertThat(MultiAddrFactory.getInstance().getLegacyBalance(), is(dormantAddressBalance));
    }

    @Test
    public void refreshXPUBData() throws Exception {
        MultiAddrFactory.getInstance().refreshXPUBData(new String[]{dormantXpub});
        assertThat(MultiAddrFactory.getInstance().getXpubBalance(), is(dormantXpubBalance));
    }
}