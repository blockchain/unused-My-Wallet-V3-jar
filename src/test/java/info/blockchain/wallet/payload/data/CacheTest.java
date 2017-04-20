package info.blockchain.wallet.payload.data;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class CacheTest {

    @Test
    public void fromJson() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        Assert.assertEquals("xpub6F2ehb9khoF6PZZxKS7vD8T2yDeDWuSR5RNH43b2wK5gY2ayWVApQezEzsFz7EpH2Jf6d6GYJzTrbfReT948CyxVgkhkkvmDBGkcY41MMnv",
            wallet.getHdWallets().get(0).getAccount(0).getCache().getChangeAccount());
        Assert.assertEquals("xpub6F2ehb9khoF6MW8WzyT8WdVhvW3RnZxXYdHDvt43LabqGKdpqt39QFgpRCMAcktZckGZJBUrVVP4uwYbrb98MdR8KujG4tu1B4sRHA9QVwE",
            wallet.getHdWallets().get(0).getAccount(0).getCache().getReceiveAccount());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        String jsonString = wallet.getHdWallets().get(0).getAccount(0).getCache().toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(2, jsonObject.keySet().size());
    }
}