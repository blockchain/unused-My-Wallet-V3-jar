package info.blockchain.wallet.payload;

import info.blockchain.MockedResponseTest;
import info.blockchain.api.data.MultiAddress;
import info.blockchain.api.data.Transaction.Direction;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.exceptions.InvalidCredentialsException;
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

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("MyWallet save successful.");
        //Responses for multi address, 'All' and individual xpub
        responseList.add("{}");//multiaddress responses - not testing this so can be empty.
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "SomePassword");

        Wallet walletBody = PayloadManager.getInstance()
            .getPayload();

        Assert.assertEquals(36, walletBody.getGuid().length());//GUIDs are 36 in length
        Assert.assertEquals("My HDWallet", walletBody.getHdWallets().get(0).getAccounts().get(0).getLabel());

        Assert.assertEquals(1, walletBody.getHdWallets().get(0).getAccounts().size());

        Assert.assertEquals(5000, walletBody.getOptions().getPbkdf2Iterations());
        Assert.assertEquals(600000, walletBody.getOptions().getLogoutTime());
        Assert.assertEquals(10000, walletBody.getOptions().getFeePerKb());
    }

    @Test(expected = ServerConnectionException.class)
    public void create_ServerConnectionException() throws Exception {

        mockInterceptor.setResponseString("Save failed.");
        mockInterceptor.setResponseCode(500);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "SomePassword");
    }

    @Test
    public void recoverFromMnemonic() throws Exception {

        String mnemonic = "all all all all all all all all all all all all";

        LinkedList<String> responseList = new LinkedList<>();
        //Responses for checking how many accounts to recover
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "balance/wallet_all_balance_1.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "balance/wallet_all_balance_2.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "balance/wallet_all_balance_3.txt").toURI())), Charset.forName("utf-8")));
        responseList.add("HDWallet successfully synced with server");

        //responses for initializing multi address
        responseList.add("{}");//multiaddress responses - not testing this so can be empty.
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);

        PayloadManager.getInstance().recoverFromMnemonic(mnemonic, "My HDWallet", "name@email.com", "SomePassword");

        Wallet walletBody = PayloadManager.getInstance()
            .getPayload();

        Assert.assertEquals(36, walletBody.getGuid().length());//GUIDs are 36 in length
        Assert.assertEquals("My HDWallet", walletBody.getHdWallets().get(0).getAccounts().get(0).getLabel());
        Assert.assertEquals("0660cc198330660cc198330660cc1983", walletBody.getHdWallets().get(0).getSeedHex());

        Assert.assertEquals(10, walletBody.getHdWallets().get(0).getAccounts().size());

        Assert.assertEquals(5000, walletBody.getOptions().getPbkdf2Iterations());
        Assert.assertEquals(600000, walletBody.getOptions().getLogoutTime());
        Assert.assertEquals(10000, walletBody.getOptions().getFeePerKb());
    }

    @Test(expected = ServerConnectionException.class)
    public void recoverFromMnemonic_ServerConnectionException() throws Exception {

        String mnemonic = "all all all all all all all all all all all all";

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "balance/wallet_all_balance_1.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "balance/wallet_all_balance_2.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "balance/wallet_all_balance_3.txt").toURI())), Charset.forName("utf-8")));
        responseList.add("Save failed");
        mockInterceptor.setResponseStringList(responseList);

        //checking if xpubs has txs succeeds but then savinf fails
        LinkedList<Integer> codes = new LinkedList<>();
        codes.add(200);
        codes.add(200);
        codes.add(200);
        codes.add(500);
        mockInterceptor.setResponseCodeList(codes);

        PayloadManager.getInstance().recoverFromMnemonic(mnemonic, "My HDWallet", "name@email.com", "SomePassword");

        Wallet walletBody = PayloadManager.getInstance()
            .getPayload();

        Assert.assertEquals(36, walletBody.getGuid().length());//GUIDs are 36 in length
        Assert.assertEquals("My HDWallet", walletBody.getHdWallets().get(0).getAccounts().get(0).getLabel());
        Assert.assertEquals("0660cc198330660cc198330660cc1983", walletBody.getHdWallets().get(0).getSeedHex());

        Assert.assertEquals(10, walletBody.getHdWallets().get(0).getAccounts().size());

        Assert.assertEquals(5000, walletBody.getOptions().getPbkdf2Iterations());
        Assert.assertEquals(600000, walletBody.getOptions().getLogoutTime());
        Assert.assertEquals(10000, walletBody.getOptions().getFeePerKb());
    }

    @Test(expected = UnsupportedVersionException.class)
    public void initializeAndDecrypt_unsupported_version() throws Exception {

        URI uri = getClass().getClassLoader()
            .getResource("wallet_v4_unsupported.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)),
            Charset.forName("utf-8"));

        mockInterceptor.setResponseString(walletBase);
        PayloadManager.getInstance().initializeAndDecrypt("any_shared_key", "any_guid", "SomeTestPassword");
    }

    @Test
    public void initializeAndDecrypt() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_v3_3.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)),
            Charset.forName("utf-8"));

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add(walletBase);
        responseList.add("{}");//multiaddress responses - not testing this so can be empty.
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().initializeAndDecrypt("any", "any", "SomeTestPassword");
    }

    @Test(expected = InvalidCredentialsException.class)
    public void initializeAndDecrypt_invalidGuid() throws Exception {

        URI uri = getClass().getClassLoader().getResource("invalid_guid.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)),
            Charset.forName("utf-8"));

        mockInterceptor.setResponseString(walletBase);
        mockInterceptor.setResponseCode(500);
        PayloadManager.getInstance().initializeAndDecrypt("any", "any", "SomeTestPassword");
    }

    @Test(expected = HDWalletException.class)
    public void save_HDWalletException() throws Exception {
        //Nothing to save
        PayloadManager.getInstance().save();
    }

    @Test
    public void save() throws Exception {

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("MyWallet save successful.");
        responseList.add("{}");//multiaddress responses - not testing this so can be empty.
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "SomePassword");

        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().save();
    }

    @Test
    public void upgradeV2PayloadToV3() throws Exception{
        //Tested in integration tests
    }

    @Test
    public void addAccount() throws Exception {

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("MyWallet save successful.");
        responseList.add("{}");//multiaddress responses - not testing this so can be empty.
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getHdWallets().get(0).getAccounts().size());

        responseList = new LinkedList<>();
        responseList.add("MyWallet save successful");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addAccount("Some Label", null);
        Assert.assertEquals(2, PayloadManager.getInstance().getPayload().getHdWallets().get(0).getAccounts().size());

        responseList = new LinkedList<>();
        responseList.add("MyWallet save successful");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addAccount("Some Label", null);
        Assert.assertEquals(3, PayloadManager.getInstance().getPayload().getHdWallets().get(0).getAccounts().size());

    }

    @Test
    public void addLegacyAddress() throws Exception {

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("MyWallet save successful.");
        responseList.add("{}");//multiaddress responses - not testing this so can be empty.
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("3e2b33d63ba45320f42d2b1de6d7ebd3ea810c35348927fd34424fe9bc53c07a");
        responseList.add("MyWallet save successful");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(2, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

    }

    @Test
    public void setKeyForLegacyAddress() throws Exception {

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("MyWallet save successful.");
        responseList.add("{}");//multiaddress responses - not testing this so can be empty.
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
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

    @Test
    public void setKeyForLegacyAddress_NoSuchAddressException() throws Exception {

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("MyWallet save successful.");
        responseList.add("{}");//multiaddress responses - not testing this so can be empty.
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        LegacyAddress existingLegacyAddressBody = PayloadManager.getInstance().getPayload()
            .getLegacyAddressList().get(0);

        //Try non matching ECKey
        ECKey ecKey = new ECKey();

        responseList = new LinkedList<>();
        responseList.add("MyWallet save successful");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);

        LegacyAddress newlyAdded = PayloadManager.getInstance()
            .setKeyForLegacyAddress(ecKey, null);

        //Ensure new address is created if no match found
        Assert.assertNotNull(newlyAdded);
        Assert.assertNotNull(newlyAdded.getPrivateKey());
        Assert.assertNotNull(newlyAdded.getAddress());
        Assert.assertNotEquals(existingLegacyAddressBody.getPrivateKey(), newlyAdded.getPrivateKey());
        Assert.assertNotEquals(existingLegacyAddressBody.getAddress(), newlyAdded.getAddress());
    }

    @Test
    public void setKeyForLegacyAddress_saveFail_revert() throws Exception {

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("MyWallet save successful.");
        responseList.add("{}");//multiaddress responses - not testing this so can be empty.
        responseList.add("{}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        responseList.add("{}");
        responseList.add("{}");
        responseList.add("{}");
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

    @Test
    public void getMultiAddress() throws Exception {

        URI uri = getClass().getClassLoader().getResource("wallet_v3_4.txt").toURI();
        String walletBase = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add(walletBase);
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m1.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m2.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m3.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m4.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m5.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m6.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m7.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m8.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m9.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m10.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m11.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m12.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m13.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m14.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m15.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m16.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m17.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m18.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m19.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m20.txt").toURI())), Charset.forName("utf-8")));
        responseList.add(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(
            "multiaddress/wallet_v3_4_m21.txt").toURI())), Charset.forName("utf-8")));

        mockInterceptor.setResponseStringList(responseList);

        PayloadManager.getInstance().initializeAndDecrypt("04ada428-151b-4fa8-95b5-10e4447fd1c1", "e1062383-14f4-4e9c-818e-d9cce739b57f", "MyTestWallet");

        //'All' accounts balance and transactions
        MultiAddress all = PayloadManager.getInstance()
            .getMultiAddress(PayloadManager.MULTI_ADDRESS_ALL);
        Assert.assertEquals(235077,all.getWallet().getFinalBalance().longValue());
        Assert.assertEquals(294,all.getWallet().getNTx());

        MultiAddress account1 = PayloadManager.getInstance()
            .getMultiAddress("xpub6CRaPB9182vBdT99Aj435Jy9hdqp6NPQUvf6xfz2ghScwNf4jwX6T5BXrJGknn3VorS3RAopzxhWfHKdcaHxSoYq3XH4kfByKQ9p9Zjbz4p");
        Assert.assertEquals(0,account1.getAddresses().get(0).getFinalBalance().longValue());
        Assert.assertEquals(2,account1.getAddresses().get(0).getNTx());

        MultiAddress account2 = PayloadManager.getInstance()
            .getMultiAddress("xpub6CRaPB9182vBgWDgyqx8DFJwMQ3gbrDW2gk3puMPcqmBxE84pNjoaYqFqmK7gGVzUPxwuHnE4rXsoboQTnvH5utSAtSw9GBnJb6g7WhzQ1z");
        Assert.assertEquals(0,account2.getAddresses().get(0).getFinalBalance().longValue());
        Assert.assertEquals(0,account2.getAddresses().get(0).getNTx());

        MultiAddress address1 = PayloadManager.getInstance()
            .getMultiAddress("1Nqz4vjxdk4sy6uGNqnXaC49QMt2aDNt2Q");
        Assert.assertEquals(0,address1.getAddresses().get(0).getFinalBalance().longValue());
        Assert.assertEquals(144,address1.getAddresses().get(0).getNTx());

        MultiAddress address2 = PayloadManager.getInstance()
            .getMultiAddress("18HuxnpyuhAUYiCSSiLhv5589ebJuSSU5A");
        Assert.assertEquals(0,address2.getAddresses().get(0).getFinalBalance().longValue());
        Assert.assertEquals(44,address2.getAddresses().get(0).getNTx());

        MultiAddress address4 = PayloadManager.getInstance()
            .getMultiAddress("15CuVHzfZsPHpfQ1GJFzZ93LGe7ZrHdBb8");
        Assert.assertEquals(85100,address4.getAddresses().get(0).getFinalBalance().longValue());
        Assert.assertEquals(18,address4.getAddresses().get(0).getNTx());

        MultiAddress address5 = PayloadManager.getInstance()
            .getMultiAddress("19hxgds7jLo68q4qXLHtTP2qWFxZBKYNfA");
        Assert.assertEquals(86977,address5.getAddresses().get(0).getFinalBalance().longValue());
        Assert.assertEquals(24,address5.getAddresses().get(0).getNTx());

        Assert.assertEquals(51979, address5.getTxs().get(0).getResult().longValue());
        Assert.assertEquals(Direction.RECEIVED, address5.getTxs().get(0).getDirection());

        Assert.assertEquals(-21000, address5.getTxs().get(1).getResult().longValue());
        Assert.assertEquals(Direction.SENT, address5.getTxs().get(1).getDirection());

        Assert.assertEquals(-34094, address5.getTxs().get(2).getResult().longValue());
        Assert.assertEquals(Direction.SENT, address5.getTxs().get(2).getDirection());

        Assert.assertEquals(-112206, address5.getTxs().get(3).getResult().longValue());
        Assert.assertEquals(Direction.SENT, address5.getTxs().get(3).getDirection());

        Assert.assertEquals(546, address5.getTxs().get(4).getResult().longValue());
        Assert.assertEquals(Direction.RECEIVED, address5.getTxs().get(4).getDirection());

        Assert.assertEquals(546, address5.getTxs().get(5).getResult().longValue());
        Assert.assertEquals(Direction.RECEIVED, address5.getTxs().get(5).getDirection());

        Assert.assertEquals(112206, address5.getTxs().get(6).getResult().longValue());
        Assert.assertEquals(Direction.RECEIVED, address5.getTxs().get(6).getDirection());

        Assert.assertEquals(2000, address5.getTxs().get(7).getResult().longValue());
        Assert.assertEquals(Direction.RECEIVED, address5.getTxs().get(7).getDirection());

        Assert.assertEquals(3000, address5.getTxs().get(8).getResult().longValue());
        Assert.assertEquals(Direction.RECEIVED, address5.getTxs().get(8).getDirection());

        Assert.assertEquals(4000, address5.getTxs().get(9).getResult().longValue());
        Assert.assertEquals(Direction.RECEIVED, address5.getTxs().get(9).getDirection());

        Assert.assertEquals(5000, address5.getTxs().get(10).getResult().longValue());
        Assert.assertEquals(Direction.RECEIVED, address5.getTxs().get(10).getDirection());

        Assert.assertEquals(-251741, address5.getTxs().get(11).getResult().longValue());
        Assert.assertEquals(Direction.SENT, address5.getTxs().get(11).getDirection());

        Assert.assertEquals(49440, address5.getTxs().get(12).getResult().longValue());
        Assert.assertEquals(Direction.RECEIVED, address5.getTxs().get(12).getDirection());


        //Transfers
//        Assert.assertEquals(Direction.TRANSFERRED, address5.getTxs().get(13).getDirection());
//        Assert.assertEquals(202301, address5.getTxs().get(13).getResult().longValue());
//        Assert.assertEquals(Direction.TRANSFERRED, address5.getTxs().get(13).getDirection());
//
//        Assert.assertEquals(22101, address5.getTxs().get(14).getResult().longValue());
//        Assert.assertEquals(Direction.TRANSFERRED, address5.getTxs().get(14).getDirection());
//
//        Assert.assertEquals(23457, address5.getTxs().get(15).getResult().longValue());
//        Assert.assertEquals(Direction.TRANSFERRED, address5.getTxs().get(15).getDirection());
//
//        Assert.assertEquals(5027, address5.getTxs().get(16).getResult().longValue());
//        Assert.assertEquals(Direction.TRANSFERRED, address5.getTxs().get(16).getDirection());

    }
}