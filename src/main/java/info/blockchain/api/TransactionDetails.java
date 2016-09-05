package info.blockchain.api;

import info.blockchain.wallet.util.FormatsUtil;
import info.blockchain.wallet.util.WebUtil;
import org.json.JSONObject;

public class TransactionDetails {

    private static final String PROTOCOL = "https://";
    private static final String SERVER_ADDRESS = "blockchain.info/";
    public static final String PROD_TRANSACTION_URL = PROTOCOL + SERVER_ADDRESS+ "tx/";

    public info.blockchain.wallet.payload.Transaction getTransactionDetails(String hash) throws Exception {

        String response = WebUtil.getInstance().getURL(PROD_TRANSACTION_URL + hash + "?format=json");

        if(response != null && FormatsUtil.getInstance().isValidJson(response)) {
            return new info.blockchain.wallet.payload.Transaction(new JSONObject(response));
        }else{
            return null;
        }
    }
}
