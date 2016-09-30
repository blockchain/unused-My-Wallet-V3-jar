package info.blockchain.wallet.payload;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class PayloadManagerTest {

    @Mock
    private PayloadManager mPayloadManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void generateNewLegacyAddress_withWrongSecondPassword_shouldFail() throws Exception{

        when(mPayloadManager.validateSecondPassword(anyString()))
                .thenReturn(false);

        LegacyAddress legacyAddress = mPayloadManager.generateLegacyAddress("Jar","1.0","second_password");

        assertThat("Address should be null", legacyAddress == null);
    }

    @Test
    public void generateNewLegacyAddress_withFailedRandomECKey_shouldFail() throws Exception{

        when(mPayloadManager.getRandomECKey())
                .thenReturn(null);

        LegacyAddress legacyAddress = mPayloadManager.generateLegacyAddress("Jar","1.0","second_password");

        assertThat("Address should be null", legacyAddress == null);
    }
}
