package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class Balance {

    private static final String PROTOCOL = "https://";
    private static final String DEV_SUBDOMAIN = "dev.";
    private static final String STAGING_SUBDOMAIN = "staging.";
    private static final String WALLET_DEV_SUBDOMAIN = "explorer."+DEV_SUBDOMAIN;
    private static final String WALLET_STAGING_SUBDOMAIN = "explorer."+STAGING_SUBDOMAIN;
    private static final String SERVER_ADDRESS = "blockchain.info/";
    private static final String BALANCE = "balance?active=";

    public static final String PROD_BALANCE_URL = PROTOCOL + SERVER_ADDRESS + BALANCE;
    public static final String DEV_MULTIADDR_URL = PROTOCOL + WALLET_DEV_SUBDOMAIN + SERVER_ADDRESS + BALANCE;
    public static final String STAGING_MULTIADDR_URL = PROTOCOL + WALLET_STAGING_SUBDOMAIN + SERVER_ADDRESS + BALANCE;

    public static final int TxFilterSent = 1;
    public static final int TxFilterReceived = 2;
    public static final int TxFilterMoved = 3;
    public static final int TxFilterAll = 4;
    public static final int TxFilterConfirmedOnly = 5;
    public static final int TxFilterRemoveUnspendable = 6;
    public static final int TxFilterUnconfirmedOnly = 7;

    private String balanceUrl = PROD_BALANCE_URL;

    public Balance() {
    }

    /**
     * @param customUrl PROD_BALANCE_URL, DEV_MULTIADDR_URL, STAGING_MULTIADDR_URL
     */
    public Balance(String customUrl) {
        this.balanceUrl = customUrl;
    }

    public JSONObject getBalance(String[] addresses) throws Exception{
        return getBalanceAPICall(addresses, -1);
    }

    public JSONObject getBalance(String[] addresses, int filter) throws Exception{
        return getBalanceAPICall(addresses, filter);
    }

    private JSONObject getBalanceAPICall(String[] addresses, int filter) throws Exception{

        StringBuilder url = new StringBuilder(balanceUrl);
        url.append(StringUtils.join(addresses, "|"));
        if(filter > 0)url.append("&filter="+filter);
        url.append("&api_code="+WebUtil.API_CODE);

        String response = WebUtil.getInstance().getURL(url.toString());

        return new JSONObject(response);
    }

    public int getXpubTransactionCount(String xpub) throws Exception {
        JSONObject jsonObject = getBalance(new String[]{xpub});
        return jsonObject.getJSONObject(xpub).getInt("n_tx");
    }
}
