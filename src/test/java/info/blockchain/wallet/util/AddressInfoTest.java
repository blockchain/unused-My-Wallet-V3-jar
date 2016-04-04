package info.blockchain.wallet.util;

import org.json.JSONObject;
import org.junit.Test;

public class AddressInfoTest {

    @Test
    public void testGetAddressInfo() throws Exception {
        JSONObject info = AddressInfo.getInstance().getAddressInfo("1JQhWJciZAhDHbjDgjs8Yj6Y7UG1ihfZdK", "&limit=0");
    }
}