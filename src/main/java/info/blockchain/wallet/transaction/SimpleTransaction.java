package info.blockchain.wallet.transaction;

import info.blockchain.wallet.transaction.data.SpendableUnspentOutputs;
import info.blockchain.wallet.transaction.data.UnspentOutputs;
import org.json.JSONObject;

import java.math.BigInteger;

public interface SimpleTransaction {
    void cacheUnspentOutputs(String address, JSONObject unspentOutputsJson);
    void clearCachedUnspentOutputs();
    UnspentOutputs getCachedCoins(String address) throws Exception;
    UnspentOutputs getCoins(JSONObject unspentOutputsJson);
    BigInteger getSweepBalance(UnspentOutputs coins, BigInteger feePerKb);
    BigInteger getSweepFee(UnspentOutputs coins, BigInteger feePerKb);
    SpendableUnspentOutputs getSpendableCoins(UnspentOutputs coins, BigInteger spendAmount, BigInteger feePerKb);
}
