package info.blockchain.wallet.api;

import info.blockchain.wallet.multiaddress.MultiAddressFactory;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
public class PersistentUrls {

    private static Logger log = LoggerFactory.getLogger(PersistentUrls.class);

    // Env enum keys
    public static final String KEY_ENV_PROD = "env_prod";
    public static final String KEY_ENV_STAGING = "env_staging";
    public static final String KEY_ENV_DEV = "env_dev";
    public static final String KEY_ENV_TESTNET = "env_testnet";

    // Base API Constants
    private static final String BASE_API_URL = "https://api.blockchain.info/";
    private static final String BASE_SERVER_URL = "https://blockchain.info/";
    private static final String BASE_WEBSOCKET_URL = "wss://ws.blockchain.info/inv";
    private static final String BASE_SFOX_URL = "https://api.staging.sfox.com/";
    private static final String BASE_COINIFY_URL = "https://app-api.coinify.com/";

    // Current API urls
    private String currentApiUrl;
    private String currentServerUrl;
    private String currentWebsocketUrl;
    private String currentSFOXUrl;
    private String currentCoinifyUrl;
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
        log.info("Setting environment to PRODUCTION");
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

    public String getCurrentSFOXUrl() {
        return currentSFOXUrl;
    }

    public String getCurrentCoinifyUrl() {
        return currentCoinifyUrl;
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

    public String getDefaultBaseSFOXUrl() {
        return BASE_SFOX_URL;
    }

    public String getDefaultBaseCoinifyUrl() {
        return BASE_COINIFY_URL;
    }

    ///////////////////////////////////////////////////////////////////////////
    // SET CURRENT OBJECTS
    ///////////////////////////////////////////////////////////////////////////

    public void setCurrentNetworkParams(AbstractBitcoinNetParams currentNetworkParams) {
        log.info("Setting bitcoin network parameter {}", currentNetworkParams);
        this.currentNetworkParams = currentNetworkParams;
    }

    public void setCurrentApiUrl(String currentApiUrl) {
        log.info("Setting API URL {}", currentApiUrl);
        this.currentApiUrl = currentApiUrl;
    }

    public void setCurrentServerUrl(String currentServerUrl) {
        log.info("Setting server URL {}", currentServerUrl);
        this.currentServerUrl = currentServerUrl;
    }

    public void setCurrentWebsocketUrl(String currentWebsocketUrl) {
        log.info("Setting websocket URL {}", currentWebsocketUrl);
        this.currentWebsocketUrl = currentWebsocketUrl;
    }

    public void setCurrentSFOXUrl(String currentSFOXUrl) {
        log.info("Setting SFOX URL {}", currentWebsocketUrl);
        this.currentSFOXUrl = currentSFOXUrl;
    }

    public void setCurrentCoinifyUrl(String currentCoinifyUrl) {
        this.currentCoinifyUrl = currentCoinifyUrl;
    }

    public void setCurrentEnvironment(Environment currentEnvironment) {
        log.info("Setting current environment {}", currentEnvironment);
        this.currentEnvironment = currentEnvironment;
    }
}
