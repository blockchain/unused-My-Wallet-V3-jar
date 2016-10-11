package info.blockchain.api;

import info.blockchain.wallet.send.SendCoins;
import info.blockchain.wallet.util.WebUtil;

import org.bitcoinj.core.Transaction;

public class PushTx implements BaseApi {

    public static final String PROD_SPEND_URL = PROTOCOL + SERVER_ADDRESS + "pushtx";

    public String submitTransaction(String hexString) throws Exception {

        hexString += "&api_code=" + API_CODE;

        return WebUtil.getInstance().postURL(PROD_SPEND_URL, "tx=" + hexString);
    }

    public String submitTransaction(Transaction tx) throws Exception {

        String encoded = SendCoins.getInstance().encodeHex(tx);
        encoded += "&api_code=" + API_CODE;

        return WebUtil.getInstance().postURL(PROD_SPEND_URL, "tx=" + encoded);
    }
}
