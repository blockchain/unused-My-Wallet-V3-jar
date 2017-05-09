package info.blockchain.wallet.api;

import info.blockchain.wallet.api.data.FeeOptions;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface FeeEndpoints {

    @GET
    Observable<FeeOptions> getFeeOptions(@Url String url);

}
