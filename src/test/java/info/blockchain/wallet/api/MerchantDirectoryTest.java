package info.blockchain.wallet.api;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.api.data.Merchant;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import io.reactivex.observers.TestObserver;

public class MerchantDirectoryTest extends MockedResponseTest {

    @Test
    public void getAllMerchants() throws Exception {

        URI uri = getClass().getClassLoader().getResource("merchant-directory.txt").toURI();
        String merchants = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        mockInterceptor.setResponseString(merchants);
        final TestObserver<List<Merchant>> testObserver = new WalletApi().getAllMerchants().test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        List<Merchant> merchantList = testObserver.values().get(0);
        Assert.assertEquals(585, merchantList.size());

        Merchant merchant = merchantList.get(0);
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
        Assert.assertEquals(Merchant.HEADING_CAFE, merchant.getCategoryId());
        Assert.assertFalse(merchant.isFeaturedMerchant());
    }
}