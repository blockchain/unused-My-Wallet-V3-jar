package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import java.net.URLEncoder;

class SettingsApi {

    private static final String METHOD_GET_INFO = "get-info";
    private static final String METHOD_UPDATE_SMS = "update-sms";
    private static final String METHOD_VERIFY_SMS = "verify-sms";
    private static final String METHOD_UPDATE_EMAIL = "update-email";
    private static final String METHOD_VERIFY_EMAIL = "verify-email";
    private static final String METHOD_UPDATE_BTC_CURRENCY = "update-btc-currency";//TODO - refactor MonetaryUtil in android
    private static final String METHOD_UPDATE_CURRENCY = "update-currency";//TODO - refactor ExchangeRateFactory in android

    //Expected responses
    private static final String SUCCESS_EMAIL_VERIFIED = "Email successfully verified";
    private static final String SUCCESS_SMS_VERIFIED = "SMS number successfully verified";
    private static final String SUCCESS_EMAIL_UPDATED = "Email Updated";
    private static final String SUCCESS_SMS_UPDATED = "SMS Number Successfully Updated. Verification Message Sent.";

    private String guid;
    private String sharedKey;

    public SettingsApi(String guid, String sharedKey) {
        this.guid = guid;
        this.sharedKey = sharedKey;
    }

    private String updateSettings(String method, String settingsPayload) {

        settingsPayload = settingsPayload.trim();

        StringBuilder args = new StringBuilder();
        try	{

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
        catch(Exception e)	{
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public String getInfo(){
        return updateSettings(SettingsApi.METHOD_GET_INFO, "");
    }

    public boolean updateSms(String mobile){
        String response =  updateSettings(SettingsApi.METHOD_UPDATE_SMS, mobile);

        if(response.equals(SUCCESS_SMS_UPDATED)){
            return true;
        }else{
            return false;
        }
    }

    public boolean verifySms(String code){
        String response = updateSettings(SettingsApi.METHOD_VERIFY_SMS, code);

        if(response.equals(SUCCESS_SMS_VERIFIED)){
            return true;
        }else{
            return false;
        }
    }

    public boolean updateEmail(String email){
        String response =  updateSettings(SettingsApi.METHOD_UPDATE_EMAIL, email);

        if(response.equals(SUCCESS_EMAIL_UPDATED)){
            return true;
        }else{
            return false;
        }
    }

    public boolean verifyEmail(String code){
        String response = updateSettings(SettingsApi.METHOD_VERIFY_EMAIL, code);

        if(response.equals(SUCCESS_EMAIL_VERIFIED)){
            return true;
        }else{
            return false;
        }
    }
}
