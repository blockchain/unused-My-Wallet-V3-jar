package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class Balance extends BaseApi {

    private static final String BALANCE = "balance?active=";

    public static final int TxFilterSent = 1;
    public static final int TxFilterReceived = 2;
    public static final int TxFilterMoved = 3;
    public static final int TxFilterAll = 4;
    public static final int TxFilterConfirmedOnly = 5;
    public static final int TxFilterRemoveUnspendable = 6;
    public static final int TxFilterUnconfirmedOnly = 7;

    public Balance() {
        // No-op
    }

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getCurrentBaseServerUrl() + BALANCE;
    }

    public JSONObject getBalance(List<String> addresses) throws Exception {
        return getBalanceAPICall(addresses, -1);
    }

    public JSONObject getBalance(List<String> addresses, int filter) throws Exception {
        return getBalanceAPICall(addresses, filter);
    }

    private JSONObject getBalanceAPICall(List<String> addresses, int filter) throws Exception {

        StringBuilder url = new StringBuilder(getRoute());
        url.append(StringUtils.join(addresses, "%7C"));
        if (filter > 0) url.append("&filter=").append(filter);
        url.append(getApiCode());

        String response = WebUtil.getInstance().getURL(url.toString());

        return new JSONObject(response);
    }

    public int getXpubTransactionCount(String xpub) throws Exception {
        JSONObject jsonObject = getBalance(Arrays.asList(xpub));
        return jsonObject.getJSONObject(xpub).getInt("n_tx");
    }

    public long getTotalBalance(List<String> addresses) throws Exception {

        long result = 0;

        JSONObject balanceResponse = getBalanceAPICall(addresses, -1);
        String finalBalanceKey = "final_balance";

        for (String address : addresses) {
            JSONObject addressJson = balanceResponse.getJSONObject(address);

            if (addressJson.has(finalBalanceKey)) {
                result += addressJson.getLong(finalBalanceKey);
            }
        }

        return result;
    }
}
