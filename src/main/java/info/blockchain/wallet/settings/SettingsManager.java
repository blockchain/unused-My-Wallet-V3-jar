package info.blockchain.wallet.settings;

import info.blockchain.wallet.api.WalletApi;
import info.blockchain.wallet.api.data.Settings;

import io.reactivex.Observable;
import okhttp3.ResponseBody;

@SuppressWarnings("WeakerAccess")
public class SettingsManager {

    //API methods
    public static final String METHOD_GET_INFO = "get-info";
    public static final String METHOD_VERIFY_EMAIL = "verify-email";
    public static final String METHOD_VERIFY_SMS = "verify-sms";
    public static final String METHOD_UPDATE_NOTIFICATION_TYPE = "update-notifications-type";
    public static final String METHOD_UPDATE_NOTIFICATION_ON = "update-notifications-on";
    public static final String METHOD_UPDATE_SMS = "update-sms";
    public static final String METHOD_UPDATE_EMAIL = "update-email";
    public static final String METHOD_UPDATE_BTC_CURRENCY = "update-btc-currency";
    public static final String METHOD_UPDATE_CURRENCY = "update-currency";
    public static final String METHOD_UPDATE_PASSWORD_HINT_1 = "update-password-hint1";
    public static final String METHOD_UPDATE_AUTH_TYPE = "update-auth-type";
    public static final String METHOD_UPDATE_BLOCK_TOR_IPS = "update-block-tor-ips";

    //Unused API methods
    /*
    public static final String METHOD_TOGGLE_SAVE_2FA = "update-never-save-auth-type";
    public static final String METHOD_TOGGLE_SAVE_2FA = "update-never-save-auth-type";
    public static final String METHOD_UPDATE_PASSWORD_HINT_2 = "update-password-hint2";
    public static final String METHOD_UPDATE_IP_LOCK = "update-ip-lock";
    public static final String METHOD_UPDATE_IP_LOCK_ON = "update-ip-lock-on";
    public static final String METHOD_UPDATE_LANGUAGE = "update-language";
    public static final String METHOD_UPDATE_LOGGING_LEVEL = "update-logging-level";
    */

    // Notification Settings
    public static final int NOTIFICATION_ON = 2;
    public static final int NOTIFICATION_OFF = 0;

    public static final int NOTIFICATION_TYPE_NONE = 0;
    public static final int NOTIFICATION_TYPE_EMAIL = 1;
    public static final int NOTIFICATION_TYPE_SMS = 32;
    public static final int NOTIFICATION_TYPE_ALL = 33;

    // Auth Settings
    public static final int AUTH_TYPE_OFF = 0;
    public static final int AUTH_TYPE_YUBI_KEY = 1;
    public static final int AUTH_TYPE_EMAIL = 2;
    public static final int AUTH_TYPE_GOOGLE_AUTHENTICATOR = 4;
    public static final int AUTH_TYPE_SMS = 5;

    private String guid;
    private String sharedKey;

    public SettingsManager() {
        // No-op
    }

    public SettingsManager(String guid, String sharedKey) {
        this.guid = guid;
        this.sharedKey = sharedKey;
    }

    public void initSettings(String guid, String sharedKey) {
        this.guid = guid;
        this.sharedKey = sharedKey;
    }

    public Observable<Settings> getInfo()  {
        return WalletApi.fetchSettings(METHOD_GET_INFO, guid, sharedKey);
    }

    public Observable<ResponseBody> updateSetting(String method, String payload) {
        return WalletApi.updateSettings(method, guid, sharedKey, payload);
    }

    public Observable<ResponseBody> updateSetting(String method, int payload) {
        return WalletApi.updateSettings(method, guid, sharedKey, payload+"");
    }
}