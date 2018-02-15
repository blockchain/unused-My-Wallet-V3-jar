package info.blockchain.wallet.api;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.data.FeeOptions;
import io.reactivex.Observable;

public class FeeApi {

    private static FeeEndpoints feeEndpoints;

    private FeeEndpoints getBaseApiInstance() {
        if (feeEndpoints == null) {
            feeEndpoints = BlockchainFramework.getRetrofitApiInstance().
                    create(FeeEndpoints.class);
        }
        return feeEndpoints;
    }

    /**
     * Returns a {@link FeeOptions} object for BTC which contains both a "regular" and a "priority" fee
     * option, both listed in Satoshis per byte.
     */
    public Observable<FeeOptions> getFeeOptions() {
        return getBaseApiInstance().getFeeOptions();
    }

    /**
     * Returns a {@link FeeOptions} object for ETH which contains both a "regular" and a "priority" fee
     * option.
     */
    public Observable<FeeOptions> getEthFeeOptions() {
        return getBaseApiInstance().getEthFeeOptions();
    }
}
