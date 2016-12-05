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

    public void setResponse_404() {
        responseString = "{\"message\":\"Not Found\"}";
        responseCode = 404;
    }

    public void setResponse_PUT_rage(){
        responseString = "{\"version\":1,\"payload\":\"UmFnZSByYWdl\",\"signature\":\"HwIx4Cs+1pB+8iCDREC1PiDqkDnEDhfcto6bQjxzo3RrHB562kg8nTjbFoaydlUI6tDkl3WnZahrmFZ8ErNqmBY=\",\"type_id\":1}";
        responseCode = 200;
    }

    public void setResponse_GET_rage(){
        responseString = "{\"payload\":\"UmFnZSByYWdl\",\"version\":1,\"type_id\":1,\"signature\":\"HwIx4Cs+1pB+8iCDREC1PiDqkDnEDhfcto6bQjxzo3RrHB562kg8nTjbFoaydlUI6tDkl3WnZahrmFZ8ErNqmBY=\",\"created_at\":1480592845000,\"updated_at\":1480592845000,\"address\":\"1ErzrzB1FE1YyQ7LADMzye9J3Q8QeR1mja\"}";
        responseCode = 200;
    }

    public void setResponse_PUT_more_rage(){
        responseString = "{\"version\":1,\"payload\":\"UmFnZSByYWdlIHNvbWUgbW9yZQ==\",\"signature\":\"H7zIO7fzkb8t+zdbiEzlKt/8InFjH5N2ja+SaJPcAuheP3soAJwxVrnzG0tDQpxyJKSgYn/9il6XsLW3rmm3a+g=\",\"prev_magic_hash\":\"73d03136dfdadf66b4048f938ad8acf6084134a84ac6f542e0144b29999a6836\",\"type_id\":1}";
        responseCode = 200;
    }

    public void setResponse_GET_more_rage(){
        responseString = "{\"payload\":\"UmFnZSByYWdlIHNvbWUgbW9yZQ==\",\"version\":1,\"type_id\":1,\"signature\":\"H7zIO7fzkb8t+zdbiEzlKt/8InFjH5N2ja+SaJPcAuheP3soAJwxVrnzG0tDQpxyJKSgYn/9il6XsLW3rmm3a+g=\",\"prev_magic_hash\":\"73d03136dfdadf66b4048f938ad8acf6084134a84ac6f542e0144b29999a6836\",\"created_at\":1480592845000,\"updated_at\":1480592845000,\"address\":\"1ErzrzB1FE1YyQ7LADMzye9J3Q8QeR1mja\"}";
        responseCode = 200;
    }

    public void setResponse_DELETE_ok(){
        responseCode = 200;
    }

    public void setResponse_init_magic_1() {
        responseString = "{\"payload\":\"IllvbG8xIg==\",\"version\":1,\"type_id\":2,\"signature\":\"H6OB5DyNcStzh1BGnaZroXvp+L7zLd9OzxGo7bYEO0aDOJznhVVSjXBVW8Hc28wG8359L2yvuOOJGCU1PKZfpo0=\",\"created_at\":1480593578000,\"updated_at\":1480593578000,\"address\":\"12sC9tqHzAhdoukhCbTnyx2MjYXNXBGHnF\"}";
        responseCode = 200;
    }

    public void setResponse_yolo_1(){
        responseString = "{\"version\":1,\"payload\":\"IllvbG8xIg==\",\"signature\":\"H6OB5DyNcStzh1BGnaZroXvp+L7zLd9OzxGo7bYEO0aDOJznhVVSjXBVW8Hc28wG8359L2yvuOOJGCU1PKZfpo0=\",\"type_id\":2}";
        responseCode = 200;
    }

    public void setResponse_init_magic_2() {
        responseString = "{\"payload\":\"IllvbG8yIg==\",\"version\":1,\"type_id\":2,\"signature\":\"IF54TFosfFpbEX2KGf+cC6sieXJMTwyYUEkmRlb4TuVHHCMPZ1GkJow4sVx+hgxItDt91rtQRNKsDDPlHR3fRhw=\",\"prev_magic_hash\":\"6a2d2c5837632f3c0c41acca320d2c2b6ffd9725281f47b776ed61d79d49a4e4\",\"created_at\":1480593578000,\"updated_at\":1480593578000,\"address\":\"12sC9tqHzAhdoukhCbTnyx2MjYXNXBGHnF\"}";
        responseCode = 200;
    }

    public void setResponse_yolo_2(){
        responseString = "{\"version\":1,\"payload\":\"IllvbG8yIg==\",\"signature\":\"IF54TFosfFpbEX2KGf+cC6sieXJMTwyYUEkmRlb4TuVHHCMPZ1GkJow4sVx+hgxItDt91rtQRNKsDDPlHR3fRhw=\",\"prev_magic_hash\":\"6a2d2c5837632f3c0c41acca320d2c2b6ffd9725281f47b776ed61d79d49a4e4\",\"type_id\":2}";
        responseCode = 200;
    }

    public void setResponse_yolo_3(){
        responseString = "{\"version\":1,\"payload\":\"IllvbG8zIg==\",\"signature\":\"IMT7E45CBpfrTfN5e9yJwapXjpxIWdASdmU6rpwYvbWvHlb3rLKinoqtmpI5k5X2fWNMseS6DFofF5+lIIHI/FM=\",\"prev_magic_hash\":\"0391d90d61bd9ede410bd1db84498ee3dd0e6e93fb27e023b67036ee539aabaa\",\"type_id\":2}";
        responseCode = 200;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

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
