package info.blockchain.wallet;

import retrofit2.Retrofit;

public interface FrameworkInterface {

    /**
     * Provides an instance of Retrofit with it's base URL set to {@link
     * info.blockchain.wallet.api.PersistentUrls#BASE_API_URL} or its debug/staging/testnet equivalent
     */
    Retrofit getRetrofitApiInstance();

    /**
     * Provides an instance of Retrofit with it's base URL set to {@link
     * info.blockchain.wallet.api.PersistentUrls#BASE_SERVER_URL} or its debug/staging/testnet equivalent
     */
    Retrofit getRetrofitServerInstance();

    /**
     * Provides an ApiCode used for bci platform usage statistics.
     */
    String getApiCode();

    /*
     * Provides device/platform name for analytical use in wallet payload.
     * Flags wallets and addresses as created on a certain platform - for issue debugging.
     */
    String getDevice();

    /*
     * Provides app version for analytical use in wallet payload.
     * Flags wallets and addresses as created on a certain app version - for issue debugging.
     */
    String getAppVersion();
}
