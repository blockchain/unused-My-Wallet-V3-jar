package info.blockchain.api;

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

    @GET(Urls.Metadata.AUTH)
    Call<Auth> getNonce();

    @POST(Urls.Metadata.AUTH)
    Call<Auth> getToken(@Body HashMap<String, String> body);


    @GET(Urls.Metadata.TRUSTED)
    Call<Trusted> getTrustedList(@Header("Authorization") String jwToken);

    @GET(Urls.Metadata.TRUSTED)
    Call<Trusted> getTrusted(@Header("Authorization") String jwToken, @Query("mdid") String mdid);

    @PUT(Urls.Metadata.TRUSTED + "/{mdid}")
    Call<Trusted> putTrusted(@Header("Authorization") String jwToken, @Path("mdid") String mdid);

    @DELETE(Urls.Metadata.TRUSTED + "/{mdid}")
    Call<ResponseBody> deleteTrusted(@Header("Authorization") String jwToken, @Path("mdid") String mdid);


    @POST(Urls.Metadata.MESSAGES)
    Call<Message> postMessage(@Header("Authorization") String jwToken, @Body Message body);

    @GET(Urls.Metadata.MESSAGES)
    Call<List<Message>> getMessages(@Header("Authorization") String jwToken, @Query("new") Boolean onlyProcessed);

    @GET(Urls.Metadata.MESSAGE + "/{uuid}")
    Call<Message> getMessage(@Header("Authorization") String jwToken, @Path("uuid") String messageId);

    @PUT(Urls.Metadata.MESSAGE + "/{uuid}/processed")
    Call<Void> processMessage(@Header("Authorization") String jwToken, @Path("uuid") String id, @Body MessageProcessRequest body);


    @POST(Urls.Metadata.SHARE)
    Call<Invitation> postShare(@Header("Authorization") String jwToken);

    @POST(Urls.Metadata.SHARE + "/{uuid}")
    Call<Invitation> postToShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);

    @GET(Urls.Metadata.SHARE + "/{uuid}")
    Call<Invitation> getShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);

    @DELETE(Urls.Metadata.SHARE + "/{uuid}")
    Call<Invitation> deleteShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);


    @PUT(Urls.Metadata.METADATA + "/{addr}")
    Call<Void> putMetadata(@Path("addr") String address, @Body MetadataRequest body);

    @GET(Urls.Metadata.METADATA + "/{addr}")
    Call<MetadataResponse> getMetadata(@Path("addr") String address);

    @DELETE(Urls.Metadata.METADATA + "/{addr}")
    Call<Void> deleteMetadata(@Path("addr") String address, @Query("signature") String signature);
}
