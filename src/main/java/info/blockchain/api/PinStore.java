package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class PinStore {

    //Protocols
    private static final String PROTOCOL = "https://";

    //Sub-domains
    private static final String DEV_SUBDOMAIN = "dev.";
    private static final String STAGING_SUBDOMAIN = "staging.";
    private static final String WALLET_DEV_SUBDOMAIN = "explorer."+DEV_SUBDOMAIN;
    private static final String WALLET_STAGING_SUBDOMAIN = "explorer."+STAGING_SUBDOMAIN;

    //Domain
    private static final String SERVER_ADDRESS = "blockchain.info/";

    //Pin-store
    private static final String PIN_STORE = "pin-store";
    public static final String PROD_PIN_STORE_URL = PROTOCOL + SERVER_ADDRESS + PIN_STORE;
    public static final String DEV_PIN_STORE_URL = PROTOCOL + WALLET_DEV_SUBDOMAIN + SERVER_ADDRESS + PIN_STORE;
    public static final String STAGING_PIN_STORE_URL = PROTOCOL + WALLET_STAGING_SUBDOMAIN + SERVER_ADDRESS + PIN_STORE;

    private String sessionId;
    public static String KEY_AUTH_REQUIRED = "Authorization Required";

    private String pinstoreUrl = PROD_PIN_STORE_URL;

    public PinStore() {
    }

    /**
     *
     * @param customPinstoreUrl
     */
    public PinStore(String customPinstoreUrl) {
        this.pinstoreUrl = customPinstoreUrl;
    }

    public JSONObject validateAccess(String key, String pin) throws Exception {

        StringBuilder args = new StringBuilder();

        args.append("key=" + key);
        args.append("&pin=" + pin);
        args.append("&method=get");
        args.append("&api_code=" + WebUtil.API_CODE);

        String response = WebUtil.getInstance().postURL(pinstoreUrl, args.toString(), 1);

        if (response == null || response.length() == 0)
            throw new Exception("Invalid Server Response");

        return new JSONObject(response);
    }

    public JSONObject setAccess(String key, String value, String pin) throws Exception {

        StringBuilder args = new StringBuilder();

        args.append("key=" + key);
        args.append("&value=" + value);
        args.append("&pin=" + pin);
        args.append("&method=put");
        args.append("&api_code=" + WebUtil.API_CODE);

        String response = WebUtil.getInstance().postURL(pinstoreUrl, args.toString());

        if (response == null || response.length() == 0)
            throw new Exception("Invalid Server Response");

        return new JSONObject(response);
    }
}
