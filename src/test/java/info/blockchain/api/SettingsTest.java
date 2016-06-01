package info.blockchain.api;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.payload.Payload;
import info.blockchain.wallet.payload.PayloadFactory;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SettingsTest {

    String guid = "ba773137-56a6-426b-981e-40efd1ddac54";
    String password = "MyTestWallet";
    Settings settingsApi;

    @Before
    public void setUp() throws Exception {

        manualPairWallet(guid, password);
        Payload payload = PayloadFactory.getInstance().get();
        settingsApi = new Settings(guid, payload.getSharedKey());
    }

    @Test
    public void testUpdateSms() throws Exception {
        final String value = "+44 75 1234 1234";
        settingsApi.setSms(value, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getSms(), is(value));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });

    }

    @Test
    public void testUpdateEmail() throws Exception {
        final String value = "nope@nope.com";
        settingsApi.setEmail(value, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getEmail(), is(value));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });
    }

    @Test
    public void testUpdatePasswordHint1() throws Exception {
        final String value = "pw1";
        settingsApi.setPasswordHint1(value, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getPasswordHint1(), is(value));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });
    }

    @Test
    public void testUpdatePasswordHint2() throws Exception {
        final String value = "pw2";
        settingsApi.setPasswordHint2(value, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getPasswordHint2(), is(value));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });
    }

    @Test
    public void testUpdateBtcCurrency() throws Exception {
        settingsApi.setBtcCurrency(Settings.UNIT_BTC, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getBtcCurrency(), is(Settings.UNIT_BTC));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });

        settingsApi.setBtcCurrency(Settings.UNIT_MBC, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getBtcCurrency(), is(Settings.UNIT_MBC));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });

        settingsApi.setBtcCurrency(Settings.UNIT_UBC, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getBtcCurrency(), is(Settings.UNIT_UBC));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });
    }

    @Test
    public void testUpdateFiatCurrency() throws Exception {
        for (int i = 0; i < settingsApi.UNIT_FIAT.length; i++) {
            final int finalI = i;
            settingsApi.setFiatCurrency(Settings.UNIT_FIAT[i], new Settings.ResultListener() {
                public void onSuccess() {
                    assertThat(settingsApi.getFiatCurrency(), is(Settings.UNIT_FIAT[finalI]));
                }

                public void onFail() {
                    fail("");
                }

                public void onBadRequest() {
                    fail("");
                }
            });
        }
    }

    @Test
    public void testSetTorBlocked() throws Exception {

        settingsApi.setTorBlocked(false, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("block tor", !settingsApi.isTorBlocked());
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });

        settingsApi.setTorBlocked(true, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("block tor", settingsApi.isTorBlocked());
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });
    }

    @Test
    public void testEnableNotifications() throws Exception {

        settingsApi.enableNotifications(false, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("notifications toggle", !settingsApi.isNotificationsOn());
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });

        settingsApi.enableNotifications(true, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("notifications toggle", settingsApi.isNotificationsOn());
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });
    }

    @Test
    public void testSetNotificationsType() throws Exception {

        settingsApi.setNotificationType(Settings.NOTIFICATION_TYPE_EMAIL, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("notifications types", settingsApi.getNotificationTypes().contains(Settings.NOTIFICATION_TYPE_EMAIL));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });

        settingsApi.setNotificationType(Settings.NOTIFICATION_TYPE_ALL_DISABLE, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("notifications toggle", settingsApi.getNotificationTypes().isEmpty());
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });
    }

    @Test
    public void testAuthType() throws Exception {

        settingsApi.setAuthType(Settings.AUTH_TYPE_SMS, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getAuthType(), is(Settings.AUTH_TYPE_SMS));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });

        settingsApi.setAuthType(Settings.AUTH_TYPE_EMAIL, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getAuthType(), is(Settings.AUTH_TYPE_EMAIL));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });

        settingsApi.setAuthType(Settings.AUTH_TYPE_OFF, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat(settingsApi.getAuthType(), is(Settings.AUTH_TYPE_OFF));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });
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