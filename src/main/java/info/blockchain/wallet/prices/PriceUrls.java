package info.blockchain.wallet.prices;

public final class PriceUrls {

    public PriceUrls() {
        throw new UnsupportedOperationException("You can't implement this class");
    }

    /* Base endpoint for all price operations */
    private static final String PRICE = "price";

    /* Additional paths */
    private static final String INDEX_SERIES = "/index-series";
    private static final String INDEX = "/index";

    /* Complete paths */
    static final String PRICE_SERIES = PRICE + INDEX_SERIES;
    static final String SINGLE_PRICE = PRICE + INDEX;
}
