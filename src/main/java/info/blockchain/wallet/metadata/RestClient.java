package info.blockchain.wallet.metadata;

import info.blockchain.api.MetadataEndpoints;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RestClient {

    // TODO: 05/12/2016 This class should only be for testing convenience
    private static MetadataEndpoints restService = null;

    public static MetadataEndpoints getClient(OkHttpClient client) {
        if (restService == null) {
            restService = getRetrofitInstance(client).create(MetadataEndpoints.class);
        }
        return restService;
    }

    public static Retrofit getRetrofitInstance(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(MetadataEndpoints.API_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }
}
