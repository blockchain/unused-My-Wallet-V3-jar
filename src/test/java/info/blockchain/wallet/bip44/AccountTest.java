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
public class AccountTest {

    String seed = "15e23aa73d25994f1921a1256f93f72c";
    String xpub = "xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV";
    String xpriv = "xprv9v8qUhWNujRdAYSQHLkaX6wZbYfAv6VLZUnWAR5C5UK6ZE4KKjcZQMWBECoGcrGJMiPf3KDATPyqa8zurUu8T5Cfuz9BNXizu2AtK84MecB";
    DeterministicKey key;

    @Before
    public void setup() {
        key = HDKeyDerivation
            .createMasterPrivateKey(seed.getBytes());
    }

    @Test
    public void xpubstr() throws Exception {
        HDAccount account = new HDAccount(BitcoinMainNetParams.get(), xpub);
        Assert.assertEquals(xpub,account.getXpub());
    }

    @Test
    public void xprvstr() throws Exception {
        HDAccount account = new HDAccount(BitcoinMainNetParams.get(), key, 0);
        Assert.assertEquals(xpriv,account.getXPriv());
    }

    @Test
    public void getId() throws Exception {
        HDAccount account = new HDAccount(BitcoinMainNetParams.get(), xpub, 1);
        Assert.assertEquals(xpub,account.getXpub());
        Assert.assertEquals(1,account.getId());

        account = new HDAccount(BitcoinMainNetParams.get(), xpub);
        Assert.assertEquals(xpub,account.getXpub());
        Assert.assertEquals(0,account.getId());
    }

    @Test
    public void getReceive() throws Exception {
        HDAccount account = new HDAccount(BitcoinMainNetParams.get(), key, 0);
        Assert.assertTrue(account.getReceive().isReceive());
        Assert.assertEquals("M/0H/0",account.getReceive().getPath());
        Assert.assertEquals("1GfNtDKUu9KZt8ae7c9UM6NUD1uViZcsEA",account.getReceive().getAddressAt(0).getAddressString());
    }

    @Test
    public void getChange() throws Exception {
        HDAccount account = new HDAccount(BitcoinMainNetParams.get(), key, 0);
        Assert.assertFalse(account.getChange().isReceive());
        Assert.assertEquals("M/0H/1",account.getChange().getPath());
        Assert.assertEquals("12boKefnALjsXoQXyHg79aU7qSAFfg5Nze",account.getChange().getAddressAt(0).getAddressString());
    }

    @Test
    public void getChain() throws Exception {
        HDAccount account = new HDAccount(BitcoinMainNetParams.get(), key, 0);
        Assert.assertFalse(account.getChain(1).isReceive());
        Assert.assertEquals("M/0H/1",account.getChain(1).getPath());
        Assert.assertEquals("12boKefnALjsXoQXyHg79aU7qSAFfg5Nze",account.getChain(1).getAddressAt(0).getAddressString());
    }

    @Test
    public void getPath() throws Exception {
        HDAccount account = new HDAccount(BitcoinMainNetParams.get(), key, 0);
        Assert.assertEquals("M/0H",account.getPath());
    }
}