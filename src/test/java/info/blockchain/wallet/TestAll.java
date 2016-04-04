package info.blockchain.wallet;

import info.blockchain.wallet.multiaddr.MultiAddrFactoryTest;
import info.blockchain.wallet.transaction.SimpleTransactionTest;
import info.blockchain.wallet.util.AddressInfoTest;
import info.blockchain.wallet.util.FeeUtilTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MultiAddrFactoryTest.class,
        SimpleTransactionTest.class,
        FeeUtilTest.class,
        AddressInfoTest.class
})

public class TestAll {
    // the class remains empty,
    // used only as a holder for the above annotations
}
