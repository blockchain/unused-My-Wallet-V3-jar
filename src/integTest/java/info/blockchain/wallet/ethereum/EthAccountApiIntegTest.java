package info.blockchain.wallet.ethereum;

import static org.junit.Assert.assertNotNull;

import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.ethereum.data.EthAddressResponseMap;
import io.reactivex.observers.TestObserver;
import org.junit.Test;

public class EthAccountApiIntegTest extends BaseIntegTest{

    private EthAccountApi accountApi = new EthAccountApi();

    @Test
    public void getEthAddress() throws Exception {
        final TestObserver<EthAddressResponseMap> testObserver = accountApi.getEthAddress("0xccc1dd86df371ecc3ea28a6877fd2301a09effd0").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertNotNull(testObserver.values().get(0));
        assertNotNull(testObserver.values().get(0).getEthAddressResponseMap().values().size());
    }

    @Test
    public void getEthAddressLegacy() throws Exception {
        final TestObserver<EthAddressResponseMap> testObserver = accountApi.getEthAddress("0xf67064C27D19AA787e1d8aFd477D86b4b63c8dA9").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertNotNull(testObserver.values().get(0));
        assertNotNull(testObserver.values().get(0).getEthAddressResponseMap().values().size());
    }
}
