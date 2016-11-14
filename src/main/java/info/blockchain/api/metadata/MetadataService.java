package info.blockchain.api.metadata;

import info.blockchain.api.metadata.data.Auth;
import info.blockchain.api.metadata.data.Message;
import info.blockchain.api.metadata.data.Share;
import info.blockchain.api.metadata.data.Status;
import info.blockchain.api.metadata.data.Trusted;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface MetadataService {

    String API_URL = "https://api.dev.blockchain.co.uk/metadata/";

    @GET("auth")
    Call<Auth> getNonce();

    @POST("auth")
    Call<Auth> getToken(@Body HashMap<String, String> body);


    @GET("trusted")
    Call<Trusted> getTrustedList(@Header("Authorization") String jwToken);

    @GET("trusted")
    Call<Trusted> getTrusted(@Header("Authorization") String jwToken, @Query("mdid") String mdid);

    @PUT("trusted/{mdid}")
    Call<Trusted> putTrusted(@Header("Authorization") String jwToken, @Path("mdid") String mdid);

    @DELETE("trusted/{mdid}")
    Call<Status> deleteTrusted(@Header("Authorization") String jwToken, @Path("mdid") String mdid);


    @POST("messages")
    Call<Message> postMessage(@Header("Authorization") String jwToken, @Body Message body);

    @GET("messages")
    Call<List<Message>> getMessages(@Header("Authorization") String jwToken, @Query("new") Boolean onlyProcessed);

    @GET("messages")
    Call<List<Message>> getMessages(@Header("Authorization") String jwToken, @Query("uuid") String messageId);

    @GET("message/{uuid}")
    Call<Message> getMessage(@Header("Authorization") String jwToken, @Path("uuid") String messageId);


    @POST("share")
    Call<Share> postShare(@Header("Authorization") String jwToken);

    @POST("share/{uuid}")
    Call<Share> postToShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);

    @GET("share/{uuid}")
    Call<Share> getShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);

    @DELETE("share/{uuid}")
    Call<Share> deleteShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);
}
