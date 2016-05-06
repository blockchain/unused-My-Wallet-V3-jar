package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class Unspent {

    public JSONObject getUnspentOutputs(String address) throws Exception {

        address += "&api_code="+WebUtil.API_CODE;
        String response = WebUtil.getInstance().getURL(WebUtil.UNSPENT_OUTPUTS_URL + address);

        if(response != null) {
            return new JSONObject(response);
        }else{
            return null;
        }
    }
}
