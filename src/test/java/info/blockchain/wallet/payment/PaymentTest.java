package info.blockchain.wallet.payment;

import info.blockchain.test_data.UnspentTestData;
import info.blockchain.util.FeeUtil;
import info.blockchain.wallet.send.SendCoins;
import info.blockchain.wallet.payment.data.SpendableUnspentOutputs;
import info.blockchain.wallet.payment.data.UnspentOutputs;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PaymentTest {

    @Test
    public void testGetCoins() {

        Payment transaction = new Payment();

        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));
        assertThat(coins.getOutputs().size(), is(UnspentTestData.UNSPENT_OUTPUTS_COUNT));
        assertThat(coins.getBalance().longValue(), is(UnspentTestData.BALANCE));
        assertThat(coins.getNotice(), is(UnspentTestData.NOTICE));
    }

    @Test
    public void testGetCachedCoins() throws Exception {

        //Cache it
        Payment transaction = new Payment();
        transaction.cacheUnspentOutputs(UnspentTestData.ADDRESS, new JSONObject(UnspentTestData.apiResponseString));

        //Read cached
        UnspentOutputs coins = transaction.getCachedCoins(UnspentTestData.ADDRESS);
        assertThat(coins.getOutputs().size(), is(UnspentTestData.UNSPENT_OUTPUTS_COUNT));
        assertThat(coins.getBalance().longValue(), is(UnspentTestData.BALANCE));
        assertThat(coins.getNotice(), is(UnspentTestData.NOTICE));
    }

    @Test
    public void testGetSweepFee() throws Exception {

        Payment transaction = new Payment();

        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        BigInteger sweepBalance = transaction.getSweepFee(coins, FeeUtil.AVERAGE_FEE_PER_KB);

        //Manually calculated fee
        long size = (1 * 34) + (UnspentTestData.WORTHY_UNSPENT_OUTPUTS_COUNT * 148) + 10;//36840L
        double txBytes = ((double)size / 1000.0);
        long feeManual = (long)Math.ceil(FeeUtil.AVERAGE_FEE_PER_KB.doubleValue() * txBytes);

        assertThat(sweepBalance.longValue(), is(feeManual));
    }

    @Test
    public void testGetSweepBalance() throws Exception {

        Payment transaction = new Payment();

        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        BigInteger sweepBalance = transaction.getSweepBalance(coins, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(sweepBalance.longValue(), is(276480L));
    }

    @Test
    public void testGetSpendableCoins() {

        Payment transaction = new Payment();

        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        SpendableUnspentOutputs spendableCoins = null;
        BigInteger spendAmount = BigInteger.ZERO;
        BigInteger feePerKb = info.blockchain.wallet.util.FeeUtil.AVERAGE_FEE_PER_KB;
        BigInteger dust = SendCoins.bDust;
        int inputs = 0;
        int outputs = 0;

        //First coin minus fee
        inputs = 1;
        outputs = 1;
        spendAmount = BigInteger.valueOf(80000l - 5760l);
        spendableCoins = transaction.getSpendableCoins(coins, spendAmount, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(UnspentTestData.feeMap.get(outputs)[inputs]));

        /*
            Dust inclusion removed to match up with js
         */
        //First coin minus fee, add dust to test if consumed
//        inputs = 1;
//        outputs = 1;
//        spendAmount = BigInteger.valueOf(80000l - 5760l - dust.longValue());
//        spendableCoins = transaction.getSpendableCoins(coins, spendAmount, FeeUtil.AVERAGE_FEE_PER_KB);
//        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
//        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(UnspentTestData.feeMap.get(outputs)[inputs]));

        //First coin minus fee, add a bit more than dust to push it to 2 expected outputs
        inputs = 1;
        outputs = 2;
        spendAmount = BigInteger.valueOf(80000l - 5760l - (dust.longValue()*2));
        spendableCoins = transaction.getSpendableCoins(coins, spendAmount, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(UnspentTestData.feeMap.get(outputs)[inputs]));

        //First coin should use 2 inputs and expect change because fee must still be included
        inputs = 2;
        outputs = 2;
        spendAmount = BigInteger.valueOf(80000l);
        spendableCoins = transaction.getSpendableCoins(coins, spendAmount, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(UnspentTestData.feeMap.get(outputs)[inputs]));

        //Two coins minus fee should be exactly 2 inputs and expect no change
        inputs = 2;
        outputs = 1;
        spendAmount = BigInteger.valueOf(150000l - 10200l);
        spendableCoins = transaction.getSpendableCoins(coins, spendAmount, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(UnspentTestData.feeMap.get(outputs)[inputs]));

        inputs = 3;
        outputs = 2;
        spendAmount = BigInteger.valueOf(150000l);
        spendableCoins = transaction.getSpendableCoins(coins, spendAmount, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(UnspentTestData.feeMap.get(outputs)[inputs]));

        inputs = 5;
        outputs = 2;
        spendAmount = BigInteger.valueOf(260000l);
        spendableCoins = transaction.getSpendableCoins(coins, spendAmount, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(UnspentTestData.feeMap.get(outputs)[inputs]));

        //5 inputs, but fee of 24540l pushes to 6 inputs
        inputs = 6;
        outputs = 2;
        spendAmount = BigInteger.valueOf(280000l);
        spendableCoins = transaction.getSpendableCoins(coins, spendAmount, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(UnspentTestData.feeMap.get(outputs)[inputs]));

    }
}