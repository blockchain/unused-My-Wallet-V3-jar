package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.json.JSONObject;

public class Unspent extends BaseApi {

    private static final String UNSPENT = "unspent?active=";

    public Unspent() {
        // No-op
    }

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getCurrentBaseServerUrl() + UNSPENT;
    }

    public JSONObject getUnspentOutputs(String address) throws Exception {

        address += getApiCode();
        String response = WebUtil.getInstance().getURL(
                getRoute() + address);

        if (response != null && !response.equals("No free outputs to spend")) {
            return new JSONObject(response);
        } else {
            return null;
        }
    }
}
