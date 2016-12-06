package info.blockchain.api;

import info.blockchain.wallet.util.FormatsUtil;
import info.blockchain.wallet.util.WebUtil;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MerchantDirectory extends BaseApi {

    private static final String MERCHANT = "merchant";

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getCurrentBaseApiUrl() + MERCHANT;
    }

    public ArrayList<Merchant> getAllMerchants() throws Exception {

        String result = WebUtil.getInstance().getURL(getRoute());

        if (result == null || !FormatsUtil.getInstance().isValidJson(result)) {
            throw new Exception("Merchant api call returned null or empty");
        } else {

            return parse(result);
        }
    }

    public ArrayList<Merchant> parse(String data) throws JSONException{

        ArrayList<Merchant> merchantList = new ArrayList<Merchant>();

        JSONArray jsonArray = new JSONArray(data);

        if (jsonArray.length() > 0) {

            for (int i = 0; i < jsonArray.length(); i++) {

                Merchant merchant = new Merchant();
                JSONObject jsonObj = jsonArray.getJSONObject(i);

                if (jsonObj.has("id")) {
                    merchant.id = jsonObj.optInt("id", 0);
                }
                if (jsonObj.has("name")) {
                    merchant.name = jsonObj.optString("name", "");
                }
                if (jsonObj.has("address")) {
                    merchant.address = jsonObj.optString("address", "");
                }
                if (jsonObj.has("city")) {
                    merchant.city = jsonObj.optString("city", "");
                }
                if (jsonObj.has("postal_code")) {
                    merchant.postal_code = jsonObj.optString("postal_code", "");
                }
                if (jsonObj.has("phone")) {
                    merchant.phone = jsonObj.optString("phone", "");
                }
                if (jsonObj.has("website")) {
                    merchant.website = jsonObj.optString("website", "");
                }
                if (jsonObj.has("latitude")) {
                    merchant.latitude = jsonObj.optDouble("latitude", 0.0);
                }
                if (jsonObj.has("longitude")) {
                    merchant.longitude = jsonObj.optDouble("longitude", 0.0);
                }
                if (jsonObj.has("featured_merchant")) {
                    merchant.featured_merchant = jsonObj.optBoolean("featured_merchant", false);
                }
                if (jsonObj.has("description")) {
                    merchant.description = jsonObj.optString("description", "");
                }
                if (jsonObj.has("category_id")) {
                    merchant.category_id = jsonObj.optInt("category_id", Merchant.HEADING_CAFE);
                }

                merchantList.add(merchant);
            }
        }

        return merchantList;
    }

    public static class Merchant {

        public static final int HEADING_CAFE = 1;
        public static final int HEADING_BAR = 2;
        public static final int HEADING_RESTAURANT = 3;
        public static final int HEADING_SPEND = 4;
        public static final int HEADING_ATM = 5;

        public int id;
        public String name;
        public String address;
        public String city;
        public String postal_code;
        public String phone;
        public String website;
        public double latitude;
        public double longitude;
        public boolean featured_merchant;
        public String description;
        public int category_id;

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
