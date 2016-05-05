package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class Unspent {

    public static  JSONObject getUnspentOutputs(String address) throws Exception {

        String response = WebUtil.getInstance().getURL(WebUtil.UNSPENT_OUTPUTS_URL + address);

        if(response != null) {
            return new JSONObject(response);
        }else{
            return null;
        }
    }
}
