package info.blockchain.api;

import org.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class UnspentTest {

    private final String dormantAddress = "1FeexV6bAHb8ybZjqQMjJrcCrHGW9sb6uF";

    @Test
    public void refreshLegacyAddressData() throws Exception {

        Unspent unspent = new Unspent();
        JSONObject json = unspent.getUnspentOutputs(dormantAddress);

        assertThat("JSON doesn't have unspent_outputs key", json.has("unspent_outputs"));
    }
}
