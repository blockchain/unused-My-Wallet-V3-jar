package info.blockchain.wallet.bip44;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.BitcoinMainNetParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by riaanvos on 27/01/2017.
 */
public class HDChainTest {

    String seed = "15e23aa73d25994f1921a1256f93f72c";
    DeterministicKey key;

    @Before
    public void setup() {
        key = HDKeyDerivation
            .createMasterPrivateKey(seed.getBytes());
    }

    @Test
    public void isReceive() throws Exception {
        HDChain chain = new HDChain(BitcoinMainNetParams.get(), key, true);
        Assert.assertTrue(chain.isReceive());

        chain = new HDChain(BitcoinMainNetParams.get(), key, false);
        Assert.assertFalse(chain.isReceive());
    }

    @Test
    public void getAddressAt() throws Exception {
        HDChain chain = new HDChain(BitcoinMainNetParams.get(), key, true);
        Assert.assertEquals("1HxBEXhu5LPibpTAQ1EoNTJavDSbwajJTg", chain.getAddressAt(0).getAddressString());
    }

    @Test
    public void getPath() throws Exception {
        HDChain chain = new HDChain(BitcoinMainNetParams.get(), key, true);
        Assert.assertEquals("M/0", chain.getPath());
    }
}