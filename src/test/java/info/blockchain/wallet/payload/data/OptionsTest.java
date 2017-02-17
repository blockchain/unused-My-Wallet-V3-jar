package info.blockchain.wallet.payload.data;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class OptionsTest {

    @Test
    public void fromJson_1() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        Options options = wallet.getOptions();
        Assert.assertEquals(10000, options.getFeePerKb());
        Assert.assertEquals(5000, options.getPbkdf2Iterations());
        Assert.assertFalse(options.isHtml5Notifications());
        Assert.assertEquals(3600000, options.getLogoutTime());
    }

    @Test
    public void fromJson_2() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_2.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        Options options = wallet.getOptions();
        Assert.assertEquals(0, options.getFeePerKb());

        //Expect iterations to default. 0 not allowed
        Assert.assertEquals(5000, options.getPbkdf2Iterations());

        Assert.assertFalse(options.isHtml5Notifications());
        Assert.assertEquals(600000, options.getLogoutTime());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        Options options = wallet.getOptions();
        String jsonString = options.toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(4, jsonObject.keySet().size());
    }
}