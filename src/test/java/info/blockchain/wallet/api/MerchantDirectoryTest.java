package info.blockchain.wallet.api;

import info.blockchain.MockedResponseTest;
import info.blockchain.wallet.api.data.MerchantBody;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Call;

public class MerchantDirectoryTest extends MockedResponseTest {

    @Test
    public void getAllMerchants() throws Exception {

        URI uri = getClass().getClassLoader().getResource("merchant-directory.txt").toURI();
        String merchants = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(merchants);
        Call<ArrayList<MerchantBody>> call = WalletApi.getAllMerchants();

        ArrayList<MerchantBody> merchantList = call.execute().body();
        Assert.assertEquals(585, merchantList.size());

        MerchantBody merchant = merchantList.get(0);
        Assert.assertEquals(1, merchant.getId());
        Assert.assertEquals("Bubble-T Broca", merchant.getName());
        Assert.assertEquals("87 Rue Broca Paris", merchant.getAddress());
        Assert.assertEquals("Paris", merchant.getCity());
        Assert.assertEquals("75013", merchant.getPostalCode());
        Assert.assertEquals("+33 6 66 04 63 8", merchant.getPhone());
        Assert.assertEquals("www.facebook.com/BubbleTBroca", merchant.getWebsite());
        Assert.assertEquals(48.835972, merchant.getLatitude(), 0.0);
        Assert.assertEquals(2.346812, merchant.getLongitude(), 0.0);
        Assert.assertEquals("Our mission is to bring an authentic teahouse.", merchant.getDescription());
        Assert.assertTrue(merchant.isBlockchainMerchant());
        Assert.assertTrue(merchant.isApproved());
        Assert.assertEquals(MerchantBody.HEADING_CAFE, merchant.getCategoryId());
        Assert.assertFalse(merchant.isFeaturedMerchant());
    }
}