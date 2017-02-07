package info.blockchain.wallet.api;

import info.blockchain.wallet.api.data.FeesResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WalletEndpoints {

    @POST("wallet")
    Call<Void> postToWallet(@Query("method") String method,
                                    @Query("guid") String guid,
                                    @Query("sharedKey") String sharedKey,
                                    @Query("payload") String payload,
                                    @Query("length") int length,
                                    @Query("api_code")String apiCode);

    @GET("fees")
    Call<FeesResponse> getFees();

    @GET("v2/randombytes")
    Call<ResponseBody> getRandomBytes(@Query("bytes")int bytes, @Query("format") String format);
}