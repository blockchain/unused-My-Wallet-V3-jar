package info.blockchain.api;

public class PersistentUrls {

    public static final String KEY_ENV_PROD = "env_prod";
    public static final String KEY_ENV_STAGING = "env_staging";
    public static final String KEY_ENV_DEV = "env_dev";

    private String multiAddressUrl = MultiAddress.PROD_MULTIADDR_URL;
    private String balanceUrl = Balance.PROD_BALANCE_URL;
    private String dynamicFeeUrl = DynamicFee.PROD_DYNAMIC_FEE;
    private String addressInfoUrl = AddressInfo.PROD_ADDRESS_INFO_URL;
    private String pinstoreUrl = PinStore.PROD_PIN_STORE_URL;
    private String settingsUrl = Settings.PROD_PAYLOAD_URL;
    private String transactionDetailsUrl = TransactionDetails.PROD_TRANSACTION_URL;
    private String unspentUrl = Unspent.PROD_UNSPENT_OUTPUTS_URL;
    private String walletPayloadUrl = WalletPayload.PROD_PAYLOAD_URL;

    private static PersistentUrls instance = null;

    private Environment currentEnvironment = Environment.PRODUCTION;

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

    public void setProductionEnvironment() {
        setMultiAddressUrl(MultiAddress.PROD_MULTIADDR_URL);
        setSettingsUrl(Settings.PROD_PAYLOAD_URL);
        setWalletPayloadUrl(WalletPayload.PROD_PAYLOAD_URL);
        setAddressInfoUrl(AddressInfo.PROD_ADDRESS_INFO_URL);
        setBalanceUrl(Balance.PROD_BALANCE_URL);
        setDynamicFeeUrl(DynamicFee.PROD_DYNAMIC_FEE);
        setPinstoreUrl(PinStore.PROD_PIN_STORE_URL);
        setTransactionDetailsUrl(TransactionDetails.PROD_TRANSACTION_URL);
        setUnspentUrl(Unspent.PROD_UNSPENT_OUTPUTS_URL);

        currentEnvironment = Environment.PRODUCTION;
    }

    public void setMultiAddressUrl(String multiAddressUrl) {
        this.multiAddressUrl = multiAddressUrl;
    }

    public void setBalanceUrl(String balanceUrl) {
        this.balanceUrl = balanceUrl;
    }

    public void setDynamicFeeUrl(String dynamicFeeUrl) {
        this.dynamicFeeUrl = dynamicFeeUrl;
    }

    public void setAddressInfoUrl(String addressInfoUrl) {
        this.addressInfoUrl = addressInfoUrl;
    }

    public void setPinstoreUrl(String pinstoreUrl) {
        this.pinstoreUrl = pinstoreUrl;
    }

    public void setSettingsUrl(String settingsUrl) {
        this.settingsUrl = settingsUrl;
    }

    public void setTransactionDetailsUrl(String transactionDetailsUrl) {
        this.transactionDetailsUrl = transactionDetailsUrl;
    }

    public void setUnspentUrl(String unspentUrl) {
        this.unspentUrl = unspentUrl;
    }

    public void setWalletPayloadUrl(String walletPayloadUrl) {
        this.walletPayloadUrl = walletPayloadUrl;
    }

    public void setCurrentEnvironment(Environment currentEnvironment) {
        this.currentEnvironment = currentEnvironment;
    }

    public String getMultiAddressUrl() {
        return multiAddressUrl;
    }

    public String getBalanceUrl() {
        return balanceUrl;
    }

    public String getDynamicFeeUrl() {
        return dynamicFeeUrl;
    }

    public String getAddressInfoUrl() {
        return addressInfoUrl;
    }

    public String getPinstoreUrl() {
        return pinstoreUrl;
    }

    public String getSettingsUrl() {
        return settingsUrl;
    }

    public String getTransactionDetailsUrl() {
        return transactionDetailsUrl;
    }

    public String getUnspentUrl() {
        return unspentUrl;
    }

    public String getWalletPayloadUrl() {
        return walletPayloadUrl;
    }

    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }
}
