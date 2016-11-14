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

    //Broken
    @PUT("message/{uuid}")
    Call<Message> processMessage(@Header("Authorization") String jwToken, @Path("uuid") String messageId, @Query("processed") boolean processed);


    @POST("share")
    Call<Share> postShare(@Header("Authorization") String jwToken);

    @POST("share/{uuid}")
    Call<Share> postToShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);

    @GET("share/{uuid}")
    Call<Share> getShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);

    @DELETE("share/{uuid}")
    Call<Share> deleteShare(@Header("Authorization") String jwToken, @Path("uuid") String uuid);


    /*
    //INFO
    getHealth()                 GET /healthz    ignore
    getStats()                  GET /statz      ignore

    //AUTH
    getNonce()                   GET /auth   ✓
    getToken(key)                POST /auth  ✓

    //TRUSTED
    getTrustedList(token)               GET /trusted            ✓
    getTrusted(token, mdid)             GET /trusted/{mdid}     ✓
    putTrusted(token, address)          PUT /trusted/{mdid}     ✓
    deleteTrusted(token, mdid)          DELETE /trusted/{mdid}  ✓

    //MESSAGES
    postMessage(key, token, address, msg, type)     POST /messages                  ✓
    getMessages(token, onlyProcessed)               GET /messages?:from             ✓
    getMessages(token, uuid)                         GET /messages/:ID               ✓
    processMessage(token, uuid)                     PATCH /messages/:ID
    setProcessed(token, uuid, processed)            PUT /messages/{uuid}/processed  nope

    //METADATA
    getMetadata(address)                        GET /metadata/{mdid}
    putMetadata(key, payload, type, magic)      PUT /metadata/{mdid}?form
    deleteMetadata(key, magic)                  DELETE /metadata/{mdid}?

    //MDID - needs admin?
    getMDID(token, guid)         GET /mdid/{guid}
    addMDID(token, guid, mdid)   PUT /guid/{mdid}
    getGUID(token, mdid)         GET /guid/{mdid}

    //SHARE
    postShare(token)            POST /share             ✓
    postToShare(token, id)      POST /share/{id}        ✓
    getShare(id)                GET /share/{id}         ✓
    deleteShare(token, id)      DELETE /share/{id}      ✓
     */
}
