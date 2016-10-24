package info.blockchain.wallet.payload;

import info.blockchain.util.AssertJson;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class CacheTest {

    @Test
    public void testGetReceiveAccount() throws Exception {
        Cache cache = Cache.fromJson(new JSONObject(testString));
        Assert.assertEquals(cache.getReceiveAccount(), "xpub1");
    }

    @Test
    public void testSetReceiveAccount() throws Exception {
        Cache cache = Cache.fromJson(new JSONObject(testString));
        cache.setReceiveAccount("test 1");
        Assert.assertEquals(cache.getReceiveAccount(), "test 1");
    }

    @Test
    public void testGetChangeAccount() throws Exception {
        Cache cache = Cache.fromJson(new JSONObject(testString));
        Assert.assertEquals(cache.getChangeAccount(), "xpub2");
    }

    @Test
    public void testSetChangeAccount() throws Exception {
        Cache cache = Cache.fromJson(new JSONObject(testString));
        cache.setChangeAccount("test 2");
        Assert.assertEquals(cache.getChangeAccount(), "test 2");
    }

    @Test
    public void testToJSON() throws Exception {

        Cache cache = Cache.fromJson(new JSONObject(testString));
        AssertJson.assertEqual(testString, cache.toJson().toString());
    }

    private final String testString = "{\n" +
            "            \"receiveAccount\": \"xpub1\",\n" +
            "            \"changeAccount\": \"xpub2\"\n" +
            "          }";
}