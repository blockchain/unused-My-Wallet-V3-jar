package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

// TODO: 20/10/16 This class doesn't seem to be used - We parse the data but never do anything with it
public class PaidTo implements PayloadJsonKeys{

    // TODO: 20/10/16 Simplify these keys again after refactor
    String KEY_PAIDTO__EMAIL = "email";
    String KEY_PAIDTO__MOBILE = "mobile";
    String KEY_PAIDTO__REDEEMED_AT = "redeemedAt";
    String KEY_PAIDTO__ADDRESS = "address";

    private String strEmail = null;
    private String strMobile = null;
    private Integer strRedeemedAt = null;
    private String strAddress = null;

    public PaidTo(JSONObject t) {

        setEmail(t.isNull(KEY_PAIDTO__EMAIL) ? null : t.optString(KEY_PAIDTO__EMAIL, null));
        setMobile(t.isNull(KEY_PAIDTO__MOBILE) ? null : t.optString(KEY_PAIDTO__MOBILE, null));
        setRedeemedAt(t.isNull(KEY_PAIDTO__REDEEMED_AT) ? null : (Integer) t.get(KEY_PAIDTO__REDEEMED_AT));
        setAddress(t.isNull(KEY_PAIDTO__ADDRESS) ? null : t.optString(KEY_PAIDTO__ADDRESS, null));
    }

    public PaidTo(String email, String mobile, Integer redeemed, String address) {
        this.strEmail = email;
        this.strMobile = mobile;
        this.strRedeemedAt = redeemed;
        this.strAddress = address;
    }

    public String getEmail() {
        return strEmail;
    }

    public void setEmail(String strEmail) {
        this.strEmail = strEmail;
    }

    public String getMobile() {
        return strMobile;
    }

    public void setMobile(String strMobile) {
        this.strMobile = strMobile;
    }

    public Integer getRedeemedAt() {
        return strRedeemedAt;
    }

    public void setRedeemedAt(Integer strRedeemedAt) {
        this.strRedeemedAt = strRedeemedAt;
    }

    public String getAddress() {
        return strAddress;
    }

    public void setAddress(String strAddress) {
        this.strAddress = strAddress;
    }

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        if ((strEmail != null) && !"".equals(strEmail)) {
            obj.put(KEY_PAIDTO__EMAIL, strEmail);
        } else {
            obj.put(KEY_PAIDTO__EMAIL, JSONObject.NULL);
        }
        if ((strMobile != null) && !"".equals(strMobile)) {
            obj.put(KEY_PAIDTO__MOBILE, strMobile);
        } else {
            obj.put(KEY_PAIDTO__MOBILE, JSONObject.NULL);
        }
        if ((strRedeemedAt != null)) {
            obj.put(KEY_PAIDTO__REDEEMED_AT, strRedeemedAt);
        } else {
            obj.put(KEY_PAIDTO__REDEEMED_AT, JSONObject.NULL);
        }
        if ((strAddress != null) && !"".equals(strAddress)) {
            obj.put(KEY_PAIDTO__ADDRESS, strAddress);
        } else {
            obj.put(KEY_PAIDTO__ADDRESS, JSONObject.NULL);
        }
        return obj;
    }

}