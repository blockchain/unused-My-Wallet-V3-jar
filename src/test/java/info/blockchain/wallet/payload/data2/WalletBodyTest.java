package info.blockchain.wallet.payload.data2;

import info.blockchain.MockedResponseTest;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.NoSuchAddressException;
import info.blockchain.wallet.payload.data.PayloadTest;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class WalletBodyTest extends MockedResponseTest{

    @Test
    public void fromJson_1() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);
        Assert.assertEquals("a09910d9-1906-4ea1-a956-2508c3fe0661", wallet.getGuid());
        Assert.assertEquals("d14f3d2c-f883-40da-87e2-c8448521ee64", wallet.getSharedKey());
        Assert.assertTrue(wallet.isDoubleEncryption());
        Assert.assertEquals("1f7cb884545e89e4083c10522bf8b991e8e13551aa5816110cb9419277fb4652", wallet.getDpasswordhash());

        for(Entry<String, String> item : wallet.getTxNotes().entrySet()){
            Assert.assertEquals("94a4934712fd40f2b91b7be256eacad49a50b850c949313b07046664d24c0e4c", item.getKey());
            Assert.assertEquals("Bought Pizza", item.getValue());
        }

        //Options parsing tested in OptionsBodyTest
        Assert.assertNotNull(wallet.getOptions());

        //HdWallets parsing tested in HdWalletsBodyTest
        Assert.assertNotNull(wallet.getHdWallets());

        //Keys parsing tested in KeysBodyTest
        Assert.assertNotNull(wallet.getLegacyAddressList());

        //AddressBook parsing tested in AddressBookBodyTest
        Assert.assertNotNull(wallet.getAddressBook());
    }

    @Test
    public void fromJson_2() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_2.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);
        Assert.assertEquals("9ebb4d4f-f36e-40d6-9a3e-5a3cca5f83d6", wallet.getGuid());
        Assert.assertEquals("41cf823f-2dcd-4967-88d1-ef9af8689fc6", wallet.getSharedKey());
        Assert.assertFalse(wallet.isDoubleEncryption());
        Assert.assertNull(wallet.getDpasswordhash());

        //Options parsing tested in OptionsBodyTest
        Assert.assertNotNull(wallet.getOptions());

        //Keys parsing tested in KeysBodyTest
        Assert.assertNotNull(wallet.getLegacyAddressList());
    }

    @Test
    public void fromJson_3() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_3.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);
        Assert.assertEquals("2ca9b0e4-6b82-4dae-9fef-e8b300c72aa2", wallet.getGuid());
        Assert.assertEquals("e8553981-b196-47cc-8858-5b0d16284f61", wallet.getSharedKey());
        Assert.assertFalse(wallet.isDoubleEncryption());
        Assert.assertNull(wallet.getDpasswordhash());

        //Options parsing tested in OptionsBodyTest
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

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_4.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);
        Assert.assertEquals("4077b6d9-73b3-4d22-96d4-9f8810fec435", wallet.getGuid());
        Assert.assertEquals("fa1beb37-5836-41d1-9f73-09f292076eb9", wallet.getSharedKey());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);
        String jsonString = wallet.toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(9, jsonObject.keySet().size());
    }

    @Test
    public void validateSecondPassword() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        wallet.validateSecondPassword("hello");
        Assert.assertTrue(true);
    }

    @Test(expected = DecryptionException.class)
    public void validateSecondPassword_fail() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        wallet.validateSecondPassword("bogus");
    }

    @Test
    public void addAccount() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        Assert.assertEquals(1, wallet.getHdWallet().getAccounts().size());
        wallet.addAccount("Some Label",null);
        Assert.assertEquals(2, wallet.getHdWallet().getAccounts().size());

        AccountBody account = wallet.getHdWallet()
            .getAccount(wallet.getHdWallet().getAccounts().size() - 1);

        Assert.assertEquals("xpub6DTFzKMsjf1Tt9KwHMYnQxMLGuVRcobDZdzDuhtc6xfvafsBFqsBS4RNM54kdJs9zK8RKkSbjSbwCeUJjxiySaBKTf8dmyXgUgVnFY7yS9x", account.getXpub());
        Assert.assertEquals("xprv9zTuaopyuHTAffFUBL1n3pQbisewDLsNCR4d7KUzYd8whsY2iJYvtG6tVp1c3jRU4euNj3qdb6wCrmCwg1JRPfPghmH3hJ5ubRJVmqMGwyy", account.getXpriv());
    }

    @Test(expected = DecryptionException.class)
    public void addAccount_doubleEncryptionError() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        Assert.assertEquals(1, wallet.getHdWallet().getAccounts().size());
        wallet.addAccount("Some Label","hello");
    }

    @Test
    public void addAccount_doubleEncrypted() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_7.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        Assert.assertEquals(2, wallet.getHdWallet().getAccounts().size());
        wallet.addAccount("Some Label","hello");
        Assert.assertEquals(3, wallet.getHdWallet().getAccounts().size());

        AccountBody account = wallet.getHdWallet()
            .getAccount(wallet.getHdWallet().getAccounts().size() - 1);

        Assert.assertEquals("xpub6DEe2bJAU7GbUw3HDGPUY9c77mUcP9xvAWEhx9GReuJM9gppeGxHqBcaYAfrsyY8R6cfVRsuFhi2PokQFYLEQBVpM8p4MTLzEHpVu4SWq9a", account.getXpub());

        //Private key will be encrypted
        String decryptedXpriv = DoubleEncryptionFactory.getInstance().decrypt(
            account.getXpriv(), wallet.getSharedKey(), "hello",
            wallet.getOptions().getPbkdf2Iterations());
        Assert.assertEquals("xprv9zFHd5mGdjiJGSxp7ErUB1fNZje7yhF4oHK79krp6ZmNGtVg6je3HPJ6gueSWrVR9oqdqriu2DcshvTfSRu6PXyWiAbP8n6S7DVWEpu5kAE", decryptedXpriv);
    }

    @Test
    public void addLegacyAddress()
        throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        Assert.assertEquals(0, wallet.getLegacyAddressList().size());
        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, wallet.getLegacyAddressList().size());

        LegacyAddressBody address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        Assert.assertNotNull(address.getPrivateKey());
        Assert.assertNotNull(address.getAddressString());

        Assert.assertEquals("1", address.getAddressString().substring(0, 1));
    }

    @Test
    public void addLegacyAddress_doubleEncrypted()
        throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        Assert.assertEquals(19, wallet.getLegacyAddressList().size());
        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", "hello");
        Assert.assertEquals(20, wallet.getLegacyAddressList().size());

        LegacyAddressBody address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        Assert.assertNotNull(address.getPrivateKey());
        Assert.assertNotNull(address.getAddressString());

        Assert.assertEquals("==", address.getPrivateKey().substring(address.getPrivateKey().length() - 2));
        Assert.assertEquals("1", address.getAddressString().substring(0, 1));
    }

    @Test
    public void setKeyForLegacyAddress()
        throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", null);

        LegacyAddressBody address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        ECKey ecKey = DeterministicKey.fromPrivate(Base58.decode(address.getPrivateKey()));

        wallet.setKeyForLegacyAddress(ecKey,null);
    }

    @Test(expected = NoSuchAddressException.class)
    public void setKeyForLegacyAddress_NoSuchAddressException()
        throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", null);

        LegacyAddressBody address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

        //Try to set address key with ECKey not found in available addresses.
        ECKey ecKey = new ECKey();
        wallet.setKeyForLegacyAddress(ecKey,null);
    }

    @Test
    public void setKeyForLegacyAddress_doubleEncrypted()
        throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", "hello");

        LegacyAddressBody address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

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

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        wallet.addLegacyAddress("Some Label", "hello");

        LegacyAddressBody address = wallet.getLegacyAddressList().get(wallet.getLegacyAddressList().size() - 1);

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

