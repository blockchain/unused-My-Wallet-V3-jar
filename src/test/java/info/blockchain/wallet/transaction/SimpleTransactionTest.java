package info.blockchain.wallet.transaction;

import info.blockchain.wallet.send.UnspentOutputsBundle;
import info.blockchain.wallet_rafactored.transaction.BaseTransaction;
import info.blockchain.wallet_rafactored.transaction.SimpleTransaction;
import org.junit.Test;

public class SimpleTransactionTest {

    @Test
    public void getUnspentOutputsShouldNotReturnNull() {

        SimpleTransaction transaction = new BaseTransaction();
        UnspentOutputsBundle freshCoins = transaction.getFreshCoins("1rW486AbUx2LapYca7kddpULJVqMGMhTH");
        System.out.println("freshCoins: "+freshCoins);
        assert(freshCoins != null);

        //Todo - advance transaction
//        AdvancedTransaction advTransaction = new BaseTransaction();
//        advTransaction.someExtraMethodForAdvancedTx();
    }

    @Test
    public void cachingUnspentOutputsShouldBeCached() {

        //Cache it
        SimpleTransaction transaction = new BaseTransaction();
        transaction.cacheUnspentOutputs("1rW486AbUx2LapYca7kddpULJVqMGMhTH");

        //Read cached
        UnspentOutputsBundle cachedCoins = transaction.getCachedCoins("1rW486AbUx2LapYca7kddpULJVqMGMhTH");
        System.out.println("getCachedCoins: " + cachedCoins);
        assert(cachedCoins != null);

        UnspentOutputsBundle freshCoins = transaction.getFreshCoins("1rW486AbUx2LapYca7kddpULJVqMGMhTH");
        System.out.println("freshCoins: "+freshCoins);
        assert(freshCoins != null);

        transaction.clearCachedUnspentOutputs();
        cachedCoins = transaction.getCachedCoins("1rW486AbUx2LapYca7kddpULJVqMGMhTH");
        assert(cachedCoins == null);
    }

    @Test
    public void clearingCachedUnspentOutputsShouldReturnEmpty() {

        //Cache it
        SimpleTransaction transaction = new BaseTransaction();
        transaction.cacheUnspentOutputs("1rW486AbUx2LapYca7kddpULJVqMGMhTH");

        //Read cached
        UnspentOutputsBundle cachedCoins = transaction.getCachedCoins("1rW486AbUx2LapYca7kddpULJVqMGMhTH");

        //Clear cache
        transaction.clearCachedUnspentOutputs();
        cachedCoins = transaction.getCachedCoins("1rW486AbUx2LapYca7kddpULJVqMGMhTH");
        assert(cachedCoins == null);
    }
}