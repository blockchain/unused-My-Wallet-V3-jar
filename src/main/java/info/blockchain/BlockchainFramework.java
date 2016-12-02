package info.blockchain;

import com.google.gson.Gson;

import retrofit2.Retrofit;

/**
 * Class for initializing an instance of the Wallet JAR
 */
public final class BlockchainFramework {

    private static FrameworkInterface blockchainInterface;

    public static void init(FrameworkInterface frameworkInterface) {
        blockchainInterface = frameworkInterface;
    }

    public static Retrofit getRetrofitApiInstance() {
        return blockchainInterface.getRetrofitApiInstance();
    }

    public static Retrofit getRetrofitServerInstance() {
        return blockchainInterface.getRetrofitServerInstance();
    }

    public static Gson getGsonInstance() {
        return blockchainInterface.getGsonInstance();
    }

}
