package info.blockchain.wallet.api;

import info.blockchain.wallet.api.data.FeeList;
import info.blockchain.wallet.api.data.Merchant;
import info.blockchain.wallet.api.data.Settings;
import info.blockchain.wallet.api.data.Status;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.List;

@SuppressWarnings("SameParameterValue")
public interface WalletEndpoints {

    @FormUrlEncoded
    @POST("wallet")
    Observable<ResponseBody> postToWallet(
        @Field("method") String method,
        @Field("guid") String guid,
        @Field("sharedKey") String sharedKey,
        @Field("payload") String payload,
        @Field("length") int length,
        @Field("api_code") String apiCode);

    @FormUrlEncoded
    @POST("wallet")
    Observable<Settings> fetchSettings(
        @Field("method") String method,
        @Field("guid") String guid,
        @Field("sharedKey") String sharedKey,
        @Field("format") String format,
        @Field("api_code") String apiCode);

    @FormUrlEncoded
    @POST("wallet")
    Observable<ResponseBody> updateSettings(
        @Field("method") String method,
        @Field("guid") String guid,
        @Field("sharedKey") String sharedKey,
        @Field("payload") String payload,
        @Field("length") int length,
        @Field("format") String format,
        @Field("api_code") String apiCode);

    @FormUrlEncoded
    @POST("wallet")
    Call<ResponseBody> fetchWalletData(
        @Field("method") String method,
        @Field("guid") String guid,
        @Field("sharedKey") String sharedKey,
        @Field("format") String format,
        @Field("api_code") String apiCode);

    @FormUrlEncoded
    @POST("wallet")
    Observable<ResponseBody> syncWallet(
        @Field("method") String method,
        @Field("guid") String guid,
        @Field("sharedKey") String sharedKey,
        @Field("payload") String payload,
        @Field("length") int length,
        @Field("checksum") String checksum,
        @Field("active") String active,
        @Field("email") String email,
        @Field("device") String device,
        @Field("old_checksum") String old_checksum,
        @Field("api_code") String apiCode);

    @FormUrlEncoded
    @POST("wallet")
    Call<ResponseBody> syncWalletCall(
            @Field("method") String method,
            @Field("guid") String guid,
            @Field("sharedKey") String sharedKey,
            @Field(value = "payload", encoded = false) String payload,
            @Field("length") int length,
            @Field("checksum") String checksum,
            @Field("active") String active,
            @Field("email") String email,
            @Field("device") String device,
            @Field("old_checksum") String old_checksum,
            @Field("api_code") String apiCode);

    @FormUrlEncoded
    @POST("wallet")
    Call<ResponseBody> fetchPairingEncryptionPasswordCall(
        @Field("method") String method,
        @Field("guid") String guid,
        @Field("api_code") String apiCode);

    @FormUrlEncoded
    @POST("wallet")
    Observable<ResponseBody> fetchPairingEncryptionPassword(
            @Field("method") String method,
            @Field("guid") String guid,
            @Field("api_code") String apiCode);

    @GET("wallet/{guid}?format=json&resend_code=false")
    Observable<Response<ResponseBody>> getSessionId(
            @Path("guid") String guid);

    @GET("wallet/{guid}")
    Observable<Response<ResponseBody>> fetchEncryptedPayload(
        @Path("guid") String guid,
        @Header("cookie") String sessionId,
        @Query("format") String format,
        @Query("resend_code") boolean resendCode,
        @Query("api_code") String apiCode);

    @GET("fees")
    Observable<FeeList> getFees();

    @GET("v2/randombytes")
    Call<ResponseBody> getRandomBytesCall(
        @Query("bytes") int bytes,
        @Query("format") String format);

    @GET("v2/randombytes")
    Observable<ResponseBody> getRandomBytes(
            @Query("bytes") int bytes,
            @Query("format") String format);

    @POST("pin-store")
    Observable<Response<Status>> pinStore(
        @Query("key") String key,
        @Query("pin") String pin,
        @Query("value") String value,
        @Query("method") String method,
        @Query("api_code") String apiCode);

    @GET("merchant")
    Observable<List<Merchant>> getAllMerchants();

    @GET("frombtc")
    Observable<ResponseBody> getHistoricPrice(
        @Query("value") long value,
        @Query("currency") String currency,
        @Query("time") long time,
        @Query("api_code") String apiCode);

    @GET("event")
    Observable<Status> logEvent(
        @Query("name") String name,
        @Query("api_code") String apiCode);
}