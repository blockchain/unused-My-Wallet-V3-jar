package info.blockchain.api;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Ignore
public class BalanceTest {

    private final String dormantAddress = "1FeexV6bAHb8ybZjqQMjJrcCrHGW9sb6uF";
    private final int dormantAddressTxCount = 88;

    @Test
    public void refreshLegacyAddressData() throws Exception {

        Balance balance = new Balance();
//        JSONObject json = balance.getBalance(new String[]{dormantAddress});

        assertThat(dormantAddressTxCount, is(balance.getXpubTransactionCount(dormantAddress)));

    }
}
