package info.blockchain.wallet.payment;

import info.blockchain.BaseTest;
import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.test_data.UnspentTestData;
import info.blockchain.wallet.api.data.FeesItem;
import info.blockchain.wallet.api.data.FeesResponse;
import info.blockchain.wallet.util.PrivateKeyFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

public class PaymentTest extends BaseTest{

    @Test
    public void estimatedFee() throws Exception {
        
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

        for (int testCase = 0; testCase < cases.size(); testCase++) {

            int inputs = cases.get(testCase)[0];
            int outputs = cases.get(testCase)[1];

            BigInteger absoluteFee = Payment.estimatedFee(inputs, outputs, BigInteger.valueOf(cases.get(testCase)[2]));
            assert (cases.get(testCase)[3] == absoluteFee.longValue());
        }
    }

    @Test
    public void estimatedSize() throws Exception {
        Assert.assertEquals(192, Payment.estimatedSize(1, 1));
        Assert.assertEquals(226, Payment.estimatedSize(1, 2));
        Assert.assertEquals(340, Payment.estimatedSize(2, 1));
        Assert.assertEquals(374, Payment.estimatedSize(2, 2));
    }

    @Test
    public void isAdequateFee() throws Exception {
        Assert.assertTrue(Payment.isAdequateFee(1, 1, BigInteger.valueOf(193)));
        Assert.assertFalse(Payment.isAdequateFee(1, 1, BigInteger.valueOf(192)));
    }

    private long calculateFee(int outputs, int inputs, BigInteger feePerKb) {
        //Manually calculated fee
        long size = (outputs * 34) + (inputs * 148) + 10;//36840L
        double txBytes = ((double) size / 1000.0);
        return (long) Math.ceil(feePerKb.doubleValue() * txBytes);
    }

    @Test
    public void testGetDynamicFee() throws Exception {

        mockInterceptor.setResponseString("{\"mempool\":57126,\"default\":{\"fee\":65000,\"surge\":false,\"ok\":true},\"estimate\":[{\"fee\":71500,\"surge\":false,\"ok\":true},{\"fee\":65300,\"surge\":false,\"ok\":true},{\"fee\":65200,\"surge\":false,\"ok\":true},{\"fee\":65100,\"surge\":false,\"ok\":true},{\"fee\":65000,\"surge\":false,\"ok\":true},{\"fee\":59090.90909090909,\"surge\":false,\"ok\":true}]}");
        Call<FeesResponse> dynamicFee = Payment.getDynamicFee();

        Response<FeesResponse> exe = dynamicFee.execute();
        FeesResponse fee = exe.body();

        Assert.assertEquals(57126, fee.getMempool());
        Assert.assertEquals(65000, fee.getDefaultFee().getFee(), 0.0);
        Assert.assertTrue(fee.getDefaultFee().isOk());
        Assert.assertFalse(fee.getDefaultFee().isSurge());

        Assert.assertEquals(71500, fee.getEstimate().get(0).getFee(), 0.0);
        Assert.assertTrue(fee.getEstimate().get(0).isOk());
        Assert.assertFalse(fee.getEstimate().get(0).isSurge());

        Assert.assertEquals(65300, fee.getEstimate().get(1).getFee(), 0.0);
        Assert.assertTrue(fee.getEstimate().get(1).isOk());
        Assert.assertFalse(fee.getEstimate().get(1).isSurge());

        Assert.assertEquals(65200, fee.getEstimate().get(2).getFee(), 0.0);
        Assert.assertTrue(fee.getEstimate().get(2).isOk());
        Assert.assertFalse(fee.getEstimate().get(2).isSurge());

        Assert.assertEquals(65100, fee.getEstimate().get(3).getFee(), 0.0);
        Assert.assertTrue(fee.getEstimate().get(3).isOk());
        Assert.assertFalse(fee.getEstimate().get(3).isSurge());

        Assert.assertEquals(65000, fee.getEstimate().get(4).getFee(), 0.0);
        Assert.assertTrue(fee.getEstimate().get(4).isOk());
        Assert.assertFalse(fee.getEstimate().get(4).isSurge());

        Assert.assertEquals(59090.90909090909, fee.getEstimate().get(5).getFee(), 0.0);
        Assert.assertTrue(fee.getEstimate().get(5).isOk());
        Assert.assertFalse(fee.getEstimate().get(5).isSurge());

        FeesItem defaultFee = Payment.getDefaultFee();
        Assert.assertEquals(65000, defaultFee.getFee(), 0.0);
        Assert.assertTrue(defaultFee.isOk());
        Assert.assertFalse(defaultFee.isSurge());
    }

