package info.blockchain.wallet.metadata;

import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDWallet;
import info.blockchain.wallet.bip44.HDWalletFactory;
import info.blockchain.wallet.bip44.HDWalletFactory.Language;
import info.blockchain.wallet.contacts.data.PublicContactDetails;
import info.blockchain.wallet.util.MetadataUtil;
import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetadataNodeFactoryIntegTest extends BaseIntegTest {

    MetadataNodeFactory subject;

    private HDWallet getWallet() throws Exception {
        return HDWalletFactory
            .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(), Language.US,
                "15e23aa73d25994f1921a1256f93f72c",
                "",
                1);
    }

    @Test
    public void Constructor_legacyNode_should_be_unavailable() throws Exception {

        subject = new MetadataNodeFactory("guid","sharedKey","walletPassword");
        subject.saveMetadataHdNodes(getWallet().getMasterKey());

        subject = new MetadataNodeFactory("guid","sharedKey","walletPassword");
        subject.saveMetadataHdNodes(getWallet().getMasterKey());

        Assert.assertTrue(subject.isMetadataUsable());
        Assert.assertFalse(subject.isLegacySecondPwNodeAvailable());
    }

    @Test
    public void saveMetadataHdNodes() throws Exception {

        subject = new MetadataNodeFactory("guid","sharedKey","walletPassword");
        subject.saveMetadataHdNodes(getWallet().getMasterKey());

        Assert.assertTrue(subject.isMetadataUsable());
    }
}
