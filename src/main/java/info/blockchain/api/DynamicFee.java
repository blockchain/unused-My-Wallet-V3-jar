package info.blockchain.api;

import info.blockchain.util.FeeUtil;
import info.blockchain.wallet.payment.data.SuggestedFee;
import info.blockchain.wallet.util.WebUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

public class DynamicFee {

    private static final String PROTOCOL = "https://";
    private static final String SERVER_ADDRESS = "blockchain.info/";

    private static final String API_SUBDOMAIN = "api.";
    private static final String DEV_SUBDOMAIN = "dev.";
    private static final String STAGING_SUBDOMAIN = "staging.";

    private static final String API_DEV_SUBDOMAIN = API_SUBDOMAIN +DEV_SUBDOMAIN;
    private static final String API_STAGING_SUBDOMAIN = API_SUBDOMAIN +STAGING_SUBDOMAIN;

    private static final String FEES = "fees";
    public static final String PROD_DYNAMIC_FEE = PROTOCOL + API_SUBDOMAIN + SERVER_ADDRESS + FEES;
    public static final String DEV_DYNAMIC_FEE = PROTOCOL + API_DEV_SUBDOMAIN + SERVER_ADDRESS + FEES;
    public static final String STAGING_DYNAMIC_FEE = PROTOCOL + API_STAGING_SUBDOMAIN + SERVER_ADDRESS + FEES;

    private String dynamicFeeUrl = PROD_DYNAMIC_FEE;

    public DynamicFee() {
    }

    /**
     * @param customUrl PROD_DYNAMIC_FEE, DEV_DYNAMIC_FEE, STAGING_DYNAMIC_FEE
     */
    public DynamicFee(String customUrl) {
        this.dynamicFeeUrl = customUrl;
    }

    public SuggestedFee getDynamicFee() {

        String response = null;
        try {
            response = WebUtil.getInstance().getURL(dynamicFeeUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultFee();
        }

        if(response != null) {

            JSONObject dynamicFeeJson = new JSONObject(response);
            if(dynamicFeeJson != null){

                SuggestedFee suggestedFee = new SuggestedFee();
                JSONObject defaultJson = dynamicFeeJson.getJSONObject("default");
                suggestedFee.defaultFeePerKb = BigInteger.valueOf(defaultJson.getLong("fee"));
                suggestedFee.isSurge = defaultJson.getBoolean("surge");

                JSONArray estimateArray = dynamicFeeJson.getJSONArray("estimate");
                suggestedFee.estimateList = new ArrayList<SuggestedFee.Estimates>();
                for(int i = 0; i < estimateArray.length(); i++){

                    JSONObject estimateJson = estimateArray.getJSONObject(i);

                    BigInteger fee = BigInteger.valueOf(estimateJson.getLong("fee"));
                    boolean surge = estimateJson.getBoolean("surge");
                    boolean ok = estimateJson.getBoolean("ok");

                    suggestedFee.estimateList.add(new SuggestedFee.Estimates(fee, surge, ok));
                }
                return suggestedFee;
            }

        }

        return getDefaultFee();
    }

    public SuggestedFee getDefaultFee(){
        SuggestedFee defaultFee = new SuggestedFee();
        defaultFee.defaultFeePerKb = FeeUtil.AVERAGE_FEE_PER_KB;
        return defaultFee;
    }
}
