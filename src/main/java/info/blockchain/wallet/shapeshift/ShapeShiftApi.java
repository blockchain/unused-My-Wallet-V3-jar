package info.blockchain.wallet.shapeshift;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.shapeshift.data.MarketInfo;
import info.blockchain.wallet.shapeshift.data.QuoteRequest;
import info.blockchain.wallet.shapeshift.data.QuoteResponseWrapper;
import info.blockchain.wallet.shapeshift.data.SendAmountResponseWrapper;
import info.blockchain.wallet.shapeshift.data.TimeRemaining;
import info.blockchain.wallet.shapeshift.data.TradeStatusResponse;

import io.reactivex.Observable;

public class ShapeShiftApi {

    private static ShapeShiftEndpoints shift;

    private ShapeShiftEndpoints getApiInstance() {
        if (shift == null) {
            shift = BlockchainFramework.getRetrofitShapeShiftInstance().
                create(ShapeShiftEndpoints.class);
        }
        return shift;
    }

    public Observable<MarketInfo> getRate(String coinPair) {
        return getApiInstance().getMarketInfo(coinPair);
    }

    public Observable<SendAmountResponseWrapper> getQuote(QuoteRequest request) {
        return getApiInstance().getQuote(request);
    }

    public Observable<QuoteResponseWrapper> getApproximateQuote(QuoteRequest request) {
        return getApiInstance().getApproximateQuote(request);
    }

    public Observable<TradeStatusResponse> getTradeStatus(String address) {
        return getApiInstance().getTradeStatus(address);
    }

    public Observable<TimeRemaining> getTimeRemaining(String address) {
        return getApiInstance().getTimeRemaining(address);
    }
}
