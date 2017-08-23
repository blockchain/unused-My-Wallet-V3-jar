package info.blockchain.wallet.ethereum;

import info.blockchain.wallet.ethereum.data.EthAccount;

import java.util.HashMap;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface EthEndpoints {

    @GET("eth/account/{address}")
    Single<EthAccount> getEthAccount(@Query("address") String address);

    @GET("eth/account/{address}/isContract")
    Single<HashMap<String, Boolean>> getIfContract(@Query("address") String address);

}
