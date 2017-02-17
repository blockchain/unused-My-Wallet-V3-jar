package info.blockchain.wallet.payload;

import info.blockchain.MockedResponseTest;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.exceptions.InvalidCredentialsException;
import info.blockchain.wallet.exceptions.NoSuchAddressException;
import info.blockchain.wallet.exceptions.ServerConnectionException;
import info.blockchain.wallet.exceptions.UnsupportedVersionException;
import info.blockchain.wallet.payload.data.LegacyAddress;
import info.blockchain.wallet.payload.data.Wallet;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class PayloadManagerTest extends MockedResponseTest {

    @After
    public void tearDown() throws Exception {
        PayloadManager.getInstance().wipe();
    }

    @Test
    public void getInstance() throws Exception {
        Assert.assertNotNull(PayloadManager.getInstance());
    }

    @Test
    public void create() throws Exception {
        PayloadManager.getInstance().setTempPassword("SomePassword");

        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().create("My HDWallet", "name@email.com");

        Wallet walletBody = PayloadManager.getInstance()
            .getPayload();

        Assert.assertEquals(36, walletBody.getGuid().length());//GUIDs are 36 in length
        Assert.assertEquals("My HDWallet", walletBody.getHdWallet().getAccounts().get(0).getLabel());

        Assert.assertEquals(1, walletBody.getHdWallet().getAccounts().size());

        Assert.assertEquals(5000, walletBody.getOptions().getPbkdf2Iterations());
        Assert.assertEquals(600000, walletBody.getOptions().getLogoutTime());
        Assert.assertEquals(10000, walletBody.getOptions().getFeePerKb());
    }

    @Test(expected = ServerConnectionException.class)
    public void create_ServerConnectionException() throws Exception {
        PayloadManager.getInstance().setTempPassword("SomePassword");

        mockInterceptor.setResponseString("Save failed.");
        mockInterceptor.setResponseCode(500);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com");
    }

    @Test
    public void recoverFromMnemonic() throws Exception {
        PayloadManager.getInstance().setTempPassword("SomePassword");

        String mnemonic = "all all all all all all all all all all all all";

        LinkedList<String> xpubs = new LinkedList<>();
        xpubs.add("{\"xpub6BiVtCpG9fQPxnPmHXG8PhtzQdWC2Su4qWu6XW9tpWFYhxydCLJGrWBJZ5H6qTAHdPQ7pQhtpjiYZVZARo14qHiay2fvrX996oEP42u8wZy\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQCgxA541qm9qZ9VrGLScde4zsAMj2d15ewiMysCAnbgvSDSZXhFUdsyA2BfzzMrMFJbC4VSkXbzrXLZRitAmUVURmivxxqMJ\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQDvwDNekCEzAr3gYcoGXEF27bMwSBsCVP3bJYdUZ6m3jhv9vSG7hVxff3VEfnfK4fcMr2YRwfTfHcJwM4ioS6Eiwnrm1wcuf\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQGq7bXBjjf5zyguEXHrmxDu4t7pdTFUtDWD5epi4ecKmWBTMHvPQtRmQnby8gET7ArTzxjL4SNYdD2RYSdjk7fwYeEDMzkce\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQJXDcLwQU1cXECNqaGYb3nNSu1ZEuwFKMXjDbCni6eMhN6rFkdxQsgF1amKAqeLSN63zrYPKJ3GU2ppowBWZSdGBk7QUxgLV\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQNBuKZoKzhzmENDKdCeXQsNVPF2Ynt8rhyYznmPURQNDmnNnX9SYahZ1DVTaNtsh3pJ4b2jKvsZhpv2oVj76YETCGztKJ3LM\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("HDWallet successfully synced with server");
        mockInterceptor.setResponseStringList(xpubs);

        PayloadManager.getInstance().recoverFromMnemonic(mnemonic, "My HDWallet", "name@email.com");

        Wallet walletBody = PayloadManager.getInstance()
            .getPayload();

        Assert.assertEquals(36, walletBody.getGuid().length());//GUIDs are 36 in length
        Assert.assertEquals("My HDWallet", walletBody.getHdWallet().getAccounts().get(0).getLabel());

        Assert.assertEquals(10, walletBody.getHdWallet().getAccounts().size());

        Assert.assertEquals(5000, walletBody.getOptions().getPbkdf2Iterations());
        Assert.assertEquals(600000, walletBody.getOptions().getLogoutTime());
        Assert.assertEquals(10000, walletBody.getOptions().getFeePerKb());
    }

    @Test(expected = ServerConnectionException.class)
    public void recoverFromMnemonic_ServerConnectionException() throws Exception {
        PayloadManager.getInstance().setTempPassword("SomePassword");

        String mnemonic = "all all all all all all all all all all all all";

        LinkedList<String> xpubs = new LinkedList<>();
        xpubs.add("{\"xpub6BiVtCpG9fQPxnPmHXG8PhtzQdWC2Su4qWu6XW9tpWFYhxydCLJGrWBJZ5H6qTAHdPQ7pQhtpjiYZVZARo14qHiay2fvrX996oEP42u8wZy\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQCgxA541qm9qZ9VrGLScde4zsAMj2d15ewiMysCAnbgvSDSZXhFUdsyA2BfzzMrMFJbC4VSkXbzrXLZRitAmUVURmivxxqMJ\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQDvwDNekCEzAr3gYcoGXEF27bMwSBsCVP3bJYdUZ6m3jhv9vSG7hVxff3VEfnfK4fcMr2YRwfTfHcJwM4ioS6Eiwnrm1wcuf\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQGq7bXBjjf5zyguEXHrmxDu4t7pdTFUtDWD5epi4ecKmWBTMHvPQtRmQnby8gET7ArTzxjL4SNYdD2RYSdjk7fwYeEDMzkce\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQJXDcLwQU1cXECNqaGYb3nNSu1ZEuwFKMXjDbCni6eMhN6rFkdxQsgF1amKAqeLSN63zrYPKJ3GU2ppowBWZSdGBk7QUxgLV\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQNBuKZoKzhzmENDKdCeXQsNVPF2Ynt8rhyYznmPURQNDmnNnX9SYahZ1DVTaNtsh3pJ4b2jKvsZhpv2oVj76YETCGztKJ3LM\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("Save failed");
        mockInterceptor.setResponseStringList(xpubs);

        //checking if xpubs has txs succeeds but then savinf fails
        LinkedList<Integer> codes = new LinkedList<>();
        codes.add(200);
        codes.add(200);
        codes.add(200);
        codes.add(200);
        codes.add(200);
        codes.add(200);
        codes.add(200);
        codes.add(200);
        codes.add(200);
        codes.add(200);
        codes.add(500);
        mockInterceptor.setResponseCodeList(codes);

        PayloadManager.getInstance().recoverFromMnemonic(mnemonic, "My HDWallet", "name@email.com");

        Wallet walletBody = PayloadManager.getInstance()
            .getPayload();

        Assert.assertEquals(36, walletBody.getGuid().length());//GUIDs are 36 in length
        Assert.assertEquals("My HDWallet", walletBody.getHdWallet().getAccounts().get(0).getLabel());

        Assert.assertEquals(10, walletBody.getHdWallet().getAccounts().size());

        Assert.assertEquals(5000, walletBody.getOptions().getPbkdf2Iterations());
        Assert.assertEquals(600000, walletBody.getOptions().getLogoutTime());
        Assert.assertEquals(10000, walletBody.getOptions().getFeePerKb());
    }

    @Test(expected = UnsupportedVersionException.class)
    public void initializeAndDecrypt_unsupported_version() throws Exception {

        PayloadManager.getInstance().setTempPassword("SomeTestPassword");

        URI uri = getClass().getClassLoader()
            .getResource("wallet_v4_unsupported.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)),
            Charset.forName("utf-8"));

        mockInterceptor.setResponseString(walletBase);
        PayloadManager.getInstance().initializeAndDecrypt("any_shared_key", "any_guid");
    }

    @Test
    public void initializeAndDecrypt() throws Exception {

        PayloadManager.getInstance().setTempPassword("SomeTestPassword");

        URI uri = getClass().getClassLoader().getResource("wallet_v3_3.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)),
            Charset.forName("utf-8"));

        mockInterceptor.setResponseString(walletBase);
        PayloadManager.getInstance().initializeAndDecrypt("any", "any");
    }

    @Test(expected = InvalidCredentialsException.class)
    public void initializeAndDecrypt_invalidGuid() throws Exception {

        PayloadManager.getInstance().setTempPassword("SomeTestPassword");

        URI uri = getClass().getClassLoader().getResource("invalid_guid.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)),
            Charset.forName("utf-8"));

        mockInterceptor.setResponseString(walletBase);
        mockInterceptor.setResponseCode(500);
        PayloadManager.getInstance().initializeAndDecrypt("any", "any");
    }

    @Test(expected = HDWalletException.class)
    public void save_HDWalletException() throws Exception {
        //Nothing to save
        PayloadManager.getInstance().save();
    }

    @Test
    public void save() throws Exception {

        PayloadManager.getInstance().setTempPassword("SomePassword");

        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().create("My HDWallet", "name@email.com");

        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().save();
    }

    @Test
    public void upgradeV2PayloadToV3() throws Exception{
        //Tested in integration tests
    }

    @Test
    public void addAccount() throws Exception {

        PayloadManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getHdWallet().getAccounts().size());
        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().addAccount("Some Label", null);
        Assert.assertEquals(2, PayloadManager.getInstance().getPayload().getHdWallet().getAccounts().size());
        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().addAccount("Some Label", null);
        Assert.assertEquals(3, PayloadManager.getInstance().getPayload().getHdWallet().getAccounts().size());

    }

    @Test
    public void addLegacyAddress() throws Exception {

        PayloadManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("3e2b33d63ba45320f42d2b1de6d7ebd3ea810c35348927fd34424fe9bc53c07a");
        responseList.add("MyWallet save successful");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(2, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

    }

    @Test
    public void setKeyForLegacyAddress() throws Exception {

        PayloadManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        LegacyAddress legacyAddressBody = PayloadManager.getInstance().getPayload()
            .getLegacyAddressList().get(0);

        ECKey ecKey = DeterministicKey
            .fromPrivate(Base58.decode(legacyAddressBody.getPrivateKey()));


        legacyAddressBody.setPrivateKey(null);
        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().setKeyForLegacyAddress(ecKey,null);
    }

    @Test(expected = NoSuchAddressException.class)
    public void setKeyForLegacyAddress_NoSuchAddressException() throws Exception {

        PayloadManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        LegacyAddress legacyAddressBody = PayloadManager.getInstance().getPayload()
            .getLegacyAddressList().get(0);

        //Try non matching ECKey
        ECKey ecKey = new ECKey();
        legacyAddressBody.setPrivateKey(null);
        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().setKeyForLegacyAddress(ecKey,null);
    }

    @Test
    public void setKeyForLegacyAddress_saveFail_revert() throws Exception {

        PayloadManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        LegacyAddress legacyAddressBody = PayloadManager.getInstance().getPayload()
            .getLegacyAddressList().get(0);

        ECKey ecKey = DeterministicKey
            .fromPrivate(Base58.decode(legacyAddressBody.getPrivateKey()));

        legacyAddressBody.setPrivateKey(null);
        mockInterceptor.setResponseCode(500);
        mockInterceptor.setResponseString("Oops something went wrong");
        PayloadManager.getInstance().setKeyForLegacyAddress(ecKey,null);

        //Ensure private key reverted on save fail
        Assert.assertNull(legacyAddressBody.getPrivateKey());
    }
}