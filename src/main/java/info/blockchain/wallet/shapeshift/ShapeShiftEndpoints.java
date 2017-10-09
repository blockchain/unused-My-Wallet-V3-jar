package info.blockchain.wallet.shapeshift;

import info.blockchain.wallet.shapeshift.data.MarketInfo;
import info.blockchain.wallet.shapeshift.data.ShapeShiftQuoteRequest;
import info.blockchain.wallet.shapeshift.data.ShapeShiftQuoteResponseWrapper;
import info.blockchain.wallet.shapeshift.data.ShapeShiftSendAmountRequest;
import info.blockchain.wallet.shapeshift.data.ShapeShiftSendAmountResponseWrapper;
import info.blockchain.wallet.shapeshift.data.ShapeShiftTradeStatusResponse;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ShapeShiftEndpoints {

    @GET(ShapeShiftUrls.MARKET_INFO + "/{pair}")
    Observable<MarketInfo> getMarketInfo(@Path("pair") String pair);

    @POST(ShapeShiftUrls.SENDAMOUNT)
    Observable<ShapeShiftSendAmountResponseWrapper> getSendAmount(@Body ShapeShiftSendAmountRequest request);

    @POST(ShapeShiftUrls.SENDAMOUNT)
    Observable<ShapeShiftQuoteResponseWrapper> getQuote(@Body ShapeShiftQuoteRequest request);

    @GET(ShapeShiftUrls.TX_STATS + "/{address}")
    Observable<ShapeShiftTradeStatusResponse> getTradeStatus(@Path("address") String address);
}
