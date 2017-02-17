package info.blockchain.wallet.settings;

import info.blockchain.MockedResponseTest;
import info.blockchain.wallet.api.data.SettingsBody;
import java.io.IOException;
import okhttp3.ResponseBody;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Call;

public class SettingsTest extends MockedResponseTest {

    private String guid = "49819a7c-2426-49da-90fd-9dabbd837cc8";
    private String sharedKey = "f57b221c-38ec-4e5d-8164-f120d9df0d0b";

    @Test
    public void getInfo() throws Exception {

        mockInterceptor.setResponseString("{\"btc_currency\":\"BTC\",\"notifications_type\":[],\"language\":\"en\",\"notifications_on\":0,\"ip_lock_on\":0,\"dial_code\":\"44\",\"block_tor_ips\":0,\"currency\":\"GBP\",\"notifications_confirmations\":0,\"auto_email_backup\":0,\"never_save_auth_type\":0,\"email\":\"john@mail.com\",\"sms_verified\":0,\"is_api_access_enabled\":0,\"auth_type\":0,\"my_ip\":\"211.160.45.230\",\"email_verified\":0,\"languages\":{\"de\":\"German\",\"no\":\"Norwegian\",\"hi\":\"Hindi\",\"ru\":\"Russian\",\"pt\":\"Portuguese\",\"bg\":\"Bulgarian\",\"fr\":\"French\",\"hu\":\"Hungarian\",\"zh-cn\":\"Chinese\",\"sl\":\"Slovenian\",\"id\":\"Indonesian\",\"sv\":\"Swedish\",\"ko\":\"Korean\",\"el\":\"Greek\",\"en\":\"English\",\"it\":\"Italian\",\"es\":\"Spanish\",\"vi\":\"Vietnam\",\"th\":\"Thai\",\"ja\":\"Japanese\",\"pl\":\"Polish\",\"da\":\"Danish\",\"ro\":\"Romanian\",\"nl\":\"Dutch\",\"tr\":\"Turkish\"},\"invited\":{\"sfox\":false},\"country_code\":\"GB\",\"unsubscribed\":false,\"logging_level\":0,\"guid\":\"49819a7c-2426-49da-90fd-9dabbd837cc8\",\"btc_currencies\":{\"BTC\":\"Bitcoin\",\"UBC\":\"Bits(uBTC)\",\"MBC\":\"MilliBit(mBTC)\"},\"currencies\":{\"ISK\":\"IcelandicKróna\",\"HKD\":\"HongKongDollar\",\"TWD\":\"NewTaiwandollar\",\"CHF\":\"SwissFranc\",\"EUR\":\"Euro\",\"DKK\":\"DanishKrone\",\"CLP\":\"ChileanPeso\",\"USD\":\"U.S.dollar\",\"CAD\":\"CanadianDollar\",\"CNY\":\"Chineseyuan\",\"THB\":\"Thaibaht\",\"AUD\":\"AustralianDollar\",\"SGD\":\"SingaporeDollar\",\"KRW\":\"SouthKoreanWon\",\"JPY\":\"JapaneseYen\",\"PLN\":\"PolishZloty\",\"GBP\":\"GreatBritishPound\",\"SEK\":\"SwedishKrona\",\"NZD\":\"NewZealandDollar\",\"BRL\":\"BrazilReal\",\"RUB\":\"RussianRuble\"}}");
        Settings settings = new Settings(guid, sharedKey);

        Call<SettingsBody> call = settings.getInfo();

        SettingsBody settingsBody = call.execute().body();
        Assert.assertEquals("BTC", settingsBody.getBtcCurrency());
        Assert.assertEquals(0, settingsBody.getNotificationsType().size());
        Assert.assertEquals("en", settingsBody.getLanguage());
        Assert.assertEquals(0, settingsBody.getNotificationsOn());
        Assert.assertEquals(0, settingsBody.getIpLockOn());
        Assert.assertEquals("44", settingsBody.getDialCode());
        Assert.assertEquals(0, settingsBody.getBlockTorIps());
        Assert.assertEquals("GBP", settingsBody.getCurrency());
        Assert.assertEquals(0, settingsBody.getNotificationsConfirmations());
        Assert.assertEquals(0, settingsBody.getAutoEmailBackup());
        Assert.assertEquals(0, settingsBody.getNeverSaveAuthType());
        Assert.assertEquals("john@mail.com", settingsBody.getEmail());
        Assert.assertEquals(0, settingsBody.getSmsVerified());
        Assert.assertEquals(0, settingsBody.getIsApiAccessEnabled());
        Assert.assertEquals(0, settingsBody.getAuthType());
        Assert.assertEquals("211.160.45.230", settingsBody.getMyIp());
        Assert.assertEquals(0, settingsBody.getEmailVerified());
        Assert.assertEquals("GB", settingsBody.getCountryCode());
        Assert.assertEquals(0, settingsBody.getLoggingLevel());
        Assert.assertEquals("49819a7c-2426-49da-90fd-9dabbd837cc8", settingsBody.getGuid());

    }

    @Test
    public void updateSetting() throws IOException {

        Settings settings = new Settings(guid, sharedKey);

        mockInterceptor.setResponseString("Email Updated");
        Call<ResponseBody> call = settings.updateSetting(Settings.METHOD_UPDATE_EMAIL, "moo@moo.com");
        Assert.assertNotNull(call.execute().body().string());

        mockInterceptor.setResponseString("Email Verified");
        call = settings.updateSetting(Settings.METHOD_VERIFY_EMAIL, "1234");
        Assert.assertNotNull(call.execute().body().string());

        mockInterceptor.setResponseString("SMS Verified");
        call = settings.updateSetting(Settings.METHOD_VERIFY_SMS, "1234");
        Assert.assertNotNull(call.execute().body().string());

        mockInterceptor.setResponseString("Notifications settings updated");
        call = settings.updateSetting(Settings.METHOD_UPDATE_NOTIFICATION_TYPE, SettingsBody.NOTIFICATION_TYPE_EMAIL);
        Assert.assertNotNull(call.execute().body().string());

        mockInterceptor.setResponseString("Notifications settings updated");
        call = settings.updateSetting(Settings.METHOD_UPDATE_NOTIFICATION_ON, SettingsBody.NOTIFICATION_ON);
        Assert.assertNotNull(call.execute().body().string());

        mockInterceptor.setResponseString("SMS Number Successfully Updated. Verification Message Sent.");
        call = settings.updateSetting(Settings.METHOD_UPDATE_SMS, "+44 75 0000 0000");
        Assert.assertNotNull(call.execute().body().string());

        mockInterceptor.setResponseString("Currency Successfully updated");
        call = settings.updateSetting(Settings.METHOD_UPDATE_BTC_CURRENCY, SettingsBody.UNIT_BTC);
        Assert.assertNotNull(call.execute().body().string());

        mockInterceptor.setResponseString("Currency Successfully updated");
        call = settings.updateSetting(Settings.METHOD_UPDATE_CURRENCY, SettingsBody.UNIT_FIAT[0]);
        Assert.assertNotNull(call.execute().body().string());

        mockInterceptor.setResponseString("Updated Password Hint");
        call = settings.updateSetting(Settings.METHOD_UPDATE_PASSWORD_HINT_1, "my hint");
        Assert.assertNotNull(call.execute().body().string());

        mockInterceptor.setResponseString("Two factor authentication settings updated.");
        call = settings.updateSetting(Settings.METHOD_UPDATE_AUTH_TYPE, SettingsBody.AUTH_TYPE_SMS);
        Assert.assertNotNull(call.execute().body().string());
    }
}