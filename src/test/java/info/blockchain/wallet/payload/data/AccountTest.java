package info.blockchain.wallet.payload.data;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AccountTest {

    String seed = "15e23aa73d25994f1921a1256f93f72c";
    String xpub = "xpub698BtD3Gk6yvP2WsPNHatEtJ9aVfKZDBvhi6xoUodor5S2PTsGvox9pf5VCtfx7naLadpNPRe8JQCW9RcJV2rhBQ37CrNpBCDaowY17Mp5X";
    String xpriv = "xprv9v8qUhWNujRdAYSQHLkaX6wZbYfAv6VLZUnWAR5C5UK6ZE4KKjcZQMWBECoGcrGJMiPf3KDATPyqa8zurUu8T5Cfuz9BNXizu2AtK84MecB";

    DeterministicKey key;

    @Before
    public void setup() {
        key = HDKeyDerivation
            .createMasterPrivateKey(seed.getBytes());
    }

    @Test
    public void fromJson_1() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        HDWallet hdWallet = wallet.getHdWallets().get(0);

        List<Account> accounts = hdWallet.getAccounts();

        Assert.assertEquals(68, accounts.size());
        Assert.assertEquals("My Wallet", accounts.get(0).getLabel());
        Assert.assertFalse(accounts.get(0).isArchived());
        Assert.assertEquals(
            "FhyK+vchEiRwTFmIFye+V182e38sNjStJGT/eFcy7TKyrfGh+XnfaJi90IRHatMJVCvIo7jZApX7x3zc87UzqR8tH2yIpqIcjR3IXWA7IGXfi9grNc6UU3tF5BaYV/jlrAOij/7mGsZSYam5G5Fz8gscVYf4+4ZC9eM+q80lN1Q=",
            accounts.get(0).getXpriv());
        Assert.assertEquals(
            "xpub6DEe2bJAU7GbP12FBdsBckUkGPzQKMnZXaF2ajz2NCFfYJMEzb5G3oGwYrE6WQjnjhLeB6TgVudV3B9kKtpQmYeBJZLRNyXCobPht2jPUBm",
            accounts.get(0).getXpub());
        Assert.assertNotNull(accounts.get(0).getCache());
        Assert.assertNotNull(accounts.get(0).getAddressLabels());

        Assert.assertEquals("Savings 1", accounts.get(1).getLabel());
        Assert.assertTrue(accounts.get(1).isArchived());
        Assert.assertEquals(
            "nz3xhp6xFfBxOe4+l1xLePSdL5E4cBDCj/TC1nNNSokvHZTdXgbuTV5Ow+Gh7ZbOpth3Oh2iZrCibpwgiiler0A/TKDu++V1QnuJOmK/77WGYizm/e563eultBUuQCNktEfNGQUVveCYeF+TfsTU24tS3xbKzK4JeYiXaVlN4fk=",
            accounts.get(1).getXpriv());
        Assert.assertEquals(
            "xpub6DEe2bJAU7GbQcGHvqgJ4T6pzZUU8j1WqLPyVtaWJFewfjChAKtUX5uRza9rabc6rAgFhXptveBmaoy7ptVGgbYT8KKaJ9E7wmyj5o4aqvr",
            accounts.get(1).getXpub());
        Assert.assertNotNull(accounts.get(1).getCache());

        //AddressLabel parsing tested in AddressLabelTest
        Assert.assertNotNull(accounts.get(1).getAddressLabels());
    }

    @Test
    public void fromJson_6() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        HDWallet hdWallet = wallet.getHdWallets().get(0);

        List<Account> accounts = hdWallet.getAccounts();

        Assert.assertEquals(1, accounts.size());
        Assert.assertEquals("My Bitcoin Wallet", accounts.get(0).getLabel());
        Assert.assertFalse(accounts.get(0).isArchived());
        Assert.assertEquals(
            "xprv9xvLaqAsee2mgFsgMQsVLCTh858tA559kD9wczD5nYGJMa4M56MvLgYGGn75MSdDFZSBYeYeCgAZdqKQitXux3ebiTi67eYH1a1VS2rdKZW",
            accounts.get(0).getXpriv());
        Assert.assertEquals(
            "xpub6BugzLhmV1b4tjx9TSQVhLQRg6yNZXo17S5YRNchLsoHENPVcdgAtUrk82X5LNuaViWoxsqMhCd3UBxhQRHvyrUeqqA7tupvSpkoC73nhL1",
            accounts.get(0).getXpub());
        Assert.assertNotNull(accounts.get(0).getCache());
        Assert.assertNotNull(accounts.get(0).getAddressLabels());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        HDWallet hdWallet = wallet.getHdWallets().get(0);

        List<Account> accounts = hdWallet.getAccounts();
        Account accountBody = accounts.get(0);

        String jsonString = accountBody.toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(6, jsonObject.keySet().size());
    }
}