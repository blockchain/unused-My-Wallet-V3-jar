package info.blockchain.wallet;

import retrofit2.Retrofit;

/**
 * Class for initializing an instance of the HDWallet JAR
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

    public static Retrofit getRetrofitSFOXInstance() {
        return blockchainInterface.getRetrofitSFOXInstance();
    }

    public static Retrofit getRetrofitCoinifyInstance() {
        return blockchainInterface.getRetrofitCoinifyInstance();
    }

    public static String getApiCode() {
        return blockchainInterface.getApiCode();
    }

    public static String getDevice() {
        return blockchainInterface.getDevice();
    }

    public static String getAppVersion() {
        return blockchainInterface.getAppVersion();
    }
}
