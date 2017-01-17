package info.blockchain.util;

import info.blockchain.api.PersistentUrls;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RestClient {

    public static Retrofit getRetrofitInstance(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(PersistentUrls.getInstance().getCurrentBaseApiUrl())
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }
}
