package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class Wallet {

    //Protocols
    private static final String PROTOCOL = "https://";

    //Sub-domains
    private static final String DEV_SUBDOMAIN = "dev.";
    private static final String STAGING_SUBDOMAIN = "staging.";
    private static final String WALLET_DEV_SUBDOMAIN = "explorer."+DEV_SUBDOMAIN;
    private static final String WALLET_STAGING_SUBDOMAIN = "explorer."+STAGING_SUBDOMAIN;

    //Domain
    private static final String SERVER_ADDRESS = "blockchain.info/";

    //Wallet
    private static final String WALLET = "wallet";
    public static final String PROD_PAYLOAD_URL = PROTOCOL + SERVER_ADDRESS + WALLET; //PAIRING_URL and SID_URL
    public static final String DEV_PAYLOAD_URL = PROTOCOL + WALLET_DEV_SUBDOMAIN + SERVER_ADDRESS + WALLET;
    public static final String STAGING_PAYLOAD_URL = PROTOCOL + WALLET_STAGING_SUBDOMAIN + SERVER_ADDRESS + WALLET;

    private String sessionId;
    public static String KEY_AUTH_REQUIRED = "Authorization Required";

    private String payloadUrl = PROD_PAYLOAD_URL;

    public Wallet() {
    }

    /**
     *
     * @param customPayloadUrl PROD_PAYLOAD_URL, DEV_PAYLOAD_URL, STAGING_PAYLOAD_URL
     */
    public Wallet(String customPayloadUrl) {
        this.payloadUrl = customPayloadUrl;
    }

    public String getSessionId(String guid) throws Exception {

        if(sessionId == null) {
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
     * @param guid
     * @param sharedKey
     * @return Either encrypted string (v1) or json (v2, v3)
     * @throws Exception
     */
    public String fetchWalletData(String guid, String sharedKey) throws Exception {

        String response = WebUtil.getInstance().postURL(
                WebUtil.PROD_PAYLOAD_URL,
                "method=wallet.aes.json&guid=" + guid +
                        "&sharedKey=" + sharedKey +
                        "&format=json" +
                        "&api_code=" + WebUtil.API_CODE);

        if (response == null){
            throw new Exception("Payload fetch from server is null");
        }

        return response;

    }
}
