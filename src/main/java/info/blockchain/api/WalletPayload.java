package info.blockchain.api;

import info.blockchain.wallet.payload.LegacyAddress;
import info.blockchain.wallet.util.WebUtil;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WalletPayload implements BaseApi {

    private static final String WALLET = "wallet";
    public static final String PROD_PAYLOAD_URL = PROTOCOL + SERVER_ADDRESS + WALLET;

    private String sessionId;
    public static final String KEY_AUTH_REQUIRED = "Authorization Required";

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

        args.append("guid=").append(guid);
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
     * @return Either encrypted string (v1) or json (v2, v3)
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

    public boolean savePayloadToServer(String method, String guid, String sharedKey,
                                       List<LegacyAddress> legacyAddresses, JSONObject encryptedPayload,
                                       boolean syncPubkeys, String newChecksum, String oldChecksum, String email) {

        StringBuilder args = new StringBuilder();
        try {

            String urlEncodedPayload = URLEncoder.encode(encryptedPayload.toString(), StandardCharsets.UTF_8.toString());

            args.append("guid=");
            args.append(URLEncoder.encode(guid, "utf-8"));
            args.append("&sharedKey=");
            args.append(URLEncoder.encode(sharedKey, "utf-8"));
            args.append("&payload=");
            args.append(urlEncodedPayload);
            args.append("&method=");
            args.append(method);
            args.append("&length=");
            args.append(encryptedPayload.toString().length());
            args.append("&checksum=");
            args.append(URLEncoder.encode(newChecksum, "utf-8"));

        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return false;
        } catch (JSONException je) {
            je.printStackTrace();
            return false;
        }

        if (legacyAddresses != null && syncPubkeys) {
            args.append("&active=");

            List<String> addrs = new ArrayList<String>();
            for (LegacyAddress addr : legacyAddresses) {
                if (addr.getTag() == 0L) {
                    addrs.add(addr.getAddress());
                }
            }

            args.append(StringUtils.join(addrs.toArray(new String[addrs.size()]), "|"));
        }

        if (email != null && email.length() > 0) {
            try {
                args.append("&email=");
                args.append(URLEncoder.encode(email, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        args.append("&device=");
        args.append("android");

        if (oldChecksum != null && oldChecksum.length() > 0) {
            args.append("&old_checksum=");
            args.append(oldChecksum);
        }

        args.append("&api_code=" + API_CODE);

        try {
            String response = WebUtil.getInstance().postURL(payloadUrl, args.toString());
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
