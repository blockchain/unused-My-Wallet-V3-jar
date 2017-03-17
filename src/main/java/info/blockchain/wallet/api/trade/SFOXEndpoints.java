package info.blockchain.wallet.api.trade;

import io.reactivex.Observable;
import java.util.Map;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Path;

public interface SFOXEndpoints {

    @GET("v2/partner/{partner_name}/transaction")
    Observable<SFOXResponse> getTransactions(
        @HeaderMap Map<String, String> headers
        , @Path("partner_name") String partnerName);
}
