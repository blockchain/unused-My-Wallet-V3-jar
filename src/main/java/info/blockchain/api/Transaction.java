package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class Transaction {

    private static final String PROTOCOL = "https://";
    private static final String SERVER_ADDRESS = "blockchain.info/";
    public static final String PROD_ADDRESS_INFO_URL = PROTOCOL + SERVER_ADDRESS + "address/";

    public JSONObject getAddressInfo(String address, String parameter) {

        JSONObject jsonObject  = null;

        try {
            StringBuilder url = new StringBuilder(PROD_ADDRESS_INFO_URL);
            url.append(address);
            url.append("?format=json");
            if(parameter != null && !parameter.isEmpty())
                url.append(parameter);

            String response = WebUtil.getInstance().getURL(url.toString());
            jsonObject = new JSONObject(response);
        }
        catch(Exception e) {
            jsonObject = null;
            e.printStackTrace();
        }

        return jsonObject;
    }
}
