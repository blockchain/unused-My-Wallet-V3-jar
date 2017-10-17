package info.blockchain.wallet.api;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.api.data.WalletOptions;
import info.blockchain.wallet.payload.data.WalletBase;

import io.reactivex.Observable;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.reactivex.observers.TestObserver;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WalletApiTest extends MockedResponseTest {

    private WalletApi subject = new WalletApi();

    @Test
    public void getRandomBytes() throws Exception {
        mockInterceptor.setResponseString("2191f7564486869d6b08c56f496008d7c9741cf7111ed23d4ca4178a35446827");
        final TestObserver<ResponseBody> testObserver = subject.getRandomBytes().test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEquals("2191f7564486869d6b08c56f496008d7c9741cf7111ed23d4ca4178a35446827",
                testObserver.values().get(0).string());
    }

    @Test
    public void getEncryptedPayload() throws IOException, URISyntaxException {
        URI uri = getClass().getClassLoader().getResource("encrypted-payload.txt").toURI();
        String encryptedPayload = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(encryptedPayload);
        final TestObserver<Response<ResponseBody>> testObserver =
                subject.fetchEncryptedPayload("a09910d9-1906-4ea1-a956-2508c3fe0661", "").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        WalletBase walletBaseBody = WalletBase.fromJson(testObserver.values().get(0).body().string());
        assertEquals("a09910d9-1906-4ea1-a956-2508c3fe0661", walletBaseBody.getGuid());
    }

    @Test
    public void getEncryptedPayload_invalid_guid() throws IOException, URISyntaxException {
        mockInterceptor.setResponseCode(500);
        mockInterceptor.setResponseString("{\"initial_error\":\"Unknown HDWallet Identifier. Please check you entered it correctly.\",\"extra_seed\":\"4dc0bb48895c28a0bd715a3ae1490701811e9f480c0201b087fe4f07ec6a9cde817d96789c3af69112595de7f07b4f2b50b9a36b39f9874bdc7c21abf1093cd8\",\"symbol_local\":{\"symbol\":\"$\",\"code\":\"USD\",\"symbolAppearsAfter\":false,\"name\":\"U.S. dollar\",\"local\":true,\"conversion\":96245.46443249},\"war_checksum\":\"d3e3b31c57f823ed\",\"language\":\"en\",\"symbol_btc\":{\"symbol\":\"BTC\",\"code\":\"BTC\",\"symbolAppearsAfter\":true,\"name\":\"Bitcoin\",\"local\":false,\"conversion\":100000000.00000000}}");
        final TestObserver<Response<ResponseBody>> testObserver =
                subject.fetchEncryptedPayload("a09910d9-1906-4ea1-a956-2508c3fe0661", "").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEquals(500, testObserver.values().get(0).code());
        assertTrue(testObserver.values().get(0).message().contains("initial_error"));
    }

    @Test
    public void getPairingEncryptionPassword() throws IOException, URISyntaxException {
        mockInterceptor.setResponseString("5001071ac0ea0b6993444716729429c1d7637def2bcc73a6ad6360c9cec06d47");
        Call<ResponseBody> call = subject.fetchPairingEncryptionPasswordCall("a09910d9-1906-4ea1-a956-2508c3fe0661");

        Response<ResponseBody> exe = call.execute();
        assertEquals("5001071ac0ea0b6993444716729429c1d7637def2bcc73a6ad6360c9cec06d47", exe.body().string());
    }

    @Test
    public void getMobileNotice() throws IOException, URISyntaxException {
        mockInterceptor.setResponseString("{\n"
            + "  \"androidBuyPercent\": 1.00,\n"
            + "  \"android\": {\n"
            + "    \"showUnocoin\": false\n"
            + "  },\n"
            + "  \"mobile_notice\": {\n"
            + "   \"en-EN\": \"Text warning 1 EN.\",\n"
            + "   \"en-CA\": \"Text warning 2 CA.\"\n"
            + "\t}\n"
            + "}");
        final TestObserver<WalletOptions> testObserver = subject.getWalletOptions().test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEquals("Text warning 1 EN.",
            testObserver.values().get(0).getMobileNotice().get("en-EN"));
        assertEquals("Text warning 2 CA.",
            testObserver.values().get(0).getMobileNotice().get("en-CA"));
    }

    @Test
    public void getMobileNotice_null() throws IOException, URISyntaxException {
        mockInterceptor.setResponseString("{\n"
            + "\t\"androidBuyPercent\": 1.00,\n"
            + "\t\"android\": {\n"
            + "\t\t\"showUnocoin\": false\n"
            + "\t},\n"
            + "\t\"mobile_notice\": null\n"
            + "}");
        final TestObserver<WalletOptions> testObserver = subject.getWalletOptions().test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertNull(testObserver.values().get(0).getMobileNotice());
    }
}