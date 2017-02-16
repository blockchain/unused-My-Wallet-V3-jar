package info.blockchain.wallet.payload;

import info.blockchain.MockedResponseTest;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.exceptions.NoSuchAddressException;
import info.blockchain.wallet.exceptions.UnsupportedVersionException;
import info.blockchain.wallet.payload.data2.LegacyAddressBody;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class WalletManagerTest extends MockedResponseTest {

    @After
    public void tearDown() throws Exception {
        WalletManager.getInstance().wipe();
    }

    @Test
    public void getInstance() throws Exception {
        Assert.assertNotNull(WalletManager.getInstance());
    }

    @Test
    public void create() throws Exception {
        WalletManager.getInstance().setTempPassword("SomePassword");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().create("My HDWallet", "name@email.com");
        // TODO: 15/02/2017 tests
    }

    @Test(expected = UnsupportedVersionException.class)
    public void initializeAndDecrypt_unsupported_version() throws Exception {

        WalletManager.getInstance().setTempPassword("SomeTestPassword");

        URI uri = WalletManagerTest.class.getClassLoader()
            .getResource("wallet_v4_unsupported.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)),
            Charset.forName("utf-8"));

        mockInterceptor.setResponseString(walletBase);
        WalletManager.getInstance().initializeAndDecrypt("any_shared_key", "any_guid");
    }

    @Test
    public void initializeAndDecrypt() throws Exception {

        WalletManager.getInstance().setTempPassword("SomeTestPassword");

        URI uri = WalletManagerTest.class.getClassLoader().getResource("wallet_v3_3.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)),
            Charset.forName("utf-8"));

        mockInterceptor.setResponseString(walletBase);
        WalletManager.getInstance().initializeAndDecrypt("any", "any");
    }

    @Test(expected = HDWalletException.class)
    public void save_HDWalletException() throws Exception {
        //Nothing to save
        WalletManager.getInstance().save();
    }

    @Test
    public void save() throws Exception {

        WalletManager.getInstance().setTempPassword("SomePassword");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().create("My HDWallet", "name@email.com");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().save();
    }

    @Test
    public void upgradeV2PayloadToV3_alreadyUpgraded() throws Exception{

        WalletManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().create("My HDWallet", "name@email.com");

        WalletManager.getInstance().upgradeV2PayloadToV3(null, "Label");
    }

    @Test
    public void upgradeV2PayloadToV3() throws Exception{

        WalletManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().create("My HDWallet", "name@email.com");

        // TODO: 15/02/2017 remove hd part

        WalletManager.getInstance().upgradeV2PayloadToV3(null, "Label");
    }

    @Test
    public void addAccount() throws Exception {

        WalletManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(1, WalletManager.getInstance().getWalletBody().getHdWallet().getAccounts().size());
        WalletManager.getInstance().addAccount("Some Label", null);
        Assert.assertEquals(2, WalletManager.getInstance().getWalletBody().getHdWallet().getAccounts().size());
        WalletManager.getInstance().addAccount("Some Label", null);
        Assert.assertEquals(3, WalletManager.getInstance().getWalletBody().getHdWallet().getAccounts().size());

    }

    @Test
    public void addLegacyAddress() throws Exception {

        WalletManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(0, WalletManager.getInstance().getWalletBody().getLegacyAddressList().size());

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        WalletManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, WalletManager.getInstance().getWalletBody().getLegacyAddressList().size());

        mockInterceptor.setResponseString("3e2b33d63ba45320f42d2b1de6d7ebd3ea810c35348927fd34424fe9bc53c07a");
        WalletManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(2, WalletManager.getInstance().getWalletBody().getLegacyAddressList().size());

    }

    @Test
    public void setKeyForLegacyAddress() throws Exception {

        WalletManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(0, WalletManager.getInstance().getWalletBody().getLegacyAddressList().size());

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        WalletManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, WalletManager.getInstance().getWalletBody().getLegacyAddressList().size());

        LegacyAddressBody legacyAddressBody = WalletManager.getInstance().getWalletBody()
            .getLegacyAddressList().get(0);

        ECKey ecKey = DeterministicKey
            .fromPrivate(Base58.decode(legacyAddressBody.getPrivateKey()));


        legacyAddressBody.setPrivateKey(null);
        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().setKeyForLegacyAddress(ecKey,null);
    }

    @Test(expected = NoSuchAddressException.class)
    public void setKeyForLegacyAddress_NoSuchAddressException() throws Exception {

        WalletManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(0, WalletManager.getInstance().getWalletBody().getLegacyAddressList().size());

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        WalletManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, WalletManager.getInstance().getWalletBody().getLegacyAddressList().size());

        LegacyAddressBody legacyAddressBody = WalletManager.getInstance().getWalletBody()
            .getLegacyAddressList().get(0);

        //Try non matching ECKey
        ECKey ecKey = new ECKey();
        legacyAddressBody.setPrivateKey(null);
        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().setKeyForLegacyAddress(ecKey,null);
    }

    @Test
    public void setKeyForLegacyAddress_saveFail_revert() throws Exception {

        WalletManager.getInstance().setTempPassword("MyTestWallet");

        mockInterceptor.setResponseString("MyWallet save successful.");
        WalletManager.getInstance().create("My HDWallet", "name@email.com");

        Assert.assertEquals(0, WalletManager.getInstance().getWalletBody().getLegacyAddressList().size());

        mockInterceptor.setResponseString("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        WalletManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, WalletManager.getInstance().getWalletBody().getLegacyAddressList().size());

        LegacyAddressBody legacyAddressBody = WalletManager.getInstance().getWalletBody()
            .getLegacyAddressList().get(0);

        ECKey ecKey = DeterministicKey
            .fromPrivate(Base58.decode(legacyAddressBody.getPrivateKey()));

        legacyAddressBody.setPrivateKey(null);
        mockInterceptor.setResponseCode(500);
        mockInterceptor.setResponseString("Oops something went wrong");
        WalletManager.getInstance().setKeyForLegacyAddress(ecKey,null);

        //Ensure private key reverted on save fail
        Assert.assertNull(legacyAddressBody.getPrivateKey());
    }
}