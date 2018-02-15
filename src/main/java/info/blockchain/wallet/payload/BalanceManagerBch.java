package info.blockchain.wallet.payload;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.blockexplorer.FilterType;
import info.blockchain.api.data.Balance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

public class BalanceManagerBch extends BalanceManager {

    private static Logger log = LoggerFactory.getLogger(BalanceManagerBch.class);

    public BalanceManagerBch(BlockExplorer blockExplorer) {
        super(blockExplorer);
    }

    @Override
    public Call<HashMap<String, Balance>> getBalanceOfAddresses(List<String> addresses) {
        return getBlockExplorer().getBalance("bch", addresses, FilterType.RemoveUnspendable);
    }

    @Override
    Logger getLog() {
        return log;
    }

}
