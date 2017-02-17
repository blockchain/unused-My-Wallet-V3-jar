package info.blockchain.wallet.wallet;

import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.payload.PayloadManager;
import info.blockchain.wallet.payload.data.HDWallet;
import info.blockchain.wallet.payload.data.LegacyAddress;
import info.blockchain.wallet.payload.data.Payload;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class PayloadManagerIntegTest extends BaseIntegTest{

    private Payload getRestoredWallet_All_All(PayloadManager payloadManager) throws Exception {

        String mnemonic = "all all all all all all all all all all all all";
        return payloadManager.restoreHDWallet("password", mnemonic, "");
    }

    @Test
    public void upgradeV2PayloadToV3() throws Exception {

        final PayloadManager payloadManager = PayloadManager.getInstance();

        //Restore some wallet
        getRestoredWallet_All_All(payloadManager);

        //Remove HD part so we can upgrade it to HD again
        Payload payload = payloadManager.getPayload();
        payload.setHdWalletList(new ArrayList<HDWallet>());

        //Add legacy so we have at least 1 address
        final LegacyAddress legacyAddress = payloadManager.generateLegacyAddress("android", "6.6", null);
        payloadManager.addLegacyAddress(legacyAddress);

        final String guidOriginal = payloadManager.getPayload().getGuid();

        //Now we have legacy wallet (only addresses)
        payloadManager.upgradeV2PayloadToV3("", true, "My Bci HDWallet", new PayloadManager.UpgradePayloadListener() {
            public void onDoubleEncryptionPasswordError() {
                Assert.assertEquals("upgradeV2PayloadToV3 failed", false);
            }

            public void onUpgradeSuccess() {

                //Check that existing legacy addresses still exist
                Assert.assertEquals(legacyAddress.getAddress(), payloadManager.getPayload().getLegacyAddressStringList().get(0));

                //Check that Guid is still same
                Assert.assertEquals(payloadManager.getPayload().getGuid(), guidOriginal);

                //Check that wallet is flagged as upgraded
                Assert.assertTrue(payloadManager.getPayload().isUpgraded());

                //Check that 1 account exists with keys
                String xpriv = payloadManager.getPayload().getHdWallet().getAccounts().get(0).getXpriv();
                Assert.assertTrue(xpriv != null && !xpriv.isEmpty());

                //Check that mnemonic exists
                try {
                    Assert.assertEquals(payloadManager.getMnemonic().length, 12);
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail("upgradeV2PayloadToV3 failed");
                }
            }

            public void onUpgradeFail() {
                Assert.fail("upgradeV2PayloadToV3 failed");
            }
        });

        PayloadManager.getInstance().wipe();
    }
}
