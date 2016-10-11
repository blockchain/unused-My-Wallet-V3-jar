package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class MultiAddress implements BaseApi {

    private static final String MULTI_ADDRESS = "multiaddr?active=";
    public static final String PROD_MULTIADDR_URL = PROTOCOL + SERVER_ADDRESS + MULTI_ADDRESS;

    private String multiAddrressUrl = PROD_MULTIADDR_URL;

    public MultiAddress() {
        multiAddrressUrl = PersistentUrls.getInstance().getMultiAddressUrl();
    }

    public JSONObject getLegacy(String[] addresses, boolean simple) throws Exception {


        JSONObject jsonObject;

        StringBuilder url = new StringBuilder(multiAddrressUrl);
        url.append(StringUtils.join(addresses, "|"));
        if (simple) {
            url.append("&simple=true&format=json");
        } else {
            url.append("&symbol_btc=" + "BTC" + "&symbol_local=" + "USD");
        }
        url.append("&api_code=" + API_CODE);

        String response = WebUtil.getInstance().getURL(url.toString());
        jsonObject = new JSONObject(response);

        return jsonObject;
    }

    public JSONObject getXPUB(String[] xpubs) throws Exception {

        StringBuilder url = new StringBuilder(multiAddrressUrl);
        url.append(StringUtils.join(xpubs, "|"));
        url.append("&api_code=" + API_CODE);

        String response = WebUtil.getInstance().getURL(url.toString());

        return new JSONObject(response);
    }
}
