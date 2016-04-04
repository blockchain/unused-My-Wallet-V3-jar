package info.blockchain.wallet_rafactored.transaction;

import info.blockchain.wallet.send.UnspentOutputsBundle;

public interface SimpleTransaction {
    void cacheUnspentOutputs(String address);
    void clearCachedUnspentOutputs();
    UnspentOutputsBundle getFreshCoins(String address);
    UnspentOutputsBundle getCachedCoins(String address);
}
