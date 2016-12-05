package info.blockchain.api;

import info.blockchain.wallet.transaction.Transaction;
import info.blockchain.wallet.util.FormatsUtil;
import info.blockchain.wallet.util.WebUtil;

import org.json.JSONObject;

public class TransactionDetails extends BaseApi {

    private static final String TX = "tx/";

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getCurrentBaseServerUrl() + TX;
    }

    public Transaction getTransactionDetails(String hash) throws Exception {

        String response = WebUtil.getInstance().getURL(
                getRoute() + hash + "?format=json");

        if (response != null && FormatsUtil.getInstance().isValidJson(response)) {
            return new Transaction(new JSONObject(response));
        } else {
            return null;
        }
    }
}
