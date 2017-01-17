package info.blockchain.api;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WalletEndpoints {

    @POST("wallet")
    Call<Void> postMdidRegistration(@Query("method") String method,
                                    @Query("guid") String guid,
                                    @Query("sharedKey") String sharedKey,
                                    @Query("payload") String payload,
                                    @Query("length") int length);

}