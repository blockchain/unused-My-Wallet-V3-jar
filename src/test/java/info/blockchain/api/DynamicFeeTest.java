package info.blockchain.api;

import info.blockchain.wallet.payment.data.SuggestedFee;

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;

public class DynamicFeeTest {

    @Test
    public void testGetDynamicFee() throws Exception {
        SuggestedFee suggestedFee = new DynamicFee().getDynamicFee();

        assertThat("Ensure suggested fee is > 0", suggestedFee.defaultFeePerKb != null &&
                suggestedFee.defaultFeePerKb.compareTo(BigInteger.ZERO) > 0);

        assertThat("Suggested fee blocks array size <= 0", suggestedFee.estimateList.size() > 0);
    }
}