package info.blockchain.api;


import javax.annotation.Nonnull;

public abstract class BaseApi {

    private final static String API_CODE = "25a6ad13-1633-4dfb-b6ee-9b91cdf0b5c3";

    @Nonnull
    String getApiCode() {
        return PersistentUrls.getInstance().getCurrentEnvironment() == PersistentUrls.Environment.PRODUCTION
                ? "&api_code=" + API_CODE : "";
    }

    abstract String getRoute();
}
