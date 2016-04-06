package info.blockchain.wallet.transaction;

import info.blockchain.wallet.transaction.data.UnspentOutputs;
import org.json.JSONObject;

public interface SimpleTransaction {
    void cacheUnspentOutputs(String address, JSONObject unspentOutputsJson);
    void clearCachedUnspentOutputs();
    UnspentOutputs getCachedCoins(String address) throws Exception;
    UnspentOutputs getCoins(String address, JSONObject unspentOutputsJson);
}
