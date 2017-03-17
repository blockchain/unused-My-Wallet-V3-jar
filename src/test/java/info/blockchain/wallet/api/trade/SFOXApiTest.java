package info.blockchain.wallet.api.trade;

import static org.junit.Assert.*;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.api.WalletApi;
import io.reactivex.observers.TestObserver;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;

public class SFOXApiTest extends MockedResponseTest {

    private SFOXApi subject = new SFOXApi();

    @Test
    public void getTransactions1() throws Exception {

        URI uri = getClass().getClassLoader().getResource("trade/sfox_1.txt").toURI();
        String sfoxResponse1 = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(sfoxResponse1);
        final TestObserver<SFOXResponse> testObserver = subject.getTransactions("").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEquals("buy", testObserver.values().get(0).getAction());
        assertEquals("100", testObserver.values().get(0).getAmount());
        assertEquals("usd", testObserver.values().get(0).getAmountCurrency());
        assertEquals("pending", testObserver.values().get(0).getStatus());
        assertEquals("579afee4-5b5f-11e6-8e25-14109fd9ceb9", testObserver.values().get(0).getTransactionId());
    }

    @Test
    public void getTransactions2() throws Exception {

        URI uri = getClass().getClassLoader().getResource("trade/sfox_2.txt").toURI();
        String sfoxResponse1 = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(sfoxResponse1);
        final TestObserver<SFOXResponse> testObserver = subject.getTransactions("").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEquals("buy", testObserver.values().get(0).getAction());
        assertEquals("100", testObserver.values().get(0).getAmount());
        assertEquals("usd", testObserver.values().get(0).getAmountCurrency());
        assertEquals("completed", testObserver.values().get(0).getStatus());
        assertEquals("579afee4-5b5f-11e6-8e25-14109fd9ceb9", testObserver.values().get(0).getTransactionId());
        assertEquals("0.16666667", testObserver.values().get(0).getBaseAmount());
        assertEquals("btc", testObserver.values().get(0).getBaseCurrency());
        assertEquals("1", testObserver.values().get(0).getFeeAmount());
        assertEquals("usd", testObserver.values().get(0).getFeeCurrency());
        assertEquals("100", testObserver.values().get(0).getQuoteAmount());
        assertEquals("usd", testObserver.values().get(0).getQuoteCurrency());
        assertEquals("07c89dda-5b60-11e6-87d5-14109fd9ceb9", testObserver.values().get(0).getQuoteId());
    }
}