package info.blockchain.api;

public class PersistentUrls {

    // Env enum keys
    public static final String KEY_ENV_PROD = "env_prod";
    public static final String KEY_ENV_STAGING = "env_staging";
    public static final String KEY_ENV_DEV = "env_dev";

    // Base API Constants
    private static final String BASE_API_URL = "https://api.blockchain.info/";
    private static final String BASE_SERVER_URL = "https://blockchain.info/";

    // Current API urls, set to default to production
    private String currentApiUrl = BASE_API_URL;
    private String currentServerUrl = BASE_SERVER_URL;
    private Environment currentEnvironment = Environment.PRODUCTION;

    // Instance
    private static PersistentUrls instance;

    public enum Environment {

        PRODUCTION(KEY_ENV_PROD),
        STAGING(KEY_ENV_STAGING),
        DEV(KEY_ENV_DEV);

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
        // No-op
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
        setCurrentApiUrl(BASE_API_URL);
        setCurrentServerUrl(BASE_SERVER_URL);
        currentEnvironment = Environment.PRODUCTION;
    }

    public String getCurrentBaseApiUrl() {
        return currentApiUrl;
    }

    public String getCurrentBaseServerUrl() {
        return currentServerUrl;
    }

    public String getDefaultBaseApiUrl() {
        return BASE_API_URL;
    }

    public String getDefaultBaseServerUrl() {
        return BASE_SERVER_URL;
    }

    public void setCurrentApiUrl(String currentApiUrl) {
        this.currentApiUrl = currentApiUrl;
    }

    public void setCurrentServerUrl(String currentServerUrl) {
        this.currentServerUrl = currentServerUrl;
    }

    public void setCurrentEnvironment(Environment currentEnvironment) {
        this.currentEnvironment = currentEnvironment;
    }

    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }
}
