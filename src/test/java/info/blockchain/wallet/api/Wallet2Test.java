package info.blockchain.wallet.api;

import static org.junit.Assert.*;

import info.blockchain.BaseTest;
import okhttp3.ResponseBody;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Call;
import retrofit2.Response;

public class Wallet2Test extends BaseTest{

    @Test
    public void getRandomBytes() throws Exception {

        mockInterceptor.setResponseString("2191f7564486869d6b08c56f496008d7c9741cf7111ed23d4ca4178a35446827");
        Call<ResponseBody> call = Wallet2.getRandomBytes();
        Response<ResponseBody> exe = call.execute();

        Assert.assertEquals("2191f7564486869d6b08c56f496008d7c9741cf7111ed23d4ca4178a35446827",exe.body().string());
    }

}