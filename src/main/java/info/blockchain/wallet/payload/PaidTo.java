package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

// TODO: 20/10/16 This class doesn't seem to be used - We parse the data but never do anything with it
public class PaidTo {

    private final String KEY_EMAIL = "email";
    private final String KEY_MOBILE = "mobile";
    private final String KEY_REDEEMED_AT = "redeemedAt";
    private final String KEY_ADDRESS = "address";

    private String strEmail = null;
    private String strMobile = null;
    private Integer strRedeemedAt = null;
    private String strAddress = null;

    public PaidTo(JSONObject t) {

        setEmail(t.isNull(KEY_EMAIL) ? null : t.optString(KEY_EMAIL, null));
        setMobile(t.isNull(KEY_MOBILE) ? null : t.optString(KEY_MOBILE, null));
        setRedeemedAt(t.isNull(KEY_REDEEMED_AT) ? null : (Integer) t.get(KEY_REDEEMED_AT));
        setAddress(t.isNull(KEY_ADDRESS) ? null : t.optString(KEY_ADDRESS, null));
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
            obj.put(KEY_EMAIL, strEmail);
        } else {
            obj.put(KEY_EMAIL, JSONObject.NULL);
        }
        if ((strMobile != null) && !"".equals(strMobile)) {
            obj.put(KEY_MOBILE, strMobile);
        } else {
            obj.put(KEY_MOBILE, JSONObject.NULL);
        }
        if ((strRedeemedAt != null)) {
            obj.put(KEY_REDEEMED_AT, strRedeemedAt);
        } else {
            obj.put(KEY_REDEEMED_AT, JSONObject.NULL);
        }
        if ((strAddress != null) && !"".equals(strAddress)) {
            obj.put(KEY_ADDRESS, strAddress);
        } else {
            obj.put(KEY_ADDRESS, JSONObject.NULL);
        }
        return obj;
    }

}