package info.blockchain.wallet;

import info.blockchain.wallet.api.Environment;

import org.bitcoinj.params.AbstractBitcoinNetParams;

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

    public static Retrofit getRetrofitExplorerInstance() {
        return blockchainInterface.getRetrofitExplorerInstance();
    }

    public static Environment getEnvironment() {
        return blockchainInterface.getEnvironment();
    }

    public static AbstractBitcoinNetParams getNetworkParameters() {
        return blockchainInterface.getNetworkParameters();
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
