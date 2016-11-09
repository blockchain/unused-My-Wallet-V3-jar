package info.blockchain.api.metadata;

import info.blockchain.api.metadata.request.MessagePostRequest;
import info.blockchain.api.metadata.response.AuthNonceResponse;
import info.blockchain.api.metadata.response.AuthTokenResponse;
import info.blockchain.api.metadata.response.AuthTrustedResponse;
import info.blockchain.api.metadata.response.MessagePostResponse;
import info.blockchain.api.metadata.response.StatusResponse;
import info.blockchain.api.metadata.response.TrustedPutResponse;

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

    String API_URL = "http://localhost:8080/";

    @GET("auth")
    Call<AuthNonceResponse> getNonce();

    @POST("auth")
    Call<AuthTokenResponse> getToken(@Body HashMap<String, String> body);


    @GET("trusted")
    Call<AuthTrustedResponse> getTrustedList(@Header("Authorization") String jwToken);

    @GET("trusted")
    Call<AuthTrustedResponse> getTrusted(@Header("Authorization") String jwToken, @Query("mdid") String mdid);

    @PUT("trusted/{mdid}")
    Call<TrustedPutResponse> putTrusted(@Header("Authorization") String jwToken, @Path("mdid") String mdid);

    @DELETE("trusted/{mdid}")
    Call<StatusResponse> deleteTrusted(@Header("Authorization") String jwToken, @Path("mdid") String mdid);


    @POST("messages")
    Call<MessagePostResponse> postMessage(@Header("Authorization") String jwToken, @Body MessagePostRequest body);

    @GET("messages")
    Call<List<MessagePostResponse>> getMessages(@Header("Authorization") String jwToken, @Query("new") Boolean onlyProcessed);

//    @GET("messages/{mdid}")
//    Call<MessagePostResponse> getMessages(@Header("Authorization") String jwToken, @Path("mdid") String mdid);





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
    getMessages(token, onlyProcessed)               GET /messages?:from             pending
    getMessage(token, uuid)                         GET /messages/:ID
                                                    PATCH /messages/:ID
    setProcessed(token, uuid, processed)            PUT /messages/{uuid}/processed

    //METADATA
    getMetadata(address)                        GET /metadata/{mdid}
    putMetadata(key, payload, type, magic)      PUT /metadata/{mdid}?form
    deleteMetadata(key, magic)                  DELETE /metadata/{mdid}?

    //MDID
    getMDID(token, guid)         GET /mdid/{guid}
    addMDID(token, guid, mdid)   PUT /guid/{mdid}
    getGUID(token, mdid)         GET /guid/{mdid}

    //SHARE
    postShare(token)            POST /share
    postToShare(token, id)      POST /share/{id}
    getShare(id)                GET /share/{id}
    deleteShare(token, id)      DELETE /share/{id}
     */
}
