package info.blockchain.wallet.metadata;

import info.blockchain.wallet.metadata.data.Auth;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.Message;
import info.blockchain.wallet.metadata.data.MessageProcessRequest;
import info.blockchain.wallet.metadata.data.MetadataRequest;
import info.blockchain.wallet.metadata.data.MetadataResponse;
import info.blockchain.wallet.metadata.data.Trusted;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MetadataEndpoints {

    ///////////////////////////////////////////////////////////////////////////
    // AUTH
    ///////////////////////////////////////////////////////////////////////////

    @GET("iwcs/auth")
    Call<Auth> getNonce();

    @POST("iwcs/auth")
    Call<Auth> getToken(@Body HashMap<String, String> body);


    ///////////////////////////////////////////////////////////////////////////
    // TRUSTED
    ///////////////////////////////////////////////////////////////////////////

    @GET("iwcs/trusted")
    Call<Trusted> getTrustedList(@Header("Authorization") String jwToken);

    @GET("iwcs/trusted")
    Call<Trusted> getTrusted(@Header("Authorization") String jwToken, @Query("mdid") String mdid);

    @PUT("iwcs/trusted/{mdid}")
    Call<Trusted> putTrusted(@Header("Authorization") String jwToken, @Path("mdid") String mdid);

    @DELETE("iwcs/trusted/{mdid}")
    Call<ResponseBody> deleteTrusted(@Header("Authorization") String jwToken, @Path("mdid") String mdid);


    ///////////////////////////////////////////////////////////////////////////
    // MESSAGES
    ///////////////////////////////////////////////////////////////////////////

    @POST("iwcs/messages")
    Call<Message> postMessage(@Header("Authorization") String jwToken, @Body Message body);

    @GET("iwcs/messages")
    Call<List<Message>> getMessages(@Header("Authorization") String jwToken, @Query("new") Boolean onlyProcessed);

    @GET("iwcs/message/{uuid}")
    Call<Message> getMessage(@Header("Authorization") String jwToken, @Path("uuid") String messageId);

    @PUT("iwcs/message/{uuid}/processed")
    Call<Void> processMessage(@Header("Authorization") String jwToken, @Path("uuid") String id, @Body MessageProcessRequest body);


    ///////////////////////////////////////////////////////////////////////////
    // SHARING
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Arbitrary JSON can be sent here, but for now we're not using it for anything so an empty
     * JsonObject can be sent.
     */
    @POST("iwcs/share")
    Call<Invitation> postShare(@Header("Authorization") String jwToken, @Body String jsonData);

    @POST("iwcs/share" + "/{uuid}")
    Call<Invitation> postToShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid, @Body String jsonData);

    @GET("iwcs/share" + "/{uuid}")
    Call<Invitation> getShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);

    @DELETE("iwcs/share" + "/{uuid}")
    Call<Invitation> deleteShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);


    ///////////////////////////////////////////////////////////////////////////
    // CRUD OPERATIONS
    ///////////////////////////////////////////////////////////////////////////

    @PUT("iwcs/{addr}")
    Call<Void> putMetadata(@Path("addr") String address, @Body MetadataRequest body);

    @GET("iwcs/{addr}")
    Call<MetadataResponse> getMetadata(@Path("addr") String address);

    @DELETE("iwcs/{addr}")
    Call<Void> deleteMetadata(@Path("addr") String address, @Query("signature") String signature);
}
