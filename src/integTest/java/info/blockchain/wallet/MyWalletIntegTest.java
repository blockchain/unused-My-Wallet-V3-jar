//package info.blockchain.wallet;
//
//import info.blockchain.wallet.payload.data2.LegacyAddressBody;
//import info.blockchain.wallet.payload.data2.HDWalletBody;
//import info.blockchain.wallet.payload.data2.WalletBody;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class MyWalletIntegTest extends BaseIntegTest{
//
//    @Test
//    public void upgradeV2PayloadToV3() throws Exception {
//
//        //Create a wallet
//        WalletBody walletBody = new WalletBody("HDAccount name");
//
//        //Remove HD part
//        walletBody.setHdWallets(new ArrayList<HDWalletBody>());
//
//        //Add legacy so we have at least 1 address
//        LegacyAddressBody legacyAddress = LegacyAddressBody.generateNewLegacy();
//        walletBody.addLegacyAddress(legacyAddress);
//
//        final String guidOriginal = walletBody.getGuid();
//
//        walletBody.upgradeV2PayloadToV3(null, "HDAccount Name2");
//
//        //Check that existing legacy addresses still exist
//        Assert.assertEquals(legacyAddress.getAddressString(), walletBody.getKeys().get(0).getAddressString());
//
//        //Check that Guid is still same
//        Assert.assertEquals(walletBody.getGuid(), guidOriginal);
//
//        //Check that wallet is flagged as upgraded
//        Assert.assertTrue(walletBody.isUpgraded());
//
//        System.out.println(walletBody.toJson());
//
//        //Check that 1 account exists with keys
//        String xpriv = walletBody.getHdWallet().getAccounts().get(0).getXpriv();
//        Assert.assertTrue(xpriv != null && !xpriv.isEmpty());
//
//        //Check that mnemonic exists
//        try {
//            Assert.assertEquals(walletBody.getHdWallet().getMnemonic().size(), 12);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Assert.fail("upgradeV2PayloadToV3 failed");
//        }
//    }
//}
