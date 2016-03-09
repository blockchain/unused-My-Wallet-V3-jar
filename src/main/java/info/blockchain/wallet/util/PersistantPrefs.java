package info.blockchain.wallet.util;

public interface PersistantPrefs {

    public static final String DEFAULT_CURRENCY = "USD";

    public static final String KEY_PIN_IDENTIFIER       = "pin_kookup_key";
    public static final String KEY_ENCRYPTED_PASSWORD   = "encrypted_password";
    public static final String KEY_GUID                 = "guid";
    public static final String KEY_SHARED_KEY           = "sharedKey";
    public static final String KEY_PIN_FAILS            = "pin_fails";
    // public static final String KEY_LOGGED_IN              = "logged_in";
    public static final String KEY_BTC_UNITS            = "btcUnits";
    public static final String KEY_SELECTED_FIAT        = "ccurrency";
    public static final String KEY_INITIAL_ACCOUNT_NAME = "_1ST_ACCOUNT_NAME";
	  public static final String KEY_EMAIL           		= "email";
	  public static final String KEY_EMAIL_VERIFIED 		= "code_verified";
    public static final String KEY_SESSION_ID 			= "session_id";
    public static final String KEY_HD_UPGRADED_LAST_REMINDER = "hd_upgraded_last_reminder";
    public static final String KEY_ASK_LATER = "ask_later";
    public static final String KEY_EMAIL_VERIFY_ASK_LATER = "email_verify_ask_later";
    public static final String KEY_BALANCE_DISPLAY_STATE = "balance_display_state";
    public static final String KEY_SCHEME_URL = "scheme_url";
    public static final String KEY_CURRENT_APP_VERSION = "KEY_CURRENT_APP_VERSION";

    public abstract String getValue(String name, String value);
    public boolean setValue(String name, String value);
    public int getValue(String name, int value);
    public boolean setValue(String name, int value);
    public boolean setValue(String name, long value);
    public long getValue(String name, long value);
    public boolean getValue(String name, boolean value);
    public boolean setValue(String name, boolean value);
    public boolean has(String name);
    public boolean removeValue(String name);
    public boolean clear();

}
