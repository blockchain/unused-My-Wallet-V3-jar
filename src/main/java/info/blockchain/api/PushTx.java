package info.blockchain.api;

import info.blockchain.wallet.send.SendCoins;
import info.blockchain.wallet.util.WebUtil;
import org.bitcoinj.core.Transaction;

public class PushTx {

    public String submitTransaction(String hexString) throws Exception {

        hexString += "&api_code="+WebUtil.API_CODE;

        String response = WebUtil.getInstance().postURL(WebUtil.SPEND_URL, "tx=" + hexString);

        return response;
    }

    public String submitTransaction(Transaction tx) throws Exception{

        String encoded = SendCoins.getInstance().encodeHex(tx);
        encoded += "&api_code="+WebUtil.API_CODE;

        String response = WebUtil.getInstance().postURL(WebUtil.SPEND_URL, "tx=" + encoded);

        return response;
    }
}