//    @Test
//    public void createNewWallet() throws Exception {
//
//        String label = "HDAccount 1";
//        WalletBody payload = new WalletBody(label);
//
//        Assert.assertEquals(payload.getGuid().length(), 36);//GUIDs are 36 in length
//        Assert.assertEquals(payload.getHdWallet().getAccounts().get(0).getLabel(), label);
//        System.out.println(payload.toJson());
//    }
//
//    @Test
//    public void recoverFromMnemonic() throws Exception {
//
//        String mnemonic = "all all all all all all all all all all all all";
//
//        LinkedList<String> xpubs = new LinkedList<>();
//        xpubs.add("{\"xpub6BiVtCpG9fQPxnPmHXG8PhtzQdWC2Su4qWu6XW9tpWFYhxydCLJGrWBJZ5H6qTAHdPQ7pQhtpjiYZVZARo14qHiay2fvrX996oEP42u8wZy\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BiVtCpG9fQQCgxA541qm9qZ9VrGLScde4zsAMj2d15ewiMysCAnbgvSDSZXhFUdsyA2BfzzMrMFJbC4VSkXbzrXLZRitAmUVURmivxxqMJ\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BiVtCpG9fQQDvwDNekCEzAr3gYcoGXEF27bMwSBsCVP3bJYdUZ6m3jhv9vSG7hVxff3VEfnfK4fcMr2YRwfTfHcJwM4ioS6Eiwnrm1wcuf\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BiVtCpG9fQQGq7bXBjjf5zyguEXHrmxDu4t7pdTFUtDWD5epi4ecKmWBTMHvPQtRmQnby8gET7ArTzxjL4SNYdD2RYSdjk7fwYeEDMzkce\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BiVtCpG9fQQJXDcLwQU1cXECNqaGYb3nNSu1ZEuwFKMXjDbCni6eMhN6rFkdxQsgF1amKAqeLSN63zrYPKJ3GU2ppowBWZSdGBk7QUxgLV\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BiVtCpG9fQQNBuKZoKzhzmENDKdCeXQsNVPF2Ynt8rhyYznmPURQNDmnNnX9SYahZ1DVTaNtsh3pJ4b2jKvsZhpv2oVj76YETCGztKJ3LM\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("HDWallet successfully synced with server");
//        mockInterceptor.setResponseStringList(xpubs);
//
//        String label = "HDAccount 1";
//        WalletBody payload = WalletBody.recoverFromMnemonic(mnemonic, label);
//
//        Assert.assertEquals(payload.getGuid().length(), 36);//GUIDs are 36 in length
//        Assert.assertEquals(payload.getHdWallet().getAccounts().get(0).getLabel(), label);
//        System.out.println(payload.toJson());
//    }
//
//    @Test
//    public void recoverFromMnemonic_passphrase() throws Exception {
//
//        String mnemonic = "all all all all all all all all all all all all";
//
//        LinkedList<String> xpubs = new LinkedList<>();
//        xpubs.add("{\"xpub6BvvF1nwmp518uYADntSdRPYGiDBzywww8LYFDEQ7cVC2ZxxcDf5NARWeFAbkLewsAMjmp2zfVGo7uv4uP9jHmczH2i4Lq1RioMGRmZ6myC\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BvvF1nwmp51A6sqV9YTcTGKmWorM48PmZdSXEYgG9pQffDsUavLdPz14RX5tTghiGfApJLqYdv9ramj9agke9o1uKYLesYp6rPKExDmCFX\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BvvF1nwmp51CapAefmDYrKWeGC2Y96TcGtB6BTfiTJezHLjBxgsWdKRvWWChGAhWPjdRjSUsDeEgnSar2xjenixNArkytRU2heAWr3HmQ5\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BvvF1nwmp51G75mNUQLmQZm8r3CXBFJChJt6fvoURVS1Nz1jCVN6Nf5nMUfDuT53X8uAXjAX3eHJRPWcpDYMVPwzv1hpMAJKvKQVMefiRJ\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BvvF1nwmp51Gpj1eAidXtRpoq6AUwzZ3L2uv49oWnMQiW9KZ42UYrrM3fHoCyidHzAY14GRrZ8fSS2JZroAEXD5bqiLvjGDNGYbuMCa6vi\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BvvF1nwmp51MoY8LZ6RqZ6xc9PE5mASd2jpTGARe61HwscsK1tVLF5xJFf1QKnNP2T5YAKDyrK2WGAZS1p5aD9EuYhqC53EFYC7UpnYnz5\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BvvF1nwmp51N9UVeokUscF6vwT8TN35TSxQmW8GSJPgj7NQwUKrR9rZvug2KLeZf4SnviBmmqgtaWJstuMT18bcNpPttrhrBEWptdYHGcF\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BvvF1nwmp51RG4LdnpS4wW4q7hyjPfujhQ6iWQDKdQPBvjaYQz9CbJD6zYae1M9FfEFCCb2CyjcwPKj2qzQAyYNq3XM5rn1XNanTB8Mc3p\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BvvF1nwmp51V9WWEKjQRhqKYmuHj5gkjCr45c4BUAiLkS5y33zcQT39ZnXztG4NSwF98mo4DP1rTyugJsLbFKxDNQCXJegHoULicosyjMG\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("{\"xpub6BvvF1nwmp51XdWV7XBeBgcErsvkJ6f79vzppG278gJ4MPfJ9G5mPqaS8w1zWVyhVrXj3nnr2BSaLcNxHVM548go7UvS3MV1uynsi813YrY\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
//        xpubs.add("HDWallet successfully synced with server");
//        mockInterceptor.setResponseStringList(xpubs);
//
//        String label = "HDAccount 1";
//        WalletBody payload = WalletBody.recoverFromMnemonic(mnemonic, "somePassphrase", label);
//
//        Assert.assertEquals(payload.getGuid().length(), 36);//GUIDs are 36 in length
//        Assert.assertEquals(payload.getHdWallet().getAccounts().get(0).getLabel(), label);
//        System.out.println(payload.toJson());
//    }

