package info.blockchain.wallet.shapeshift;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.shapeshift.data.MarketInfo;
import info.blockchain.wallet.shapeshift.data.Quote;
import info.blockchain.wallet.shapeshift.data.QuoteRequest;
import info.blockchain.wallet.shapeshift.data.QuoteResponseWrapper;
import info.blockchain.wallet.shapeshift.data.Trade;
import info.blockchain.wallet.shapeshift.data.TradeStatusResponse;

import org.junit.Test;

import java.math.BigDecimal;

import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ShapeShiftApiTest extends MockedResponseTest {

    private ShapeShiftApi subject = new ShapeShiftApi();

    @Test
    public void getMarketInfo() throws Exception {
        mockInterceptor
            .setResponseString("{\"pair\":\"btc_eth\",\"rate\":15.06742777,\"minerFee\":0.001,\"limit\":2.17562517,\"minimum\":0.0001324,\"maxLimit\":1.08781258}");
        final TestObserver<MarketInfo> testObserver = subject.getRate(ShapeShiftPairs.BTC_ETH).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        MarketInfo response = testObserver.values().get(0);
        assertEquals(15.06742777, response.getRate(), 0);
        assertEquals(2.17562517, response.getLimit(), 0);
        assertEquals(0.0001324, response.getMinimum(), 0);
        assertEquals(1.08781258, response.getMaxLimit(), 0);
        assertEquals(0.001, response.getMinerFee(), 0);
        assertEquals(ShapeShiftPairs.BTC_ETH, response.getPair());
    }

    @Test
    public void getQuote() throws Exception {
        mockInterceptor
            .setResponseString("{\"success\":{\"orderId\":\"2b087b88-4d92-4dce-8167-2a616accfe23\","
                + "\"pair\":\"eth_btc\","
                + "\"withdrawalAmount\":\"0.11029696\","
                + "\"depositAmount\":\"1.7278182\","
                + "\"expiration\":1000,"
                + "\"quotedRate\":\"0.06441474\","
                + "\"maxLimit\":16.65419494,"
                + "\"minerFee\":\"0.001\"}}");

        QuoteRequest request = new QuoteRequest();
        request.setDepositAmount(0.1102969);
        request.setPair("eth_btc");
        final TestObserver<QuoteResponseWrapper> testObserver = subject.getApproximateQuote(request).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        Quote wrapper = testObserver.values().get(0).getWrapper();
        assertEquals("eth_btc", wrapper.getPair());
        assertEquals(BigDecimal.valueOf(0.11029696), wrapper.getWithdrawalAmount());
        assertEquals(BigDecimal.valueOf(1.7278182), wrapper.getDepositAmount());
        assertEquals(1000L, wrapper.getExpiration());
        assertEquals(BigDecimal.valueOf(0.06441474), wrapper.getQuotedRate());
        assertEquals(BigDecimal.valueOf(0.001), wrapper.getMinerFee());
    }

    @Test
    public void getSendAmount() throws Exception {
        mockInterceptor
            .setResponseString("{\n"
                + "\t\"status\": \"complete\",\n"
                + "\t\"address\": \"3PpfQbaETF1PCUh2iZKfMoyMhCmZWmVz9Z\",\n"
                + "\t\"withdraw\": \"0x9240d92140a48164ef71d9b0fade096583354e5a\",\n"
                + "\t\"incomingCoin\": 0.0001332,\n"
                + "\t\"incomingType\": \"BTC\",\n"
                + "\t\"outgoingCoin\": \"0.00099547\",\n"
                + "\t\"outgoingType\": \"ETH\",\n"
                + "\t\"transaction\": \"0xc1361e8ec096dfe48f524bd67fe811e5fd86a41c868ff5843f04619906882123\"\n"
                + "}");

        final TestObserver<TradeStatusResponse> testObserver = subject.getTradeStatus("someAddress").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        TradeStatusResponse response = testObserver.values().get(0);
        assertEquals(Trade.STATUS.COMPLETE, response.getStatus());
        assertEquals("3PpfQbaETF1PCUh2iZKfMoyMhCmZWmVz9Z", response.getAddress());
        assertEquals("0x9240d92140a48164ef71d9b0fade096583354e5a", response.getWithdraw());
        assertEquals(0.0001332, response.getIncomingCoin(), 0);
        assertEquals("BTC", response.getIncomingType());
        assertEquals(0.00099547, response.getOutgoingCoin(), 0);
        assertEquals("ETH", response.getOutgoingType());
        assertEquals("0xc1361e8ec096dfe48f524bd67fe811e5fd86a41c868ff5843f04619906882123", response.getTransaction());
        assertNull(response.getError());
    }
}