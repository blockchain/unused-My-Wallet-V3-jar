package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

/**
 * Created by adambennett on 09/11/2016.
 */
public class Notifications extends BaseApi {

    private static final String UPDATE_FIREBASE = "?method=update-firebase&payload=";
    private static final String WALLET = "wallet";
    public static final String PROD_FIREBASE_URL = PROTOCOL + SERVER_ADDRESS + WALLET + UPDATE_FIREBASE;

    public Notifications() {
        // No-op
    }

    public void updateFirebaseNotificationToken(String token, String guid, String sharedKey) throws Exception {

        String url = PersistentUrls.getInstance().getWalletPayloadUrl() + UPDATE_FIREBASE + token;

        String urlParams = "&guid=" + guid
                + "&payload=" + token + "&format=json"
                + "&sharedKey=" + sharedKey
                + "&length=" + token.length()
                + getApiCode();

        String response = WebUtil.getInstance().postURL(url, urlParams);

        if (response == null || response.length() == 0){
            throw new Exception("Invalid Server Response");
        }

        JSONObject jsonObject = new JSONObject(response);

        if (!jsonObject.has("success") || !jsonObject.getBoolean("success")) {
            throw new Exception("Save failed" + response);
        }
    }
}
