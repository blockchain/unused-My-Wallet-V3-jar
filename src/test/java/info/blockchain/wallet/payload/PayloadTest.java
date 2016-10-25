package info.blockchain.wallet.payload;

import info.blockchain.test_data.PayloadTestData;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PayloadTest {

    @Test
    public void parsePayload_shouldPass() throws Exception {

        Payload payload = new Payload(PayloadTestData.jsonObject, 5000);

        assertThat(payload.getGuid(), is(PayloadTestData.GUID));
        assertThat(payload.getSharedKey(), is(PayloadTestData.SHARED_KEY));

        assertThat(payload.getDoubleEncryptionPbkdf2Iterations(), is(PayloadTestData.PDKDF2_ITERATIONS));
        assertThat(payload.getDoublePasswordHash(), is(PayloadTestData.DPASSWORD_HASH));
        assertThat(payload.isDoubleEncrypted(), is(Boolean.parseBoolean(PayloadTestData.DOUBLE_ENCRYPTED)));
        assertThat(payload.getHdWallet().getAccounts().size(), is(PayloadTestData.TOTAL_ACCOUNTS));
        assertThat(payload.getLegacyAddressList().size(), is(PayloadTestData.TOTAL_LEGACY_ADDRESSES));
        assertThat(payload.getAddressBookEntryList().size(), is(PayloadTestData.TOTAL_ADDRESSBOOK_ENTRIES));

        assertThat(payload.getOptions().isHtml5Notifications(), is(Boolean.parseBoolean(PayloadTestData.HTML_NOTIFICATIONS)));
        assertThat(payload.getOptions().getIterations(), is(PayloadTestData.PDKDF2_ITERATIONS));

        assertThat(payload.getHdWallet().getAccounts().get(0).getXpriv(), is(PayloadTestData.ACCOUNT_1_XPRIV));
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpriv(), is(PayloadTestData.ACCOUNT_2_XPRIV));
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpriv(), is(PayloadTestData.ACCOUNT_3_XPRIV));

        assertThat(payload.getHdWallet().getAccounts().get(0).getXpub(), is(PayloadTestData.ACCOUNT_1_XPUB));
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpub(), is(PayloadTestData.ACCOUNT_2_XPUB));
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpub(), is(PayloadTestData.ACCOUNT_3_XPUB));

        assertThat(payload.getHdWallet().getAccounts().get(0).getLabel(), is(PayloadTestData.ACCOUNT_1_LABEL));
        assertThat(payload.getHdWallet().getAccounts().get(1).getLabel(), is(PayloadTestData.ACCOUNT_2_LABEL));
        assertThat(payload.getHdWallet().getAccounts().get(2).getLabel(), is(PayloadTestData.ACCOUNT_3_LABEL));

        assertThat(payload.getHdWallet().getAccounts().get(0).isArchived(), is(Boolean.valueOf(PayloadTestData.ACCOUNT_1_ARCHIVED)));
        assertThat(payload.getHdWallet().getAccounts().get(1).isArchived(), is(Boolean.valueOf(PayloadTestData.ACCOUNT_2_ARCHIVED)));
        assertThat(payload.getHdWallet().getAccounts().get(2).isArchived(), is(Boolean.valueOf(PayloadTestData.ACCOUNT_3_ARCHIVED)));

        assertThat(payload.getHdWallet().getSeedHex(), is(PayloadTestData.HD_WALLET_SEED_HEX));

        assertThat(payload.getLegacyAddressList().get(0).getLabel(), is(PayloadTestData.ADDRESS_1_LABEL));
        Assert.assertEquals(payload.getLegacyAddressList().get(1).getLabel(), null);

        assertThat(payload.getLegacyAddressList().get(0).getEncryptedKey(), is(PayloadTestData.ADDRESS_1_KEY));
        assertThat(payload.getLegacyAddressList().get(1).getEncryptedKey(), is(PayloadTestData.ADDRESS_2_KEY));

        assertThat(payload.getLegacyAddressList().get(0).getAddress(), is(PayloadTestData.ADDRESS_1_ADDRESS));
        assertThat(payload.getLegacyAddressList().get(1).getAddress(), is(PayloadTestData.ADDRESS_2_ADDRESS));

        assertThat(payload.getAddressBookEntryList().get(0).getLabel(), is(PayloadTestData.ADDRESSBOOK_1_LABEL));
        assertThat(payload.getAddressBookEntryList().get(0).getAddress(), is(PayloadTestData.ADDRESSBOOK_1_ADDRESS));
    }

    /**
     * Test should still pass even if certain optional json parts have been left out
     */
    @Test
    public void parsePayload_withMinimalValues_shouldPass() throws Exception {

        Payload payload = new Payload(PayloadTestData.jsonObject_minimal, 5000);

        assertThat(payload.getGuid(), is(PayloadTestData.GUID));
        assertThat(payload.getSharedKey(), is(PayloadTestData.SHARED_KEY));

        assertThat(payload.getDoubleEncryptionPbkdf2Iterations(), is(PayloadTestData.PDKDF2_ITERATIONS));
        assertThat(payload.isDoubleEncrypted(), is(false));

        assertThat(payload.getOptions().isHtml5Notifications(), is(false));
        assertThat(payload.getOptions().getIterations(), is(BlockchainWallet.DEFAULT_PBKDF2_ITERATIONS_V2));
    }
}