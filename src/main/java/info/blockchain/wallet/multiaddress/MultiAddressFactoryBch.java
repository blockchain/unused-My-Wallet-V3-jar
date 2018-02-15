package info.blockchain.wallet.multiaddress;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.blockexplorer.FilterType;
import info.blockchain.api.data.MultiAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import retrofit2.Call;

public class MultiAddressFactoryBch extends MultiAddressFactory {

    private static Logger log = LoggerFactory.getLogger(MultiAddressFactoryBch.class);

    public MultiAddressFactoryBch(BlockExplorer blockExplorer) {
        super(blockExplorer);
    }

    @Override
    protected Call<MultiAddress> getMultiAddress(List<String> allActive, int limit, int offset, String context) {
        return getBlockExplorer().getMultiAddress("bch", allActive, context, FilterType.RemoveUnspendable, limit, offset);
    }

    @Override
    Logger getLog() {
        return log;
    }

}
