package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class Balance implements BaseApi {

    private static final String BALANCE = "balance?active=";
    public static final String PROD_BALANCE_URL = PROTOCOL + SERVER_ADDRESS + BALANCE;

    public static final int TxFilterSent = 1;
    public static final int TxFilterReceived = 2;
    public static final int TxFilterMoved = 3;
    public static final int TxFilterAll = 4;
    public static final int TxFilterConfirmedOnly = 5;
    public static final int TxFilterRemoveUnspendable = 6;
    public static final int TxFilterUnconfirmedOnly = 7;

    private String balanceUrl = PROD_BALANCE_URL;

    public Balance() {
        balanceUrl = PersistentUrls.getInstance().getBalanceUrl();
    }

    public JSONObject getBalance(String[] addresses) throws Exception {
        return getBalanceAPICall(addresses, -1);
    }

    public JSONObject getBalance(String[] addresses, int filter) throws Exception {
        return getBalanceAPICall(addresses, filter);
    }

    private JSONObject getBalanceAPICall(String[] addresses, int filter) throws Exception {

        StringBuilder url = new StringBuilder(balanceUrl);
        url.append(StringUtils.join(addresses, "|"));
        if (filter > 0) url.append("&filter=").append(filter);
        url.append("&api_code=" + API_CODE);

        String response = WebUtil.getInstance().getURL(url.toString());

        return new JSONObject(response);
    }

    public int getXpubTransactionCount(String xpub) throws Exception {
        JSONObject jsonObject = getBalance(new String[]{xpub});
        return jsonObject.getJSONObject(xpub).getInt("n_tx");
    }
}
