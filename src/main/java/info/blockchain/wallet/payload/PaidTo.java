package info.blockchain.wallet.payload;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class PaidTo {

    private String strEmail = null;
    private String strMobile = null;
    private String strRedeemedAt = null;
    private String strAddress = null;

    public PaidTo() { ; }

    public PaidTo(String email, String mobile, String redeemed, String address) {
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

    public String getRedeemedAt() {
        return strRedeemedAt;
    }

    public void setRedeemedAt(String strRedeemedAt) {
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

        obj.put("email", strEmail);
        obj.put("mobile", strMobile);
        obj.put("redeemedAt", strRedeemedAt);
        obj.put("address", strAddress);

        return obj;
    }

}