//    @Test
//    public void decryptAndInitializeWallet() throws Exception {
//
//        String guid = "e5eba801-c8bc-4a64-99ba-094e12a80766";
//        String sharedKey = "db81b289-8cac-46a9-ac0d-0c0658ec0dfa";
//        String password = "SomeTestPassword";
//
//        mockInterceptor.setResponseString("{\"extra_seed\":\"09ab6f898f2ffd630c9ce5d80feb963812e3e8623e897335bc345bb572650cc2e32458e4897e45984a3199c95c4087bef53a5345275be39622ed88f5c96c114e\",\"payload\":\"{\\\"pbkdf2_iterations\\\":7520,\\\"version\\\":3,\\\"payload\\\":\\\"mKN78QPyI\\/ipXzKshUNiEoieTpHJWrVo\\/E09To8wbg0AX37bVIzK5XbjyN\\/ZGcu7wOaboIdufwG3dK0tnH5CgAukyn+08Onl0GD8OM6iJ8NKMBFzadkPAW\\/LrQqzht1p3AOH2ALixg4nDZrWKidite7a04UvHx7RbLXNDmvRrsUgJSuIIQJ7TGIWWMy8C7fCvh+WEYHDxIgjUe6E4e9yQ8p6JpahFi5nn10ol1Pt9HBcgQfTWuFwGozz00aQUHQYOq7X57oxFYBwGor5HyjIgbE3lxAz\\/9yR2tOpA\\/ZFLPXbVppBTBOu6sR6blMtp7ZIOqYC\\/NXGYyVwlHlTZfZyRE6E7B1zRnWSS9NW\\/L2wHMaOZ6HpoyAtBUQo+Pu\\/ZiEPScAw4xvs8RSmNHGgDTxW4LGgUGDdOfc1Nivm9kAbYWZ11HOS2glLBxUkPpGCeUeQeB4tEd6q7BEAihWSv2sPETIJ21X2Dt6IdSQRAVfm4q8IGlBvSYbCEI1Ay2vFe2MdaZSAhM9ndhOcU3VMJe0+sHiI5oqdLhogPCpgVm0pnSVq5MerG7suqBd2KQgBczRp6ANkSY+O96lEKftSHV7N2UO3jIGiX5H\\/Txh61kJ00q79HSUcysUU9apuBipdYQUeNNYlDrUZLACP2U+Re7+GG8O5atFupDkJgYsLNkaZZpezPtu1gmp6DLxa2qtBHt2tzm5N0BlYCA\\/xUeiqRGP8B3Ea5Om\\/hS66zDVuqeoIn6NG060K9QLZ4OARSrDxStdY9Zagj6hQwy27+pCnSlAgBXHbecbsSO81irQz6AA8jcsnGgjKtItPghTYJTsaY9g3ZqDWF17OK2TNuZ1++aDbU2m0st3b0LDosGDjgMhc6HK0d9zvBvNsjk5Fd4FYjDWk7LMnQCDp06TQRT4ByJOqfQKNl2Pgyzp+QDZPljm+q8aTgASSSzuFmqobpXHEZO+\\/y0X9+TV43Pw+tXtXwVaLEFiETqoq6s7Kd4Tt3aML6zJ2UJfu7q2YcUqq8i9k+HDGpr0dcBGFSQt6DcUTdwddxzErgg6pRPTUP8y10rLy8Zd8O2A4WvWF88Dynv8ilNehwi5q47\\/pJNPiPABR1yiuwLw9\\/DJ7PIhzZGoJy62cJZ4nneJ0PwIPPtWC1nt+BrUqpP94uFOhAy2eP9bbXOpHshn82+hiP4ndkjeQvGVKJbfdSzZTd8i7UdzWm0bXhfdN3X+iAU6XwA09gUu0DtfN6sT3s6vanoSHB+S+ydZ804EZ9VuhKwnrWoWsLRkX\\/X5hPEPwW4hWlvxpTabKXO5XHQGi7A5l3weyKer9v3HDm1Q5sw\\/A8aLlJCYANjAkvRqp\\/ASTLeaKTt2S+lhuRbHZ6kTTfvWe0Injzejo3ElkbjQFpKyM4KAo2V2rU8uWWJoksAItKYTqDhqvddRz+VL3VPwLC4LWFpko1+87FVRhDApxNwNR0Ax+f3DfIA66K71TWZXDd7knCC6ZlUFRrLDsHGqPIBgXqykOX03rCeGAw9A7L7twqJ\\/r9ypy7B0ngv8ZM0Fpe8vyrojDM4tz0UUoepppmNez6I+rcDAk5N3E0c2eg7crxJVJwGapug8EKTF55c\\/jqUMx2xqtya8l8MnQmTIsn+aHeiQa4eHhF2llIfWE2sIalSS2X5fBHrfPm\\/6syCpwqkapLW2i9kb1H1MBgpV0lg6K2HrsriSeerNoM6M=\\\"}\",\"symbol_local\":{\"symbol\":\"$\",\"code\":\"USD\",\"symbolAppearsAfter\":false,\"name\":\"U.S. dollar\",\"local\":true,\"conversion\":100744.50186881},\"payload_checksum\":\"fc631f8434f45c43e7040f1192b6676a8bd49e0fd00fb4848acdc0dcaa665400\",\"war_checksum\":\"d3e3b31c57f823ed\",\"language\":\"en\",\"symbol_btc\":{\"symbol\":\"BTC\",\"code\":\"BTC\",\"symbolAppearsAfter\":true,\"name\":\"Bitcoin\",\"local\":false,\"conversion\":100000000.00000000},\"storage_token\":\"8760ce3a41614a981b2a5bd776bb5603\",\"sync_pubkeys\":false}");
//        Call<ResponseBody> call = WalletApi.fetchWalletData(guid, sharedKey);
//
//        Response<ResponseBody> exe = call.execute();
//
//        String encryptedWalletData = exe.body().string();
//        System.out.println(encryptedWalletData);
//
//        WalletBody.decryptAndInitializeWallet(encryptedWalletData, password);
//    }

}
/*
have a data2 package now (which will need to be renamed along with classses after refactor)
all the 'bodies' are actual wallet payload elements
new HDWalletFactory class (same as WalletFactory class but static)

moved 'payload' package json element classes to a 'data' package for now
the WalletBody class can now create and restore statically - no need for PayloadManager
- continue moving code from payload manager to right places (like decryptAndInitializeWallet)
 */