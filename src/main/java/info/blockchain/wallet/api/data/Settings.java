package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class Settings {

    @JsonIgnore
    public static final int NOTIFICATION_ON = 2;
    @JsonIgnore
    public static final int NOTIFICATION_OFF = 0;

    @JsonIgnore
    public static final int NOTIFICATION_TYPE_NONE = 0;
    @JsonIgnore
    public static final int NOTIFICATION_TYPE_EMAIL = 1;
    @JsonIgnore
    public static final int NOTIFICATION_TYPE_SMS = 32;
    @JsonIgnore
    public static final int NOTIFICATION_TYPE_ALL = 33;

    @JsonIgnore
    public static final int AUTH_TYPE_OFF = 0;
    @JsonIgnore
    public static final int AUTH_TYPE_YUBI_KEY = 1;
    @JsonIgnore
    public static final int AUTH_TYPE_EMAIL = 2;
    @JsonIgnore
    public static final int AUTH_TYPE_GOOGLE_AUTHENTICATOR = 4;
    @JsonIgnore
    public static final int AUTH_TYPE_SMS = 5;

    @JsonIgnore
    public static final String UNIT_BTC = "BTC";
    @JsonIgnore
    public static final String UNIT_MBC = "MBC";
    @JsonIgnore
    public static final String UNIT_UBC = "UBC";

    @JsonIgnore
    public static String[] UNIT_FIAT = {
        "AUD", "BRL", "CAD", "CHF", "CLP", "CNY", "DKK", "EUR", "GBP", "HKD",
        "ISK", "JPY", "KRW", "NZD", "PLN", "RUB", "SEK", "SGD", "THB", "TWD", "USD"
    };

    @JsonProperty("btc_currency")
    private String btcCurrency;

    @JsonProperty("notifications_type")
    private ArrayList<Integer> notificationsType;

    @JsonProperty("language")
    private String language;

    @JsonProperty("notifications_on")
    private int notificationsOn;

    @JsonProperty("ip_lock_on")
    private int ipLockOn;

    @JsonProperty("dial_code")
    private String dialCode;

    @JsonProperty("block_tor_ips")
    private int blockTorIps;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("notifications_confirmations")
    private int notificationsConfirmations;

    @JsonProperty("auto_email_backup")
    private int autoEmailBackup;

    @JsonProperty("never_save_auth_type")
    private int neverSaveAuthType;

    @JsonProperty("email")
    private String email;

    @JsonProperty("sms_number")
    private String smsNumber;

    @JsonProperty("sms_verified")
    private int smsVerified;

    @JsonProperty("is_api_access_enabled")
    private int isApiAccessEnabled;

    @JsonProperty("auth_type")
    private int authType;

    @JsonProperty("my_ip")
    private String myIp;

    @JsonProperty("email_verified")
    private int emailVerified;

    @JsonProperty("password_hint1")
    private String passwordHint1;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("state")
    private String state;

    @JsonProperty("logging_level")
    private int loggingLevel;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("invited")
    private HashMap<String, Boolean> invited;

    public String getBtcCurrency() {
        return btcCurrency;
    }

    public ArrayList<Integer> getNotificationsType() {
        return notificationsType;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isNotificationsOn() {
        return intToBoolean(notificationsOn);
    }

    public int getIpLockOn() {
        return ipLockOn;
    }

    public String getDialCode() {
        return dialCode;
    }

    public boolean isBlockTorIps() {
        return intToBoolean(blockTorIps);
    }

    public String getCurrency() {
        return currency;
    }

    public int getNotificationsConfirmations() {
        return notificationsConfirmations;
    }

    public boolean isAutoEmailBackup() {
        return intToBoolean(autoEmailBackup);
    }

    public boolean isNeverSaveAuthType() {
        return intToBoolean(neverSaveAuthType);
    }

    public String getEmail() {
        return email;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public boolean isSmsVerified() {
        return intToBoolean(smsVerified);
    }

    public boolean isApiAccessEnabled() {
        return intToBoolean(isApiAccessEnabled);
    }

    public int getAuthType() {
        return authType;
    }

    public String getMyIp() {
        return myIp;
    }

    public boolean isEmailVerified() {
        return intToBoolean(emailVerified);
    }

    public String getPasswordHint1() {
        return passwordHint1;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getState() {
        return state;
    }

    public int getLoggingLevel() {
        return loggingLevel;
    }

    public String getGuid() {
        return guid;
    }

    @JsonIgnore
    public static Settings fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, Settings.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    private boolean intToBoolean(int value) {
        return value != 0;
    }

    public HashMap<String, Boolean> getInvited() {
        return invited;
    }
}
