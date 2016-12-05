package info.blockchain.api;

import info.blockchain.util.FeeUtil;
import info.blockchain.wallet.payment.data.SuggestedFee;
import info.blockchain.wallet.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

public class DynamicFee extends BaseApi {

    private static final String FEES = "fees";

    public DynamicFee() {
        // No-op
    }

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getDefaultBaseApiUrl() + FEES;
    }

    public SuggestedFee getDynamicFee() throws Exception {

        String response = WebUtil.getInstance().getRequest(getRoute());

        if (response != null) {

            JSONObject dynamicFeeJson = new JSONObject(response);

            SuggestedFee suggestedFee = new SuggestedFee();
            JSONObject defaultJson = dynamicFeeJson.getJSONObject("default");
            suggestedFee.defaultFeePerKb = BigInteger.valueOf(defaultJson.getLong("fee"));
            suggestedFee.isSurge = defaultJson.getBoolean("surge");

            JSONArray estimateArray = dynamicFeeJson.getJSONArray("estimate");
            suggestedFee.estimateList = new ArrayList<SuggestedFee.Estimates>();
            for (int i = 0; i < estimateArray.length(); i++) {

                JSONObject estimateJson = estimateArray.getJSONObject(i);

                BigInteger fee = BigInteger.valueOf(estimateJson.getLong("fee"));
                boolean surge = estimateJson.getBoolean("surge");
                boolean ok = estimateJson.getBoolean("ok");

                suggestedFee.estimateList.add(new SuggestedFee.Estimates(fee, surge, ok));
            }
            return suggestedFee;

        }

        return getDefaultFee();
    }

    public SuggestedFee getDefaultFee() {
        SuggestedFee defaultFee = new SuggestedFee();
        defaultFee.defaultFeePerKb = FeeUtil.AVERAGE_FEE_PER_KB;
        return defaultFee;
    }
}
