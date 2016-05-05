package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class MultiAddress {

    public JSONObject getLegacy(String[] addresses, boolean simple) throws Exception{


        JSONObject jsonObject  = null;

        StringBuilder url = new StringBuilder(WebUtil.MULTIADDR_URL);
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

        StringBuilder url = new StringBuilder(WebUtil.MULTIADDR_URL);
        url.append(StringUtils.join(xpubs, "|"));
        url.append("&api_code="+WebUtil.API_CODE);

        String response = WebUtil.getInstance().getURL(url.toString());
        JSONObject jsonObject = new JSONObject(response);

        return jsonObject;
    }
}
