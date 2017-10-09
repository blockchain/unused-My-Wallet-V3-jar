package info.blockchain.wallet.shapeshift;

import com.fasterxml.jackson.core.JsonProcessingException;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.shapeshift.data.MarketInfo;
import info.blockchain.wallet.shapeshift.data.ShapeShiftQuoteRequest;
import info.blockchain.wallet.shapeshift.data.ShapeShiftQuoteResponse;
import info.blockchain.wallet.shapeshift.data.ShapeShiftQuoteResponseWrapper;
import info.blockchain.wallet.shapeshift.data.ShapeShiftSendAmountRequest;
import info.blockchain.wallet.shapeshift.data.ShapeShiftSendAmountResponse;
import info.blockchain.wallet.shapeshift.data.ShapeShiftSendAmountResponseWrapper;
import info.blockchain.wallet.shapeshift.data.ShapeShiftTradeStatusResponse;
import io.reactivex.Observable;

public class ShapeShiftApi {

    private static ShapeShiftEndpoints shift;

    private ShapeShiftEndpoints getApiInstance() {
        if (shift == null) {
            shift = BlockchainFramework.getRetrofitApiInstance().
                create(ShapeShiftEndpoints.class);
        }
        return shift;
    }

    public Observable<MarketInfo> getMarketInfo(String pair) {
        return getApiInstance().getMarketInfo(pair);
    }

    public Observable<ShapeShiftSendAmountResponseWrapper> getSendAmount(ShapeShiftSendAmountRequest request) {
        return getApiInstance().getSendAmount(request);
    }

    public Observable<ShapeShiftQuoteResponseWrapper> getQuote(ShapeShiftQuoteRequest request) {
        return getApiInstance().getQuote(request);
    }

    public Observable<ShapeShiftTradeStatusResponse> getTradeStatus(String address) {
        return getApiInstance().getTradeStatus(address);
    }
}
