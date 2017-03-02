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
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
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
        responseList.add("{\n"
            + "    \"xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 23,\n"
            + "        \"total_received\": 22154257\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 20,\n"
            + "        \"total_received\": 18192818\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 36,\n"
            + "        \"total_received\": 12099702\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 20,\n"
            + "        \"total_received\": 11963629\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQCgxA541qm9qZ9VrGLScde4zsAMj2d15ewiMysCAnbgvSDSZXhFUdsyA2BfzzMrMFJbC4VSkXbzrXLZRitAmUVURmivxxqMJ\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 20,\n"
            + "        \"total_received\": 15137242\n"
            + "    }\n"
            + "}");
        responseList.add("{\n"
            + "    \"xpub6BiVtCpG9fQQGq7bXBjjf5zyguEXHrmxDu4t7pdTFUtDWD5epi4ecKmWBTMHvPQtRmQnby8gET7ArTzxjL4SNYdD2RYSdjk7fwYeEDMzkce\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 2,\n"
            + "        \"total_received\": 4242108\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQUGTtsZvQdWaXHNmNd1Rzo8C8kfhzJQsLw1nijQ3HNSGMrLyMygHMvRTv9SL7o29hMPrtC32vfoW3NkGjCETYZpH4s6isLX3\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQNBuKZoKzhzmENDKdCeXQsNVPF2Ynt8rhyYznmPURQNDmnNnX9SYahZ1DVTaNtsh3pJ4b2jKvsZhpv2oVj76YETCGztKJ3LM\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 9,\n"
            + "        \"total_received\": 4346308\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQXPkGarFwhcPbhRN5TEfpCfHPe37cdG7iYgYMjt85hZ1HHPAbqYneHs4bZtJ47dGRncD2z5q1aix83zgjEwQ3KkNuyyK8eFx\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQdziwDT8EyYPLnuXs14FwNZqGHhMzPDMdLKc97agwFKMb3FfiweRsnqkeHYymF31RJc9EozZxHUSHzkjQ2H9SKGe7GmRDGPM\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQV7PkQJFKHKs2BQVYJ2k7bF8E2dTtqb61viou61EaAm2McoArGW2pjfe8wxLmESVEcDo4pHLLe2KZkLthXBXBR8rvem35ZnN\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQDvwDNekCEzAr3gYcoGXEF27bMwSBsCVP3bJYdUZ6m3jhv9vSG7hVxff3VEfnfK4fcMr2YRwfTfHcJwM4ioS6Eiwnrm1wcuf\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 9,\n"
            + "        \"total_received\": 4785453\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQamLupKW3xzULucDGpsp3KgKfVdfmP65MJPJ6bU7UuaKBZeUYQW58hU5iAKEdMJHeQNsMEquLMf8he4M6wZ3fA6P1vAHGdhH\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQJXDcLwQU1cXECNqaGYb3nNSu1ZEuwFKMXjDbCni6eMhN6rFkdxQsgF1amKAqeLSN63zrYPKJ3GU2ppowBWZSdGBk7QUxgLV\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 4,\n"
            + "        \"total_received\": 4285772\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQR6cSuFeDaSvCDgNvNme499JUGX4RHDiZVWwZy9NwNieWKXHLe8XRbdrEmY87aqztBCbRJkXWV7VJB96XBT5cpkqYMHwvLWB\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    }\n"
            + "}");
        responseList.add("{\n"
            + "    \"xpub6BiVtCpG9fQR4Bp1D4k4P1a48uHPJPtHmnHjrvwpZgg47sJfg9e5wqjEVZs1YdhR3EsfWo16qPcA7fsk6Hzr5e8VAjNbgmVy67DGkoGJfv4\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQmHu21ccttmBpbz5uT8zUQ5nXoTBkMbJBAZ35KTZ9bCi6ChqHZFUc6D2UnrZwLWZZqye9GDtRDw8T9kxEt13fN2UVFBgBEzJ\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRBDv37eyBUDVV4Wpp5w5G1ZdCBwv3cBEUor71SXG48SqYtKccateyEfjoRwDYSojk8XDkBaK6HrGt4A68oJzb536gPQG5c36\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRCXQmCHhnL9AqkNyVEesEsP7xunYZrtboZpqUEne9MQqGc9dZDryV27179yfD9rRQsxErUwwEgDKz1EJLS9i1sh6XPG8yoH6\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRSxhKnoTeamr7c6LnWkFqUASymUyfga1r2sjttqqyjXk5N5ec36HfD1XL2475EwMsN3pSyvDhuqU7v4Rv6mryVNGjSrhzFc1\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQgFPkux7cvyVgyAwWWYRG935BDBYTcXEJGnr1H3vfTfaA8Zg2pRPKxLPKRSY9ztrirhhD2Ud4KKeR11oWpomvNUY8jgXcSWN\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQrFR7q8AQCrDwH5ZiPv9ozL6xg4eiXCGCTDYmw4uZkZgYDfaS42jeA2tjMWQ2vzaHtRjFTTMYCYpGEsgFku324rRdMDckp1i\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRPvqDD7BCc9fCy1WhR7h7N3QnVPHW3QrhmXXrzpUnWogvr2x6ekxX1jCCvyDo11jz75zzC6AS2TU5DE7CcV6xaLt1X8h3iPe\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQwh4qKjcJnN2NdZWDqKjKsAMFMbmrsgAKSQBhZDc5cubR5ZhoBt51jjZY79NYpVpRJBWJAHsnaXtT6uqQ6Ps9d1YkDpY7QH2\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQhRocxvUvFdoG8FJqm8PYPjgGKrryMpRDCcHjzXkYbjuEDFdPRKUv7jJ5H2FUuCFLY2FBNA7gosDpf36coCvBtc48DkoqX5M\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRVMuTAkEfkJmL7vsFbw8cRtexWLnG98KSnHy4akwmQrLUnszJL4TTvuvKgtCGr6s7fS8py5ZaGfBupSxy8qynUUg9ynP66L8\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQR6krjJPX1Gi9R5aPzGHCgjL6CEEeYRemjYPuTocawpXgJLMNbJvrToG94hVpT4RxakNde8UEnePB2rJKheeUd89dvaHK7cnT\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRLT8VREWcckFJD7syF6hF6W7PKcbyjKpQdh2Aj46zm2nXaLRJmTak6E9VBq2c5ZDMuJNU4dvkEsRfZXXQL7Agxwy2pURfWBA\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQxjv3ciBgJguCKHLN25rUzZDnDPRLssQVqtZJTgRFPNMXpkE5RUSBxiBx4twZ9ecfHtNUnxvsURC9whqRazKa6ziYSC9rvEj\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQR2aaiVePK2BVjSwVa8uNe3uKTKZEtagRqarGM4BHcFV7K6KS5cUU5DkFJLDvwH289DskNRysWZZtmBKZb4fTCXQgNjtrwsun\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRGMmvS6eCvosDg67t6hwX5pp3cLLNUvsoTHHpK2Yf6cYRJgK6XYVzrHHV5YL42kdUaz1oQdbyTdJCfpiehde5r6SVJHUHs8A\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQprM8465d1xUgyrh1KY7UrkPa9f5pt58znTb4rf1bgvvGgxj3ASXvcn9yyGqinFcV7n5LpW5vAg95k99zYZ5BXFQS4J7yZw9\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRJVkYhiHDzuZyDeCmch3agVDwV9ryfx87gPBXa7LbSHWM9sn9aFmSgouwDLbH2hesXt8VZUrzKdMdMbJ3ayxbfgygqZqRrsV\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRRqpQafDuYL8X1N2ffjW1UhUZ2TU1H8dJhprANMLAuF2bqrK3iPWxHzEvFWQaEga3wkq956WVfZBL1fgNWMyK1YMkQbeTnBt\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQuhhzr1YNBXEPPCLBYVu6FpSYYpeBHaSVBDhavkcTZZr7ZGPULBUzAQ8QaxYtG5U1KyBq61cAP6VedtPXYvXu1ch4rNLDZYd\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    }\n"
            + "}");
        responseList.add("HDWallet successfully synced with server");

        //responses for initializing multi address
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");

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

        LinkedList<String> xpubs = new LinkedList<>();
        xpubs.add("{\n"
            + "    \"xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 23,\n"
            + "        \"total_received\": 22154257\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 20,\n"
            + "        \"total_received\": 18192818\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 36,\n"
            + "        \"total_received\": 12099702\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 20,\n"
            + "        \"total_received\": 11963629\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQCgxA541qm9qZ9VrGLScde4zsAMj2d15ewiMysCAnbgvSDSZXhFUdsyA2BfzzMrMFJbC4VSkXbzrXLZRitAmUVURmivxxqMJ\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 20,\n"
            + "        \"total_received\": 15137242\n"
            + "    }\n"
            + "}");
        xpubs.add("{\n"
            + "    \"xpub6BiVtCpG9fQQGq7bXBjjf5zyguEXHrmxDu4t7pdTFUtDWD5epi4ecKmWBTMHvPQtRmQnby8gET7ArTzxjL4SNYdD2RYSdjk7fwYeEDMzkce\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 2,\n"
            + "        \"total_received\": 4242108\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQUGTtsZvQdWaXHNmNd1Rzo8C8kfhzJQsLw1nijQ3HNSGMrLyMygHMvRTv9SL7o29hMPrtC32vfoW3NkGjCETYZpH4s6isLX3\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQNBuKZoKzhzmENDKdCeXQsNVPF2Ynt8rhyYznmPURQNDmnNnX9SYahZ1DVTaNtsh3pJ4b2jKvsZhpv2oVj76YETCGztKJ3LM\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 9,\n"
            + "        \"total_received\": 4346308\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQXPkGarFwhcPbhRN5TEfpCfHPe37cdG7iYgYMjt85hZ1HHPAbqYneHs4bZtJ47dGRncD2z5q1aix83zgjEwQ3KkNuyyK8eFx\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQdziwDT8EyYPLnuXs14FwNZqGHhMzPDMdLKc97agwFKMb3FfiweRsnqkeHYymF31RJc9EozZxHUSHzkjQ2H9SKGe7GmRDGPM\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQV7PkQJFKHKs2BQVYJ2k7bF8E2dTtqb61viou61EaAm2McoArGW2pjfe8wxLmESVEcDo4pHLLe2KZkLthXBXBR8rvem35ZnN\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQDvwDNekCEzAr3gYcoGXEF27bMwSBsCVP3bJYdUZ6m3jhv9vSG7hVxff3VEfnfK4fcMr2YRwfTfHcJwM4ioS6Eiwnrm1wcuf\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 9,\n"
            + "        \"total_received\": 4785453\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQamLupKW3xzULucDGpsp3KgKfVdfmP65MJPJ6bU7UuaKBZeUYQW58hU5iAKEdMJHeQNsMEquLMf8he4M6wZ3fA6P1vAHGdhH\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQJXDcLwQU1cXECNqaGYb3nNSu1ZEuwFKMXjDbCni6eMhN6rFkdxQsgF1amKAqeLSN63zrYPKJ3GU2ppowBWZSdGBk7QUxgLV\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 4,\n"
            + "        \"total_received\": 4285772\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQR6cSuFeDaSvCDgNvNme499JUGX4RHDiZVWwZy9NwNieWKXHLe8XRbdrEmY87aqztBCbRJkXWV7VJB96XBT5cpkqYMHwvLWB\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    }\n"
            + "}");
        xpubs.add("{\n"
            + "    \"xpub6BiVtCpG9fQR4Bp1D4k4P1a48uHPJPtHmnHjrvwpZgg47sJfg9e5wqjEVZs1YdhR3EsfWo16qPcA7fsk6Hzr5e8VAjNbgmVy67DGkoGJfv4\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQmHu21ccttmBpbz5uT8zUQ5nXoTBkMbJBAZ35KTZ9bCi6ChqHZFUc6D2UnrZwLWZZqye9GDtRDw8T9kxEt13fN2UVFBgBEzJ\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRBDv37eyBUDVV4Wpp5w5G1ZdCBwv3cBEUor71SXG48SqYtKccateyEfjoRwDYSojk8XDkBaK6HrGt4A68oJzb536gPQG5c36\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRCXQmCHhnL9AqkNyVEesEsP7xunYZrtboZpqUEne9MQqGc9dZDryV27179yfD9rRQsxErUwwEgDKz1EJLS9i1sh6XPG8yoH6\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRSxhKnoTeamr7c6LnWkFqUASymUyfga1r2sjttqqyjXk5N5ec36HfD1XL2475EwMsN3pSyvDhuqU7v4Rv6mryVNGjSrhzFc1\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQgFPkux7cvyVgyAwWWYRG935BDBYTcXEJGnr1H3vfTfaA8Zg2pRPKxLPKRSY9ztrirhhD2Ud4KKeR11oWpomvNUY8jgXcSWN\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQrFR7q8AQCrDwH5ZiPv9ozL6xg4eiXCGCTDYmw4uZkZgYDfaS42jeA2tjMWQ2vzaHtRjFTTMYCYpGEsgFku324rRdMDckp1i\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRPvqDD7BCc9fCy1WhR7h7N3QnVPHW3QrhmXXrzpUnWogvr2x6ekxX1jCCvyDo11jz75zzC6AS2TU5DE7CcV6xaLt1X8h3iPe\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQwh4qKjcJnN2NdZWDqKjKsAMFMbmrsgAKSQBhZDc5cubR5ZhoBt51jjZY79NYpVpRJBWJAHsnaXtT6uqQ6Ps9d1YkDpY7QH2\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQhRocxvUvFdoG8FJqm8PYPjgGKrryMpRDCcHjzXkYbjuEDFdPRKUv7jJ5H2FUuCFLY2FBNA7gosDpf36coCvBtc48DkoqX5M\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRVMuTAkEfkJmL7vsFbw8cRtexWLnG98KSnHy4akwmQrLUnszJL4TTvuvKgtCGr6s7fS8py5ZaGfBupSxy8qynUUg9ynP66L8\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQR6krjJPX1Gi9R5aPzGHCgjL6CEEeYRemjYPuTocawpXgJLMNbJvrToG94hVpT4RxakNde8UEnePB2rJKheeUd89dvaHK7cnT\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRLT8VREWcckFJD7syF6hF6W7PKcbyjKpQdh2Aj46zm2nXaLRJmTak6E9VBq2c5ZDMuJNU4dvkEsRfZXXQL7Agxwy2pURfWBA\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQxjv3ciBgJguCKHLN25rUzZDnDPRLssQVqtZJTgRFPNMXpkE5RUSBxiBx4twZ9ecfHtNUnxvsURC9whqRazKa6ziYSC9rvEj\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQR2aaiVePK2BVjSwVa8uNe3uKTKZEtagRqarGM4BHcFV7K6KS5cUU5DkFJLDvwH289DskNRysWZZtmBKZb4fTCXQgNjtrwsun\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRGMmvS6eCvosDg67t6hwX5pp3cLLNUvsoTHHpK2Yf6cYRJgK6XYVzrHHV5YL42kdUaz1oQdbyTdJCfpiehde5r6SVJHUHs8A\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQprM8465d1xUgyrh1KY7UrkPa9f5pt58znTb4rf1bgvvGgxj3ASXvcn9yyGqinFcV7n5LpW5vAg95k99zYZ5BXFQS4J7yZw9\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRJVkYhiHDzuZyDeCmch3agVDwV9ryfx87gPBXa7LbSHWM9sn9aFmSgouwDLbH2hesXt8VZUrzKdMdMbJ3ayxbfgygqZqRrsV\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQRRqpQafDuYL8X1N2ffjW1UhUZ2TU1H8dJhprANMLAuF2bqrK3iPWxHzEvFWQaEga3wkq956WVfZBL1fgNWMyK1YMkQbeTnBt\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    },\n"
            + "    \"xpub6BiVtCpG9fQQuhhzr1YNBXEPPCLBYVu6FpSYYpeBHaSVBDhavkcTZZr7ZGPULBUzAQ8QaxYtG5U1KyBq61cAP6VedtPXYvXu1ch4rNLDZYd\": {\n"
            + "        \"final_balance\": 0,\n"
            + "        \"n_tx\": 0,\n"
            + "        \"total_received\": 0\n"
            + "    }\n"
            + "}");
        xpubs.add("Save failed");
        mockInterceptor.setResponseStringList(xpubs);

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
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83125.51953450,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S. dollar\",\"conversion\":83125.51953450,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
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
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83125.51953450,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S. dollar\",\"conversion\":83125.51953450,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
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
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83125.51953450,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S. dollar\",\"conversion\":83125.51953450,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getHdWallets().get(0).getAccounts().size());
        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().addAccount("Some Label", null);
        Assert.assertEquals(2, PayloadManager.getInstance().getPayload().getHdWallets().get(0).getAccounts().size());
        mockInterceptor.setResponseString("MyWallet save successful.");
        PayloadManager.getInstance().addAccount("Some Label", null);
        Assert.assertEquals(3, PayloadManager.getInstance().getPayload().getHdWallets().get(0).getAccounts().size());

    }

    @Test
    public void addLegacyAddress() throws Exception {

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("MyWallet save successful.");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83125.51953450,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S. dollar\",\"conversion\":83125.51953450,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("3e2b33d63ba45320f42d2b1de6d7ebd3ea810c35348927fd34424fe9bc53c07a");
        responseList.add("MyWallet save successful");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(2, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

    }

    @Test
    public void setKeyForLegacyAddress() throws Exception {

        LinkedList<String> responseList = new LinkedList<>();
        responseList.add("MyWallet save successful.");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83125.51953450,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S. dollar\",\"conversion\":83125.51953450,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
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
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83125.51953450,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S. dollar\",\"conversion\":83125.51953450,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().addLegacyAddress("Some Label", null);
        Assert.assertEquals(1, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        LegacyAddress existingLegacyAddressBody = PayloadManager.getInstance().getPayload()
            .getLegacyAddressList().get(0);

        //Try non matching ECKey
        ECKey ecKey = new ECKey();
//        legacyAddressBody.setPrivateKey(null);
        mockInterceptor.setResponseString("MyWallet save successful.");

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
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83193.98341112,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S.dollar\",\"conversion\":83193.98341112,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        responseList.add("{\"recommend_include_fee\":true,\"sharedcoin_endpoint\":\"https://api.sharedcoin.com\",\"info\":{\"nconnected\":199,\"conversion\":83125.51953450,\"symbol_local\":{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"U.S. dollar\",\"conversion\":83125.51953450,\"symbolAppearsAfter\":false,\"local\":true},\"symbol_btc\":{\"code\":\"BTC\",\"symbol\":\"BTC\",\"name\":\"Bitcoin\",\"conversion\":100000000.00000000,\"symbolAppearsAfter\":true,\"local\":false},\"latest_block\":{\"block_index\":1467925,\"hash\":\"0000000000000000009f45dca81e45f31f85f0d62ead9f730e2c68049c0b2065\",\"height\":455266,\"time\":1488367871}},\"wallet\":{\"n_tx\":0,\"n_tx_filtered\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0},\"addresses\":[{\"address\":\"xpub6BsQU7jwGZa3rX4ZJ783pBQCRmA7vHxYmvrEbxnMBNojxT8a8dpAKWdUdXhAR9rd2sXU6WFhYtX7oijyxEkyWZvGN6SCwo562houBR3BcXS\",\"n_tx\":0,\"total_received\":0,\"total_sent\":0,\"final_balance\":0,\"gap_limit\":20,\"change_index\":0,\"account_index\":0}],\"txs\":[]}");
        mockInterceptor.setResponseStringList(responseList);
        PayloadManager.getInstance().create("My HDWallet", "name@email.com", "MyTestWallet");

        Assert.assertEquals(0, PayloadManager.getInstance().getPayload().getLegacyAddressList().size());

        responseList = new LinkedList<>();
        responseList.add("cb600366ef7a94b991aa04557fc1d9c272ba00df6b1d9791d71c66efa0ae7fe9");
        responseList.add("MyWallet save successful");
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