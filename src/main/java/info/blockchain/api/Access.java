package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class Access {

    private String sessionId;
    public static String KEY_AUTH_REQUIRED = "Authorization Required";

    public JSONObject validateAccess(String key, String pin) throws Exception {

        StringBuilder args = new StringBuilder();

        args.append("key=" + key);
        args.append("&pin=" + pin);
        args.append("&method=get");
        args.append("&api_code=" + WebUtil.API_CODE);

        String response = WebUtil.getInstance().postURL(WebUtil.ACCESS_URL, args.toString(), 1);

        if (response == null || response.length() == 0)
            throw new Exception("Invalid Server Response");

        return new JSONObject(response);
    }

    public JSONObject setAccess(String key, String value, String pin) throws Exception {

        StringBuilder args = new StringBuilder();

        args.append("key=" + key);
        args.append("&value=" + value);
        args.append("&pin=" + pin);
        args.append("&method=put");
        args.append("&api_code=" + WebUtil.API_CODE);

        String response = WebUtil.getInstance().postURL(WebUtil.ACCESS_URL, args.toString());

        if (response == null || response.length() == 0)
            throw new Exception("Invalid Server Response");

        return new JSONObject(response);
    }

    public String getSessionId(String guid) throws Exception {

        if(sessionId == null) {
            sessionId = WebUtil.getInstance().getCookie(
                    WebUtil.PAIRING_URL + "/" +
                            guid +
                            "?format=json&" +
                            "resend_code=false",
                    "SID");
        }

        return sessionId;
    }

    public String getPairingEncryptionPassword(final String guid) throws Exception {
        StringBuilder args = new StringBuilder();

        args.append("guid=" + guid);
        args.append("&method=pairing-encryption-password");

        return WebUtil.getInstance().postURL(WebUtil.PAIRING_URL, args.toString());
    }

    public String getEncryptedPayload(final String guid, final String sessionId) throws Exception {

        String response = WebUtil.getInstance().getURL(
                WebUtil.PAIRING_URL +
                        "/" + guid +
                        "?format=json&resend_code=false",
                "SID=" + sessionId);

        JSONObject jsonObject = new JSONObject(response);

        if (jsonObject.toString().contains("initial_error")) {
            String authError = (String) jsonObject.get("initial_error");
            if (authError != null && authError.contains(KEY_AUTH_REQUIRED))
                return KEY_AUTH_REQUIRED;
        }

        String payload = (String) jsonObject.get("payload");
        if (payload == null || payload.length() == 0) {
            throw new Exception("Error Fetching Wallet Payload");
        }

        return payload;
    }
}
