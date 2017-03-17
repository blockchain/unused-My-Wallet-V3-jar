package info.blockchain.wallet.multiaddress;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.wallet.multiaddress.TransactionSummary.Direction;
import info.blockchain.wallet.payload.data.AddressLabels;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultiAddressFactoryTest extends MockedResponseTest{

    MultiAddressFactory multiAddressFactory;

    private final String dormantAddress = "1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW";
    private final String dormantXpub = "xpub6CFgfYG9chNp7rzZ7ByXyAJruku5JSVhtGmGqR9tmeLRwu3jtioyBZpXC6GAnpMQPBQg5rviqTwMN4EwgMCZNVT3N22sSnM1yEfBQzjHXJt";

    @Before
    public void setUp() throws Exception {
        multiAddressFactory = new MultiAddressFactory(new BlockExplorer());
    }

    @Test
    public void getMultiAddress_legacyAddress() throws Exception {

        URI uri = getClass().getClassLoader().getResource("multiaddress/multi_address_1jH7K.txt").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(response);

        List<TransactionSummary> summary = multiAddressFactory.getAccountTransactions(
            new ArrayList<>(Arrays.asList(dormantAddress)), new ArrayList<String>(), null, dormantAddress, 100, 0);

        Assert.assertEquals(2, summary.size());
        Assert.assertEquals(1, summary.get(0).getInputsMap().size());
        Assert.assertEquals(1, summary.get(0).getOutputsMap().size());
        Assert.assertEquals(20000, summary.get(0).getTotal().longValue());
        Assert.assertEquals(Direction.SENT, summary.get(0).getDirection());
        Assert.assertEquals(10000, summary.get(0).getFee().longValue());
        Assert.assertEquals(1436437493, summary.get(0).getTime());
        Assert.assertEquals("04734caac4e2ae7feba9b74fb8d2c145db9ea9651487371c4d741428f8f5a24b", summary.get(0).getHash());
    }

    @Test
    public void getMultiAddress_xpub() throws Exception {

        URI uri = getClass().getClassLoader().getResource("multiaddress/multi_address_xpub6CFg.txt").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(response);

        List<TransactionSummary> summary = multiAddressFactory.getAccountTransactions(
            new ArrayList<>(Arrays.asList(dormantXpub)), new ArrayList<String>(), null, dormantXpub, 100, 0);

        Assert.assertEquals(34, summary.size());
        Assert.assertEquals(1, summary.get(0).getInputsMap().size());
        Assert.assertEquals(1, summary.get(0).getOutputsMap().size());
        Assert.assertEquals(20000, summary.get(0).getTotal().longValue());
        Assert.assertEquals(Direction.SENT, summary.get(0).getDirection());
        Assert.assertEquals(10000, summary.get(0).getFee().longValue());
        Assert.assertEquals(1452868237, summary.get(0).getTime());
        Assert.assertEquals("34c22edb3466708b974a7549d5b3cb51e05d4444f74d2a1b41484f8711dffd04", summary.get(0).getHash());

        Assert.assertEquals(5, multiAddressFactory.getNextChangeAddressIndex(dormantXpub));
        Assert.assertEquals(10, multiAddressFactory.getNextReceiveAddressIndex(dormantXpub, new ArrayList<AddressLabels>()));

        Assert.assertTrue(multiAddressFactory.isOwnHDAddress("1CAAzobQ2UrE4QUR3HJrkZs8UFA8wi5wwQ"));
        Assert.assertTrue(multiAddressFactory.isOwnHDAddress("1KTKN43STRsmRSNtChuDUzQtcQGMXyBMN1"));
        Assert.assertTrue(multiAddressFactory.isOwnHDAddress("1CkFCfj7YQ8hjH1ReW398rax9NXCJcceE9"));
        Assert.assertFalse(multiAddressFactory.isOwnHDAddress("1PPNN4psDFyAgdjQcKBJ8GSgE4ES4GHP9c"));
    }

    @Test
    public void getAll() throws Exception {

        URI uri = getClass().getClassLoader().getResource("multiaddress/multi_address_all.txt").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        mockInterceptor.setResponseString(response);

        List<TransactionSummary> summary = multiAddressFactory.getAccountTransactions(
            new ArrayList<>(Arrays.asList(dormantAddress, dormantXpub)), new ArrayList<String>(), null, null, 100, 0);

        Assert.assertEquals(36, summary.size());
        Assert.assertEquals(1, summary.get(0).getInputsMap().size());
        Assert.assertEquals(1, summary.get(0).getOutputsMap().size());
        Assert.assertEquals(20000, summary.get(0).getTotal().longValue());
        Assert.assertEquals(Direction.SENT, summary.get(0).getDirection());
        Assert.assertEquals(10000, summary.get(0).getFee().longValue());
        Assert.assertEquals(1452868237, summary.get(0).getTime());
        Assert.assertEquals("34c22edb3466708b974a7549d5b3cb51e05d4444f74d2a1b41484f8711dffd04", summary.get(0).getHash());

        Assert.assertTrue(multiAddressFactory.isOwnHDAddress("1CAAzobQ2UrE4QUR3HJrkZs8UFA8wi5wwQ"));
        Assert.assertTrue(multiAddressFactory.isOwnHDAddress("1KTKN43STRsmRSNtChuDUzQtcQGMXyBMN1"));
        Assert.assertTrue(multiAddressFactory.isOwnHDAddress("1CkFCfj7YQ8hjH1ReW398rax9NXCJcceE9"));
        Assert.assertFalse(multiAddressFactory.isOwnHDAddress("1PPNN4psDFyAgdjQcKBJ8GSgE4ES4GHP9c"));

        Assert.assertEquals(5, multiAddressFactory.getNextChangeAddressIndex(dormantXpub));
        Assert.assertEquals(10, multiAddressFactory.getNextReceiveAddressIndex(dormantXpub, new ArrayList<AddressLabels>()));
    }
}