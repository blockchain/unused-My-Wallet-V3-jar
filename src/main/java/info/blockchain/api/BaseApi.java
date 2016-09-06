package info.blockchain.api;


public interface BaseApi {

    String PROTOCOL = "https://";

    String DEV_SUBDOMAIN = "dev.";
    String API_SUBDOMAIN = "api.";
    String STAGING_SUBDOMAIN = "staging.";
    String EXPLORER_SUBDOMAIN = "explorer.";

    String WALLET_DEV_SUBDOMAIN = EXPLORER_SUBDOMAIN + DEV_SUBDOMAIN;
    String WALLET_STAGING_SUBDOMAIN = EXPLORER_SUBDOMAIN + STAGING_SUBDOMAIN;

    String API_DEV_SUBDOMAIN = API_SUBDOMAIN + DEV_SUBDOMAIN;
    String API_STAGING_SUBDOMAIN = API_SUBDOMAIN + STAGING_SUBDOMAIN;

    String SERVER_ADDRESS = "blockchain.info/";
    String DEV_SERVER_ADDRESS = "blockchain.co.uk/";

    //Android API code
    String API_CODE = "25a6ad13-1633-4dfb-b6ee-9b91cdf0b5c3";
}
