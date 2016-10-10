package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

public class PaidTo {

    private String strEmail = null;
    private String strMobile = null;
    private Integer strRedeemedAt = null;
    private String strAddress = null;

    public PaidTo() {
        ;
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
            obj.put("email", strEmail);
        } else {
            obj.put("email", JSONObject.NULL);
        }
        if ((strMobile != null) && !"".equals(strMobile)) {
            obj.put("mobile", strMobile);
        } else {
            obj.put("mobile", JSONObject.NULL);
        }
        if ((strRedeemedAt != null) && !"".equals(strRedeemedAt)) {
            obj.put("redeemedAt", strRedeemedAt);
        } else {
            obj.put("redeemedAt", JSONObject.NULL);
        }
        if ((strAddress != null) && !"".equals(strAddress)) {
            obj.put("address", strAddress);
        } else {
            obj.put("address", JSONObject.NULL);
        }
        return obj;
    }

}
