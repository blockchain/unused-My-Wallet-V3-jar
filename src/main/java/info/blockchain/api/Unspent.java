package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.json.JSONObject;

public class Unspent extends BaseApi {

    private static final String UNSPENT = "unspent?active=";
    public static final String PROD_UNSPENT_OUTPUTS_URL = PROTOCOL + SERVER_ADDRESS + UNSPENT;

    public Unspent() {
        // No-op
    }

    public JSONObject getUnspentOutputs(String address) throws Exception {

        address += getApiCode();
        String response = WebUtil.getInstance().getURL(PersistentUrls.getInstance().getUnspentUrl() + address);

        if (response != null && !response.equals("No free outputs to spend")) {
            return new JSONObject(response);
        } else {
            return null;
        }
    }
}
