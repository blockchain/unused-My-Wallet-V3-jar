package info.blockchain.api;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.payload.Payload;
import info.blockchain.wallet.payload.PayloadFactory;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class SettingsApiTest {

    String guid = "ba773137-56a6-426b-981e-40efd1ddac54";
    String password = "MyTestWallet";
    SettingsApi settingsApi;

    @Before
    public void setUp() throws Exception {

        manualPairWallet(guid, password);
        Payload payload = PayloadFactory.getInstance().get();
        settingsApi = new SettingsApi(guid, payload.getSharedKey());
    }

    @Test
    public void testUpdateSms() throws Exception {
        assertThat("sms update", settingsApi.updateSms("+44 75 1234 1234"));
    }

    @Test
    public void testUpdateEmail() throws Exception {
        assertThat("email update", settingsApi.updateEmail("nope@nope.com"));
    }

    private void manualPairWallet(final String guid, final String password) throws Exception {
        StringBuilder args = new StringBuilder();

        args.append("guid=" + guid);
        args.append("&method=pairing-encryption-password");

        //Get new SID
        String sid = WebUtil.getInstance().getCookie(WebUtil.PAIRING_URL + "/" + guid + "?format=json&resend_code=false", "SID");

        //Pair wallet with new SID
        String response = WebUtil.getInstance().getURL(WebUtil.PAIRING_URL + "/" + guid + "?format=json&resend_code=false", "SID=" + sid);
        JSONObject jsonObject = new JSONObject(response);
//        Log.i("Pairing", "Returned object:" + jsonObject.toString());

        if (jsonObject.toString().contains("initial_error")) {
            String authError = (String) jsonObject.get("initial_error");
            System.out.println(authError);
        }

        String payload = (String) jsonObject.get("payload");
        if (payload == null || payload.length() == 0) {
            throw new Exception("Error Fetching Wallet Payload");
        }

        JSONObject jsonObj = new JSONObject(response);
        if (jsonObj != null && jsonObj.has("payload")) {
            String encrypted_payload = (String) jsonObj.getString("payload");

            int iterations = PayloadFactory.WalletPbkdf2Iterations;
            if (jsonObj.has("pbkdf2_iterations")) {
                iterations = Integer.valueOf(jsonObj.get("pbkdf2_iterations").toString()).intValue();
            }

            JSONObject asd = new JSONObject(encrypted_payload);

            String paylaod2 = "";
            if (asd.has("payload")) {
                paylaod2 = asd.getString("payload");
            }

            String decrypted_payload = null;
            try {
                decrypted_payload = AESUtil.decrypt(paylaod2, new CharSequenceX(password), iterations);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (decrypted_payload != null) {

                PayloadFactory.getInstance(decrypted_payload);

                JSONObject payloadObj = new JSONObject(decrypted_payload);
                if (payloadObj != null && payloadObj.has("sharedKey")) {
                    PayloadFactory.getInstance().setTempPassword(new CharSequenceX(password));
                    PayloadFactory.getInstance().get().setSharedKey(payloadObj.getString("sharedKey"));
                }

            } else {
                System.out.println("Failed to pair");
            }

        }
    }
}