    @Test
    public void getCoins_2FreeOutputs() throws Exception {

        mockInterceptor.setResponseString("{\"unspent_outputs\":[{\"tx_hash\":\"70249ac0178e498fd368048273195f4649aff7fef712b1dd1c1cab50e503c461\",\"tx_hash_big_endian\":\"61c403e550ab1c1cddb112f7fef7af49465f1973820468d38f498e17c09a2470\",\"tx_index\":183109705,\"tx_output_n\":0,\"script\":\"76a914aa8b3e5f56605d8b7e4f65120a90bd927f4d0aab88ac\",\"value\":3264,\"value_hex\":\"0cc0\",\"confirmations\":16377},{\"tx_hash\":\"533840c41575ac613fb807e03ee9d1b320a7f4065c3f3d790565529f1c505a3a\",\"tx_hash_big_endian\":\"3a5a501c9f526505793d3f5c06f4a720b3d1e93ee007b83f61ac7515c4403853\",\"tx_index\":186493886,\"tx_output_n\":0,\"script\":\"76a914aa8b3e5f56605d8b7e4f65120a90bd927f4d0aab88ac\",\"value\":5020,\"value_hex\":\"139c\",\"confirmations\":14299}]}");
        Call<UnspentOutputs> unspentOutputs = Payment.getUnspentCoins(new ArrayList<String>());

        Response<UnspentOutputs> execute = unspentOutputs.execute();

        UnspentOutputs unspent = execute.body();
        Assert.assertEquals(183109705, unspent.getUnspentOutputs().get(0).getTxIndex());
        Assert.assertEquals(186493886, unspent.getUnspentOutputs().get(1).getTxIndex());
    }

    @Test
    public void getCoins_NoFreeOutputs() throws Exception {

        mockInterceptor.setResponseString("No free outputs to spend");
        mockInterceptor.setResponseCode(500);
        Call<UnspentOutputs> unspentOutputs = Payment.getUnspentCoins(new ArrayList<String>());
        Response<UnspentOutputs> exe = unspentOutputs.execute();

        Assert.assertEquals(500, exe.code());
        Assert.assertEquals("No free outputs to spend", exe.errorBody().string());
    }

    @Test
    public void getSweepBundle() throws IOException {

        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);
        Pair<BigInteger, BigInteger> sweepBundle = Payment.getSweepableCoins(unspentOutputs, BigInteger.valueOf(30000L));

        long feeManual = calculateFee(1, UnspentTestData.UNSPENT_OUTPUTS_COUNT, BigInteger.valueOf(30000L));

