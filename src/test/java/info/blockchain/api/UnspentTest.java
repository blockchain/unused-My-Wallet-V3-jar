package info.blockchain.api;

import org.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class UnspentTest {

    String xpub = "xpub6DHgYHmEhNxMuqjcoCAX2bfawU8rsufyDjHBgyq43Gz8X6iAgPVYMRByF7ETJmVFcDhkbdbUn31qMHaRzed17FrghmoQsQsdxdLv3nV2jMs";

    @Test
    public void refreshLegacyAddressData() throws Exception {

        Unspent unspent = new Unspent();
        JSONObject json = unspent.getUnspentOutputs(xpub);

        assertThat("JSON doesn't have unspent_outputs key", json.has("unspent_outputs"));
    }
}
