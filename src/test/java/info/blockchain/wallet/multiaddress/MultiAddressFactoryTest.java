package info.blockchain.wallet.multiaddress;

import info.blockchain.MockedResponseTest;
import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.MultiAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Call;

public class MultiAddressFactoryTest extends MockedResponseTest{

    private final String dormantAddress = "1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW";
    private final String dormantXpub = "xpub6CFgfYG9chNp7rzZ7ByXyAJruku5JSVhtGmGqR9tmeLRwu3jtioyBZpXC6GAnpMQPBQg5rviqTwMN4EwgMCZNVT3N22sSnM1yEfBQzjHXJt";

    @Test
    public void getMultiAddress_legacyAddress() throws Exception {

        URI uri = getClass().getClassLoader().getResource("multi_address_1jH7K.txt").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(response);

        Call<MultiAddress> call = MultiAddressFactory.getMultiAddress(
            Arrays.asList(dormantAddress), null,
            BlockExplorer.TX_FILTER_ALL, 100, 0);

        MultiAddress body = call.execute().body();

        Assert.assertEquals(0, body.getWallet().getFinalBalance().longValue());
    }

    @Test
    public void getMultiAddress_xpub() throws Exception {

        URI uri = getClass().getClassLoader().getResource("multi_address_xpub6CFg.txt").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(response);

        Call<MultiAddress> call = MultiAddressFactory.getMultiAddress(
            Arrays.asList(dormantXpub), null,
            BlockExplorer.TX_FILTER_ALL, 100, 0);

        MultiAddress body = call.execute().body();

        Assert.assertEquals(10000, body.getWallet().getFinalBalance().longValue());

        Assert.assertTrue(MultiAddressFactory.isOwnHDAddress(body, "1CAAzobQ2UrE4QUR3HJrkZs8UFA8wi5wwQ"));
        Assert.assertTrue(MultiAddressFactory.isOwnHDAddress(body, "1KTKN43STRsmRSNtChuDUzQtcQGMXyBMN1"));
        Assert.assertTrue(MultiAddressFactory.isOwnHDAddress(body, "1CkFCfj7YQ8hjH1ReW398rax9NXCJcceE9"));
        Assert.assertFalse(MultiAddressFactory.isOwnHDAddress(body, "1PPNN4psDFyAgdjQcKBJ8GSgE4ES4GHP9c"));

        Assert.assertEquals(5, MultiAddressFactory.getHighestIndexes(body, dormantXpub).getRight().intValue());
        Assert.assertEquals(10, MultiAddressFactory.getHighestIndexes(body, dormantXpub).getLeft().intValue());

        Assert.assertEquals(6, MultiAddressFactory.getNextChangeAddress(body, dormantXpub));
        Assert.assertEquals(11, MultiAddressFactory.getNextReceiveAddress(body, dormantXpub));
    }

    @Test
    public void getAll() throws Exception {

        URI uri = getClass().getClassLoader().getResource("multi_address_all.txt").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        mockInterceptor.setResponseString(response);

        Call<MultiAddress> call = MultiAddressFactory.getMultiAddress(
            Arrays.asList(dormantAddress, dormantXpub), null,
            BlockExplorer.TX_FILTER_ALL, 100, 0);

        MultiAddress body = call.execute().body();

        Assert.assertEquals(10000, body.getWallet().getFinalBalance().longValue());

        Assert.assertTrue(MultiAddressFactory.isOwnHDAddress(body, "1CAAzobQ2UrE4QUR3HJrkZs8UFA8wi5wwQ"));
        Assert.assertTrue(MultiAddressFactory.isOwnHDAddress(body, "1KTKN43STRsmRSNtChuDUzQtcQGMXyBMN1"));
        Assert.assertTrue(MultiAddressFactory.isOwnHDAddress(body, "1CkFCfj7YQ8hjH1ReW398rax9NXCJcceE9"));
        Assert.assertFalse(MultiAddressFactory.isOwnHDAddress(body, "1PPNN4psDFyAgdjQcKBJ8GSgE4ES4GHP9c"));

        Assert.assertEquals(5, MultiAddressFactory.getHighestIndexes(body, dormantXpub).getRight().intValue());
        Assert.assertEquals(10, MultiAddressFactory.getHighestIndexes(body, dormantXpub).getLeft().intValue());

        Assert.assertEquals(dormantXpub, MultiAddressFactory.getXpubFromAddress(body, "1CAAzobQ2UrE4QUR3HJrkZs8UFA8wi5wwQ"));

        Assert.assertEquals(6, MultiAddressFactory.getNextChangeAddress(body, dormantXpub));
        Assert.assertEquals(11, MultiAddressFactory.getNextReceiveAddress(body, dormantXpub));
    }
}