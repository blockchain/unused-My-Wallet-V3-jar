package info.blockchain.wallet.metadata;

import java.io.IOException;

import java.util.LinkedList;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MockInterceptor implements Interceptor {

    LinkedList<String> responseStringList;
    LinkedList<Integer> responseCodeList;
    boolean ioException = false;

    public void setResponseStringList(LinkedList<String> responseStringList){
        this.responseStringList = responseStringList;
    }

    public void setResponseString(String response) {

        this.responseStringList = new LinkedList<>();
        this.responseStringList.add(response);
    }

    public void setResponseCodeList(LinkedList<Integer> responseCodeList){
        this.responseCodeList = responseCodeList;
    }

    public void setResponseCode(int responseCode) {

        this.responseCodeList = new LinkedList<>();
        this.responseCodeList.add(responseCode);
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

        String responseString = responseStringList.getFirst();
        if(responseCodeList == null || responseCodeList.size() == 0) {
            responseCodeList = new LinkedList<>();
            responseCodeList.add(200);
        }

        int responseCode = responseCodeList.getFirst();

        Response response = new Response.Builder()
                .code(responseCode)
                .message(responseString)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), responseString.getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        //Reset responses
        responseStringList.removeFirst();
        responseCodeList.removeFirst();

        return response;
    }
}
