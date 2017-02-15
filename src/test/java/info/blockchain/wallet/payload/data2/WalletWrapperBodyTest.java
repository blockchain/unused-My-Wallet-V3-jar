package info.blockchain.wallet.payload.data2;

import info.blockchain.wallet.payload.data.PayloadTest;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class WalletWrapperBodyTest {

    @Test
    public void fromJson_v3_1() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_v3_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBaseBody base = WalletBaseBody.fromJson(body);
        WalletWrapperBody walletWrapperBody = base.getWalletWrapperBody();

        Assert.assertFalse(base.isV1Wallet());
        Assert.assertEquals(3, walletWrapperBody.getVersion());
        Assert.assertEquals(5000, walletWrapperBody.getPbkdf2Iterations());
        Assert.assertEquals("7cic8QztPn", walletWrapperBody.getPayload().substring(0, 10));
    }

    @Test
    public void fromJson_v3_2() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_v3_2.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBaseBody base = WalletBaseBody.fromJson(body);
        WalletWrapperBody walletWrapperBody = base.getWalletWrapperBody();

        Assert.assertFalse(base.isV1Wallet());
        Assert.assertEquals(3, walletWrapperBody.getVersion());
        Assert.assertEquals(5000, walletWrapperBody.getPbkdf2Iterations());
        Assert.assertEquals("tAAhCmDNFl", walletWrapperBody.getPayload().substring(0, 10));
    }

    @Test
    public void fromJson_v3_3() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_v3_3.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBaseBody base = WalletBaseBody.fromJson(body);
        WalletWrapperBody walletWrapperBody = base.getWalletWrapperBody();

        Assert.assertFalse(base.isV1Wallet());
        Assert.assertEquals(3, walletWrapperBody.getVersion());
        Assert.assertEquals(7520, walletWrapperBody.getPbkdf2Iterations());
        Assert.assertEquals("mKN78QPyI/", walletWrapperBody.getPayload().substring(0, 10));
    }

    @Test
    public void fromJson_v2_1() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_v2_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBaseBody base = WalletBaseBody.fromJson(body);
        WalletWrapperBody walletWrapperBody = base.getWalletWrapperBody();

        Assert.assertFalse(base.isV1Wallet());
        Assert.assertEquals(2, walletWrapperBody.getVersion());
        Assert.assertEquals(5000, walletWrapperBody.getPbkdf2Iterations());
        Assert.assertEquals("iDQNpZ56UV", walletWrapperBody.getPayload().substring(0, 10));
    }

    @Test
    public void fromJson_v2_2() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_v2_2.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBaseBody base = WalletBaseBody.fromJson(body);
        WalletWrapperBody walletWrapperBody = base.getWalletWrapperBody();

        Assert.assertFalse(base.isV1Wallet());
        Assert.assertEquals(2, walletWrapperBody.getVersion());
        Assert.assertEquals(1000, walletWrapperBody.getPbkdf2Iterations());
        Assert.assertEquals("nTru1Jhx1I", walletWrapperBody.getPayload().substring(0, 10));
    }

    @Test
    public void fromJson_v1_1() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_v1_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBaseBody base = WalletBaseBody.fromJson(body);
        WalletWrapperBody walletWrapperBody = base.getWalletWrapperBody();
        Assert.assertNull(walletWrapperBody);
        Assert.assertTrue(base.isV1Wallet());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_v3_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBaseBody base = WalletBaseBody.fromJson(body);
        WalletWrapperBody walletWrapperBody = base.getWalletWrapperBody();;
        String jsonString = walletWrapperBody.toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(3, jsonObject.keySet().size());
    }
}