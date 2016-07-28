package info.blockchain.wallet.multiaddr;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MultiAddrFactoryTest {

    //Address with lost private key, so multiaddress response won't change
    private final String dormantAddress = "15sAyHb9zBsZbVnaSXz2UivTZYxnjjrEkX";
    private final long dormantAddressBalance = 10000L;

    private final String dormantXpub = "xpub6CFgfYG9chNp7rzZ7ByXyAJruku5JSVhtGmGqR9tmeLRwu3jtioyBZpXC6GAnpMQPBQg5rviqTwMN4EwgMCZNVT3N22sSnM1yEfBQzjHXJt";
    private final long dormantXpubBalance = 10000L;

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