package info.blockchain.wallet.payload.data;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.NoSuchAddressException;
import info.blockchain.wallet.payment.Payment;
import info.blockchain.wallet.payment.SpendableUnspentOutputs;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/*
WalletBase
    |
    |__WalletWrapper
            |
            |__Wallet
 */
public class WalletTest extends MockedResponseTest{

    @Test
    public void fromJson_1() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        Assert.assertEquals("a09910d9-1906-4ea1-a956-2508c3fe0661", wallet.getGuid());
        Assert.assertEquals("d14f3d2c-f883-40da-87e2-c8448521ee64", wallet.getSharedKey());
        Assert.assertTrue(wallet.isDoubleEncryption());
        Assert.assertEquals("1f7cb884545e89e4083c10522bf8b991e8e13551aa5816110cb9419277fb4652", wallet.getDpasswordhash());

        for(Entry<String, String> item : wallet.getTxNotes().entrySet()){
            Assert.assertEquals("94a4934712fd40f2b91b7be256eacad49a50b850c949313b07046664d24c0e4c", item.getKey());
            Assert.assertEquals("Bought Pizza", item.getValue());
        }

        //Options parsing tested in OptionsTest
        Assert.assertNotNull(wallet.getOptions());

        //HdWallets parsing tested in HdWalletsBodyTest
        Assert.assertNotNull(wallet.getHdWallets());

        //Keys parsing tested in KeysBodyTest
        Assert.assertNotNull(wallet.getLegacyAddressList());

