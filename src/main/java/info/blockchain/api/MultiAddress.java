package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class MultiAddress {

    private static final String PROTOCOL = "https://";
    private static final String DEV_SUBDOMAIN = "dev.";
    private static final String STAGING_SUBDOMAIN = "staging.";
    private static final String WALLET_DEV_SUBDOMAIN = "explorer."+DEV_SUBDOMAIN;
    private static final String WALLET_STAGING_SUBDOMAIN = "explorer."+STAGING_SUBDOMAIN;
    private static final String SERVER_ADDRESS = "blockchain.info/";
    private static final String MULTI_ADDRESS = "multiaddr?active=";

    public static final String PROD_MULTIADDR_URL = PROTOCOL + SERVER_ADDRESS + MULTI_ADDRESS;
    public static final String DEV_MULTIADDR_URL = PROTOCOL + WALLET_DEV_SUBDOMAIN + SERVER_ADDRESS + MULTI_ADDRESS;
    public static final String STAGING_MULTIADDR_URL = PROTOCOL + WALLET_STAGING_SUBDOMAIN + SERVER_ADDRESS + MULTI_ADDRESS;

    private String multiAddrressUrl = PROD_MULTIADDR_URL;

    public MultiAddress() {
    }

    /**
     * @param customUrl PROD_MULTIADDR_URL, DEV_MULTIADDR_URL, STAGING_MULTIADDR_URL
     */
    public MultiAddress(String customUrl) {
        this.multiAddrressUrl = customUrl;
    }

    public JSONObject getLegacy(String[] addresses, boolean simple) throws Exception{


        JSONObject jsonObject  = null;

        StringBuilder url = new StringBuilder(multiAddrressUrl);
        url.append(StringUtils.join(addresses, "|"));
        if(simple) {
            url.append("&simple=true&format=json");
        }
        else {
            url.append("&symbol_btc="+ "BTC" + "&symbol_local=" + "USD");
        }
        url.append("&api_code="+WebUtil.API_CODE);

        String response = WebUtil.getInstance().getURL(url.toString());
        jsonObject = new JSONObject(response);

        return jsonObject;
    }

    public JSONObject getXPUB(String[] xpubs) throws Exception {

        StringBuilder url = new StringBuilder(multiAddrressUrl);
        url.append(StringUtils.join(xpubs, "|"));
        url.append("&api_code="+WebUtil.API_CODE);

        String response = WebUtil.getInstance().getURL(url.toString());
        JSONObject jsonObject = new JSONObject(response);

        return jsonObject;
    }
}
