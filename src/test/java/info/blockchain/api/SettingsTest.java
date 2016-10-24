package info.blockchain.api;

import info.blockchain.wallet.payload.Payload;
import info.blockchain.wallet.payload.PayloadManager;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Ignore
public class SettingsTest {

    String mnemonic = "all all all all all all all all all all all all";
    Settings settingsApi;

    @Before
    public void setUp() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();
        Payload payload = payloadManager.restoreHDWallet("password", mnemonic, "");

        settingsApi = new Settings(payload.getGuid(), payload.getSharedKey());
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
    public void testEnableOnlyEmailNotifications() throws Exception {

        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_EMAIL, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("notifications toggle", settingsApi.isNotificationsOn());
                assertThat("notifications toggle", settingsApi.getNotificationTypes().contains(Settings.NOTIFICATION_TYPE_EMAIL));
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
    public void testEnableOnlySmsNotifications() throws Exception {

        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_SMS, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("notifications toggle", settingsApi.isNotificationsOn());
                assertThat("notifications toggle", settingsApi.getNotificationTypes().contains(Settings.NOTIFICATION_TYPE_SMS));
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
    public void testEnableEmailAndSmsNotifications() throws Exception {

        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_ALL, new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("notifications toggle", settingsApi.isNotificationsOn());
                assertThat(settingsApi.getNotificationTypes(), CoreMatchers.hasItem(Settings.NOTIFICATION_TYPE_ALL));
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
    public void testDisableNotifications() throws Exception {

        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_EMAIL, new Settings.ResultListener() {
            public void onSuccess() {
            }

            public void onFail() {
            }

            public void onBadRequest() {
            }
        });
        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_SMS, new Settings.ResultListener() {
            public void onSuccess() {
            }

            public void onFail() {
            }

            public void onBadRequest() {
            }
        });

        Thread.sleep(1000);

        settingsApi.disableNotification(Settings.NOTIFICATION_TYPE_SMS, new Settings.ResultListener() {
            public void onSuccess() {
                //Only email should be active now
                assertThat("notifications toggle", settingsApi.isNotificationsOn());
                assertThat("notifications toggle", settingsApi.getNotificationTypes().contains(Settings.NOTIFICATION_TYPE_EMAIL));
            }

            public void onFail() {
                fail("");
            }

            public void onBadRequest() {
                fail("");
            }
        });

        settingsApi.disableNotification(Settings.NOTIFICATION_TYPE_EMAIL, new Settings.ResultListener() {
            public void onSuccess() {
                //None should be active now
                assertThat("notifications toggle", !settingsApi.isNotificationsOn());
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
    public void testDisableAllNotifications() throws Exception {

        settingsApi.disableAllNotifications(new Settings.ResultListener() {
            public void onSuccess() {
                assertThat("notifications toggle", !settingsApi.isNotificationsOn());
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
}