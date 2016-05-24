package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class Settings {

    //API methods
    public static final String METHOD_GET_INFO = "get-info";
    public static final String METHOD_TOGGLE_SAVE_2FA = "update-never-save-auth-type";
    public static final String METHOD_VERIFY_EMAIL = "verify-email";
    public static final String METHOD_VERIFY_SMS = "verify-sms";
    public static final String METHOD_UPDATE_NOTIFICATION_TYPE = "update-notifications-type";
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

    private String btcCurrency;

    private boolean notificationsOn;

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

    private String[] notificationType;
    private String language;
    private boolean ipLockOn;
    private boolean blockTorIps;
    private int notificationsConfirmations;
    private boolean autoEmailBackup;
    private boolean neverSaveAuthType;
    private String myIp;
    private String countryCode;
    private int loggingLevel;

    private String guid;
    private String sharedKey;

    public Settings(String guid, String sharedKey){

        this.guid = guid;
        this.sharedKey = sharedKey;

        String jsonString = null;
        try {
            jsonString = getInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(jsonString != null)
            parseJson(jsonString);
    }

    public Settings(String settingsJson){

        if(settingsJson != null)
            parseJson(settingsJson);
    }

    private String updateSettings(String method, String settingsPayload) throws Exception{

        settingsPayload = settingsPayload.trim();

        StringBuilder args = new StringBuilder();
        if(settingsPayload != null && !settingsPayload.isEmpty()) {
            args.append("length=" + settingsPayload.length());
            args.append("&payload=" + URLEncoder.encode(settingsPayload, "utf-8"));
            args.append("&method="+method);
        }else{
            args.append("method="+method);
        }

        args.append("&guid="+URLEncoder.encode(this.guid, "utf-8"));
        args.append("&sharedKey="+URLEncoder.encode(this.sharedKey, "utf-8"));
        args.append("&api_code=" + WebUtil.API_CODE);
        args.append("&format=plain");

        String response = WebUtil.getInstance().postURL(WebUtil.PAYLOAD_URL, args.toString());
        return response;
    }

    public String getInfo() throws Exception{
        return updateSettings(METHOD_GET_INFO, "");
    }

    private boolean updateValue(String method, String value){
        try {
            updateSettings(method, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void parseJson(String jsonString){

        JSONObject jsonObject = new JSONObject(jsonString);
        if(jsonObject.has("btc_currency"))btcCurrency = jsonObject.getString("btc_currency");
        JSONArray notificationTypeJsonArray = jsonObject.getJSONArray("notifications_type");//TODO
        if(jsonObject.has("language"))language = jsonObject.getString("language");
        if(jsonObject.has("notifications_on"))notificationsOn = toBoolean(jsonObject.getInt("notifications_on"));
        if(jsonObject.has("ip_lock_on"))ipLockOn = toBoolean(jsonObject.getInt("ip_lock_on"));
        if(jsonObject.has("dial_code"))dialCode = jsonObject.getString("dial_code");
        if(jsonObject.has("block_tor_ips"))blockTorIps = toBoolean(jsonObject.getInt("block_tor_ips"));
        if(jsonObject.has("currency"))currency = jsonObject.getString("currency");
        if(jsonObject.has("notifications_confirmations"))notificationsConfirmations = jsonObject.getInt("notifications_confirmations");
        if(jsonObject.has("auto_email_backup"))autoEmailBackup = toBoolean(jsonObject.getInt("auto_email_backup"));
        if(jsonObject.has("never_save_auth_type"))neverSaveAuthType = toBoolean(jsonObject.getInt("never_save_auth_type"));
        if(jsonObject.has("email"))email = jsonObject.getString("email");
        if(jsonObject.has("sms_verified"))smsVerified = toBoolean(jsonObject.getInt("sms_verified"));
        if(jsonObject.has("is_api_access_enabled"))isApiAccessEnabled = toBoolean(jsonObject.getInt("is_api_access_enabled"));
        if(jsonObject.has("auth_type"))authType = jsonObject.getInt("auth_type");
        if(jsonObject.has("my_ip"))myIp = jsonObject.getString("my_ip");
        if(jsonObject.has("email_verified"))emailVerified = toBoolean(jsonObject.getInt("email_verified"));
        if(jsonObject.has("password_hint1"))passwordHint1 = jsonObject.getString("password_hint1");
        if(jsonObject.has("country_code"))countryCode = jsonObject.getString("country_code");
        if(jsonObject.has("logging_level"))loggingLevel = jsonObject.getInt("logging_level");
        if(jsonObject.has("guid"))guid = jsonObject.getString("guid");
        if(jsonObject.has("sms_number"))sms = jsonObject.getString("sms_number");
    }

    private boolean toBoolean (int value) {
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

    public String getCurrency() {
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

    public String getGuid() {
        return guid;
    }

    public String getSms() {
        return sms;
    }

    public boolean setEmail(String email){
        boolean success = updateValue(METHOD_UPDATE_EMAIL, email);
        if(success){
            this.email = email;
            this.emailVerified = false;
        }
        return success;
    }

    public boolean setSms(String sms){
        boolean success = updateValue(METHOD_UPDATE_SMS, sms);
        if(success){
            this.sms = sms;
            this.smsVerified = false;
        }
        return success;
    }

    public boolean verifyEmail(String code){
        boolean success = updateValue(METHOD_VERIFY_EMAIL, code);
        if(success){
            this.emailVerified = true;
        }
        return success;
    }

    public boolean verifySms(String code){
        boolean success = updateValue(METHOD_VERIFY_SMS, code);
        if(success){
            this.smsVerified = true;
        }
        return success;
    }

    public boolean setPasswordHint1(String hint)  {
        if(isBadPasswordHint(hint)){
            return false;
        }else{
            boolean success = updateValue(METHOD_UPDATE_PASSWORD_HINT_1, hint);
            if(success){
                this.passwordHint1 = hint;
            }
            return success;
        }
    }

    public boolean setPasswordHint2(String hint) throws Exception {
        if(isBadPasswordHint(hint)){
            return false;
        }else{
            boolean success = updateValue(METHOD_UPDATE_PASSWORD_HINT_2, hint);
            if(success){
                this.passwordHint2 = hint;
            }
            return success;
        }
    }

    private boolean isBadPasswordHint(String hint)  {
        if(hint == null || hint.isEmpty() || hint.length() > 255){
            return true;
        }else{
            return false;
        }
    }

    public boolean setBtcCurrency(String btcCurrency){
        boolean success = updateValue(METHOD_UPDATE_BTC_CURRENCY, btcCurrency);
        if(success){
            this.btcCurrency = btcCurrency;
        }
        return success;
    }

    public boolean setFiatCurrency(String currency){
        boolean success = updateValue(METHOD_UPDATE_CURRENCY, currency);
        if(success){
            this.currency = currency;
        }
        return success;
    }

    public boolean setTorBlocked(boolean block){
        int value = 0;
        if(block)value = 1;
        boolean success = updateValue(METHOD_UPDATE_BLOCK_TOR_IPS, value+"");
        if(success){
            this.blockTorIps = block;
        }
        return success;
    }

    public boolean isTorBlocked() {
        return blockTorIps;
    }
}
