package info.blockchain;

import com.google.gson.Gson;

import retrofit2.Retrofit;

public interface FrameworkInterface {

    Retrofit getRetrofitApiInstance();

    Retrofit getRetrofitServerInstance();

    Gson getGsonInstance();

}
