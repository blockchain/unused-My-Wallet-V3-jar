package info.blockchain.wallet.api;

import info.blockchain.MockedResponseTest;
import info.blockchain.wallet.payload.data.WalletBase;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import okhttp3.ResponseBody;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

public class WalletApiTest extends MockedResponseTest {

    @Test
    public void getRandomBytes() throws Exception {
        mockInterceptor.setResponseString("2191f7564486869d6b08c56f496008d7c9741cf7111ed23d4ca4178a35446827");
        Call<ResponseBody> call = WalletApi.getRandomBytes();
        Response<ResponseBody> exe = call.execute();

        Assert.assertEquals("2191f7564486869d6b08c56f496008d7c9741cf7111ed23d4ca4178a35446827",exe.body().string());
    }

    @Test
    public void getEncryptedPayload() throws IOException, URISyntaxException {

        URI uri = getClass().getClassLoader().getResource("encrypted-payload.txt").toURI();
        String encryptedPayload = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(encryptedPayload);
        Call<ResponseBody> call = WalletApi.fetchEncryptedPayload("a09910d9-1906-4ea1-a956-2508c3fe0661");

        Response<ResponseBody> exe = call.execute();

        WalletBase walletBaseBody = WalletBase.fromJson(exe.body().string());
        Assert.assertEquals("a09910d9-1906-4ea1-a956-2508c3fe0661", walletBaseBody.getGuid());
    }

    @Test
    public void getEncryptedPayload_invalid_guid() throws IOException, URISyntaxException {

        mockInterceptor.setResponseCode(500);
        mockInterceptor.setResponseString("{\"initial_error\":\"Unknown HDWallet Identifier. Please check you entered it correctly.\",\"extra_seed\":\"4dc0bb48895c28a0bd715a3ae1490701811e9f480c0201b087fe4f07ec6a9cde817d96789c3af69112595de7f07b4f2b50b9a36b39f9874bdc7c21abf1093cd8\",\"symbol_local\":{\"symbol\":\"$\",\"code\":\"USD\",\"symbolAppearsAfter\":false,\"name\":\"U.S. dollar\",\"local\":true,\"conversion\":96245.46443249},\"war_checksum\":\"d3e3b31c57f823ed\",\"language\":\"en\",\"symbol_btc\":{\"symbol\":\"BTC\",\"code\":\"BTC\",\"symbolAppearsAfter\":true,\"name\":\"Bitcoin\",\"local\":false,\"conversion\":100000000.00000000}}");
        Call<ResponseBody> call = WalletApi.fetchEncryptedPayload("a09910d9-1906-4ea1-a956-2508c3fe0661");

        Response<ResponseBody> exe = call.execute();

        Assert.assertEquals(500, exe.code());
        Assert.assertTrue(exe.errorBody().string().contains("initial_error"));
    }

    @Test
    public void getPairingEncryptionPassword() throws IOException, URISyntaxException {

        mockInterceptor.setResponseString("5001071ac0ea0b6993444716729429c1d7637def2bcc73a6ad6360c9cec06d47");
        Call<ResponseBody> call = WalletApi.fetchPairingEncryptionPassword("a09910d9-1906-4ea1-a956-2508c3fe0661");

        Response<ResponseBody> exe = call.execute();
        Assert.assertEquals("5001071ac0ea0b6993444716729429c1d7637def2bcc73a6ad6360c9cec06d47", exe.body().string());
    }
}