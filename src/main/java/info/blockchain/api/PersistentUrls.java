package info.blockchain.api;

public class PersistentUrls {

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

    public enum Environment {
        PROD, DEV, STAGING
    }

    private Environment currentEnvironment = Environment.PROD;

    private PersistentUrls() {
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

        currentEnvironment = Environment.PROD;
    }

//    public void setDevelopmentEnvironment() {
//        setMultiAddressUrl(MultiAddress.DEV_MULTIADDR_URL);
//        setSettingsUrl(Settings.DEV_PAYLOAD_URL);
//        setWalletPayloadUrl(WalletPayload.DEV_PAYLOAD_URL);
//        setAddressInfoUrl(AddressInfo.DEV_ADDRESS_INFO_URL);
//        setBalanceUrl(Balance.DEV_BALANCE_URL);
//        setDynamicFeeUrl(DynamicFee.DEV_DYNAMIC_FEE);
//        setPinstoreUrl(PinStore.DEV_PIN_STORE_URL);
//        setTransactionDetailsUrl(TransactionDetails.DEV_TRANSACTION_URL);
//        setUnspentUrl(Unspent.DEV_UNSPENT_OUTPUTS_URL);
//
//        currentEnvironment = Environment.DEV;
//    }
//
//    public void setStagingEnvironment() {
//        setMultiAddressUrl(MultiAddress.STAGING_MULTIADDR_URL);
//        setSettingsUrl(Settings.STAGING_PAYLOAD_URL);
//        setWalletPayloadUrl(WalletPayload.STAGING_PAYLOAD_URL);
//        setAddressInfoUrl(AddressInfo.STAGING_ADDRESS_INFO_URL);
//        setBalanceUrl(Balance.STAGING_BALANCE_URL);
//        setDynamicFeeUrl(DynamicFee.STAGING_DYNAMIC_FEE);
//        setPinstoreUrl(PinStore.STAGING_PIN_STORE_URL);
//        setTransactionDetailsUrl(TransactionDetails.STAGING_TRANSACTION_URL);
//        setUnspentUrl(Unspent.STAGING_UNSPENT_OUTPUTS_URL);
//
//        currentEnvironment = Environment.STAGING;
//    }

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
