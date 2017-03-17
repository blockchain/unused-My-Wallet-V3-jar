package info.blockchain.wallet.payload.data;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class LegacyAddressTest {

    @Test
    public void fromJson_1() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        Assert.assertEquals(19, wallet.getLegacyAddressList().size());

        LegacyAddress addressBody = wallet.getLegacyAddressList().get(0);
        Assert.assertEquals("import 1", addressBody.getLabel());
        Assert.assertEquals("19Axrcn8nsdZkSJtbnyM1rCs1PGwSzzzNn", addressBody.getAddress());
        Assert.assertEquals("g9rIjgOlfASQuJv38i1xdLmP1m2gMTPe96YzJJ9hjI2BBz5RErNOSeHPdeU2ZIOnsk+M1dfFw649MHXb7RAcZg==", addressBody.getPrivateKey());
        Assert.assertEquals(1433495572, addressBody.getCreatedTime());
        Assert.assertEquals(0, addressBody.getTag());
        Assert.assertEquals("android", addressBody.getCreatedDeviceName());
        Assert.assertNull(addressBody.getCreatedDeviceVersion());

        addressBody = wallet.getLegacyAddressList().get(1);
        Assert.assertEquals("1rW486AbUx2LapYca7kddpULJVqMGMhTH", addressBody.getLabel());
        Assert.assertEquals("1rW486AbUx2LapYca7kddpULJVqMGMhTH", addressBody.getAddress());
        Assert.assertEquals("TIMsVAyWiVcTmcwc6Xv5r494sZcMBRQue6DamkXzDAzOn0cGQSf2XDN+1ZKM/cHgsL0oeJqxrBs9c5TzsNHIZg==", addressBody.getPrivateKey());
        Assert.assertEquals(1434379366, addressBody.getCreatedTime());
        Assert.assertEquals(2, addressBody.getTag());
        Assert.assertEquals("web", addressBody.getCreatedDeviceName());
        Assert.assertEquals("6.1.16", addressBody.getCreatedDeviceVersion());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        LegacyAddress addressBody = wallet.getLegacyAddressList().get(0);

        String jsonString = addressBody.toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(7, jsonObject.keySet().size());
    }
}