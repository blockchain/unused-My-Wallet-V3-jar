package info.blockchain.wallet.ethereum;

import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.ethereum.data.EthAddressResponseMap;

import org.junit.Test;

import java.util.Arrays;

import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EthAccountApiIntegTest extends BaseIntegTest {

    private EthAccountApi accountApi = new EthAccountApi();

    @Test
    public void getEthAddress() throws Exception {
        final TestObserver<EthAddressResponseMap> testObserver =
                accountApi.getEthAddress(
                        Arrays.asList("0xccc1dd86df371ecc3ea28a6877fd2301a09effd0",
                                "0xF85608F8fe3887Dab333Ec250A972C1DC19C52B3"))
                        .test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertNotNull(testObserver.values().get(0));
        assertEquals(2, testObserver.values().get(0).getEthAddressResponseMap().values().size());
    }

}
