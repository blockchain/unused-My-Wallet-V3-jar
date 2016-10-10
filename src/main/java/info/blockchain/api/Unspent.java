package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.json.JSONObject;

public class Unspent implements BaseApi {

    private static final String UNSPENT = "unspent?active=";
    public static final String PROD_UNSPENT_OUTPUTS_URL = PROTOCOL + SERVER_ADDRESS + UNSPENT;

    private String unspentUrl = PROD_UNSPENT_OUTPUTS_URL;

    public Unspent() {
        unspentUrl = PersistentUrls.getInstance().getUnspentUrl();
    }

    public JSONObject getUnspentOutputs(String address) throws Exception {

        address += "&api_code=" + API_CODE;
        String response = WebUtil.getInstance().getURL(unspentUrl + address);

        if (response != null && !response.equals("No free outputs to spend")) {
            return new JSONObject(response);
        } else {
            return null;
        }
    }
}
