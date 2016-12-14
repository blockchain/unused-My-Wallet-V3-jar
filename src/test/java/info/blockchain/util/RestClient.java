package info.blockchain.util;

import info.blockchain.api.MetadataEndpoints;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RestClient {

    public static Retrofit getRetrofitInstance(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(MetadataEndpoints.API_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }
}
