package info.blockchain.wallet.transaction;

import info.blockchain.api.Unspent;
import info.blockchain.wallet.transaction.data.UnspentOutputs;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class SimpleTransactionTest {

    String address = "xpub6DHgYHmEhNxMrZP3rvfSnnPHVi5X2H4h4HLmBnRPHyZ2krP9SKrhEGNFUFRvDwGEAX3KTdD27TQFsTfyjAiCqBvnqWcRwUKZBe5X9MsKkZT";
    JSONObject unspentJson = null;


    @Before
    public void meh() throws Exception {
        unspentJson = Unspent.getUnspentOutputs(address);
    }

    @Test
    public void testGetCachedCoins() throws Exception {

        //Cache it
        SimpleTransaction transaction = new BaseTransaction();
        transaction.cacheUnspentOutputs(address, unspentJson);

        //Read cached
        UnspentOutputs cachedCoins = transaction.getCachedCoins(address);
        assert(cachedCoins != null);
    }

    @Test
    public void testGetCoins() {

        SimpleTransaction transaction = new BaseTransaction();
        UnspentOutputs coins = transaction.getCoins(address, unspentJson);
        assert(coins != null);
    }
}