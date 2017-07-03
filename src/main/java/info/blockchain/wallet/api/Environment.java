package info.blockchain.wallet.api;

public enum Environment {

    PRODUCTION(PersistentUrls.KEY_ENV_PROD),
    STAGING(PersistentUrls.KEY_ENV_STAGING),
    DEV(PersistentUrls.KEY_ENV_DEV),
    TESTNET(PersistentUrls.KEY_ENV_TESTNET);

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
