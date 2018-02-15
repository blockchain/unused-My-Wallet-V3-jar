package info.blockchain.wallet.api;

import info.blockchain.wallet.api.data.FeeOptions;
import io.reactivex.Observable;
import retrofit2.http.GET;

public interface FeeEndpoints {

    @GET("mempool/fees")
    Observable<FeeOptions> getFeeOptions();

    @GET("eth/fees")
    Observable<FeeOptions> getEthFeeOptions();
}
