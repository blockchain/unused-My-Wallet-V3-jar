package info.blockchain.api;

import info.blockchain.wallet.transaction.Transaction;
import info.blockchain.wallet.util.FormatsUtil;
import info.blockchain.wallet.util.WebUtil;

import org.json.JSONObject;

public class TransactionDetails implements BaseApi {

    private static final String TX = "tx/";
    public static final String PROD_TRANSACTION_URL = PROTOCOL + SERVER_ADDRESS + TX;

    private String txDetailsUrl = PROD_TRANSACTION_URL;

    public TransactionDetails() {
        txDetailsUrl = PersistentUrls.getInstance().getTransactionDetailsUrl();
    }

    public Transaction getTransactionDetails(String hash) throws Exception {

        String response = WebUtil.getInstance().getURL(txDetailsUrl + hash + "?format=json");

        if (response != null && FormatsUtil.getInstance().isValidJson(response)) {
            return new Transaction(new JSONObject(response));
        } else {
            return null;
        }
    }
}
