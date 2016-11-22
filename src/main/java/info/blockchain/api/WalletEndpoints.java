package info.blockchain.api;

import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

interface WalletEndpoints {

    String API_URL = "https://explorer.dev.blockchain.co.uk/";

    @POST("wallet")
    @Headers("Origin: http://localhost:8080")
    Call<Void> postMdidRegistration(@Query("method") String method,
                                    @Query("guid") String guid,
                                    @Query("sharedKey") String sharedKey,
                                    @Query("payload") String payload,
                                    @Query("length") int length);

}