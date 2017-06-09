package info.blockchain.wallet.multiaddress;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.wallet.multiaddress.TransactionSummary.Direction;
import info.blockchain.wallet.payload.data.AddressLabel;
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
        multiAddressFactory = new MultiAddressFactory(
            new BlockExplorer(BlockchainFramework.getRetrofitExplorerInstance(), BlockchainFramework.getApiCode()));
    }

    @Test
    public void getMultiAddress_legacyAddressOnly() throws Exception {

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
    public void getMultiAddress_xpubOnly() throws Exception {

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
        Assert.assertEquals(10, multiAddressFactory.getNextReceiveAddressIndex(dormantXpub, new ArrayList<AddressLabel>()));

        Assert.assertTrue(multiAddressFactory.isOwnHDAddress("1CAAzobQ2UrE4QUR3HJrkZs8UFA8wi5wwQ"));
        Assert.assertTrue(multiAddressFactory.isOwnHDAddress("1KTKN43STRsmRSNtChuDUzQtcQGMXyBMN1"));
        Assert.assertTrue(multiAddressFactory.isOwnHDAddress("1CkFCfj7YQ8hjH1ReW398rax9NXCJcceE9"));
        Assert.assertFalse(multiAddressFactory.isOwnHDAddress("1PPNN4psDFyAgdjQcKBJ8GSgE4ES4GHP9c"));
    }

    @Test
    public void getMultiAddress_xpubAndLegacyAddress() throws Exception {

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
        Assert.assertEquals(10, multiAddressFactory.getNextReceiveAddressIndex(dormantXpub, new ArrayList<AddressLabel>()));
    }

    @Test
    public void getMultiAddress_MoreCases() throws Exception {

        String xpub1 = "xpub6Bx1J3neE11W2XpvKRFQVwWpZFsDfnRkLJ2V4JjPWNRDXbRvZrwnytbSbBng2F1fRejxkMWAi6fYJuAJrGg6TP8Key4jvs9YqpVo5LJ8jSk";
        String xpub2 = "xpub6Bx1J3neE11W3XsMUTWVBKECFJee9TjJDSZJ53LKhr7AaAPJpNtz4KZTCe8nctTdu6kLYB4uZncjsy7EBi18mKb4HLg3WLfhPFW2KFGjScE";
        String address = "1DtkXqBjvXWsboMpc72U1kfRrK8JTntBLQ";

        URI uri = getClass().getClassLoader().getResource("multiaddress/multi_address_1Dtk.txt").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(response);

        List<TransactionSummary> summary = multiAddressFactory.getAccountTransactions(
            new ArrayList<>(Arrays.asList(xpub1, xpub2, address)), new ArrayList<String>(),
            null, null, 100, 0);

        Assert.assertEquals(7, summary.size());

        TransactionSummary txSummary = summary.get(0);
        Assert.assertEquals(1, txSummary.getInputsMap().size());
        Assert.assertEquals(1, txSummary.getOutputsMap().size());
        Assert.assertEquals(166486, txSummary.getTotal().longValue());
        Assert.assertEquals(Direction.SENT, txSummary.getDirection());
        Assert.assertEquals(27120, txSummary.getFee().longValue());
        Assert.assertEquals(1492614742, txSummary.getTime());
        Assert.assertEquals("de2db2e9b430f949f8c94ef4cd9093a020ef10c614b6802320920f7d84a8afab", txSummary.getHash());

        txSummary = summary.get(1);
        Assert.assertEquals(1, txSummary.getInputsMap().size());
        Assert.assertEquals(1, txSummary.getOutputsMap().size());
        Assert.assertEquals(446212, txSummary.getTotal().longValue());
        Assert.assertEquals(Direction.SENT, txSummary.getDirection());
        Assert.assertEquals(27120, txSummary.getFee().longValue());
        Assert.assertEquals(1492614706, txSummary.getTime());
        Assert.assertEquals("8a5327e09c1789f9ef9467298bfb8e46748effd79ff981226df14e5a468378b6", txSummary.getHash());

        txSummary = summary.get(2);
        Assert.assertEquals(1, txSummary.getInputsMap().size());
        Assert.assertEquals(1, txSummary.getOutputsMap().size());
        Assert.assertEquals(166486, txSummary.getTotal().longValue());
        Assert.assertEquals(Direction.TRANSFERRED, txSummary.getDirection());
        Assert.assertEquals(27120, txSummary.getFee().longValue());
        Assert.assertEquals(1492614681, txSummary.getTime());
        Assert.assertEquals("165b251a736e0e5d1e9aa287687b8d6fd5eb91c72b1138dd6047e34f8ed17217", txSummary.getHash());

        txSummary = summary.get(3);
        Assert.assertEquals(1, txSummary.getInputsMap().size());
        Assert.assertEquals(1, txSummary.getOutputsMap().size());
        Assert.assertEquals(83243, txSummary.getTotal().longValue());
        Assert.assertEquals(Direction.TRANSFERRED, txSummary.getDirection());
        Assert.assertEquals(27120, txSummary.getFee().longValue());
        Assert.assertEquals(1492614642, txSummary.getTime());
        Assert.assertEquals("0b2804884f0ae1d151a7260d2009168078259ef6428c861b001ce6a028a19977", txSummary.getHash());

        txSummary = summary.get(4);
        Assert.assertEquals(1, txSummary.getInputsMap().size());
        Assert.assertEquals(1, txSummary.getOutputsMap().size());
        Assert.assertEquals(750181, txSummary.getTotal().longValue());
        Assert.assertEquals(Direction.RECEIVED, txSummary.getDirection());
        Assert.assertEquals(0, txSummary.getFee().longValue());
        Assert.assertEquals(1492614623, txSummary.getTime());
        Assert.assertEquals("9fccf050f52ed23ee4fe20a89b03780a944d795ad897b38ff44a7369d6c7e665", txSummary.getHash());

        txSummary = summary.get(5);
        Assert.assertEquals(1, txSummary.getInputsMap().size());
        Assert.assertEquals(2, txSummary.getOutputsMap().size());
        Assert.assertEquals(909366, txSummary.getTotal().longValue());
        Assert.assertEquals(Direction.SENT, txSummary.getDirection());
        Assert.assertEquals(133680, txSummary.getFee().longValue());
        Assert.assertEquals(1492497642, txSummary.getTime());
        Assert.assertEquals("8765362f7fd1895bb35942197c9f74a6e25c85d0043f38858021442b20bfa112", txSummary.getHash());

        txSummary = summary.get(6);
        Assert.assertEquals(1, txSummary.getInputsMap().size());
        Assert.assertEquals(1, txSummary.getOutputsMap().size());
        Assert.assertEquals(909366, txSummary.getTotal().longValue());
        Assert.assertEquals(Direction.RECEIVED, txSummary.getDirection());
        Assert.assertEquals(0, txSummary.getFee().longValue());
        Assert.assertEquals(1486028570, txSummary.getTime());
        Assert.assertEquals("50115fce313d537b4a97ea24bb42d08b48f21d921b5710b765f07fc4fd23b101", txSummary.getHash());
    }

    @Test
    public void getMultiAddress_MoreCases2() throws Exception {
        //5350e5d5-bd65-456f-b150-e6cc089f0b26
        String xpub1 = "xpub6CdH6yzYXhTtR7UHJHtoTeWm3nbuyg9msj3rJvFnfMew9CBff6Rp62zdTrC57Spz4TpeRPL8m9xLiVaddpjEx4Dzidtk44rd4N2xu9XTrSV";
        String xpub2 = "xpub6CdH6yzYXhTtTGPPL4Djjp1HqFmAPx4uyqoG6Ffz9nPysv8vR8t8PEJ3RGaSRwMm7kRZ3MAcKgB6u4g1znFo82j4q2hdShmDyw3zuMxhDSL";
        String address = "189iKJLruPtUorasDuxmc6fMRVxz6zxpPS";

        URI uri = getClass().getClassLoader().getResource("multiaddress/wallet_v3_6_m1.txt")
            .toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(response);

        List<TransactionSummary> transactionSummaries = multiAddressFactory.getAccountTransactions(
            new ArrayList<>(Arrays.asList(xpub1, xpub2, address)), new ArrayList<String>(),
            null, null, 100, 0);

        Assert.assertEquals(8, transactionSummaries.size());

        TransactionSummary summary = transactionSummaries.get(0);
        Assert.assertEquals(68563, summary.getTotal().longValue());
        Assert.assertEquals(Direction.TRANSFERRED, summary.getDirection());
        Assert.assertEquals(1, summary.getInputsMap().size());
        Assert.assertTrue(summary.getInputsMap().keySet().contains("125QEfWq3eKzAQQHeqcMcDMeZGm13hVRvU"));//My Bitcoin Account
        Assert.assertEquals(2, summary.getOutputsMap().size());
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("1Nm1yxXCTodAkQ9RAEquVdSneJGeubqeTw"));//Savings account
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("189iKJLruPtUorasDuxmc6fMRVxz6zxpPS"));

        summary = transactionSummaries.get(1);
        Assert.assertEquals(138068, summary.getTotal().longValue());
        Assert.assertEquals(Direction.SENT, summary.getDirection());
        Assert.assertEquals(1, summary.getInputsMap().size());
        Assert.assertTrue(summary.getInputsMap().keySet().contains("1CQpuTQrJQLW6PEar17zsd9EV14cZknqWJ"));//My Bitcoin Wallet
        Assert.assertEquals(2, summary.getOutputsMap().size());
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("1LQwNvEMnYjNCNxeUJzDfD8mcSqhm2ouPp"));
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("1AdTcerDBY735kDhQWit5Scroae6piQ2yw"));

        summary = transactionSummaries.get(2);
        Assert.assertEquals(800100, summary.getTotal().longValue());
        Assert.assertEquals(Direction.RECEIVED, summary.getDirection());
        Assert.assertEquals(1, summary.getInputsMap().size());
        Assert.assertTrue(summary.getInputsMap().keySet().contains("19CMnkUgBnTBNiTWXwoZr6Gb3aeXKHvuGG"));
        Assert.assertEquals(1, summary.getOutputsMap().size());
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("1CQpuTQrJQLW6PEar17zsd9EV14cZknqWJ"));//My Bitcoin Wallet

        summary = transactionSummaries.get(3);
        Assert.assertEquals(35194, summary.getTotal().longValue());
        Assert.assertEquals(Direction.SENT, summary.getDirection());
        Assert.assertEquals(1, summary.getInputsMap().size());
        Assert.assertTrue(summary.getInputsMap().keySet().contains("15HjFY96ZANBkN5kvPRgrXH93jnntqs32n"));//My Bitcoin Wallet
        Assert.assertEquals(1, summary.getOutputsMap().size());
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("1PQ9ZYhv9PwbWQQN74XRqUCjC32JrkyzB9"));

        summary = transactionSummaries.get(4);
        Assert.assertEquals(98326, summary.getTotal().longValue());
        Assert.assertEquals(Direction.TRANSFERRED, summary.getDirection());
        Assert.assertEquals(1, summary.getInputsMap().size());
        Assert.assertTrue(summary.getInputsMap().keySet().contains("1Peysd3qYDe35yNp6KB1ZkbVYHr42JT9zZ"));//My Bitcoin Wallet
        Assert.assertEquals(1, summary.getOutputsMap().size());
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("189iKJLruPtUorasDuxmc6fMRVxz6zxpPS"));

        summary = transactionSummaries.get(5);
        Assert.assertEquals(160640, summary.getTotal().longValue());
        Assert.assertEquals(Direction.RECEIVED, summary.getDirection());
        Assert.assertEquals(1, summary.getInputsMap().size());
        Assert.assertTrue(summary.getInputsMap().keySet().contains("1BZe6YLaf2HiwJdnBbLyKWAqNia7foVe1w"));
        Assert.assertEquals(1, summary.getOutputsMap().size());
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("1Peysd3qYDe35yNp6KB1ZkbVYHr42JT9zZ"));//My Bitcoin Wallet

        summary = transactionSummaries.get(6);
        Assert.assertEquals(9833, summary.getTotal().longValue());
        Assert.assertEquals(Direction.TRANSFERRED, summary.getDirection());
        Assert.assertEquals(1, summary.getInputsMap().size());
        Assert.assertTrue(summary.getInputsMap().keySet().contains("17ijgwpGsVQRzMjsdAfdmeP53kpw9yvXur"));//My Bitcoin Wallet
        Assert.assertEquals(1, summary.getOutputsMap().size());
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("1AtunWT3F6WvQc3aaPuPbNGeBpVF3ZPM5r"));//Savings account

        summary = transactionSummaries.get(7);
        Assert.assertEquals(40160, summary.getTotal().longValue());
        Assert.assertEquals(Direction.RECEIVED, summary.getDirection());
        Assert.assertEquals(1, summary.getInputsMap().size());
        Assert.assertTrue(summary.getInputsMap().keySet().contains("1Baa1cjB1CyBVSjw8SkFZ2YBuiwKnKLXhe"));
        Assert.assertEquals(1, summary.getOutputsMap().size());
        Assert.assertTrue(summary.getOutputsMap().keySet().contains("17ijgwpGsVQRzMjsdAfdmeP53kpw9yvXur"));//My Bitcoin Wallet
    }
}