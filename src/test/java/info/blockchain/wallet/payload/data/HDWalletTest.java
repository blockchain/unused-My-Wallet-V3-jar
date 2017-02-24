package info.blockchain.wallet.payload.data;

import com.google.common.collect.BiMap;
import info.blockchain.MockedResponseTest;
import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.payment.Payment;
import info.blockchain.wallet.payment.SpendableUnspentOutputs;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class HDWalletTest extends MockedResponseTest {

    @Test
    public void fromJson_1() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        HDWallet hdWallet = wallet.getHdWallets().get(0);

        Assert.assertEquals(68, hdWallet.getAccounts().size());
        Assert.assertEquals("i3gtswW35zfbS/23fnh3IzKzcrpD04Tp+zeKbj++rODMOGRMO1aMQukwE3Q+63ds8pUMzBFnzomkjntprhisrQ==", hdWallet.getSeedHex());
        Assert.assertEquals("", hdWallet.getPassphrase());
        Assert.assertTrue(hdWallet.isMnemonicVerified());
        Assert.assertEquals(0, hdWallet.getDefaultAccountIdx());
    }

    @Test
    public void fromJson_2() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_2.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        Assert.assertNull(wallet.getHdWallets());
    }

    @Test
    public void fromJson_6() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        HDWallet hdWallet = wallet.getHdWallets().get(0);

        Assert.assertEquals(1, hdWallet.getAccounts().size());
        Assert.assertEquals("bfb70136ef9f973e866dff00817b8070", hdWallet.getSeedHex());
        Assert.assertEquals("somePassPhrase", hdWallet.getPassphrase());
        Assert.assertFalse(hdWallet.isMnemonicVerified());
        Assert.assertEquals(2, hdWallet.getDefaultAccountIdx());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        Wallet wallet = Wallet.fromJson(body);
        HDWallet hdWallet = wallet.getHdWallets().get(0);
        String jsonString = hdWallet.toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(5, jsonObject.keySet().size());
    }

    @Test
    public void recoverFromMnemonic() throws Exception {

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

        String label = "HDAccount 1";
        HDWallet hdWallet = HDWallet.recoverFromMnemonic(mnemonic, label);

        Assert.assertEquals(hdWallet.getAccounts().get(0).getLabel(), label);
        Assert.assertEquals(10, hdWallet.getAccounts().size());
    }

    @Test
    public void recoverFromMnemonic_passphrase() throws Exception {

        String mnemonic = "all all all all all all all all all all all all";

        LinkedList<String> xpubs = new LinkedList<>();
        xpubs.add("{\"xpub6BvvF1nwmp518uYADntSdRPYGiDBzywww8LYFDEQ7cVC2ZxxcDf5NARWeFAbkLewsAMjmp2zfVGo7uv4uP9jHmczH2i4Lq1RioMGRmZ6myC\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BvvF1nwmp51A6sqV9YTcTGKmWorM48PmZdSXEYgG9pQffDsUavLdPz14RX5tTghiGfApJLqYdv9ramj9agke9o1uKYLesYp6rPKExDmCFX\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BvvF1nwmp51CapAefmDYrKWeGC2Y96TcGtB6BTfiTJezHLjBxgsWdKRvWWChGAhWPjdRjSUsDeEgnSar2xjenixNArkytRU2heAWr3HmQ5\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BvvF1nwmp51G75mNUQLmQZm8r3CXBFJChJt6fvoURVS1Nz1jCVN6Nf5nMUfDuT53X8uAXjAX3eHJRPWcpDYMVPwzv1hpMAJKvKQVMefiRJ\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BvvF1nwmp51Gpj1eAidXtRpoq6AUwzZ3L2uv49oWnMQiW9KZ42UYrrM3fHoCyidHzAY14GRrZ8fSS2JZroAEXD5bqiLvjGDNGYbuMCa6vi\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BvvF1nwmp51MoY8LZ6RqZ6xc9PE5mASd2jpTGARe61HwscsK1tVLF5xJFf1QKnNP2T5YAKDyrK2WGAZS1p5aD9EuYhqC53EFYC7UpnYnz5\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BvvF1nwmp51N9UVeokUscF6vwT8TN35TSxQmW8GSJPgj7NQwUKrR9rZvug2KLeZf4SnviBmmqgtaWJstuMT18bcNpPttrhrBEWptdYHGcF\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BvvF1nwmp51RG4LdnpS4wW4q7hyjPfujhQ6iWQDKdQPBvjaYQz9CbJD6zYae1M9FfEFCCb2CyjcwPKj2qzQAyYNq3XM5rn1XNanTB8Mc3p\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BvvF1nwmp51V9WWEKjQRhqKYmuHj5gkjCr45c4BUAiLkS5y33zcQT39ZnXztG4NSwF98mo4DP1rTyugJsLbFKxDNQCXJegHoULicosyjMG\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BvvF1nwmp51XdWV7XBeBgcErsvkJ6f79vzppG278gJ4MPfJ9G5mPqaS8w1zWVyhVrXj3nnr2BSaLcNxHVM548go7UvS3MV1uynsi813YrY\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("HDWallet successfully synced with server");
        mockInterceptor.setResponseStringList(xpubs);

        String label = "HDAccount 1";
        HDWallet hdWallet = HDWallet.recoverFromMnemonic(mnemonic, "somePassphrase", label);

        Assert.assertEquals(hdWallet.getAccounts().get(0).getLabel(), label);
        Assert.assertEquals(10, hdWallet.getAccounts().size());
    }

    @Test
    public void getHDKeysForSigning() throws Exception{
        URI uri = getClass().getClassLoader().getResource("hd_wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        HDWallet hdWallet = HDWallet.fromJson(body);

        hdWallet.decryptHDWallet("hello", "d14f3d2c-f883-40da-87e2-c8448521ee64",5000);
        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        uri = getClass().getClassLoader().getResource("wallet_body_1_account1_unspent.txt").toURI();
        body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(body);

        Payment payment = new Payment();

        long spendAmount = 80200l + 70000l + 60000l + 50000l + 40000l + 30000l + 20000l + 10000l - Payment.DUST.longValue();
        long feeManual = Payment.DUST.longValue();

        SpendableUnspentOutputs paymentBundle = payment
            .getSpendableCoins(unspentOutputs, BigInteger.valueOf(spendAmount - feeManual), BigInteger.valueOf(30000L));

        List<ECKey> keyList = hdWallet
            .getHDKeysForSigning(hdWallet.getAccount(0), paymentBundle);

        //Contains 5 matching keys for signing
        Assert.assertEquals(5, keyList.size());
    }

    @Test
    public void getMasterKey() throws Exception {
        URI uri = getClass().getClassLoader().getResource("hd_wallet_body_2.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        //HD seed is encrypted, only xpubs available
        HDWallet hdWallet = HDWallet.fromJson(body);

        Assert.assertEquals("5F8YjqPVSq9HnXBrDxUmUoDKXsya8q5LGHnAopadTRYE",
            Base58.encode(hdWallet.getMasterKey().getPrivKeyBytes()));
    }

    @Test(expected = HDWalletException.class)
    public void getMasterKey_DecryptionException() throws Exception {
        URI uri = getClass().getClassLoader().getResource("hd_wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        HDWallet hdWallet = HDWallet.fromJson(body);

        hdWallet.getMasterKey();
    }

    @Test
    public void getMnemonic() throws Exception {
        URI uri = getClass().getClassLoader().getResource("hd_wallet_body_2.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        HDWallet hdWallet = HDWallet.fromJson(body);

        Assert.assertEquals("[car, region, outdoor, punch, poverty, shadow, insane, claim, one, whisper, learn, alert]",
            hdWallet.getMnemonic().toString());
    }

    @Test(expected = HDWalletException.class)
    public void getMnemonic_DecryptionException() throws Exception {
        URI uri = getClass().getClassLoader().getResource("hd_wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        HDWallet hdWallet = HDWallet.fromJson(body);

        hdWallet.getMnemonic().toString();
    }

    @Test
    public void getXpubToAccountIndexMap() throws Exception {

        URI uri = getClass().getClassLoader().getResource("hd_wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        HDWallet hdWallet = HDWallet.fromJson(body);

        BiMap<String, Integer> map = hdWallet.getXpubToAccountIndexMap();

        Assert.assertEquals(0, map.get("xpub6DEe2bJAU7GbP12FBdsBckUkGPzQKMnZXaF2ajz2NCFfYJMEzb5G3oGwYrE6WQjnjhLeB6TgVudV3B9kKtpQmYeBJZLRNyXCobPht2jPUBm").intValue());
        Assert.assertEquals(1, map.get("xpub6DEe2bJAU7GbQcGHvqgJ4T6pzZUU8j1WqLPyVtaWJFewfjChAKtUX5uRza9rabc6rAgFhXptveBmaoy7ptVGgbYT8KKaJ9E7wmyj5o4aqvr").intValue());
        Assert.assertEquals(2, map.get("xpub6DEe2bJAU7GbUw3HDGPUY9c77mUcP9xvAWEhx9GReuJM9gppeGxHqBcaYAfrsyY8R6cfVRsuFhi2PokQFYLEQBVpM8p4MTLzEHpVu4SWq9a").intValue());
        Assert.assertEquals(3, map.get("xpub6DEe2bJAU7GbW4d8d8Cfckg8kbHinDUQYHvXk3AobXNDYwGhaKZ1wZxGCBq67RiYzT3UuQjS3Jy3SGM3b9wz7aHVipE3Bg1HXhLguCgoALJ").intValue());
        Assert.assertEquals(4, map.get("xpub6DEe2bJAU7GbYjCHygUwVDJYv5fjCUyQ1AHvkM1ecRL2PZ7vYv9a5iRiHjxmRgi3auyaA9NSAw88VwHm4hvw4C8zLbuFjNBcw2Cx7Ymq5zk").intValue());
    }

    @Test
    public void getAccountIndexToXpubMap() throws Exception {

        URI uri = getClass().getClassLoader().getResource("hd_wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        HDWallet hdWallet = HDWallet.fromJson(body);

        Map<Integer, String> map = hdWallet.getAccountIndexToXpubMap();

        Assert.assertEquals("xpub6DEe2bJAU7GbP12FBdsBckUkGPzQKMnZXaF2ajz2NCFfYJMEzb5G3oGwYrE6WQjnjhLeB6TgVudV3B9kKtpQmYeBJZLRNyXCobPht2jPUBm", map.get(0));
        Assert.assertEquals("xpub6DEe2bJAU7GbQcGHvqgJ4T6pzZUU8j1WqLPyVtaWJFewfjChAKtUX5uRza9rabc6rAgFhXptveBmaoy7ptVGgbYT8KKaJ9E7wmyj5o4aqvr", map.get(1));
        Assert.assertEquals("xpub6DEe2bJAU7GbUw3HDGPUY9c77mUcP9xvAWEhx9GReuJM9gppeGxHqBcaYAfrsyY8R6cfVRsuFhi2PokQFYLEQBVpM8p4MTLzEHpVu4SWq9a", map.get(2));
        Assert.assertEquals("xpub6DEe2bJAU7GbW4d8d8Cfckg8kbHinDUQYHvXk3AobXNDYwGhaKZ1wZxGCBq67RiYzT3UuQjS3Jy3SGM3b9wz7aHVipE3Bg1HXhLguCgoALJ", map.get(3));
        Assert.assertEquals("xpub6DEe2bJAU7GbYjCHygUwVDJYv5fjCUyQ1AHvkM1ecRL2PZ7vYv9a5iRiHjxmRgi3auyaA9NSAw88VwHm4hvw4C8zLbuFjNBcw2Cx7Ymq5zk", map.get(4));
    }
}