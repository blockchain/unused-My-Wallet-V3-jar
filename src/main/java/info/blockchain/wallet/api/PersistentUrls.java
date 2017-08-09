package info.blockchain.wallet.api;

import info.blockchain.wallet.BlockchainFramework;

import org.bitcoinj.params.AbstractBitcoinNetParams;

@SuppressWarnings("WeakerAccess")
public class PersistentUrls {

    // Env enum keys
    public static final String KEY_ENV_PROD = "env_prod";
    public static final String KEY_ENV_STAGING = "env_staging";
    public static final String KEY_ENV_DEV = "env_dev";
    public static final String KEY_ENV_TESTNET = "env_testnet";

    // Production API Constants
    public static final String API_URL = "https://api.blockchain.info/";
    public static final String EXPLORER_URL = "https://blockchain.info/";
    public static final String WEBSOCKET_URL = "wss://ws.blockchain.info/inv";

    // Instance
    private static PersistentUrls instance;

    private PersistentUrls() {
        // Empty constructor
    }

    public static PersistentUrls getInstance() {
        if (instance == null) {
            instance = new PersistentUrls();
        }

        return instance;
    }

    public Environment getCurrentEnvironment() {
        return BlockchainFramework.getEnvironment();
    }

    public AbstractBitcoinNetParams getCurrentNetworkParams() {
        return BlockchainFramework.getNetworkParameters();
    }

}
