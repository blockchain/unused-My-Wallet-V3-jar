package info.blockchain.wallet.payload;

import info.blockchain.test_data.PayloadTestData;
import org.junit.Assert;
import org.junit.Test;

public class PayloadTest {

    @Test
    public void parsePayload_shouldPass() throws Exception {

        Payload payload = new Payload(PayloadTestData.jsonObject, 5000);

        Assert.assertEquals(payload.getGuid(), PayloadTestData.GUID);
        Assert.assertEquals(payload.getSharedKey(), PayloadTestData.SHARED_KEY);

        Assert.assertEquals(payload.getDoubleEncryptionPbkdf2Iterations(), PayloadTestData.PDKDF2_ITERATIONS);
        Assert.assertEquals(payload.getDoublePasswordHash(), PayloadTestData.DPASSWORD_HASH);
        Assert.assertEquals(payload.isDoubleEncrypted(), Boolean.parseBoolean(PayloadTestData.DOUBLE_ENCRYPTED));
        Assert.assertEquals(payload.getHdWallet().getAccounts().size(), PayloadTestData.TOTAL_ACCOUNTS);
        Assert.assertEquals(payload.getLegacyAddressList().size(), PayloadTestData.TOTAL_LEGACY_ADDRESSES);
        Assert.assertEquals(payload.getAddressBookEntryList().size(), PayloadTestData.TOTAL_ADDRESSBOOK_ENTRIES);

        Assert.assertEquals(payload.getOptions().isHtml5Notifications(), Boolean.parseBoolean(PayloadTestData.HTML_NOTIFICATIONS));
        Assert.assertEquals(payload.getOptions().getIterations(), PayloadTestData.PDKDF2_ITERATIONS);

        Assert.assertEquals(payload.getHdWallet().getAccounts().get(0).getXpriv(), PayloadTestData.ACCOUNT_1_XPRIV);
        Assert.assertEquals(payload.getHdWallet().getAccounts().get(1).getXpriv(), PayloadTestData.ACCOUNT_2_XPRIV);
        Assert.assertEquals(payload.getHdWallet().getAccounts().get(2).getXpriv(), PayloadTestData.ACCOUNT_3_XPRIV);

        Assert.assertEquals(payload.getHdWallet().getAccounts().get(0).getXpub(), PayloadTestData.ACCOUNT_1_XPUB);
        Assert.assertEquals(payload.getHdWallet().getAccounts().get(1).getXpub(), PayloadTestData.ACCOUNT_2_XPUB);
        Assert.assertEquals(payload.getHdWallet().getAccounts().get(2).getXpub(), PayloadTestData.ACCOUNT_3_XPUB);

        Assert.assertEquals(payload.getHdWallet().getAccounts().get(0).getLabel(), PayloadTestData.ACCOUNT_1_LABEL);
        Assert.assertEquals(payload.getHdWallet().getAccounts().get(1).getLabel(), PayloadTestData.ACCOUNT_2_LABEL);
        Assert.assertEquals(payload.getHdWallet().getAccounts().get(2).getLabel(), PayloadTestData.ACCOUNT_3_LABEL);

        Assert.assertEquals(payload.getHdWallet().getAccounts().get(0).isArchived(), Boolean.valueOf(PayloadTestData.ACCOUNT_1_ARCHIVED));
        Assert.assertEquals(payload.getHdWallet().getAccounts().get(1).isArchived(), Boolean.valueOf(PayloadTestData.ACCOUNT_2_ARCHIVED));
        Assert.assertEquals(payload.getHdWallet().getAccounts().get(2).isArchived(), Boolean.valueOf(PayloadTestData.ACCOUNT_3_ARCHIVED));

        Assert.assertEquals(payload.getHdWallet().getSeedHex(), PayloadTestData.HD_WALLET_SEED_HEX);

        Assert.assertEquals(payload.getLegacyAddressList().get(0).getLabel(), PayloadTestData.ADDRESS_1_LABEL);
        Assert.assertEquals(payload.getLegacyAddressList().get(1).getLabel(), null);

        Assert.assertEquals(payload.getLegacyAddressList().get(0).getEncryptedKey(), PayloadTestData.ADDRESS_1_KEY);
        Assert.assertEquals(payload.getLegacyAddressList().get(1).getEncryptedKey(), PayloadTestData.ADDRESS_2_KEY);

        Assert.assertEquals(payload.getLegacyAddressList().get(0).getAddress(), PayloadTestData.ADDRESS_1_ADDRESS);
        Assert.assertEquals(payload.getLegacyAddressList().get(1).getAddress(), PayloadTestData.ADDRESS_2_ADDRESS);

        Assert.assertEquals(payload.getAddressBookEntryList().get(0).getLabel(), PayloadTestData.ADDRESSBOOK_1_LABEL);
        Assert.assertEquals(payload.getAddressBookEntryList().get(0).getAddress(), PayloadTestData.ADDRESSBOOK_1_ADDRESS);
    }

    /**
     * Test should still pass even if certain optional json parts have been left out
     */
    @Test
    public void parsePayload_withMinimalValues_shouldPass() throws Exception {

        Payload payload = new Payload(PayloadTestData.jsonObject_minimal, 5000);

        Assert.assertEquals(payload.getGuid(), PayloadTestData.GUID);
        Assert.assertEquals(payload.getSharedKey(), PayloadTestData.SHARED_KEY);

        Assert.assertEquals(payload.getDoubleEncryptionPbkdf2Iterations(), PayloadTestData.PDKDF2_ITERATIONS);
        Assert.assertEquals(payload.isDoubleEncrypted(), false);

        Assert.assertEquals(payload.getOptions().isHtml5Notifications(), false);
        Assert.assertEquals(payload.getOptions().getIterations(), BlockchainWallet.DEFAULT_PBKDF2_ITERATIONS_V2);
    }

    @Test
    public void parsePayload_withCorruptLegacyAddress_shouldFail()  {

        try {
            new Payload(PayloadTestData.jsonObject_CorruptLegacyAddress, 5000);
            Assert.assertTrue("Corrupt wallet should not pass parse", false);
        } catch (Exception e) {
            Assert.assertTrue("Corrupt wallet parse failed", true);
        }
    }
}