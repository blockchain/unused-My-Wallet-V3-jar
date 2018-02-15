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
public class AddressTest {

    String seed = "15e23aa73d25994f1921a1256f93f72c";
    String xpub = "xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV";
    String xpriv = "L1HazQbEpwQKWnE5gXNKsFtHy2ufmpScQ8zZAd14BpwRX5DJpsdq";
    DeterministicKey key;
    HDAddress address;

    @Before
    public void setup() {
        key = HDKeyDerivation
            .createMasterPrivateKey(seed.getBytes());
        address = new HDAddress(BitcoinMainNetParams.get(), key, 0);
    }

    @Test
    public void getPubKey() throws Exception {
        Assert.assertEquals(33,address.getPubKey().length);
    }

    @Test
    public void getPubKeyHash() throws Exception {
        Assert.assertEquals(20,address.getPubKeyHash().length);
    }

    @Test
    public void getAddressString() throws Exception {
        Assert.assertEquals("1NbtWC8uX9spHtFnhoLGc2haXyfGpMuBuf",address.getAddressString());
    }

    @Test
    public void getPrivateKeyString() throws Exception {
        Assert.assertEquals(xpriv,address.getPrivateKeyString());
    }

    @Test
    public void getAddress() throws Exception {
        Assert.assertEquals("1NbtWC8uX9spHtFnhoLGc2haXyfGpMuBuf",address.getAddress().toString());
    }

    @Test
    public void getPath() throws Exception {
        Assert.assertEquals("M/0",address.getPath());
    }

    @Test
    public void getChildNum() throws Exception {
        Assert.assertEquals(0,address.getChildNum());
    }

}