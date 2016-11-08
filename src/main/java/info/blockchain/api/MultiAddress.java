package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class MultiAddress extends BaseApi {

    private static final String MULTI_ADDRESS = "multiaddr?active=";
    public static final String PROD_MULTIADDR_URL = PROTOCOL + SERVER_ADDRESS + MULTI_ADDRESS;

    public MultiAddress() {
        // No-op
    }

    public JSONObject getLegacy(String[] addresses, boolean simple) throws Exception {

        JSONObject jsonObject;

        StringBuilder url = new StringBuilder(PersistentUrls.getInstance().getMultiAddressUrl());
        url.append(StringUtils.join(addresses, "|"));
        if (simple) {
            url.append("&simple=true&format=json");
        } else {
            url.append("&symbol_btc=" + "BTC" + "&symbol_local=" + "USD");
        }
        url.append(getApiCode());

        String response = WebUtil.getInstance().getURL(url.toString());
        jsonObject = new JSONObject(response);

        return jsonObject;
    }

    public JSONObject getXPUB(String[] xpubs) throws Exception {

        StringBuilder url = new StringBuilder(PersistentUrls.getInstance().getMultiAddressUrl());
        url.append(StringUtils.join(xpubs, "|"));
        url.append(getApiCode());

        String response = WebUtil.getInstance().getURL(url.toString());

        return new JSONObject(response);
    }
}
