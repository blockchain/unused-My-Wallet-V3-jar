package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.json.JSONObject;

public class PinStore extends BaseApi {

    private static final String PIN_STORE = "pin-store";

    public PinStore() {
        // No-op
    }

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getCurrentBaseServerUrl() + PIN_STORE;
    }

    public JSONObject validateAccess(String key, String pin) throws Exception {

        final String args = "key="
                + key +
                "&pin=" + pin +
                "&method=get" +
                getApiCode();

        String response = WebUtil.getInstance().postURL(
                getRoute(), args, 1);

        if (response == null || response.length() == 0)
            throw new Exception("Invalid Server Response");

        return new JSONObject(response);
    }

    public JSONObject setAccess(String key, String value, String pin) throws Exception {

        final String args = "key="
                + key +
                "&value=" + value +
                "&pin=" + pin +
                "&method=put" +
                getApiCode();

        String response = WebUtil.getInstance().postURL(getRoute(), args);

        if (response == null || response.length() == 0)
            throw new Exception("Invalid Server Response");

        return new JSONObject(response);
    }
}
