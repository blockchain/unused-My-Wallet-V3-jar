package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

public class ExchangeTicker extends BaseApi {

    private static final String TICKER = "ticker?_=";

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getDefaultBaseServerUrl() + TICKER;
    }

    public String getExchangeRate() throws Exception {

        String response = WebUtil.getInstance().getURL(getRoute());
        if (response == null) {
            throw new Exception("Failed to get exchange rate");
        }

        return response;
    }

    /**
     * Returns the historic value of a number of Satoshi at a given time in a given currency
     *
     * @param satoshis     The amount of Satoshi to be converted
     * @param currency     The currency to be converted to as a 3 letter acronym, eg USD, GBP
     * @param timeInMillis The time at which to get the price, in milliseconds since epoch
     * @return A String with the value, eg "605"
     * @throws Exception Will be thrown if response is null or if there is an issue connecting
     */
    public String getHistoricPrice(long satoshis, String currency, long timeInMillis) throws Exception {
        String response = WebUtil.getInstance().getURL(
                PersistentUrls.getInstance().getDefaultBaseServerUrl()
                        + "frombtc?value="
                        + satoshis
                        + "&currency="
                        + currency
                        + "&time="
                        + timeInMillis);

        if (response == null) {
            throw new Exception("Failed to get historic exchange rate");
        }

        return response;
    }
}
