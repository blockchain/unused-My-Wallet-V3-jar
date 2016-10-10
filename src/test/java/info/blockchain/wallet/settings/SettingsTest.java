package info.blockchain.wallet.settings;

import info.blockchain.api.Settings;
import info.blockchain.test_data.SettingsTestData;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SettingsTest {

    @Test
    public void testSettings() throws Exception {
        Settings settings = new Settings(SettingsTestData.apiResponseString);
        assertThat(settings.getAuthType(), is(1));
        assertThat(settings.getBtcCurrency(), is("BTC"));
        assertThat(settings.getFiatCurrency(), is("GBP"));
        assertThat(settings.getDialCode(), is("44"));
        assertThat(settings.getEmail(), is("nope@nope.com"));
        assertThat(settings.getGuid(), is("a0991-nope-nope-nope-fe0661"));
        assertThat(settings.getPasswordHint1(), is("hello_"));
        assertThat(settings.getSms(), is("+44 7512341234"));
    }
}