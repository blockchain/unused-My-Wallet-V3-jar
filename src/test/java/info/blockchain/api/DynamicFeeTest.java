package info.blockchain.api;

import org.json.JSONObject;
import org.junit.Test;

public class DynamicFeeTest {

    @Test
    public void testGetDynamicFee() throws Exception {
        JSONObject result = DynamicFee.getDynamicFee();
        assert (result.has("default") && result.has("estimate"));
    }
}