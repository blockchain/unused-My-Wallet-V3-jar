package info.blockchain.wallet.multiaddr;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MultiAddrFactoryTest {

    //Address with lost private key, so multiaddress response won't change
    private final String dormantAddress = "1FeexV6bAHb8ybZjqQMjJrcCrHGW9sb6uF";
    private final long dormantAddressBalance = 7995711508537L;

    private final String dormantXpub = "xpub6CmZamQcHw2TPtbGmJNEvRgfhLwitarvzFn3fBYEEkFTqztus7W7CNbf48Kxuj1bRRBmZPzQocB6qar9ay6buVkQk73ftKE1z4tt9cPHWRn";
    private final long dormantXpubBalance = 20000L;
    private final long dormantXpubTxCount = 1L;

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

    @Test
    public void refreshLegacyAddressDataa() throws Exception {
        assertThat(MultiAddrFactory.getInstance().getXpubTransactionCount(dormantXpub), is(dormantXpubTxCount));
    }
}