package info.blockchain.api;

import info.blockchain.wallet.send.SendCoins;
import info.blockchain.wallet.util.WebUtil;
import org.bitcoinj.core.Transaction;

public class PushTx {

    private static final String PROTOCOL = "https://";
    private static final String SERVER_ADDRESS = "blockchain.info/";
    public static final String PROD_SPEND_URL = PROTOCOL + SERVER_ADDRESS + "pushtx";

    public String submitTransaction(String hexString) throws Exception {

        hexString += "&api_code="+WebUtil.API_CODE;

        String response = WebUtil.getInstance().postURL(PROD_SPEND_URL, "tx=" + hexString);

        return response;
    }

    public String submitTransaction(Transaction tx) throws Exception{

        String encoded = SendCoins.getInstance().encodeHex(tx);
        encoded += "&api_code="+WebUtil.API_CODE;

        String response = WebUtil.getInstance().postURL(PROD_SPEND_URL, "tx=" + encoded);

        return response;
    }
}
