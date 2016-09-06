package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class WalletPayload implements BaseApi {

    private static final String WALLET = "wallet";
    public static final String PROD_PAYLOAD_URL = PROTOCOL + SERVER_ADDRESS + WALLET;
    public static final String DEV_PAYLOAD_URL = PROTOCOL + WALLET_DEV_SUBDOMAIN + DEV_SERVER_ADDRESS + WALLET;
    public static final String STAGING_PAYLOAD_URL = PROTOCOL + WALLET_STAGING_SUBDOMAIN + DEV_SERVER_ADDRESS + WALLET;

    private String sessionId;
    public static String KEY_AUTH_REQUIRED = "Authorization Required";

    private String payloadUrl = PROD_PAYLOAD_URL;

    public WalletPayload() {
        payloadUrl = PersistentUrls.getInstance().getWalletPayloadUrl();
    }

    public String getSessionId(String guid) throws Exception {

        if (sessionId == null) {
            sessionId = WebUtil.getInstance().getCookie(
                    payloadUrl + "/" +
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

        return WebUtil.getInstance().postURL(payloadUrl, args.toString());
    }

    public String getEncryptedPayload(final String guid, final String sessionId) throws Exception {

        String response = WebUtil.getInstance().getURL(
                payloadUrl +
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

    /**
     * Fetches wallet data from server
     *
     * @param guid
     * @param sharedKey
     * @return Either encrypted string (v1) or json (v2, v3)
     * @throws Exception
     */
    public String fetchWalletData(String guid, String sharedKey) throws Exception {

        String response = WebUtil.getInstance().postURL(
                payloadUrl,
                "method=wallet.aes.json&guid=" + guid +
                        "&sharedKey=" + sharedKey +
                        "&format=json" +
                        "&api_code=" + API_CODE);

        if (response == null) {
            throw new Exception("Payload fetch from server is null");
        }

        return response;

    }

    public boolean savePayloadToServer(String args) {

        try {
            String response = WebUtil.getInstance().postURL(payloadUrl, args);
            if (response.contains("Wallet successfully synced")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
