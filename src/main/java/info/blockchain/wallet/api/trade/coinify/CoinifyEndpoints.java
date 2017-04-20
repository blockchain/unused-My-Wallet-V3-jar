package info.blockchain.wallet.api.trade.coinify;

import info.blockchain.wallet.api.trade.coinify.data.CoinifyTrade;
import io.reactivex.Observable;
import java.util.List;
import java.util.Map;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Path;

public interface CoinifyEndpoints {

    @GET("trades")
    Observable<List<CoinifyTrade>> getTrades(
        @HeaderMap Map<String, String> headers);

    @GET("trades/{id}")
    Observable<CoinifyTrade> getTradeInformation(
        @HeaderMap Map<String, String> headers
        , @Path("id") long id);
}
