package info.blockchain.wallet_rafactored.transaction;

import info.blockchain.wallet.send.UnspentOutputsBundle;

public interface AdvancedTransaction {
    void cacheUnspentOutputs(String address);
    void clearCachedUnspentOutputs();
    UnspentOutputsBundle getFreshCoins(String address);
    UnspentOutputsBundle getCachedCoins(String address);
    void someExtraMethodForAdvancedTx();
}
