package info.blockchain.api;

import info.blockchain.wallet.send.SendCoins;
import info.blockchain.wallet.util.WebUtil;

import org.bitcoinj.core.Transaction;

public class PushTx extends BaseApi {

    private static final String PROD_SPEND_URL = "pushtx";

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getCurrentBaseServerUrl() + PROD_SPEND_URL;
    }

    public String submitTransaction(String hexString) throws Exception {

        hexString += getApiCode();

        return WebUtil.getInstance().postURL(
                getRoute(),
                "tx=" + hexString);
    }

    public String submitTransaction(Transaction tx) throws Exception {

        String encoded = SendCoins.getInstance().encodeHex(tx);
        encoded += getApiCode();

        return WebUtil.getInstance().postURL(
                getRoute(),
                "tx=" + encoded);
    }
}
