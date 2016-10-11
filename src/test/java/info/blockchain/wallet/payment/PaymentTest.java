package info.blockchain.wallet.payment;

import info.blockchain.test_data.UnspentTestData;
import info.blockchain.util.FeeUtil;
import info.blockchain.wallet.payment.data.SpendableUnspentOutputs;
import info.blockchain.wallet.payment.data.SweepBundle;
import info.blockchain.wallet.payment.data.UnspentOutputs;
import info.blockchain.wallet.send.SendCoins;

import org.json.JSONObject;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PaymentTest {

    private long calculateFee(int outputs, int inputs, BigInteger feePerKb) {
        //Manually calculated fee
        long size = (outputs * 34) + (inputs * 148) + 10;//36840L
        double txBytes = ((double) size / 1000.0);
        return (long) Math.ceil(feePerKb.doubleValue() * txBytes);
    }

    @Test
    public void getCoins() {

        Payment transaction = new Payment();

        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));
        assertThat(coins.getOutputs().size(), is(UnspentTestData.UNSPENT_OUTPUTS_COUNT));
        assertThat(coins.getBalance().longValue(), is(UnspentTestData.BALANCE));
        assertThat(coins.getNotice(), is(UnspentTestData.NOTICE));
    }

    @Test
    public void sweepFee_shouldEqualCorrectAmount() {

        Payment transaction = new Payment();

        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        SweepBundle sweepBundle = transaction.getSweepBundle(coins, FeeUtil.AVERAGE_FEE_PER_KB);

        long feeManual = calculateFee(1, UnspentTestData.UNSPENT_OUTPUTS_COUNT, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(sweepBundle.getSweepFee().longValue(), is(feeManual));
    }

    @Test
    public void sweepBalance_shouldEqualTotalMinusFee() {

        Payment transaction = new Payment();

        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        SweepBundle sweepBundle = transaction.getSweepBundle(coins, FeeUtil.AVERAGE_FEE_PER_KB);

        long feeManual = calculateFee(1, UnspentTestData.UNSPENT_OUTPUTS_COUNT, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(sweepBundle.getSweepAmount().longValue(), is(UnspentTestData.BALANCE - feeManual));
    }

    @Test
    public void spendFirstCoin_minusFee_shouldNotExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long spendAmount = 80200l;
        int inputs = 1;
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, spendAmountMinusFee, FeeUtil.AVERAGE_FEE_PER_KB);

        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(0l));
    }

    @Test
    public void spendFirstCoin_minusDust_minusFee_shouldNotExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long consumedAmount = SendCoins.bDust.longValue();
        long spendAmount = 80200l - consumedAmount;
        int inputs = 1;
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, spendAmountMinusFee, FeeUtil.AVERAGE_FEE_PER_KB);

        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(consumedAmount));
    }

    @Test
    public void spendFirstCoin_minusLessThanDust_minusFee_shouldNotExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long consumedAmount = 300l;
        long spendAmount = 80200l - consumedAmount;
        int inputs = 1;
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, spendAmountMinusFee, FeeUtil.AVERAGE_FEE_PER_KB);

        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(consumedAmount));
    }

    @Test
    public void spendFirstTwoCoins_minusFee_shouldNotExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long spendAmount = 80200l + 70000l;
        int inputs = 2;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, spendAmountMinusFee, FeeUtil.AVERAGE_FEE_PER_KB);

        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(0l));
    }

    @Test
    public void spendFirstTwoCoins_minusDust_minusFee_shouldNotExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long consumedAmount = SendCoins.bDust.longValue();
        long spendAmount = 80200l + 70000l - consumedAmount;
        int inputs = 2;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, spendAmountMinusFee, FeeUtil.AVERAGE_FEE_PER_KB);

        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(consumedAmount));
    }

    @Test
    public void spendFirstThreeCoins_minusDust_minusFee_shouldNotExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long consumedAmount = SendCoins.bDust.longValue();
        long spendAmount = 80200l + 70000l + 60000l - consumedAmount;
        int inputs = 3;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, spendAmountMinusFee, FeeUtil.AVERAGE_FEE_PER_KB);

        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(consumedAmount));
    }

    @Test
    public void spendFirstThreeCoins_plusSome_minusFee_shouldExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long spendAmount = 80200l + 70000l + 60000l + 30000l;
        int inputs = 4;//coins
        int outputs = 2;//change
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, spendAmountMinusFee, FeeUtil.AVERAGE_FEE_PER_KB);

        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(0l));
    }

    @Test
    public void spendFirstThreeCoins_plusFee_shouldUse4Inputs_AndExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long spendAmount = 80200l + 70000l + 60000l;
        int inputs = 4;//coins
        int outputs = 2;//change
        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, BigInteger.valueOf(spendAmount), FeeUtil.AVERAGE_FEE_PER_KB);
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);
        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(0l));
    }

    @Test
    public void spendAllCoins_minusFee_shouldUse8Inputs_AndNotExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long spendAmount = 80200l + 70000l + 60000l + 50000l + 40000l + 30000l + 20000l + 10000l;
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);

        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, BigInteger.valueOf(spendAmount - feeManual), FeeUtil.AVERAGE_FEE_PER_KB);

        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(0l));
    }

    @Test
    public void spendAllCoins_minusFee_minusDust_shouldUse8Inputs_AndNotExpectChange() {

        Payment transaction = new Payment();

        /*
        8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs coins = transaction.getCoins(new JSONObject(UnspentTestData.apiResponseString));

        long spendAmount = 80200l + 70000l + 60000l + 50000l + 40000l + 30000l + 20000l + 10000l - SendCoins.bDust.longValue();
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, FeeUtil.AVERAGE_FEE_PER_KB);

        SpendableUnspentOutputs spendableCoins = transaction.getSpendableCoins(coins, BigInteger.valueOf(spendAmount - feeManual), FeeUtil.AVERAGE_FEE_PER_KB);

        assertThat(spendableCoins.getSpendableOutputs().size(), is(inputs));
        assertThat(spendableCoins.getAbsoluteFee().longValue(), is(feeManual));
        assertThat(Math.abs(spendableCoins.getConsumedAmount().longValue()), is(SendCoins.bDust.longValue()));
    }
}