package info.blockchain.wallet.metadata;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDWalletFactory;
import info.blockchain.wallet.bip44.HDWalletFactory.Language;
import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SharedMetadataTest extends MockedResponseTest {

    DeterministicKey key;

    @Before
    public void setUp() throws Exception {
        key = HDWalletFactory
            .restoreWallet(PersistentUrls.getInstance().getBitcoinParams(), Language.US,
                "15e23aa73d25994f1921a1256f93f72c", "", 1).getMasterKey();
    }

    @Test
    public void getNode() throws Exception {
        SharedMetadata sharedMetadata = new SharedMetadata.Builder(key).build();
        Assert.assertTrue(sharedMetadata.getNode().getPrivateKeyAsHex()
            .equals("b3c830cde7bac6b5d2cad754ea523fb6ff51fc59da49d28ac98268d87f23b89b"));
    }

    @Test
    public void getAddress() throws Exception {
        SharedMetadata sharedMetadata = new SharedMetadata.Builder(key).build();
        Assert.assertTrue(sharedMetadata.getAddress().equals("1B6ptXjmNHimrWNMzKSDhxTmdjhWvZE3vV"));
    }

    @Test
    public void getTrustedList() throws Exception {
    }

    @Test
    public void getTrusted() throws Exception {
    }

    @Test
    public void putTrusted() throws Exception {

    }

    @Test
    public void deleteTrusted() throws Exception {

    }

    @Test
    public void sendPaymentRequest() throws Exception {

    }

    @Test
    public void acceptPaymentRequest() throws Exception {

    }

    @Test
    public void getPaymentRequests() throws Exception {

    }

    @Test
    public void getPaymentRequestResponses() throws Exception {

    }

    @Test
    public void createInvitation() throws Exception {

    }

    @Test
    public void acceptInvitation() throws Exception {

    }

    @Test
    public void readInvitation() throws Exception {

    }

    @Test
    public void deleteInvitation() throws Exception {

    }

    @Test
    public void publishXpub() throws Exception {

    }

    @Test
    public void getPublicXpubFromMdid() throws Exception {

    }

    @Test
    public void decryptFrom() throws Exception {

        DeterministicKey a_key = HDWalletFactory
            .restoreWallet(PersistentUrls.getInstance().getBitcoinParams(), Language.US,
                "15e23aa73d25994f1921a1256f93f72c", "", 1).getMasterKey();
        SharedMetadata a_sharedMetadata = new SharedMetadata.Builder(a_key).build();

        DeterministicKey b_key = HDWalletFactory
            .restoreWallet(PersistentUrls.getInstance().getBitcoinParams(), Language.US,
                "20e3939d08ddf727f34a130704cd925e", "", 1).getMasterKey();
        SharedMetadata b_sharedMetadata = new SharedMetadata.Builder(b_key).build();

        String encryptedMessage = a_sharedMetadata
            .encryptFor(b_sharedMetadata.getXpub(), "Water is wet");
        String decryptedMessage = b_sharedMetadata
            .decryptFrom(a_sharedMetadata.getXpub(), encryptedMessage);
    }
}