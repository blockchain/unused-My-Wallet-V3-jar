package info.blockchain.wallet.ethereum;

import info.blockchain.wallet.ethereum.data.EthAccount;

import java.util.HashMap;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface EthEndpoints {

    @GET(EthUrls.ACCOUNT + "/{address}")
    Single<EthAccount> getEthAccount(@Query("address") String address);

    @GET(EthUrls.ACCOUNT + "/{address}" + EthUrls.IS_CONTRACT)
    Single<HashMap<String, Boolean>> getIfContract(@Query("address") String address);

}