        Assert.assertEquals(feeManual, sweepBundle.getRight().longValue());
        Assert.assertEquals(UnspentTestData.BALANCE - feeManual, sweepBundle.getLeft().longValue());
    }

    @Test
    public void spendFirstCoin_minusFee_shouldNotExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200l;
        int inputs = 1;
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(0l, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstCoin_minusDust_minusFee_shouldNotExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);

        long consumedAmount = Payment.DUST.longValue();
        long spendAmount = 80200l - consumedAmount;
        int inputs = 1;
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(consumedAmount, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstCoin_minusLessThanDust_minusFee_shouldNotExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);

        long consumedAmount = 300l;
        long spendAmount = 80200l - consumedAmount;
        int inputs = 1;
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(consumedAmount, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstTwoCoins_minusFee_shouldNotExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200l + 70000l;
        int inputs = 2;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(0l, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstTwoCoins_minusDust_minusFee_shouldNotExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);

        long consumedAmount = Payment.DUST.longValue();
        long spendAmount = 80200l + 70000l - consumedAmount;
        int inputs = 2;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(consumedAmount, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstThreeCoins_minusDust_minusFee_shouldNotExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);

        long consumedAmount = Payment.DUST.longValue();
        long spendAmount = 80200l + 70000l + 60000l - consumedAmount;
        int inputs = 3;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(consumedAmount, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstThreeCoins_plusSome_minusFee_shouldExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200l + 70000l + 60000l + 30000l;
        int inputs = 4;//coins
        int outputs = 2;//change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(0l, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstThreeCoins_plusFee_shouldUse4Inputs_AndExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);
        
        long spendAmount = 80200l + 70000l + 60000l;
        int inputs = 4;//coins
        int outputs = 2;//change
        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, BigInteger.valueOf(spendAmount), BigInteger.valueOf(30000L));
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(0l, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendAllCoins_minusFee_shouldUse8Inputs_AndNotExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200l + 70000l + 60000l + 50000l + 40000l + 30000l + 20000l + 10000l;
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));

        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, BigInteger.valueOf(spendAmount - feeManual), BigInteger.valueOf(30000L));

        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(0l, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendAllCoins_minusFee_minusDust_shouldUse8Inputs_AndNotExpectChange() throws IOException{

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);
        
        long spendAmount = 80200l + 70000l + 60000l + 50000l + 40000l + 30000l + 20000l + 10000l - Payment.DUST.longValue();
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));

        PaymentBundle paymentBundle = Payment.getCoinsForPayment(unspentOutputs, BigInteger.valueOf(spendAmount - feeManual), BigInteger.valueOf(30000L));

        Assert.assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        Assert.assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        Assert.assertEquals(Payment.DUST.longValue(), Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void makeTransaction() throws Exception {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);
        Payment payment = new Payment();

        long spendAmount = 80200l + 70000l + 60000l + 50000l + 40000l + 30000l + 20000l + 10000l - Payment.DUST.longValue();
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));

        PaymentBundle paymentBundle = payment
            .getCoinsForPayment(unspentOutputs, BigInteger.valueOf(spendAmount - feeManual), BigInteger.valueOf(30000L));

        List<ECKey> keys = new ArrayList<>();
        String toAddress = "1GYkgRtJmEp355xUtVFfHSFjFdbqjiwKmb";
        String changeAddress = "1GiEQZt9aX2XfDcj14tCC4xAWEJtq9EXW7";
        BigInteger bigIntFee = BigInteger.ZERO;
        BigInteger bigIntAmount = BigInteger.valueOf(
            Coin.parseCoin("0.0001").longValue());

        final HashMap<String, BigInteger> receivers = new HashMap<String, BigInteger>();
        receivers.put(toAddress, bigIntAmount);

        Transaction tx = payment.makeTransaction(
            paymentBundle.getSpendableOutputs(),
            receivers,
            bigIntFee,
            changeAddress);

        Assert.assertEquals("5ee5cb75b364c13fc3c6457be1fd90f58f0b7b2e4f37fcfbd652b90669686420",tx.getHash().toString());
    }

    @Test
    public void signTransaction() throws Exception {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson("{\"unspent_outputs\":[{\"tx_hash\":\"e4fb18c8c8279b3433001b5eed2e1a83588196095512fd1d01f236cda223b9e3\",\"tx_hash_big_endian\":\"e3b923a2cd36f2011dfd125509968158831a2eed5e1b0033349b27c8c818fbe4\",\"tx_index\":218376099,\"tx_output_n\":0,\"script\":\"76a914c27982b0008a2fdb1edd3f663ec554019204ad2e88ac\",\"value\":48916,\"value_hex\":\"00bf14\",\"confirmations\":0}]}");
        Payment payment = new Payment();

        long spendAmount = Payment.DUST.longValue();
        PaymentBundle paymentBundle = payment.getCoinsForPayment(
            unspentOutputs,
            BigInteger.valueOf(spendAmount),
            BigInteger.valueOf(30000L));

        String toAddress = "1NNDb5uQU32CtQnBxnrfvJSjkWcREoFWe7";
        String changeAddress = "1JjHeuviHUxCRGcVXYjt3XTbX8H1qifUt2";
        BigInteger bigIntFee = BigInteger.ZERO;
        BigInteger bigIntAmount = Payment.PUSHTX_MIN;

        final HashMap<String, BigInteger> receivers = new HashMap<String, BigInteger>();
        receivers.put(toAddress, bigIntAmount);

        Transaction tx = payment.makeTransaction(
            paymentBundle.getSpendableOutputs(),
            receivers,
            bigIntFee,
            changeAddress);

        List<ECKey> keys = new ArrayList<>();
        keys.add(new PrivateKeyFactory().getKey(PrivateKeyFactory.WIF_UNCOMPRESSED, "L3wP9Q3gTZ9YwuTuB8nuczhWG9uEXQEE94PTWDZgpVttFzJbKSHL"));

        Assert.assertEquals("393988f87ba7a6be24705d0821d4f61c54b341eda3776cc4f3c4eb7af5f7fa7c", tx.getHashAsString());
        payment.signTransaction(tx, keys);
        Assert.assertEquals("efe67d55f73c187447f7fbae66e6daf126efce20ca9b13897b5e81f8cabee639", tx.getHashAsString());

        mockInterceptor.setResponseString("An outpoint is already spent in [DBBitcoinTx{txIndex=218401014, ip=127.0.0.1, time=1486398252, size=191, distinctIn=null, distinctOut=null, note='null', blockIndexes=[], nTxInput=1, nTxOutput=1}] [OutpointImpl{txIndex=218376099, txOutputN=0}]");
        mockInterceptor.setResponseCode(500);
        Call<ResponseBody> call = payment.publishTransaction(tx);

        Assert.assertTrue(call.execute().errorBody().string().contains("An outpoint is already spent in"));
    }

    @Test
    public void InsufficientMoneyException(){

        UnspentOutputs unspentOutputs = null;
        try {
            unspentOutputs = new UnspentOutputs().fromJson(UnspentTestData.apiResponseString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Payment payment = new Payment();

        long spendAmount = 80200l + 70000l + 60000l + 50000l + 40000l + 30000l + 20000l + 10000l - Payment.DUST.longValue();
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));

        PaymentBundle paymentBundle = null;
        try {
            paymentBundle = payment.getCoinsForPayment(unspentOutputs, BigInteger
                .valueOf(spendAmount - feeManual), BigInteger.valueOf(30000L));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String toAddress = "1GYkgRtJmEp355xUtVFfHSFjFdbqjiwKmb";
        String changeAddress = "1GiEQZt9aX2XfDcj14tCC4xAWEJtq9EXW7";
        BigInteger bigIntFee = BigInteger.ZERO;
        BigInteger bigIntAmount = BigInteger.valueOf(6000000l);

        final HashMap<String, BigInteger> receivers = new HashMap<>();
        receivers.put(toAddress, bigIntAmount);

        try {
            payment.makeTransaction(
                paymentBundle.getSpendableOutputs(),
                receivers,
                bigIntFee,
                changeAddress);
        } catch (InsufficientMoneyException e) {
            Assert.assertTrue(e instanceof InsufficientMoneyException);
            return;
        } catch (AddressFormatException e) {
            Assert.fail();
        }
    }
}