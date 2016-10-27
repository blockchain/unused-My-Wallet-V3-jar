package info.blockchain.wallet.payload;

import info.blockchain.util.AssertJson;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class OptionsTest {

    @Test
    public void testSetIterations() throws Exception {

        Options options = Options.fromJson(new JSONObject(testString));
        options.setIterations(2300);

        Assert.assertEquals(options.getIterations(), 2300);
    }

    @Test
    public void testSetFeePerKB() throws Exception {

        Options options = Options.fromJson(new JSONObject(testString));
        options.setFeePerKB(321);

        Assert.assertEquals(options.getFeePerKB(), 321);
    }

    @Test
    public void testSetLogoutTime() throws Exception {

        Options options = Options.fromJson(new JSONObject(testString));
        options.setLogoutTime(66000);

        Assert.assertEquals(options.getLogoutTime(), 66000);
    }

    @Test
    public void testSetHtml5Notifications() throws Exception {

        Options options = Options.fromJson(new JSONObject(testString));

        options.setHtml5Notifications(true);
        Assert.assertEquals(options.isHtml5Notifications(), true);

        options.setHtml5Notifications(false);
        Assert.assertEquals(options.isHtml5Notifications(), false);
    }

    @Test
    public void testToJson() throws Exception {
        Options options = Options.fromJson(new JSONObject(testString));
        AssertJson.assertEqual(testString, options.toJson().toString());
    }

    private String testString = "{\n" +
            "    \"pbkdf2_iterations\": 5000,\n" +
            "    \"fee_per_kb\": 10000,\n" +
            "    \"html5_notifications\": false,\n" +
            "    \"logout_time\": 3600000\n" +
            "  }";
}