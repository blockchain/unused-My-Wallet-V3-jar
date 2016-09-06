package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

public class ExchangeTicker implements BaseApi {

    public static final String PROD_EXCHANGE_URL = PROTOCOL + SERVER_ADDRESS + "ticker";

    public String getExchangeRate() throws Exception {

        String response = WebUtil.getInstance().getURL(PROD_EXCHANGE_URL);
        if (response == null) {
            throw new Exception("Failed to get exchange rate");
        }

        return response;
    }
}
