package info.blockchain.api;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WalletEndpoints {

    // TODO: 01/12/2016  
    String API_URL = "https://explorer.dev.blockchain.info/";

    @POST("wallet")
    Call<Void> postMdidRegistration(@Query("method") String method,
                                    @Query("guid") String guid,
                                    @Query("sharedKey") String sharedKey,
                                    @Query("payload") String payload,
                                    @Query("length") int length);

}