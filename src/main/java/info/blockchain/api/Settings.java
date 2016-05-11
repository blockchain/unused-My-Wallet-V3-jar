package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import java.net.URLEncoder;

public class Settings {

    private static final String METHOD_UPDATE_SMS = "update-sms";
    private static final String METHOD_VERIFY_SMS = "verify-sms";
    private static final String METHOD_UPDATE_EMAIL = "update-email";
    private static final String METHOD_VERIFY_EMAIL = "verify-email";
    private static final String METHOD_UPDATE_BTC_CURRENCY = "update-btc-currency";//TODO - refactor MonetaryUtil in android
    private static final String METHOD_UPDATE_CURRENCY = "update-currency";//TODO - refactor ExchangeRateFactory in android

    private String guid;
    private String sharedKey;

    public Settings(String guid, String sharedKey) {
        this.guid = guid;
        this.sharedKey = sharedKey;
    }

    private String updateSettings(String method, String settingsPayload) {

        settingsPayload = settingsPayload.trim();

        StringBuilder args = new StringBuilder();
        try	{

            args.append("length="+settingsPayload.length());
            args.append("&payload="+ URLEncoder.encode(settingsPayload, "utf-8"));
            args.append("&method="+method);
            args.append("&guid="+URLEncoder.encode(this.guid, "utf-8"));
            args.append("&sharedKey="+URLEncoder.encode(this.sharedKey, "utf-8"));
            args.append("&api_code=" + WebUtil.API_CODE);
            args.append("&format=plain");

            String response = WebUtil.getInstance().postURL(WebUtil.PAYLOAD_URL, args.toString());
            return response;
        }
        catch(Exception e)	{
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public String updateSms(String mobile){
        return updateSettings(Settings.METHOD_UPDATE_SMS, mobile);
    }

    public String verifySms(String code){
        return updateSettings(Settings.METHOD_VERIFY_SMS, code);
    }

    public String updateEmail(String email){
        return updateSettings(Settings.METHOD_UPDATE_EMAIL, email);
    }

    public String verifyEmail(String code){
        return updateSettings(Settings.METHOD_VERIFY_EMAIL, code);
    }
}
