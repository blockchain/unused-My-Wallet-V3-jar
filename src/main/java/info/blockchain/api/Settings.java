package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class Settings extends BaseApi {

    private static final String WALLET = "wallet";
    public static final String PROD_PAYLOAD_URL = PROTOCOL + SERVER_ADDRESS + WALLET;

    public Settings() {
        // No-op
    }

    //API methods
    public static final String METHOD_GET_INFO = "get-info";
    public static final String METHOD_TOGGLE_SAVE_2FA = "update-never-save-auth-type";
    public static final String METHOD_VERIFY_EMAIL = "verify-email";
    public static final String METHOD_VERIFY_SMS = "verify-sms";
    public static final String METHOD_UPDATE_NOTIFICATION_TYPE = "update-notifications-type";
    public static final String METHOD_UPDATE_NOTIFICATION_ON = "update-notifications-on";
    public static final String METHOD_UPDATE_SMS = "update-sms";
    public static final String METHOD_UPDATE_EMAIL = "update-email";
    public static final String METHOD_UPDATE_BTC_CURRENCY = "update-btc-currency";
    public static final String METHOD_UPDATE_CURRENCY = "update-currency";
    public static final String METHOD_UPDATE_IP_LOCK = "update-ip-lock";
    public static final String METHOD_UPDATE_IP_LOCK_ON = "update-ip-lock-on";
    public static final String METHOD_UPDATE_LANGUAGE = "update-language";
    public static final String METHOD_UPDATE_BLOCK_TOR_IPS = "update-block-tor-ips";
    public static final String METHOD_UPDATE_PASSWORD_HINT_1 = "update-password-hint1";
    public static final String METHOD_UPDATE_PASSWORD_HINT_2 = "update-password-hint2";
    public static final String METHOD_UPDATE_LOGGING_LEVEL = "update-logging-level";
    public static final String METHOD_UPDATE_AUTH_TYPE = "update-auth-type";

    public static final String UNIT_BTC = "BTC";
    public static final String UNIT_MBC = "MBC";
    public static final String UNIT_UBC = "UBC";

    /**
     * Currencies handles by https://blockchain.info/ticker
     */
    public static String[] UNIT_FIAT = {
            "AUD", "BRL", "CAD", "CHF", "CLP", "CNY", "DKK", "EUR", "GBP", "HKD",
            "ISK", "JPY", "KRW", "NZD", "PLN", "RUB", "SEK", "SGD", "THB", "TWD", "USD"
    };

    public static final int NOTIFICATION_ON = 2;
    public static final int NOTIFICATION_OFF = 0;

    public static final int NOTIFICATION_TYPE_NONE = 0;
    public static final int NOTIFICATION_TYPE_EMAIL = 1;
    public static final int NOTIFICATION_TYPE_SMS = 32;
    public static final int NOTIFICATION_TYPE_ALL = 33;

    public static final int AUTH_TYPE_OFF = 0;
    public static final int AUTH_TYPE_YUBI_KEY = 1;
    public static final int AUTH_TYPE_EMAIL = 2;
    public static final int AUTH_TYPE_GOOGLE_AUTHENTICATOR = 4;
    public static final int AUTH_TYPE_SMS = 5;

    private String btcCurrency;
    private String dialCode;
    private String currency;
    private String email;
    private boolean smsVerified;
    private boolean isApiAccessEnabled;
    private int authType;
    private boolean emailVerified;
    private String passwordHint1;
    private String passwordHint2;
    private String sms;
    private boolean blockTorIps;
    private ArrayList<Integer> notificationType;
    private boolean notificationsOn;

    private String language;
    private boolean ipLockOn;
    private int notificationsConfirmations;
    private boolean autoEmailBackup;
    private boolean neverSaveAuthType;
    private String myIp;
    private String countryCode;
    private int loggingLevel;

    private String guid;
    private String sharedKey;

    public interface ResultListener {
        void onSuccess();

        void onFail();

        void onBadRequest();
    }

    public Settings(String guid, String sharedKey) {

        this.guid = guid;
        this.sharedKey = sharedKey;

        String jsonString = null;
        try {
            jsonString = getInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jsonString != null)
            parseJson(jsonString);
    }

    public Settings(String settingsJson) {

        if (settingsJson != null)
            parseJson(settingsJson);
    }

    private String updateSettings(String method, String settingsPayload) throws Exception {

        settingsPayload = settingsPayload.trim();

        StringBuilder args = new StringBuilder();
        if (!settingsPayload.isEmpty()) {
            args.append("length=").append(settingsPayload.length());
            args.append("&payload=").append(URLEncoder.encode(settingsPayload, "utf-8"));
            args.append("&method=").append(method);
        } else {
            args.append("method=").append(method);
        }

        args.append("&guid=").append(URLEncoder.encode(this.guid, "utf-8"));
        args.append("&sharedKey=").append(URLEncoder.encode(this.sharedKey, "utf-8"));
        args.append(getApiCode());
        args.append("&format=plain");

        return WebUtil.getInstance().postURL(PersistentUrls.getInstance().getWalletPayloadUrl(), args.toString());
    }

    public String getInfo() throws Exception {
        return updateSettings(METHOD_GET_INFO, "");
    }

    private boolean updateValue(String method, String value) {
        try {
            updateSettings(method, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void parseJson(String jsonString) {

        JSONObject jsonObject = new JSONObject(jsonString);
        if (jsonObject.has("btc_currency")) btcCurrency = jsonObject.getString("btc_currency");

        notificationType = new ArrayList<Integer>();
        JSONArray notificationTypeJsonArray = jsonObject.getJSONArray("notifications_type");
        for (int i = 0; i < notificationTypeJsonArray.length(); i++) {
            notificationType.add(notificationTypeJsonArray.getInt(i));
        }
        if (jsonObject.has("language")) language = jsonObject.getString("language");
        if (jsonObject.has("notifications_on"))
            notificationsOn = toBoolean(jsonObject.getInt("notifications_on"));
        if (jsonObject.has("ip_lock_on")) ipLockOn = toBoolean(jsonObject.getInt("ip_lock_on"));
        if (jsonObject.has("dial_code")) dialCode = jsonObject.getString("dial_code");
        if (jsonObject.has("block_tor_ips"))
            blockTorIps = toBoolean(jsonObject.getInt("block_tor_ips"));
        if (jsonObject.has("currency")) currency = jsonObject.getString("currency");
        if (jsonObject.has("notifications_confirmations"))
            notificationsConfirmations = jsonObject.getInt("notifications_confirmations");
        if (jsonObject.has("auto_email_backup"))
            autoEmailBackup = toBoolean(jsonObject.getInt("auto_email_backup"));
        if (jsonObject.has("never_save_auth_type"))
            neverSaveAuthType = toBoolean(jsonObject.getInt("never_save_auth_type"));
        if (jsonObject.has("email")) email = jsonObject.getString("email");
        if (jsonObject.has("sms_verified"))
            smsVerified = toBoolean(jsonObject.getInt("sms_verified"));
        if (jsonObject.has("is_api_access_enabled"))
            isApiAccessEnabled = toBoolean(jsonObject.getInt("is_api_access_enabled"));
        if (jsonObject.has("auth_type")) authType = jsonObject.getInt("auth_type");
        if (jsonObject.has("my_ip")) myIp = jsonObject.getString("my_ip");
        if (jsonObject.has("email_verified"))
            emailVerified = toBoolean(jsonObject.getInt("email_verified"));
        if (jsonObject.has("password_hint1"))
            passwordHint1 = jsonObject.getString("password_hint1");
        if (jsonObject.has("country_code")) countryCode = jsonObject.getString("country_code");
        if (jsonObject.has("logging_level")) loggingLevel = jsonObject.getInt("logging_level");
        if (jsonObject.has("guid")) guid = jsonObject.getString("guid");
        if (jsonObject.has("sms_number")) sms = jsonObject.getString("sms_number");
    }

    private boolean toBoolean(int value) {
        return value != 0;
    }

    public String getBtcCurrency() {
        return btcCurrency;
    }

    public boolean isNotificationsOn() {
        return notificationsOn;
    }

    public String getDialCode() {
        return dialCode;
    }

    public String getFiatCurrency() {
        return currency;
    }

    public String getEmail() {
        return email;
    }

    public boolean isSmsVerified() {
        return smsVerified;
    }

    public boolean isApiAccessEnabled() {
        return isApiAccessEnabled;
    }

    public int getAuthType() {
        return authType;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getPasswordHint1() {
        return passwordHint1;
    }

    public String getPasswordHint2() {
        return passwordHint2;
    }

    public String getGuid() {
        return guid;
    }

    public boolean isTorBlocked() {
        return blockTorIps;
    }

    private boolean isBadPasswordHint(String hint) {
        return hint == null || hint.isEmpty() || hint.length() > 255;
    }

    public String getSms() {
        return sms;
    }

    public ArrayList<Integer> getNotificationTypes() {
        return notificationType;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isIpLockOn() {
        return ipLockOn;
    }

    public int getNotificationsConfirmations() {
        return notificationsConfirmations;
    }

    public boolean isAutoEmailBackup() {
        return autoEmailBackup;
    }

    public boolean isNeverSaveAuthType() {
        return neverSaveAuthType;
    }

    public String getMyIp() {
        return myIp;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public int getLoggingLevel() {
        return loggingLevel;
    }

    public void setEmail(String email, ResultListener listener) {

        if (email == null || email.isEmpty()) {
            listener.onBadRequest();
        } else if (updateValue(METHOD_UPDATE_EMAIL, email)) {
            this.email = email;
            this.emailVerified = false;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void setSms(String sms, ResultListener listener) {
        if (sms == null || sms.isEmpty()) {
            listener.onBadRequest();
        } else if (updateValue(METHOD_UPDATE_SMS, sms)) {
            this.sms = sms;
            this.smsVerified = false;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void verifyEmail(String code, ResultListener listener) {
        if (code == null || code.isEmpty()) {
            listener.onBadRequest();
        } else if (updateValue(METHOD_VERIFY_EMAIL, code)) {
            this.emailVerified = true;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void verifySms(String code, ResultListener listener) {
        if (code == null || code.isEmpty()) {
            listener.onBadRequest();
        } else if (updateValue(METHOD_VERIFY_SMS, code)) {
            this.smsVerified = true;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void setPasswordHint1(String hint, ResultListener listener) {
        if (hint == null || hint.isEmpty() || isBadPasswordHint(hint)) {
            listener.onBadRequest();
        } else if (updateValue(METHOD_UPDATE_PASSWORD_HINT_1, hint)) {
            this.passwordHint1 = hint;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void setPasswordHint2(String hint, ResultListener listener) {
        if (hint == null || hint.isEmpty() || isBadPasswordHint(hint)) {
            listener.onBadRequest();
        } else if (updateValue(METHOD_UPDATE_PASSWORD_HINT_2, hint)) {
            this.passwordHint2 = hint;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void setBtcCurrency(String btcCurrency, ResultListener listener) {
        if (btcCurrency == null || btcCurrency.isEmpty()) {
            listener.onBadRequest();
        } else if (updateValue(METHOD_UPDATE_BTC_CURRENCY, btcCurrency)) {
            this.btcCurrency = btcCurrency;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void setFiatCurrency(String currency, ResultListener listener) {
        if (currency == null || currency.isEmpty()) {
            listener.onBadRequest();
        } else if (updateValue(METHOD_UPDATE_CURRENCY, currency)) {
            this.currency = currency;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void setTorBlocked(boolean block, ResultListener listener) {
        int value = 0;
        if (block) value = 1;
        boolean success = updateValue(METHOD_UPDATE_BLOCK_TOR_IPS, value + "");
        if (success) {
            this.blockTorIps = block;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    /**
     * @param type NOTIFICATION_TYPE_SMS, NOTIFICATION_TYPE_EMAIL, NOTIFICATION_TYPE_ALL
     */
    public void enableNotification(int type, ResultListener listener) {

        if ((type == NOTIFICATION_TYPE_EMAIL && notificationType.contains(NOTIFICATION_TYPE_SMS))
                || (type == NOTIFICATION_TYPE_SMS && notificationType.contains(NOTIFICATION_TYPE_EMAIL))) {
            type = NOTIFICATION_TYPE_ALL;
        }

        boolean success = updateValue(METHOD_UPDATE_NOTIFICATION_TYPE, type + "");
        if (success) {
            if (type == NOTIFICATION_TYPE_ALL) {
                notificationType.clear();
            }
            if (!notificationType.contains(type)) {
                notificationType.add(type);
            }

            enableNotifications(true, listener);

            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void disableNotification(int type, ResultListener listener) {

        if (notificationType.contains(type)) {
            notificationType.remove((Integer) type);

            if (notificationType.size() > 0) {

                //SMS removed. Email type still active
                if (type == NOTIFICATION_TYPE_SMS && notificationType.contains(NOTIFICATION_TYPE_EMAIL)) {
                    boolean success = updateValue(METHOD_UPDATE_NOTIFICATION_TYPE, NOTIFICATION_TYPE_EMAIL + "");
                    if (success) {
                        listener.onSuccess();
                    } else {
                        listener.onFail();
                    }
                }

                //Email removed. Sms type still active
                if (type == NOTIFICATION_TYPE_EMAIL && notificationType.contains(NOTIFICATION_TYPE_SMS)) {
                    boolean success = updateValue(METHOD_UPDATE_NOTIFICATION_TYPE, NOTIFICATION_TYPE_SMS + "");
                    if (success) {
                        listener.onSuccess();
                    } else {
                        listener.onFail();
                    }
                }

            } else {
                //No more notifications left - disable all
                disableAllNotifications(listener);
            }

        } else {
            listener.onSuccess();
        }
    }

    public void enableAllNotifications(ResultListener listener) {

        boolean success = updateValue(METHOD_UPDATE_NOTIFICATION_TYPE, NOTIFICATION_TYPE_ALL + "");
        if (success) {
            if (!notificationType.contains(NOTIFICATION_TYPE_ALL)) {
                notificationType.add(NOTIFICATION_TYPE_ALL);
                enableNotifications(true, listener);
            }

            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void disableAllNotifications(ResultListener listener) {

        boolean success = updateValue(METHOD_UPDATE_NOTIFICATION_TYPE, NOTIFICATION_TYPE_NONE + "");
        if (success) {
            notificationType = new ArrayList<Integer>();

            enableNotifications(false, listener);

            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    private void enableNotifications(boolean enable, ResultListener listener) {
        int value;
        if (enable) {
            value = NOTIFICATION_ON;
        } else {
            value = NOTIFICATION_OFF;
        }
        boolean success = updateValue(METHOD_UPDATE_NOTIFICATION_ON, value + "");
        if (success) {
            this.notificationsOn = enable;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }

    public void setAuthType(int type, ResultListener listener) {
        boolean success = updateValue(METHOD_UPDATE_AUTH_TYPE, type + "");
        if (success) {
            authType = type;
            listener.onSuccess();
        } else {
            listener.onFail();
        }
    }
}