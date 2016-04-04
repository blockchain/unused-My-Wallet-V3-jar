package info.blockchain.wallet.multiaddr;

import org.junit.Test;

public class MultiAddrFactoryTest {

    @Test
    public void testParseLegacy(){
        assert(MultiAddrFactory.getInstance().getLegacy(new String[]{"1D9daU9Ckd9XpFkByiGLr8T4EsVQVc78WN", "18uDFELUioNqeSTDPbybWj1zwW8b3V3QHR"}, false) != null);
    }
}