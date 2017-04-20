package info.blockchain.wallet.payload.data;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class AddressBookTest {

    @Test
    public void fromJson_1() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        Assert.assertEquals("QA first one", wallet.getAddressBook().get(0).getLabel());
        Assert.assertEquals("17k7jQsewpru3uxMkaUMxahyvACVc7fjjb", wallet.getAddressBook().get(0).getAddress());
        Assert.assertEquals("QA second one", wallet.getAddressBook().get(1).getLabel());
        Assert.assertEquals("1DiJVG3oD3yeqW26qcVaghwTjvMaVoeghX", wallet.getAddressBook().get(1).getAddress());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        String jsonString = wallet.getAddressBook().get(0).toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(2, jsonObject.keySet().size());
    }
}