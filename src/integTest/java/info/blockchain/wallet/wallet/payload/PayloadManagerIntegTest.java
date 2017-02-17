package info.blockchain.wallet.wallet.payload;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.MultiAddress;
import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.multiaddress.MultiAddressFactory;
import info.blockchain.wallet.payload.PayloadManager;
import info.blockchain.wallet.payload.data.HDWallet;
import info.blockchain.wallet.payload.data.LegacyAddress;
import info.blockchain.wallet.payload.data.Wallet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Call;

public class PayloadManagerIntegTest extends BaseIntegTest{

    @Test
    public void upgradeV2PayloadToV3() throws Exception {

        //Create a wallet
        PayloadManager.getInstance().setTempPassword("MyTestWallet");
        PayloadManager.getInstance().create("My HDWallet", "name@email.com");

        Wallet walletBody = PayloadManager.getInstance().getPayload();

        //Remove HD part
        walletBody.setHdWallets(new ArrayList<HDWallet>());

        //Add legacy so we have at least 1 address
        LegacyAddress newlyAdded = walletBody.addLegacyAddress("HDAddress label", null);

        final String guidOriginal = walletBody.getGuid();

        walletBody.upgradeV2PayloadToV3(null, "HDAccount Name2");

        //Check that existing legacy addresses still exist
        Assert.assertEquals(newlyAdded.getAddressString(), walletBody.getLegacyAddressList().get(0).getAddressString());

        //Check that Guid is still same
        Assert.assertEquals(walletBody.getGuid(), guidOriginal);

        //Check that wallet is flagged as upgraded
        Assert.assertTrue(walletBody.isUpgraded());

        //Check that 1 account exists with keys
        String xpriv = walletBody.getHdWallet().getAccounts().get(0).getXpriv();
        Assert.assertTrue(xpriv != null && !xpriv.isEmpty());

        //Check that mnemonic exists
        try {
            Assert.assertEquals(walletBody.getMnemonic(null).size(), 12);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("upgradeV2PayloadToV3 failed");
        }
    }

    @Test
    public void te() throws IOException {
        Call<MultiAddress> call = MultiAddressFactory
            .getMultiAddress(Arrays.asList("xpub6CFgfYG9chNp7rzZ7ByXyAJruku5JSVhtGmGqR9tmeLRwu3jtioyBZpXC6GAnpMQPBQg5rviqTwMN4EwgMCZNVT3N22sSnM1yEfBQzjHXJt"),
                null,
                BlockExplorer.TX_FILTER_ALL,
                1, 0);

        System.out.println(call.execute().body().toJson());
    }
}
