package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.List;

public class MultiAddress extends BaseApi {

    private static final String MULTI_ADDRESS = "multiaddr?active=";

    public MultiAddress() {
        // No-op
    }

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getCurrentBaseServerUrl() + MULTI_ADDRESS;
    }

    public JSONObject getLegacy(String[] addresses, boolean simple) throws Exception {

        JSONObject jsonObject;

        StringBuilder url = new StringBuilder(getRoute());
        url.append(StringUtils.join(addresses, "%7C"));
        if (simple) {
            url.append("&simple=true&format=json");
        } else {
            url.append("&symbol_btc=" + "BTC" + "&symbol_local=" + "USD");
        }
        url.append(getApiCode());

        String response = WebUtil.getInstance().getRequest(url.toString());
        jsonObject = new JSONObject(response);

        return jsonObject;
    }

    public JSONObject getXPUB(String[] xpubs) throws Exception {

        final String url = getRoute()
                + StringUtils.join(xpubs, "%7C")
                + getApiCode();

        String response = WebUtil.getInstance().getRequest(url);

        return new JSONObject(response);
    }

    public JSONObject getAddresses(List<String> addresses) throws Exception {

        final String url = getRoute()
                + StringUtils.join(addresses, "%7C")
                + getApiCode();

        String response = WebUtil.getInstance().getRequest(url);

        return new JSONObject(response);
    }
}
