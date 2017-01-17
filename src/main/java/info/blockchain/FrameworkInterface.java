package info.blockchain;

import retrofit2.Retrofit;

public interface FrameworkInterface {

    /**
     * Provides an instance of Retrofit with it's base URL set to {@link
     * info.blockchain.api.PersistentUrls#BASE_API_URL} or its debug/staging/testnet equivalent
     */
    Retrofit getRetrofitApiInstance();

    /**
     * Provides an instance of Retrofit with it's base URL set to {@link
     * info.blockchain.api.PersistentUrls#BASE_SERVER_URL} or its debug/staging/testnet equivalent
     */
    Retrofit getRetrofitServerInstance();

}
