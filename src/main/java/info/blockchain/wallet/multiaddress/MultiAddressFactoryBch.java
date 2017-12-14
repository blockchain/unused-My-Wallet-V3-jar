package info.blockchain.wallet.multiaddress;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.blockexplorer.FilterType;
import info.blockchain.api.data.MultiAddress;

import java.util.List;

import retrofit2.Call;

public class MultiAddressFactoryBch extends MultiAddressFactory {

    public MultiAddressFactoryBch(BlockExplorer blockExplorer) {
        super(blockExplorer);
    }

    @Override
    protected Call<MultiAddress> getMultiAddress(List<String> allActive, int limit, int offset, String context) {
        return getBlockExplorer().getMultiAddress("bch", allActive, context, FilterType.RemoveUnspendable, limit, offset);
    }

}
