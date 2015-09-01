package info.blockchain.wallet.util;

import org.json.JSONObject;

//import android.util.Log;

public class AddressInfo	{

    private static AddressInfo instance = null;

    private AddressInfo()	{ ; }

    public static AddressInfo getInstance() {

        if(instance == null) {
            instance = new AddressInfo();
        }

        return instance;
    }

    public JSONObject getAddressInfo(String address) {

        JSONObject jsonObject  = null;

        try {
            StringBuilder url = new StringBuilder(WebUtil.ADDRESS_INFO_URL);
            url.append(address);
            url.append("?format=json");

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
