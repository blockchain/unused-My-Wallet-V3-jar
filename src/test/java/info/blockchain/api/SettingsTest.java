package info.blockchain.api;

import info.blockchain.wallet.payload.Payload;
import info.blockchain.wallet.payload.PayloadManager;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SettingsTest {

    String mnemonic = "all all all all all all all all all all all all";
    Settings settingsApi;

    @Before
    public void setUp() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();
        Payload payload = payloadManager.restoreHDWallet("password", mnemonic, "");
        System.out.println(payload);
        settingsApi = new Settings(payload.getGuid(), payload.getSharedKey());
    }

    @Test
    public void testUpdateSms() throws Exception {
        final String value = "+44 75 1234 1234";
        settingsApi.setSms(value);
        assertThat(settingsApi.getSms(), is(value));
    }

    @Test
    public void testUpdateEmail() throws Exception {
        final String value = "nope@nope.com";
        settingsApi.setEmail(value);
        assertThat(settingsApi.getEmail(), is(value));
    }

    @Test
    public void testUpdatePasswordHint1() throws Exception {
        final String value = "pw1";
        settingsApi.setPasswordHint1(value);
        assertThat(settingsApi.getPasswordHint1(), is(value));
    }

    @Test
    public void testUpdatePasswordHint2() throws Exception {
        final String value = "pw2";
        settingsApi.setPasswordHint2(value);
        assertThat(settingsApi.getPasswordHint2(), is(value));
    }

    @Test
    public void testUpdateBtcCurrency() throws Exception {
        settingsApi.setBtcCurrency(Settings.UNIT_BTC);
        assertThat(settingsApi.getBtcCurrency(), is(Settings.UNIT_BTC));

        settingsApi.setBtcCurrency(Settings.UNIT_MBC);
        assertThat(settingsApi.getBtcCurrency(), is(Settings.UNIT_MBC));

        settingsApi.setBtcCurrency(Settings.UNIT_UBC);
        assertThat(settingsApi.getBtcCurrency(), is(Settings.UNIT_UBC));
    }

    @Test
    public void testUpdateFiatCurrency() throws Exception {
        for (int i = 0; i < settingsApi.UNIT_FIAT.length; i++) {
            final int finalI = i;
            settingsApi.setFiatCurrency(Settings.UNIT_FIAT[i]);
            assertThat(settingsApi.getFiatCurrency(), is(Settings.UNIT_FIAT[finalI]));
        }
    }

    @Test
    public void testSetTorBlocked() throws Exception {

        settingsApi.setTorBlocked(false);
        assertThat("block tor", !settingsApi.isTorBlocked());

        settingsApi.setTorBlocked(true);
        assertThat("block tor", settingsApi.isTorBlocked());
    }

    @Test
    public void testEnableOnlyEmailNotifications() throws Exception {

        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_EMAIL);
        assertThat("notifications toggle", settingsApi.isNotificationsOn());
        assertThat("notifications toggle", settingsApi.getNotificationTypes().contains(Settings.NOTIFICATION_TYPE_EMAIL));
    }

    @Test
    public void testEnableOnlySmsNotifications() throws Exception {

        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_SMS);
        assertThat("notifications toggle", settingsApi.isNotificationsOn());
        assertThat("notifications toggle", settingsApi.getNotificationTypes().contains(Settings.NOTIFICATION_TYPE_SMS));
    }

    @Test
    public void testEnableEmailAndSmsNotifications() throws Exception {

        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_ALL);
        assertThat("notifications toggle", settingsApi.isNotificationsOn());
        assertThat(settingsApi.getNotificationTypes(), CoreMatchers.hasItem(Settings.NOTIFICATION_TYPE_ALL));
    }

    @Test
    public void testDisableNotifications() throws Exception {

        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_EMAIL);
        settingsApi.enableNotification(Settings.NOTIFICATION_TYPE_SMS);

        Thread.sleep(1000);

        settingsApi.disableNotification(Settings.NOTIFICATION_TYPE_SMS);
        //Only email should be active now
        assertThat("notifications toggle", settingsApi.isNotificationsOn());
        assertThat("notifications toggle", settingsApi.getNotificationTypes().contains(Settings.NOTIFICATION_TYPE_EMAIL));

        Thread.sleep(1000);

        settingsApi.disableNotification(Settings.NOTIFICATION_TYPE_EMAIL);
        //None should be active now
        assertThat("notifications toggle", !settingsApi.isNotificationsOn());
        assertThat("notifications toggle", settingsApi.getNotificationTypes().isEmpty());
    }

    @Test
    public void testDisableAllNotifications() throws Exception {

        settingsApi.disableAllNotifications();
        assertThat("notifications toggle", !settingsApi.isNotificationsOn());
        assertThat("notifications toggle", settingsApi.getNotificationTypes().isEmpty());
    }

    @Test
    public void testAuthType() throws Exception {

        settingsApi.setAuthType(Settings.AUTH_TYPE_SMS);
        assertThat(settingsApi.getAuthType(), is(Settings.AUTH_TYPE_SMS));

        settingsApi.setAuthType(Settings.AUTH_TYPE_EMAIL);
        assertThat(settingsApi.getAuthType(), is(Settings.AUTH_TYPE_EMAIL));

        settingsApi.setAuthType(Settings.AUTH_TYPE_OFF);
        assertThat(settingsApi.getAuthType(), is(Settings.AUTH_TYPE_OFF));
    }
}