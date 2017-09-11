package info.blockchain.wallet.ethereum;

import info.blockchain.wallet.ethereum.data.EthAddressResponse;

import info.blockchain.wallet.ethereum.data.EthAddressResponseMap;
import java.util.HashMap;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

interface EthEndpoints {

    @GET(EthUrls.ACCOUNT + "/{address}")
    Observable<EthAddressResponseMap> getEthAccount(@Path("address") String address);

    @GET(EthUrls.ACCOUNT + "/{address}" + EthUrls.IS_CONTRACT)
    Observable<HashMap<String, Boolean>> getIfContract(@Path("address") String address);

}
