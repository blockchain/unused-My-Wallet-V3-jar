package info.blockchain.wallet.metadata;

import info.blockchain.api.MetadataEndpoints;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {

    private static MetadataEndpoints restService = null;

    public static MetadataEndpoints getClient(OkHttpClient client) {

        if (restService == null) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MetadataEndpoints.API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            restService = retrofit.create(MetadataEndpoints.class);
        }
        return restService;
    }
}
