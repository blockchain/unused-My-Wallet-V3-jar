package info.blockchain.api;

import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;

@SuppressWarnings("WeakerAccess")
public class PersistentUrls {

    // Env enum keys
    public static final String KEY_ENV_PROD = "env_prod";
    public static final String KEY_ENV_STAGING = "env_staging";
    public static final String KEY_ENV_DEV = "env_dev";
    public static final String KEY_ENV_TESTNET = "env_testnet";

    // Base API Constants
    private static final String BASE_API_URL = "https://api.blockchain.info/";
    private static final String BASE_SERVER_URL = "https://blockchain.info/";
    private static final String BASE_WEBSOCKET_URL = "wss://ws.blockchain.info/inv";

    // Current API urls
    private String currentApiUrl;
    private String currentServerUrl;
    private String currentWebsocketUrl;
    private Environment currentEnvironment;

    // Current Network Params
    private AbstractBitcoinNetParams currentNetworkParams;

    // Instance
    private static PersistentUrls instance;

    public enum Environment {

        PRODUCTION(KEY_ENV_PROD),
        STAGING(KEY_ENV_STAGING),
        DEV(KEY_ENV_DEV),
        TESTNET(KEY_ENV_TESTNET);

        private String name;

        Environment(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Environment fromString(String text) {
            if (text != null) {
                for (Environment environment : Environment.values()) {
                    if (text.equalsIgnoreCase(environment.getName())) {
                        return environment;
                    }
                }
            }
            return null;
        }
    }

    private PersistentUrls() {
        // Default to production on first invocation
        setProductionEnvironment();
    }

    public static PersistentUrls getInstance() {
        if (instance == null) {
            instance = new PersistentUrls();
        }

        return instance;
    }

    /**
     * Resets all URLs to their production base
     */
    public void setProductionEnvironment() {
        currentNetworkParams = MainNetParams.get();
        setCurrentApiUrl(BASE_API_URL);
        setCurrentServerUrl(BASE_SERVER_URL);
        setCurrentWebsocketUrl(BASE_WEBSOCKET_URL);
        currentEnvironment = Environment.PRODUCTION;
    }

    ///////////////////////////////////////////////////////////////////////////
    // GET CURRENTLY SET OBJECTS
    ///////////////////////////////////////////////////////////////////////////

    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }

    public AbstractBitcoinNetParams getCurrentNetworkParams() {
        return currentNetworkParams;
    }

    public String getCurrentBaseApiUrl() {
        return currentApiUrl;
    }

    public String getCurrentBaseServerUrl() {
        return currentServerUrl;
    }

    public String getCurrentWebsocketUrl() {
        return currentWebsocketUrl;
    }

    ///////////////////////////////////////////////////////////////////////////
    // GET DEFAULT OBJECTS
    ///////////////////////////////////////////////////////////////////////////

    public AbstractBitcoinNetParams getDefaultNetworkParams() {
        return MainNetParams.get();
    }

    public String getDefaultBaseApiUrl() {
        return BASE_API_URL;
    }

    public String getDefaultBaseServerUrl() {
        return BASE_SERVER_URL;
    }

    public String getDefaultBaseWebsocketUrl() {
        return BASE_WEBSOCKET_URL;
    }

    ///////////////////////////////////////////////////////////////////////////
    // SET CURRENT OBJECTS
    ///////////////////////////////////////////////////////////////////////////

    public void setCurrentNetworkParams(AbstractBitcoinNetParams currentNetworkParams) {
        this.currentNetworkParams = currentNetworkParams;
    }

    public void setCurrentApiUrl(String currentApiUrl) {
        this.currentApiUrl = currentApiUrl;
    }

    public void setCurrentServerUrl(String currentServerUrl) {
        this.currentServerUrl = currentServerUrl;
    }

    public void setCurrentWebsocketUrl(String currentWebsocketUrl) {
        this.currentWebsocketUrl = currentWebsocketUrl;
    }

    public void setCurrentEnvironment(Environment currentEnvironment) {
        this.currentEnvironment = currentEnvironment;
    }
}
