package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

/**
 * Created by adambennett on 09/11/2016.
 */
public class Notifications extends BaseApi {

    private static final String UPDATE_FIREBASE = "wallet?method=update-firebase&payload=";

    public Notifications() {
        // No-op
    }

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getCurrentBaseServerUrl() + UPDATE_FIREBASE;
    }

    public void updateFirebaseNotificationToken(String token, String guid, String sharedKey) throws Exception {

        String url = getRoute() + token;

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
