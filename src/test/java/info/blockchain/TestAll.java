package info.blockchain;

import info.blockchain.api.DynamicFeeTest;
import info.blockchain.wallet.multiaddr.MultiAddrFactoryTest;
import info.blockchain.wallet.transaction.AdvancedTransactionTest;
import info.blockchain.wallet.transaction.SimpleTransactionTest;
import info.blockchain.wallet.util.FeeUtilTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MultiAddrFactoryTest.class,
        SimpleTransactionTest.class,
        AdvancedTransactionTest.class,
        FeeUtilTest.class,
        DynamicFeeTest.class
})

public class TestAll {
    // the class remains empty,
    // used only as a holder for the above annotations
}
