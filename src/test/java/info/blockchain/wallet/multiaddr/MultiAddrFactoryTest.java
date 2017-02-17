//package info.blockchain.wallet.multiaddr;
//
//import info.blockchain.MockedResponseTest;
//import info.blockchain.wallet.payload.data.PayloadTest;
//import java.net.URI;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//public class MultiAddrFactoryTest extends MockedResponseTest {
//
//    String dormantAddress = "1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW";
//    String dormantXpub = "xpub6CFgfYG9chNp7rzZ7ByXyAJruku5JSVhtGmGqR9tmeLRwu3jtioyBZpXC6GAnpMQPBQg5rviqTwMN4EwgMCZNVT3N22sSnM1yEfBQzjHXJt";
//
//    @Before
//    public void setUp() throws Exception {
//
//        URI uri = PayloadTest.class.getClassLoader().getResource("multi_address_1jH7K.txt").toURI();
//        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
//        mockInterceptor.setResponseString(response);
//        MultiAddrFactory.getInstance().refreshLegacyAddressData(Arrays.asList(dormantAddress), false);
//
//        uri = PayloadTest.class.getClassLoader().getResource("multi_address_xpub6CFg.txt").toURI();
//        response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
//        mockInterceptor.setResponseString(response);
//        MultiAddrFactory.getInstance().refreshXPUBData(Arrays.asList(dormantXpub));
//    }
//
//    @Test
//    public void refreshLegacyAddressData() throws Exception {
//
//
//        Assert.assertEquals(0, MultiAddrFactory.getInstance().getLegacyBalance());
//        Assert.assertEquals(0, MultiAddrFactory.getInstance().getLegacyTxs().size());
//    }
//
//    @Test
//    public void refreshXPUBData() throws Exception {
//
//        Assert.assertEquals(10000L, MultiAddrFactory.getInstance().getXpubBalance());
//
//        Assert.assertTrue(MultiAddrFactory.getInstance().isOwnHDAddress("1CAAzobQ2UrE4QUR3HJrkZs8UFA8wi5wwQ"));
//        Assert.assertTrue(MultiAddrFactory.getInstance().isOwnHDAddress("1KTKN43STRsmRSNtChuDUzQtcQGMXyBMN1"));
//        Assert.assertTrue(MultiAddrFactory.getInstance().isOwnHDAddress("1CkFCfj7YQ8hjH1ReW398rax9NXCJcceE9"));
//        Assert.assertFalse(MultiAddrFactory.getInstance().isOwnHDAddress("1PPNN4psDFyAgdjQcKBJ8GSgE4ES4GHP9c"));
//
//
//        Assert.assertEquals(5, MultiAddrFactory.getInstance().getHighestTxChangeIdx(dormantXpub));
//        Assert.assertEquals(10, MultiAddrFactory.getInstance().getHighestTxReceiveIdx(dormantXpub));
//
//        Assert.assertEquals(dormantXpub, MultiAddrFactory.getInstance().getAddress2Xpub().get("1CAAzobQ2UrE4QUR3HJrkZs8UFA8wi5wwQ"));
//
//        Assert.assertEquals(34, MultiAddrFactory.getInstance().getXpubTxs().get(dormantXpub).size());
//        Assert.assertEquals(34, MultiAddrFactory.getInstance().getAllXpubTxs().size());
//        Assert.assertEquals(10000L, MultiAddrFactory.getInstance().getXpubAmounts().get(dormantXpub).longValue());
//    }
//}