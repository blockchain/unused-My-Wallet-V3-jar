package info.blockchain.wallet.metadata;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MockInterceptor implements Interceptor {

    String responseString = "";
    int responseCode = 200;
    boolean ioException = false;

    public void setResponseString(String response) {
        responseString = response;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setIOException(boolean throwException){
        ioException = throwException;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        if(ioException)
            throw new IOException();

        final HttpUrl uri = chain.request().url();
        final String query = uri.query();
        final String method = chain.request().method();

//        System.out.println(uri);
//        System.out.println(query);
//        System.out.println(method);
//
//        System.out.println("responseCode: "+responseCode);
//        System.out.println("responseString: "+responseString);

        Response response = new Response.Builder()
                .code(responseCode)
                .message(responseString)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), responseString.getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        return response;
    }
}
