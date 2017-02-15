//package info.blockchain.wallet;
//
//import info.blockchain.MockedResponseTest;
//import info.blockchain.wallet.exceptions.HDWalletException;
//import info.blockchain.wallet.exceptions.UnsupportedVersionException;
//import info.blockchain.wallet.payload.data.PayloadTest;
//import java.net.URI;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class MyWalletTest extends MockedResponseTest {
//
//    @After
//    public void tearDown() throws Exception {
//        MyWallet.getInstance().wipe();
//    }
//
//    @Test
//    public void getInstance() throws Exception {
//        Assert.assertNotNull(MyWallet.getInstance());
//    }
//
//    @Test
//    public void create() throws Exception {
//        MyWallet.getInstance().setTempPassword("SomePassword");
//
//        mockInterceptor.setResponseString("MyWallet save successful.");
//        MyWallet.getInstance().create("My HDWallet", "name@email.com");
//        Assert.assertNotNull(MyWallet.getInstance().getActiveXpubs().get(0));
//        Assert.assertNotNull(MyWallet.getInstance().getXpubFromAccountIndex(0));
//
//        MyWallet.getInstance().wipe();
//    }
//
//    @Test(expected = UnsupportedVersionException.class)
//    public void initializeAndDecrypt_unsupported_version() throws Exception {
//
//        MyWallet.getInstance().setTempPassword("SomeTestPassword");
//
//        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_v4_unsupported.txt").toURI();
//        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
//
//        mockInterceptor.setResponseString(walletBase);
//        MyWallet.getInstance().initializeAndDecrypt("any_shared_key", "any_guid");
//    }
//
//    @Test
//    public void initializeAndDecrypt() throws Exception {
//
//        MyWallet.getInstance().setTempPassword("SomeTestPassword");
//        String guid = "e5eba801-c8bc-4a64-99ba-094e12a80766";
//        String sharedKey = "db81b289-8cac-46a9-ac0d-0c0658ec0dfa";
//
//        URI uri = PayloadTest.class.getClassLoader().getResource("wallet_v3_3.txt").toURI();
//        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
//
//        mockInterceptor.setResponseString(walletBase);
//        MyWallet.getInstance().initializeAndDecrypt(guid, sharedKey);
//
//        MyWallet.getInstance().printIt();
//    }
//
//    @Test(expected=HDWalletException.class)
//    public void save() throws Exception {
//        MyWallet.getInstance().save();
//    }
//
//    @Test
//    public void validateSecondPassword() throws Exception {
//    }
//
//    @Test
//    public void getDecryptedWallet() throws Exception {
//    }
//
//}