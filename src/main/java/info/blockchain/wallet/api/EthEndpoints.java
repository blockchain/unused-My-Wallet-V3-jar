package info.blockchain.wallet.api;

import info.blockchain.wallet.api.data.EthAccount;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface EthEndpoints {

    @GET("eth/account/{address}")
    Single<EthAccount> getEthAccount(@Query("address") String address);

}
