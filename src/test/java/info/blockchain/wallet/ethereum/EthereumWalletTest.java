package info.blockchain.wallet.ethereum;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDWallet;
import info.blockchain.wallet.bip44.HDWalletFactory;
import info.blockchain.wallet.bip44.HDWalletFactory.Language;
import info.blockchain.wallet.util.MetadataUtil;
import java.math.BigInteger;
import java.util.LinkedList;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.utils.Numeric;

public class EthereumWalletTest extends MockedResponseTest {

    EthereumWallet subject;

    private HDWallet getWallet(String seedHex) throws Exception {
        return HDWalletFactory
            .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(), Language.US,
                seedHex, "", 1);
    }

    private HDWallet getWallet1() throws Exception {
        //bicycle balcony prefer kid flower pole goose crouch century lady worry flavor
        return getWallet("15e23aa73d25994f1921a1256f93f72c");
    }

    private HDWallet getWallet2() throws Exception {
        //radar blur cabbage chef fix engine embark joy scheme fiction master release
        return getWallet("b0a30c7e93a58094d213c4c0aaba22da");
    }

    private HDWallet getWallet3() throws Exception {
        //search present horn clip convince wash motion want sea desert admit increase =jaxx
        return getWallet("c21541b615a2f9eee417b7c1e7740eb9");
    }

    @Test
    public void constructor1() throws Exception {

        HDWallet wallet = getWallet1();

        //Arrange
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = new EthereumWallet(wallet.getMasterKey(),"My Ether Wallet");

        //Assert
        Assert.assertFalse(subject.hasSeen());
        Assert.assertEquals(0, subject.getTxNotes().size());

        Assert.assertEquals("60e2d382449758aab3866585dc69a946e3566bca0eea274b9073cb60da636133",
            EthereumAccount.deriveECKey(wallet.getMasterKey(), 0).getPrivateKeyAsHex());

        Assert.assertTrue(subject.getAccount().getAddress()
            .equalsIgnoreCase("0x14f2BD143692B14D170c34b2eE25EE5FC61e8570"));
    }

    @Test
    public void constructor2() throws Exception {

        HDWallet wallet = getWallet2();

        //Arrange
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = new EthereumWallet(wallet.getMasterKey(),"My Ether Wallet");

        //Assert
        Assert.assertFalse(subject.hasSeen());
        Assert.assertEquals(0, subject.getTxNotes().size());

        Assert.assertEquals("b96e9ccb774cc33213cbcb2c69d3cdae17b0fe4888a1ccd343cbd1a17fd98b18",
            EthereumAccount.deriveECKey(wallet.getMasterKey(), 0).getPrivateKeyAsHex());

        Assert.assertTrue(subject.getAccount().getAddress()
            .equalsIgnoreCase("0xaC39b311DCEb2A4b2f5d8461c1cdaF756F4F7Ae9"));
    }

    @Test
    public void constructor3() throws Exception {

        HDWallet wallet = getWallet3();

        //Arrange
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = new EthereumWallet(wallet.getMasterKey(),"My Ether Wallet");

        //Assert
        Assert.assertFalse(subject.hasSeen());
        Assert.assertEquals(0, subject.getTxNotes().size());

        Assert.assertEquals("6e1ae089604577d31f25617297e4f50ef1b06376d7b04419c7e82e2507927857",
            EthereumAccount.deriveECKey(wallet.getMasterKey(), 0).getPrivateKeyAsHex());

        Assert.assertTrue(subject.getAccount().getAddress()
            .equalsIgnoreCase("0x351e4184A9aBe6B71a2a7a71c2628c47cC861e51"));
    }

    @Test
    public void load() throws Exception {

        //Arrange
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(200, "{\"payload\":\"AD0iKNNKRpOEm0rq+vQljAI/36cUChu3P4HO3hkqmsv5pdLs/XTvGrU564O4jkRuJ0UiTXYiwB0KymPTY3f7ASIdPK3x5TX9Xvilp+pykwxPWTm33OzfejQ98g70gthaJkIrzmxaTmeM+/iAqpRriOMc8cJD6z9ab193hYb5XRwBjQmMqMBuOPyfFnnvQryC5jAyRfIrfA6DldQim95Hm8+gira7JcX6FqxRLtlXT//KyPxxOeGr5VAejAR1B2rnr07kWGm2BAHqJsZPqzqq8Q==\",\"version\":1,\"type_id\":5,\"signature\":\"ID8UEm8skTqVqJuQqbtKOO+oXuehr5jD368dAa+hmISPFKb1pg0MGr7kCn/zzPb0IcWhMfLsf3x+3/zpmt372tE=\",\"prev_magic_hash\":\"d2e21977d708bf6b619b5a943181077090ffdfe6c8fa747c2b914478af57e756\",\"address\":\"16dT4uai55i1gArFBE2EbyH9qvrRBphBBK\",\"created_at\":1503503369000,\"updated_at\":1503503369000}"));
        responseList.add(Pair.of(200, "{\"payload\":\"AD0iKNNKRpOEm0rq+vQljAI/36cUChu3P4HO3hkqmsv5pdLs/XTvGrU564O4jkRuJ0UiTXYiwB0KymPTY3f7ASIdPK3x5TX9Xvilp+pykwxPWTm33OzfejQ98g70gthaJkIrzmxaTmeM+/iAqpRriOMc8cJD6z9ab193hYb5XRwBjQmMqMBuOPyfFnnvQryC5jAyRfIrfA6DldQim95Hm8+gira7JcX6FqxRLtlXT//KyPxxOeGr5VAejAR1B2rnr07kWGm2BAHqJsZPqzqq8Q==\",\"version\":1,\"type_id\":5,\"signature\":\"ID8UEm8skTqVqJuQqbtKOO+oXuehr5jD368dAa+hmISPFKb1pg0MGr7kCn/zzPb0IcWhMfLsf3x+3/zpmt372tE=\",\"prev_magic_hash\":\"d2e21977d708bf6b619b5a943181077090ffdfe6c8fa747c2b914478af57e756\",\"address\":\"16dT4uai55i1gArFBE2EbyH9qvrRBphBBK\",\"created_at\":1503503369000,\"updated_at\":1503503369000}"));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = EthereumWallet.load(MetadataUtil.deriveMetadataNode(getWallet2().getMasterKey()));

        //Assert
        Assert.assertTrue(subject.hasSeen());
        Assert.assertEquals(0, subject.getTxNotes().size());
    }

    @Test
    public void loadEmptyWallet() throws Exception {

        //Arrange
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = EthereumWallet.load(MetadataUtil.deriveMetadataNode(getWallet1().getMasterKey()));

        //Assert
        Assert.assertNull(subject);
    }

    @Test
    public void save() throws Exception {

        HDWallet wallet = getWallet3();

        //Arrange
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(200, "{\"payload\":\"Y2/AoEhgHJaT6krQhwVx/Fp2wHHOi+XqKSwZS6I+GAVLe9KNh1yyHQzMFxhY3oGL3c6VZ9iD/wcMvTLFoCcoUyS5L4PsSK/pbjjNz84Y0tYu4nTMpUUL3QrgEoswX4PhmZBt9SalYwsD9XJz1oBZgvkJyKOCVa1vKfI4YnotDSQ49egkzKUtU07qTY5UdZi8Uj+NHr63TrS6r+nZ+yIF2CO7hGcWMAf+2p4MWjxJVNMgrgcIuHie4Lrm9dUVUEI70lvaTP2PtF4px/LoWq1pGQ==\",\"version\":1,\"type_id\":5,\"signature\":\"H/iNIDrfUdj+UxlVj5nn7VBXUEuZanRuqCssVnKrfZI9QruFI5Ysp+hFVrrgDeCPvpAZj4XahTvESFml0Oaucdc=\",\"prev_magic_hash\":\"148c2ed942cd9555dab065234341caf120e596f980426047fa53c011755c49b1\",\"address\":\"13th73nRVMFQrM9HBwSgYfhFNKmMqS2hC6\",\"created_at\":1503504150000,\"updated_at\":1503567702000}"));
        responseList.add(Pair.of(200, "{\"payload\":\"Y2/AoEhgHJaT6krQhwVx/Fp2wHHOi+XqKSwZS6I+GAVLe9KNh1yyHQzMFxhY3oGL3c6VZ9iD/wcMvTLFoCcoUyS5L4PsSK/pbjjNz84Y0tYu4nTMpUUL3QrgEoswX4PhmZBt9SalYwsD9XJz1oBZgvkJyKOCVa1vKfI4YnotDSQ49egkzKUtU07qTY5UdZi8Uj+NHr63TrS6r+nZ+yIF2CO7hGcWMAf+2p4MWjxJVNMgrgcIuHie4Lrm9dUVUEI70lvaTP2PtF4px/LoWq1pGQ==\",\"version\":1,\"type_id\":5,\"signature\":\"H/iNIDrfUdj+UxlVj5nn7VBXUEuZanRuqCssVnKrfZI9QruFI5Ysp+hFVrrgDeCPvpAZj4XahTvESFml0Oaucdc=\",\"prev_magic_hash\":\"148c2ed942cd9555dab065234341caf120e596f980426047fa53c011755c49b1\",\"address\":\"13th73nRVMFQrM9HBwSgYfhFNKmMqS2hC6\",\"created_at\":1503504150000,\"updated_at\":1503567702000}"));
        responseList.add(Pair.of(200, "{\"payload\":\"Yqmn5W7/GsNd/EWieVet539s2g2RPrF7GNOAVEfgLSR/pImrWOtKp2Ylh6rWHWieZ+iIfSN+nUbmnVmJ5tHOaXArx2yR1iP03crz4HI1VOcTZyXpULbRQ+sv903/AX+vQnQrElPaotmMraIAU3BIYVFm97eGIaqiZEppNTrfrREZZsZ3VmS1CHd9ILZNHSLrpEy5Kfy1chM0Nfmve1lO63sqVid1EluQnTBTTnTvZWRF4QZ3+RslIIEy2aDHyFUzoB5QATkwPEkO9ywKqi9pMg==\",\"version\":1,\"type_id\":5,\"signature\":\"IDXi2+6NFCg0S1V7C6aYX1Egv2373h09WH0itskUNWycaQwSPg3JSmBLZx/cvIENdbxcy/nFcVobHRRUPgLIy+8=\",\"prev_magic_hash\":\"88df43a57dc7cfb115077a87775845fe8d1f7bf8bbfb88dc4214188778699986\",\"address\":\"13th73nRVMFQrM9HBwSgYfhFNKmMqS2hC6\",\"created_at\":1503504150000,\"updated_at\":1503567732376}"));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = new EthereumWallet(wallet.getMasterKey(),"My Ether Wallet");
        subject.setHasSeen(true);
        subject.save();

        //Assert
        Assert.assertTrue(subject.hasSeen());
    }

    @Test
    public void signTransaction() throws Exception {
        HDWallet wallet = getWallet1();

        //Arrange
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        mockInterceptor.setResponseList(responseList);

        subject = new EthereumWallet(wallet.getMasterKey(), "My Ether Wallet");
        RawTransaction tx = createEtherTransaction();

        //Act
        byte[] signTransaction = subject.getAccount().signTransaction(tx, EthereumAccount.deriveECKey(wallet.getMasterKey(), 0));

        //Assert
        Assert.assertEquals(
            "0xf85580010a840add5355887fffffffffffffff801ca032472aef4a52"
                + "fde34912307409bc3f9d4c0be12aaa09468da6967e9ceb80ea04a01b60edf2"
                + "1a188f14b8e00ddfe5fa5e1552be20a1a5462667501d047c4a5327ed"
            , Numeric.toHexString(signTransaction));
    }

    private static RawTransaction createEtherTransaction() {
        return RawTransaction.createEtherTransaction(
            BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN, "0xadd5355",
            BigInteger.valueOf(Long.MAX_VALUE));
    }
}
