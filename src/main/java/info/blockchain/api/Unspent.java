package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class Unspent {

    private static final String PROTOCOL = "https://";
    private static final String SERVER_ADDRESS = "blockchain.info/";
    public static final String PROD_UNSPENT_OUTPUTS_URL = PROTOCOL + SERVER_ADDRESS + "unspent?active=";

    public JSONObject getUnspentOutputs(String address) throws Exception {

        address += "&api_code="+WebUtil.API_CODE;
        String response = WebUtil.getInstance().getURL(PROD_UNSPENT_OUTPUTS_URL + address);

        if(response != null && !response.equals("No free outputs to spend")) {
            return new JSONObject(response);
        }else{
            return null;
        }
    }
}
