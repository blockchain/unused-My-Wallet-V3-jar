package info.blockchain.api;

import info.blockchain.wallet.payload.Payload;
import org.json.JSONObject;

public class Settings {

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
    private String guid;
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

    SettingsApi settingsApi;

    public Settings(Payload payload){
        settingsApi = new SettingsApi(payload.getGuid(), payload.getSharedKey());
        String jsonString = settingsApi.getInfo();

        if(jsonString != null)
            parseJson(jsonString);
    }

    public Settings(String settingsJson){

        if(settingsJson != null)
            parseJson(settingsJson);
    }

    private void parseJson(String jsonString){

        JSONObject jsonObject = new JSONObject(jsonString);
        if(jsonObject.has("btc_currency"))btcCurrency = jsonObject.getString("btc_currency");
//        JSONArray notificationTypeJsonArray = jsonObject.getJSONArray("notifications_type");
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
        boolean success = settingsApi.updateEmail(email);
        if(success){
            this.email = email;
            this.emailVerified = false;
        }
        return success;
    }

    public boolean setSms(String sms){
        boolean success = settingsApi.updateSms(sms);
        if(success){
            this.sms = sms;
            this.smsVerified = false;
        }
        return success;
    }

    public boolean verifyEmail(String code){
        boolean success = settingsApi.verifyEmail(code);
        if(success){
            this.emailVerified = true;
        }
        return success;
    }

    public boolean verifySms(String code){
        boolean success = settingsApi.verifySms(code);
        if(success){
            this.smsVerified = true;
        }
        return success;
    }
}
