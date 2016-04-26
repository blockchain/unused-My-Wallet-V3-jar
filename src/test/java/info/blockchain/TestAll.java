package info.blockchain;

import info.blockchain.api.DynamicFeeTest;
import info.blockchain.wallet.CreateWalletTest;
import info.blockchain.wallet.DoubleEncryptionFactoryTest;
import info.blockchain.wallet.RestoreHDWalletTest;
import info.blockchain.wallet.crypto.AESUtilTest;
import info.blockchain.wallet.multiaddr.MultiAddrFactoryTest;
import info.blockchain.wallet.payment.PaymentTest;
import info.blockchain.wallet.util.FeeUtilTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MultiAddrFactoryTest.class,
        PaymentTest.class,
        FeeUtilTest.class,
        DynamicFeeTest.class,
        AESUtilTest.class,
        FeeUtilTest.class,
        CreateWalletTest.class,
        DoubleEncryptionFactoryTest.class,
        RestoreHDWalletTest.class
})

public class TestAll {
    // the class remains empty,
    // used only as a holder for the above annotations
}
