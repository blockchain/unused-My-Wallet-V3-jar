package info.blockchain.wallet.payload.data;

import info.blockchain.wallet.MockedResponseTest;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class WalletBaseTest extends MockedResponseTest {

    @Test
    public void fromJson_v3_1() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_v3_1.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        Assert.assertEquals("ef41d3c990b65000418ef330049f1b4efb84a0e39e91c69e37f6aa139b7ed889945bf7c16e492c6a55b5eb5bef7d5a8baccd91614e0be60ab2c39d33cab9302b", walletBaseBody.getExtraSeed());
        Assert.assertEquals("en", walletBaseBody.getLanguage());
        Assert.assertEquals("d78bf97da866cdda7271a8de0f2d101caf43ae6280b3c69b85bf82d367649ea7", walletBaseBody.getPayloadChecksum());
        Assert.assertEquals("d3e3b31c57f823ed", walletBaseBody.getWarChecksum());

        walletBaseBody.decryptPayload("MyTestWallet");
        Assert.assertEquals(5000, walletBaseBody.getWalletBody().getOptions().getPbkdf2Iterations());
        Assert.assertNotNull(walletBaseBody.getWalletBody());
        Assert.assertEquals("a09910d9-1906-4ea1-a956-2508c3fe0661", walletBaseBody.getWalletBody().getGuid());
    }

    @Test
    public void fromJson_v3_2() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_v3_2.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        Assert.assertEquals("843865504b86dba66a9225963954e065d9f3ee558515232952943319ca8b29050c5613d80247f632b074d1d47d29325469cfdd9c5a81ba58b483628824c6b89d", walletBaseBody.getExtraSeed());
        Assert.assertEquals("en", walletBaseBody.getLanguage());
        Assert.assertEquals("7416cd440f7b15182beb15614a63d5e53b3a6f65634d2b160884c131ab336b01", walletBaseBody.getPayloadChecksum());
        Assert.assertEquals("d1aeff0ddc48b949", walletBaseBody.getWarChecksum());

        walletBaseBody.decryptPayload("SomeTestPassword");
        Assert.assertEquals(5000, walletBaseBody.getWalletBody().getOptions().getPbkdf2Iterations());
        Assert.assertNotNull(walletBaseBody.getWalletBody());
        Assert.assertEquals("e5eba801-c8bc-4a64-99ba-094e12a80766", walletBaseBody.getWalletBody().getGuid());
    }

    @Test
    public void fromJson_v3_3() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_v3_3.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        Assert.assertEquals("e5b175f2d9d869ddf0caa6dd069f11268d45e7e7e97d965a710b12dced5ea3da7ae8cbea1a7ee1fca22826ebbe9c1a01b9dbd80e12034cee83510f57ed22a873", walletBaseBody.getExtraSeed());
        Assert.assertEquals("en", walletBaseBody.getLanguage());
        Assert.assertEquals("fc631f8434f45c43e7040f1192b6676a8bd49e0fd00fb4848acdc0dcaa665400", walletBaseBody.getPayloadChecksum());
        Assert.assertEquals("d1aeff0ddc48b949", walletBaseBody.getWarChecksum());

        walletBaseBody.decryptPayload("SomeTestPassword");
        Assert.assertEquals(7520, walletBaseBody.getWalletBody().getOptions().getPbkdf2Iterations());
        Assert.assertNotNull(walletBaseBody.getWalletBody());
        Assert.assertEquals("e5eba801-c8bc-4a64-99ba-094e12a80766", walletBaseBody.getWalletBody().getGuid());
    }

    @Test
    public void fromJson_v2_3() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_v2_1.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        Assert.assertEquals("c19301faaa4f3a6cfda5313e246f7c181d5a9994cb8b596da35e2e643b59101591131de7060b8aa0045a5a4839bb0910baa0057faf13880cfef86face1a25bc9", walletBaseBody.getExtraSeed());
        Assert.assertEquals("en", walletBaseBody.getLanguage());
        Assert.assertEquals("110764d05c020d4818e2529ca28df9d8b96d50c694650348f885fc075f9366d5", walletBaseBody.getPayloadChecksum());
        Assert.assertEquals("d1aeff0ddc48b949", walletBaseBody.getWarChecksum());

        walletBaseBody.decryptPayload("SomeTestPassword");
        Assert.assertEquals(5000, walletBaseBody.getWalletBody().getOptions().getPbkdf2Iterations());
        Assert.assertNotNull(walletBaseBody.getWalletBody());
        Assert.assertEquals("5f071985-01b5-4bd4-9d5f-c7cf570b1a2d", walletBaseBody.getWalletBody().getGuid());
    }

    @Test
    public void fromJson_v2_2() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_v2_2.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        Assert.assertEquals("224f165f53690ba05b7e2cefce4cd8990681d6624306b01d40fb041f14d1e6cf32c08132632fea25680a6b7dbf1b3fe7e16f99281cd21d7e008a265f8fdd06b1", walletBaseBody.getExtraSeed());
        Assert.assertEquals("en", walletBaseBody.getLanguage());
        Assert.assertEquals("31b162d3e1fd0b57d8b7dd1202c16604be221bde2fe0192fc0a4e7ce704d3446", walletBaseBody.getPayloadChecksum());
        Assert.assertEquals("d1aeff0ddc48b949", walletBaseBody.getWarChecksum());

        walletBaseBody.decryptPayload("SomeTestPassword");
        Assert.assertEquals(1000, walletBaseBody.getWalletBody().getOptions().getPbkdf2Iterations());
        Assert.assertNotNull(walletBaseBody.getWalletBody());
        Assert.assertEquals("5f071985-01b5-4bd4-9d5f-c7cf570b1a2d", walletBaseBody.getWalletBody().getGuid());
    }

    @Test
    public void fromJson_v1_1() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_v1_1.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        Assert.assertEquals("fd38fe32d040f0381045bc5daf2a32da99af97700e2b710e1e1da4e528df85caad8faea46bb48b70787eab1347faa7f2c6b669b40b8737df6aae1b80ba1db804", walletBaseBody.getExtraSeed());
        Assert.assertEquals("en", walletBaseBody.getLanguage());
        Assert.assertEquals("26c0477b045655bb7ba3e81fb99d7e8ce16f4571400223026169ba8e207677a4", walletBaseBody.getPayloadChecksum());
        Assert.assertEquals("3642b083667c7f4d", walletBaseBody.getWarChecksum());

        walletBaseBody.decryptPayload("mypassword");
        Assert.assertNotNull(walletBaseBody.getWalletBody());
        Assert.assertEquals("9ebb4d4f-f36e-40d6-9a3e-5a3cca5f83d6", walletBaseBody.getWalletBody().getGuid());
    }

    @Test
    public void fromJson_v1_2() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_v1_2.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        Assert.assertEquals("7ffafcba53d3d014d13f7e33717bac748506fb1a2095d09a137851c628a3d27c584b3eb0f48c53f5e3b3f3fdf1a84d6ab91006def452e77127a42135b5c854f8", walletBaseBody.getExtraSeed());
        Assert.assertEquals("en", walletBaseBody.getLanguage());
        Assert.assertEquals("57f97ace89c105c19c43a15f2d6e3091d457dec804243b15772d2062a32f8b7d", walletBaseBody.getPayloadChecksum());
        Assert.assertEquals("3642b083667c7f4d", walletBaseBody.getWarChecksum());

        walletBaseBody.decryptPayload("mypassword");
        Assert.assertNotNull(walletBaseBody.getWalletBody());
        Assert.assertEquals("2ca9b0e4-6b82-4dae-9fef-e8b300c72aa2", walletBaseBody.getWalletBody().getGuid());
    }

    @Test
    public void fromJson_v1_3() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_v1_3.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        Assert.assertEquals("efd75f2cbfb5a83bb6a9056d04b3f427e5c2547ec9b181d2bc719dafc3bb9f469cfa5c9935af1d09efe86091aaab674cad5f2e78b154032a76ee271c3d2c2f17", walletBaseBody.getExtraSeed());
        Assert.assertEquals("en", walletBaseBody.getLanguage());
        Assert.assertEquals("a4b67f406268dced75ac5c628da854898c9a3134b7e3755311f199723d426765", walletBaseBody.getPayloadChecksum());
        Assert.assertEquals("3642b083667c7f4d", walletBaseBody.getWarChecksum());

        walletBaseBody.decryptPayload("mypassword");
        Assert.assertNotNull(walletBaseBody.getWalletBody());
        Assert.assertEquals("4077b6d9-73b3-4d22-96d4-9f8810fec435", walletBaseBody.getWalletBody().getGuid());
    }

    @Test
    public void encryptAndWrapPayload() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_v3_1.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        /////////////
        //Decrypt
        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        walletBaseBody.decryptPayload("MyTestWallet");

        //Encrypt
        Pair pair = walletBaseBody.encryptAndWrapPayload("MyTestWallet");

        //Check wallet wrapper
        WalletWrapper encryptedwalletWrapper = (WalletWrapper) pair.getRight();
        Assert.assertEquals(5000, encryptedwalletWrapper.getPbkdf2Iterations());
        Assert.assertEquals(3, encryptedwalletWrapper.getVersion());

        //Decrypt again to check payload intact
        Wallet walletBody = encryptedwalletWrapper.decryptPayload("MyTestWallet");
        Assert.assertEquals("a09910d9-1906-4ea1-a956-2508c3fe0661", walletBody.getGuid());

        ///////Encrypt with different iterations//////
        //Decrypt
        walletBaseBody = WalletBase.fromJson(walletBase);
        walletBaseBody.decryptPayload("MyTestWallet");
        walletBaseBody.getWalletBody().getOptions().setPbkdf2Iterations(7500);

        //Encrypt
        pair = walletBaseBody.encryptAndWrapPayload("MyTestWallet");

        //Check wallet wrapper
        encryptedwalletWrapper = (WalletWrapper) pair.getRight();
        Assert.assertEquals(7500, encryptedwalletWrapper.getPbkdf2Iterations());
        Assert.assertEquals(3, encryptedwalletWrapper.getVersion());

        //Decrypt again to check payload intact
        walletBody = encryptedwalletWrapper.decryptPayload("MyTestWallet");
        Assert.assertEquals("a09910d9-1906-4ea1-a956-2508c3fe0661", walletBody.getGuid());

        ///////Encrypt with different password//////
        //Decrypt
        walletBaseBody = WalletBase.fromJson(walletBase);
        walletBaseBody.decryptPayload("MyTestWallet");
        walletBaseBody.getWalletBody().getOptions().setPbkdf2Iterations(7500);

        //Encrypt
        pair = walletBaseBody.encryptAndWrapPayload("MyNewTestWallet");

        //Check wallet wrapper
        encryptedwalletWrapper = (WalletWrapper) pair.getRight();
        Assert.assertEquals(7500, encryptedwalletWrapper.getPbkdf2Iterations());
        Assert.assertEquals(3, encryptedwalletWrapper.getVersion());

        //Decrypt again to check payload intact
        walletBody = encryptedwalletWrapper.decryptPayload("MyNewTestWallet");
        Assert.assertEquals("a09910d9-1906-4ea1-a956-2508c3fe0661", walletBody.getGuid());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = getClass().getClassLoader().getResource("wallet_v3_1.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBase walletBaseBody = WalletBase.fromJson(walletBase);
        String jsonString = walletBaseBody.toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(7, jsonObject.keySet().size());
    }
}