package info.blockchain.wallet.shapeshift;

import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.shapeshift.data.MarketInfo;

import org.junit.Test;

import io.reactivex.observers.TestObserver;

public class ShapeShiftIntegrationTest extends BaseIntegTest {

    private ShapeShiftApi subject = new ShapeShiftApi();

    @Test
    public void getRate() throws Exception {
        // Arrange

        // Act
        final TestObserver<MarketInfo> testObserver = subject.getRate(ShapeShiftPairs.BTC_ETH).test();
        // Assert
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void getQuote() throws Exception {
        // TODO: 25/10/2017
    }

    @Test
    public void getApproximateQuote() throws Exception {
        // TODO: 25/10/2017
    }

    @Test
    public void getTradeStatus() throws Exception {
        // TODO: 25/10/2017
    }

}