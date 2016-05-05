package info.blockchain.wallet.util;

import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;

public class FeeUtilTest  {

    /*
    absolute fee calculations - http://bitcoinfees.com/
    absoluteFee = ceil (148 * number_of_inputs + 34 * number_of_outputs + 10) * feePrKb/1000
     */
    @Test
    public void testEstimatedFee() throws Exception {

        ArrayList<int[]> cases = new ArrayList<int[]>();
        //new int[]{[--inputs--],[--outputs--],[--feePrKb--],[--absoluteFee--]}
        cases.add(new int[]{1, 1, 0, 0});
        cases.add(new int[]{1, 2, 0, 0});
        cases.add(new int[]{2, 1, 0, 0});
        cases.add(new int[]{1, 1, 30000, 5760});
        cases.add(new int[]{1, 2, 30000, 6780});
        cases.add(new int[]{2, 1, 30000, 10200});
        cases.add(new int[]{3, 3, 30000, 16680});
        cases.add(new int[]{5, 10, 30000, 32701});

        for (int testCase = 0; testCase < cases.size(); testCase++){

            int inputs = cases.get(testCase)[0];
            int outputs = cases.get(testCase)[1];

            BigInteger absoluteFee = FeeUtil.estimatedFee(inputs, outputs, BigInteger.valueOf(cases.get(testCase)[2]));
            assert(cases.get(testCase)[3] == absoluteFee.longValue());
        }
    }
}