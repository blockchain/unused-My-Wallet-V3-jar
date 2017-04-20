package info.blockchain.wallet.api.trade.coinify;

import static org.junit.Assert.assertEquals;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.api.trade.coinify.data.CoinifyTrade;
import info.blockchain.wallet.api.trade.coinify.data.CoinifyTransferIn;
import info.blockchain.wallet.api.trade.coinify.data.CoinifyTransferOut;
import io.reactivex.observers.TestObserver;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;

public class CoinifyApiTest extends MockedResponseTest {

    private CoinifyApi subject = new CoinifyApi();

    @Test
    public void getTradeInformation() throws Exception {

        URI uri = getClass().getClassLoader().getResource("trade/coinify_trade_1.txt").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(response);
        final TestObserver<CoinifyTrade> testObserver = subject.getTradeInformation("", 113475347L).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        CoinifyTrade trade1 = testObserver.values().get(0);

        assertEquals(113475347, trade1.getId());
        assertEquals(754035, trade1.getTraderId());
        assertEquals("awaiting_transfer_in", trade1.getState());
        assertEquals("USD", trade1.getInCurrency());
        assertEquals("BTC", trade1.getOutCurrency());
        assertEquals(1000.00, trade1.getInAmount(), 0);
        assertEquals(2.41526674, trade1.getOutAmountExpected(), 0);
        assertEquals("2016-04-01T12:27:36Z", trade1.getUpdateTime());
        assertEquals("2016-04-01T12:23:19Z", trade1.getCreateTime());

        CoinifyTransferIn transferIn = trade1
            .getTransferIn();
        assertEquals(4433662222L, transferIn.getId());
        assertEquals("USD", transferIn.getCurrency());
        assertEquals(1000.00, transferIn.getSendAmount(), 0);
        assertEquals(1000.00, transferIn.getReceiveAmount(), 0);
        assertEquals("card", transferIn.getMedium());
        assertEquals("d3aab081-7c5b-4ddb-b28b-c82cc8642a18"
            , transferIn.getDetails().getPaymentId());
        assertEquals("https://provider.com/payment/d3aab081-7c5b-4ddb-b28b-c82cc8642a18"
            ,transferIn.getDetails().getRedirectUrl());

        CoinifyTransferOut transferOut = trade1.getTransferOut();
        assertEquals(4433662233L, transferOut.getId());
        assertEquals("BTC", transferOut.getCurrency());
        assertEquals("blockchain", transferOut.getMedium());
        assertEquals(2.41526674, transferOut.getSendAmount(), 0);
        assertEquals(2.41526674, transferOut.getReceiveAmount(), 0);
        assertEquals("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", transferOut.getDetails().getAccount());
    }

    @Test
    public void getTrades() throws Exception {

        URI uri = getClass().getClassLoader().getResource("trade/coinify_trade_list_1.txt").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(response);
        final TestObserver<List<CoinifyTrade>> testObserver = subject.getTrades("").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        CoinifyTrade trade1 = testObserver.values().get(0).get(0);

        assertEquals(113475347, trade1.getId());
        assertEquals(754035, trade1.getTraderId());
        assertEquals("awaiting_transfer_in", trade1.getState());
        assertEquals("USD", trade1.getInCurrency());
        assertEquals("BTC", trade1.getOutCurrency());
        assertEquals(1000.00, trade1.getInAmount(), 0);
        assertEquals(2.41526674, trade1.getOutAmountExpected(), 0);
        assertEquals("2016-04-01T12:27:36Z", trade1.getUpdateTime());
        assertEquals("2016-04-01T12:23:19Z", trade1.getCreateTime());

        CoinifyTransferIn transferIn = trade1
            .getTransferIn();
        assertEquals(4433662222L, transferIn.getId());
        assertEquals("USD", transferIn.getCurrency());
        assertEquals(1000.00, transferIn.getSendAmount(), 0);
        assertEquals(1000.00, transferIn.getReceiveAmount(), 0);
        assertEquals("card", transferIn.getMedium());
        assertEquals("d3aab081-7c5b-4ddb-b28b-c82cc8642a18"
            , transferIn.getDetails().getPaymentId());
        assertEquals("https://provider.com/payment/d3aab081-7c5b-4ddb-b28b-c82cc8642a18"
            ,transferIn.getDetails().getRedirectUrl());

        CoinifyTransferOut transferOut = trade1.getTransferOut();
        assertEquals(4433662233L, transferOut.getId());
        assertEquals("BTC", transferOut.getCurrency());
        assertEquals("blockchain", transferOut.getMedium());
        assertEquals(2.41526674, transferOut.getSendAmount(), 0);
        assertEquals(2.41526674, transferOut.getReceiveAmount(), 0);
        assertEquals("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", transferOut.getDetails().getAccount());
    }
}