        //AddressBook parsing tested in AddressBookTest
        Assert.assertNotNull(wallet.getAddressBook());
    }

    @Test
    public void fromJson_2() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_2.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        Assert.assertEquals("9ebb4d4f-f36e-40d6-9a3e-5a3cca5f83d6", wallet.getGuid());
        Assert.assertEquals("41cf823f-2dcd-4967-88d1-ef9af8689fc6", wallet.getSharedKey());
        Assert.assertFalse(wallet.isDoubleEncryption());
        Assert.assertNull(wallet.getDpasswordhash());

        //Options parsing tested in OptionsTest
        Assert.assertNotNull(wallet.getOptions());

        //Keys parsing tested in KeysBodyTest
        Assert.assertNotNull(wallet.getLegacyAddressList());
    }

    @Test
    public void fromJson_3() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_3.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        Assert.assertEquals("2ca9b0e4-6b82-4dae-9fef-e8b300c72aa2", wallet.getGuid());
        Assert.assertEquals("e8553981-b196-47cc-8858-5b0d16284f61", wallet.getSharedKey());
        Assert.assertFalse(wallet.isDoubleEncryption());
        Assert.assertNull(wallet.getDpasswordhash());

        //Options parsing tested in OptionsTest
        Assert.assertNotNull(wallet.getWalletOptions());//very old key for options
        Assert.assertEquals(10, wallet.getWalletOptions().getPbkdf2Iterations());

        //old wallet_options should have created new options
        Assert.assertNotNull(wallet.getOptions());
        Assert.assertEquals(10, wallet.getOptions().getPbkdf2Iterations());

        //Keys parsing tested in KeysBodyTest
        Assert.assertNotNull(wallet.getLegacyAddressList());
    }

    @Test
    public void fromJson_4() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_4.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        Assert.assertEquals("4077b6d9-73b3-4d22-96d4-9f8810fec435", wallet.getGuid());
        Assert.assertEquals("fa1beb37-5836-41d1-9f73-09f292076eb9", wallet.getSharedKey());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        String jsonString = wallet.toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(10, jsonObject.keySet().size());
    }

    @Test
    public void validateSecondPassword() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        wallet.validateSecondPassword("hello");
        Assert.assertTrue(true);
    }

    @Test(expected = DecryptionException.class)
    public void validateSecondPassword_fail() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        wallet.validateSecondPassword("bogus");
    }

    @Test
    public void addAccount() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        Assert.assertEquals(1, wallet.getHdWallets().get(0).getAccounts().size());
        wallet.addAccount(0, "Some Label",null);
        Assert.assertEquals(2, wallet.getHdWallets().get(0).getAccounts().size());

        Account account = wallet.getHdWallets().get(0)
            .getAccount(wallet.getHdWallets().get(0).getAccounts().size() - 1);

        Assert.assertEquals("xpub6DTFzKMsjf1Tt9KwHMYnQxMLGuVRcobDZdzDuhtc6xfvafsBFqsBS4RNM54kdJs9zK8RKkSbjSbwCeUJjxiySaBKTf8dmyXgUgVnFY7yS9x", account.getXpub());
        Assert.assertEquals("xprv9zTuaopyuHTAffFUBL1n3pQbisewDLsNCR4d7KUzYd8whsY2iJYvtG6tVp1c3jRU4euNj3qdb6wCrmCwg1JRPfPghmH3hJ5ubRJVmqMGwyy", account.getXpriv());
    }

    @Test(expected = DecryptionException.class)
    public void addAccount_doubleEncryptionError() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        Assert.assertEquals(1, wallet.getHdWallets().get(0).getAccounts().size());
        wallet.addAccount(0, "Some Label","hello");
    }

    @Test
    public void addAccount_doubleEncrypted() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_7.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        Assert.assertEquals(2, wallet.getHdWallets().get(0).getAccounts().size());
        wallet.addAccount(0, "Some Label","hello");
        Assert.assertEquals(3, wallet.getHdWallets().get(0).getAccounts().size());

        Account account = wallet.getHdWallets().get(0)
            .getAccount(wallet.getHdWallets().get(0).getAccounts().size() - 1);

        Assert.assertEquals("xpub6DEe2bJAU7GbUw3HDGPUY9c77mUcP9xvAWEhx9GReuJM9gppeGxHqBcaYAfrsyY8R6cfVRsuFhi2PokQFYLEQBVpM8p4MTLzEHpVu4SWq9a", account.getXpub());

        //Private key will be encrypted
        String decryptedXpriv = DoubleEncryptionFactory.decrypt(
            account.getXpriv(), wallet.getSharedKey(), "hello",
            wallet.getOptions().getPbkdf2Iterations());
        Assert.assertEquals("xprv9zFHd5mGdjiJGSxp7ErUB1fNZje7yhF4oHK79krp6ZmNGtVg6je3HPJ6gueSWrVR9oqdqriu2DcshvTfSRu6PXyWiAbP8n6S7DVWEpu5kAE", decryptedXpriv);
    }

    @Test
    public void addLegacyAddress()
        throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        Assert.assertEquals(0, wallet.getLegacyAddressList().size());
        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, wallet.getLegacyAddressList().size());

        LegacyAddress address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        Assert.assertNotNull(address.getPrivateKey());
        Assert.assertNotNull(address.getAddress());

        Assert.assertEquals("1", address.getAddress().substring(0, 1));
    }

    @Test
    public void addLegacyAddress_doubleEncrypted()
        throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        Assert.assertEquals(19, wallet.getLegacyAddressList().size());
        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", "hello");
        Assert.assertEquals(20, wallet.getLegacyAddressList().size());

        LegacyAddress address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        Assert.assertNotNull(address.getPrivateKey());
        Assert.assertNotNull(address.getAddress());

        Assert.assertEquals("==", address.getPrivateKey().substring(address.getPrivateKey().length() - 2));
        Assert.assertEquals("1", address.getAddress().substring(0, 1));
    }

    @Test
    public void setKeyForLegacyAddress()
        throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", null);

        LegacyAddress address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        ECKey ecKey = DeterministicKey.fromPrivate(Base58.decode(address.getPrivateKey()));

        wallet.setKeyForLegacyAddress(ecKey,null);
    }

    @Test(expected = NoSuchAddressException.class)
    public void setKeyForLegacyAddress_NoSuchAddressException()
        throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", null);

        LegacyAddress address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        //Try to set address key with ECKey not found in available addresses.
        ECKey ecKey = new ECKey();
        wallet.setKeyForLegacyAddress(ecKey,null);
    }

    @Test
    public void setKeyForLegacyAddress_doubleEncrypted()
        throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", "hello");

        LegacyAddress address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        final String decryptedOriginalPrivateKey = AESUtil
            .decrypt(address.getPrivateKey(), wallet.getSharedKey()+"hello",
                wallet.getOptions().getPbkdf2Iterations());

        //Remove private key so we can set it again
        address.setPrivateKey(null);

        //Same key for created address, but unencrypted
        ECKey ecKey = DeterministicKey.fromPrivate(Base58.decode(decryptedOriginalPrivateKey));

        //Set private key
        wallet.setKeyForLegacyAddress(ecKey,"hello");

        //Get new set key
        address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);
        String decryptedSetPrivateKey = AESUtil
            .decrypt(address.getPrivateKey(), wallet.getSharedKey()+"hello",
                wallet.getOptions().getPbkdf2Iterations());

        //Original private key must match newly set private key (unencrypted)
        Assert.assertEquals(decryptedOriginalPrivateKey, decryptedSetPrivateKey);
    }

    @Test(expected = DecryptionException.class)
    public void setKeyForLegacyAddress_DecryptionException()
        throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", "hello");

        LegacyAddress address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        final String decryptedOriginalPrivateKey = AESUtil
            .decrypt(address.getPrivateKey(), wallet.getSharedKey()+"hello",
                wallet.getOptions().getPbkdf2Iterations());

        //Remove private key so we can set it again
        address.setPrivateKey(null);

        //Same key for created address, but unencrypted
        ECKey ecKey = DeterministicKey.fromPrivate(Base58.decode(decryptedOriginalPrivateKey));

        //Set private key
        wallet.setKeyForLegacyAddress(ecKey,"bogus");
    }

    @Test
    public void decryptHDWallet() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        wallet.decryptHDWallet(0, "hello");
    }

    @Test(expected = DecryptionException.class)
    public void decryptHDWallet_DecryptionException() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        wallet.decryptHDWallet(0, "bogus");
    }

    @Test
    public void getMasterKey() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        wallet.decryptHDWallet(0, "hello");
        Assert.assertEquals("4NPYyXS5fhyoTHgDPt81cQ4838j1tRwmeRbK8pGLB1Xg",
            Base58.encode(wallet.getHdWallets().get(0).getMasterKey().getPrivKeyBytes()));
    }

    @Test(expected = DecryptionException.class)
    public void getMasterKey_DecryptionException() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        wallet.decryptHDWallet(0, "bogus");
        wallet.getHdWallets().get(0).getMasterKey();
    }

    @Test
    public void getMnemonic() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        wallet.decryptHDWallet(0, "hello");
        Assert.assertEquals("[car, region, outdoor, punch, poverty, shadow, insane, claim, one, whisper, learn, alert]",
            wallet.getHdWallets().get(0).getMnemonic().toString());
    }

    @Test(expected = DecryptionException.class)
    public void getMnemonic_DecryptionException() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        wallet.decryptHDWallet(0, "bogus");
        wallet.getHdWallets().get(0).getMnemonic().toString();
    }

    @Test
    public void getHDKeysForSigning() throws Exception{
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        Wallet wallet = Wallet.fromJson(body);

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        uri = getClass().getClassLoader().getResource("wallet_body_1_account1_unspent.txt").toURI();
        body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(body);

        Payment payment = new Payment();

        long spendAmount = 80200l + 70000l + 60000l + 50000l + 40000l + 30000l + 20000l + 10000l - Payment.DUST.longValue();
        long feeManual = Payment.DUST.longValue();

        SpendableUnspentOutputs paymentBundle = payment
            .getSpendableCoins(unspentOutputs, BigInteger.valueOf(spendAmount - feeManual),
                BigInteger.valueOf(30000L));

        wallet.decryptHDWallet(0, "hello");
        List<ECKey> keyList = wallet.getHdWallets().get(0)
            .getHDKeysForSigning(wallet.getHdWallets().get(0).getAccount(0), paymentBundle);

        //Contains 5 matching keys for signing
        Assert.assertEquals(5, keyList.size());
    }

    @Test
    public void createNewWallet() throws Exception {

        String label = "HDAccount 1";
        Wallet payload = new Wallet(label);

        Assert.assertEquals(36, payload.getGuid().length());//GUIDs are 36 in length
        Assert.assertEquals(label, payload.getHdWallets().get(0).getAccounts().get(0).getLabel());

        Assert.assertEquals(1, payload.getHdWallets().get(0).getAccounts().size());

        Assert.assertEquals(5000, payload.getOptions().getPbkdf2Iterations());
        Assert.assertEquals(600000, payload.getOptions().getLogoutTime());
        Assert.assertEquals(10000, payload.getOptions().getFeePerKb());
    }

}