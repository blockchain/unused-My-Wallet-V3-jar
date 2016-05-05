package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class DynamicFee {

    public static JSONObject getDynamicFee() throws Exception {

        String response = WebUtil.getInstance().getURL(WebUtil.DYNAMIC_FEE);

        if(response != null) {
            return new JSONObject(response);
        }else{
            return null;
        }
    }
}
