package info.blockchain.api;

import info.blockchain.wallet.payload.LegacyAddress;
import info.blockchain.wallet.util.WebUtil;

import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.ECKey;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WalletPayload extends BaseApi {

    private static final String WALLET = "wallet";
//    public static final String PROD_PAYLOAD_URL = "https://explorer.dev.blockchain.co.uk/wallet";
    public static final String PROD_PAYLOAD_URL = PROTOCOL + SERVER_ADDRESS + WALLET;

    private String sessionId;
    public static final String KEY_AUTH_REQUIRED = "Authorization Required";

    WalletEndpoints api;

    public WalletPayload() {

//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WalletEndpoints.API_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(WalletEndpoints.class);
    }

    public String getSessionId(String guid) throws Exception {

        if (sessionId == null) {
            sessionId = WebUtil.getInstance().getCookie(
                    PersistentUrls.getInstance().getWalletPayloadUrl()
                            + "/"
                            + guid
                            + "?format=json&"
                            + "resend_code=false",
                    "SID");
        }

        return sessionId;
    }

    public String getPairingEncryptionPassword(final String guid) throws Exception {
        StringBuilder args = new StringBuilder();

        args.append("guid=").append(guid);
        args.append("&method=pairing-encryption-password");

        return WebUtil.getInstance().postURL(PersistentUrls.getInstance().getWalletPayloadUrl(), args.toString());
    }

    public String getEncryptedPayload(final String guid, final String sessionId) throws Exception {

        String response = WebUtil.getInstance().getURL(
                PersistentUrls.getInstance().getWalletPayloadUrl()
                        + "/" + guid
                        + "?format=json&resend_code=false",
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
                PersistentUrls.getInstance().getWalletPayloadUrl(),
                "method=wallet.aes.json&guid="
                        + guid
                        + "&sharedKey="
                        + sharedKey
                        + "&format=json"
                        + getApiCode());

        if (response == null) {
            throw new Exception("Payload fetch from server is null");
        }

        return response;

    }

    public void savePayloadToServer(String method, String guid, String sharedKey,
                                       List<LegacyAddress> legacyAddresses, JSONObject encryptedPayload,
                                       boolean syncPubkeys, String newChecksum, String oldChecksum, String email) throws Exception{

        StringBuilder args = new StringBuilder();

        String urlEncodedPayload = URLEncoder.encode(encryptedPayload.toString());

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
            args.append("&email=");
            args.append(URLEncoder.encode(email, "utf-8"));
        }

        args.append("&device=");
        args.append("android");

        if (oldChecksum != null && oldChecksum.length() > 0) {
            args.append("&old_checksum=");
            args.append(oldChecksum);
        }

        // TODO: 15/11/16 Add this after metadata testing on dev is complete
//        args.append(getApiCode());

        String response = WebUtil.getInstance().postURL(PersistentUrls.getInstance().getWalletPayloadUrl(), args.toString());
    }

    public boolean registerMdid(ECKey walletKey, String guid, String sharedKey) throws Exception {
        return updateMdid("register-mdid", walletKey, guid, sharedKey);
    }

    public boolean unregisterMdid(ECKey walletKey, String guid, String sharedKey) throws Exception {
        return updateMdid("unregister-mdid", walletKey, guid, sharedKey);
    }

    private boolean updateMdid(String method, ECKey walletKey, String guid, String sharedKey) throws Exception {

        String signedGuid = walletKey.signMessage(guid);

        Call<Void> call = api.postMdidRegistration(method,
                guid,
                sharedKey,
                signedGuid,
                signedGuid.length());

        Response<Void> result = call.execute();

        if(!result.isSuccessful())
            throw new Exception(result.code()+" "+result.message());

        return true;
    }
}
