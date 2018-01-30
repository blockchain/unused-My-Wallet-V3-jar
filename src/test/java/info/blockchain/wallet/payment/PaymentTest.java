package info.blockchain.wallet.payment;

import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.test_data.UnspentTestData;
import info.blockchain.wallet.util.PrivateKeyFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.BitcoinCashMainNetParams;
import org.bitcoinj.params.BitcoinMainNetParams;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PaymentTest extends MockedResponseTest {

    private Payment subject = new Payment();
    private NetworkParameters bitcoinMainNetParams = BitcoinMainNetParams.get();
    private NetworkParameters bitcoinCashMainNetParams = BitcoinCashMainNetParams.get();

    private String getTestData(String file) throws Exception {
        URI uri = getClass().getClassLoader().getResource(file).toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
    }

    @Test
    public void estimatedFee() {

        ArrayList<int[]> cases = new ArrayList<>();
        //new int[]{[--inputs--],[--outputs--],[--feePrKb--],[--absoluteFee--]}
        cases.add(new int[]{1, 1, 0, 0});
        cases.add(new int[]{1, 2, 0, 0});
        cases.add(new int[]{2, 1, 0, 0});
        cases.add(new int[]{1, 1, 30000, 5760});
        cases.add(new int[]{1, 2, 30000, 6780});
        cases.add(new int[]{2, 1, 30000, 10200});
        cases.add(new int[]{3, 3, 30000, 16680});
        cases.add(new int[]{5, 10, 30000, 32701});

        for (int[] aCase : cases) {

            int inputs = aCase[0];
            int outputs = aCase[1];

            BigInteger absoluteFee = subject.estimatedFee(inputs, outputs, BigInteger.valueOf(aCase[2]));
            assert (aCase[3] == absoluteFee.longValue());
        }
    }

    @Test
    public void estimatedSize() {
        assertEquals(192, subject.estimatedSize(1, 1));
        assertEquals(226, subject.estimatedSize(1, 2));
        assertEquals(340, subject.estimatedSize(2, 1));
        assertEquals(374, subject.estimatedSize(2, 2));
    }

    @Test
    public void isAdequateFee() {
        assertTrue(subject.isAdequateFee(1, 1, BigInteger.valueOf(193)));
        assertFalse(subject.isAdequateFee(1, 1, BigInteger.valueOf(192)));
    }

    private long calculateFee(int outputs, int inputs, BigInteger feePerKb) {
        //Manually calculated fee
        long size = (outputs * 34) + (inputs * 148) + 10;//36840L
        double txBytes = ((double) size / 1000.0);
        return (long) Math.ceil(feePerKb.doubleValue() * txBytes);
    }

    @Test
    public void getCoins_2FreeOutputs() throws Exception {

        mockInterceptor.setResponseString("{\"unspent_outputs\":[{\"tx_hash\":\"70249ac0178e498fd368048273195f4649aff7fef712b1dd1c1cab50e503c461\",\"tx_hash_big_endian\":\"61c403e550ab1c1cddb112f7fef7af49465f1973820468d38f498e17c09a2470\",\"tx_index\":183109705,\"tx_output_n\":0,\"script\":\"76a914aa8b3e5f56605d8b7e4f65120a90bd927f4d0aab88ac\",\"value\":3264,\"value_hex\":\"0cc0\",\"confirmations\":16377},{\"tx_hash\":\"533840c41575ac613fb807e03ee9d1b320a7f4065c3f3d790565529f1c505a3a\",\"tx_hash_big_endian\":\"3a5a501c9f526505793d3f5c06f4a720b3d1e93ee007b83f61ac7515c4403853\",\"tx_index\":186493886,\"tx_output_n\":0,\"script\":\"76a914aa8b3e5f56605d8b7e4f65120a90bd927f4d0aab88ac\",\"value\":5020,\"value_hex\":\"139c\",\"confirmations\":14299}]}");
        Call<UnspentOutputs> unspentOutputs = subject.getUnspentCoins(new ArrayList<String>());

        Response<UnspentOutputs> execute = unspentOutputs.execute();

        UnspentOutputs unspent = execute.body();
        assertEquals(183109705, unspent.getUnspentOutputs().get(0).getTxIndex());
        assertEquals(186493886, unspent.getUnspentOutputs().get(1).getTxIndex());
    }

    @Test
    public void getCoins_NoFreeOutputs() throws Exception {

        mockInterceptor.setResponseString("No free outputs to spend");
        mockInterceptor.setResponseCode(500);
        Call<UnspentOutputs> unspentOutputs = subject.getUnspentCoins(new ArrayList<String>());
        Response<UnspentOutputs> exe = unspentOutputs.execute();

        assertEquals(500, exe.code());
        assertEquals("No free outputs to spend", exe.errorBody().string());
    }

    @Test
    public void getMaximumAvailable() throws IOException {

        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);
        Pair<BigInteger, BigInteger> sweepBundle = subject
                .getMaximumAvailable(unspentOutputs, BigInteger.valueOf(30000L));

//        long feeManual = calculateFee(1, UnspentTestData.UNSPENT_OUTPUTS_COUNT, BigInteger.valueOf(30000L));

        //Assume 2 outputs to line up with web. Not 100% correct but acceptable to
        //keep values across platforms constant.
        long feeManual = calculateFee(2, UnspentTestData.UNSPENT_OUTPUTS_COUNT, BigInteger.valueOf(30000L));

        assertEquals(feeManual, sweepBundle.getRight().longValue());
        assertEquals(UnspentTestData.BALANCE - feeManual, sweepBundle.getLeft().longValue());
    }

    @Test
    public void spendFirstCoin_minusFee_shouldNotExpectChange() throws IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200L;
        int inputs = 1;
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(0L, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstCoin_minusDust_minusFee_shouldNotExpectChange() throws IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long consumedAmount = Payment.DUST.longValue();
        long spendAmount = 80200L - consumedAmount;
        int inputs = 1;
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(consumedAmount, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstCoin_minusLessThanDust_minusFee_shouldNotExpectChange() throws
            IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long consumedAmount = 300L;
        long spendAmount = 80200L - consumedAmount;
        int inputs = 1;
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(consumedAmount, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstTwoCoins_minusFee_shouldNotExpectChange() throws IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200L + 70000L;
        int inputs = 2;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(0L, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstTwoCoins_minusDust_minusFee_shouldNotExpectChange() throws IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long consumedAmount = Payment.DUST.longValue();
        long spendAmount = 80200L + 70000L - consumedAmount;
        int inputs = 2;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(consumedAmount, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstThreeCoins_minusDust_minusFee_shouldNotExpectChange() throws IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long consumedAmount = Payment.DUST.longValue();
        long spendAmount = 80200L + 70000L + 60000L - consumedAmount;
        int inputs = 3;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(consumedAmount, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstThreeCoins_plusSome_minusFee_shouldExpectChange() throws IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200L + 70000L + 60000L + 30000L;
        int inputs = 4;//coins
        int outputs = 2;//change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        BigInteger spendAmountMinusFee = BigInteger.valueOf(spendAmount - feeManual);
        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, spendAmountMinusFee, BigInteger.valueOf(30000L));

        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(0L, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendFirstThreeCoins_plusFee_shouldUse4Inputs_AndExpectChange() throws IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200L + 70000L + 60000L;
        int inputs = 4;//coins
        int outputs = 2;//change
        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, BigInteger.valueOf(spendAmount),
                        BigInteger.valueOf(30000L));
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));
        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(0L, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendAllCoins_minusFee_shouldUse8Inputs_AndNotExpectChange() throws IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200L + 70000L + 60000L + 50000L + 40000L + 30000L + 20000L + 10000L;
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));

        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, BigInteger.valueOf(spendAmount - feeManual),
                        BigInteger.valueOf(30000L));

        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(0L, Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void spendAllCoins_minusFee_minusDust_shouldUse8Inputs_AndNotExpectChange() throws
            IOException {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);

        long spendAmount = 80200L + 70000L + 60000L + 50000L + 40000L + 30000L + 20000L + 10000L - Payment.DUST.longValue();
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));

        SpendableUnspentOutputs paymentBundle = subject
                .getSpendableCoins(unspentOutputs, BigInteger.valueOf(spendAmount - feeManual),
                        BigInteger.valueOf(30000L));

        assertEquals(inputs, paymentBundle.getSpendableOutputs().size());
        assertEquals(feeManual, paymentBundle.getAbsoluteFee().longValue());
        assertEquals(Payment.DUST.longValue(), Math.abs(paymentBundle.getConsumedAmount().longValue()));
    }

    @Test
    public void makeTransaction() throws Exception {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);
        Payment payment = new Payment();

        long spendAmount = 80200L + 70000L + 60000L + 50000L + 40000L + 30000L + 20000L + 10000L - Payment.DUST.longValue();
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));

        SpendableUnspentOutputs paymentBundle = payment
                .getSpendableCoins(unspentOutputs, BigInteger.valueOf(spendAmount - feeManual),
                        BigInteger.valueOf(30000L));

        String toAddress = "1GYkgRtJmEp355xUtVFfHSFjFdbqjiwKmb";
        String changeAddress = "1GiEQZt9aX2XfDcj14tCC4xAWEJtq9EXW7";
        BigInteger bigIntFee = BigInteger.ZERO;
        BigInteger bigIntAmount = BigInteger.valueOf(
                Coin.parseCoin("0.0001").longValue());

        final HashMap<String, BigInteger> receivers = new HashMap<>();
        receivers.put(toAddress, bigIntAmount);

        Transaction tx = payment.makeSimpleTransaction(bitcoinMainNetParams,
                paymentBundle.getSpendableOutputs(),
                receivers,
                bigIntFee,
                changeAddress);

        assertEquals("5ee5cb75b364c13fc3c6457be1fd90f58f0b7b2e4f37fcfbd652b90669686420", tx.getHash().toString());
    }

    @Test
    public void signTransaction() throws Exception {

        /*
        8 available Payment. [80200,70000,60000,50000,40000,30000,20000,10000]
         */
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson("{\"unspent_outputs\":[{\"tx_hash\":\"e4fb18c8c8279b3433001b5eed2e1a83588196095512fd1d01f236cda223b9e3\",\"tx_hash_big_endian\":\"e3b923a2cd36f2011dfd125509968158831a2eed5e1b0033349b27c8c818fbe4\",\"tx_index\":218376099,\"tx_output_n\":0,\"script\":\"76a914c27982b0008a2fdb1edd3f663ec554019204ad2e88ac\",\"value\":48916,\"value_hex\":\"00bf14\",\"confirmations\":0}]}");
        Payment payment = new Payment();

        long spendAmount = Payment.DUST.longValue();
        SpendableUnspentOutputs paymentBundle = payment.getSpendableCoins(
                unspentOutputs,
                BigInteger.valueOf(spendAmount),
                BigInteger.valueOf(30000L));

        String toAddress = "1NNDb5uQU32CtQnBxnrfvJSjkWcREoFWe7";
        String changeAddress = "1JjHeuviHUxCRGcVXYjt3XTbX8H1qifUt2";
        BigInteger bigIntFee = BigInteger.ZERO;
        BigInteger bigIntAmount = Payment.PUSHTX_MIN;

        final HashMap<String, BigInteger> receivers = new HashMap<>();
        receivers.put(toAddress, bigIntAmount);

        Transaction tx = payment.makeSimpleTransaction(bitcoinMainNetParams,
                paymentBundle.getSpendableOutputs(),
                receivers,
                bigIntFee,
                changeAddress);

        List<ECKey> keys = new ArrayList<>();
        keys.add(new PrivateKeyFactory().getKey(PrivateKeyFactory.WIF_UNCOMPRESSED, "L3wP9Q3gTZ9YwuTuB8nuczhWG9uEXQEE94PTWDZgpVttFzJbKSHL"));

        assertEquals("393988f87ba7a6be24705d0821d4f61c54b341eda3776cc4f3c4eb7af5f7fa7c", tx.getHashAsString());
        payment.signSimpleTransaction(bitcoinMainNetParams, tx, keys);
        assertEquals("efe67d55f73c187447f7fbae66e6daf126efce20ca9b13897b5e81f8cabee639", tx.getHashAsString());

        mockInterceptor.setResponseString("An outpoint is already spent in [DBBitcoinTx{txIndex=218401014, ip=127.0.0.1, time=1486398252, size=191, distinctIn=null, distinctOut=null, note='null', blockIndexes=[], nTxInput=1, nTxOutput=1}] [OutpointImpl{txIndex=218376099, txOutputN=0}]");
        mockInterceptor.setResponseCode(500);
        Call<ResponseBody> call = payment.publishSimpleTransaction(tx);

        assertTrue(call.execute().errorBody().string().contains("An outpoint is already spent in"));
    }

    @Test(expected = InsufficientMoneyException.class)
    public void InsufficientMoneyException() throws Exception {

        UnspentOutputs unspentOutputs = null;
        try {
            unspentOutputs = UnspentOutputs.fromJson(UnspentTestData.apiResponseString);
        } catch (IOException e) {
            fail();
        }
        Payment payment = new Payment();

        long spendAmount = 80200L + 70000L + 60000L + 50000L + 40000L + 30000L + 20000L + 10000L - Payment.DUST.longValue();
        int inputs = 8;//coins
        int outputs = 1;//no change
        long feeManual = calculateFee(outputs, inputs, BigInteger.valueOf(30000L));

        SpendableUnspentOutputs paymentBundle = payment.getSpendableCoins(unspentOutputs, BigInteger
                .valueOf(spendAmount - feeManual), BigInteger.valueOf(30000L));

        String toAddress = "1GYkgRtJmEp355xUtVFfHSFjFdbqjiwKmb";
        String changeAddress = "1GiEQZt9aX2XfDcj14tCC4xAWEJtq9EXW7";
        BigInteger bigIntFee = BigInteger.ZERO;
        BigInteger bigIntAmount = BigInteger.valueOf(6000000L);

        final HashMap<String, BigInteger> receivers = new HashMap<>();
        receivers.put(toAddress, bigIntAmount);

        payment.makeSimpleTransaction(bitcoinMainNetParams,
                paymentBundle.getSpendableOutputs(),
                receivers,
                bigIntFee,
                changeAddress);
    }

    @Test
    public void signBCHTransaction() throws Exception {

        //Arrange
        subject = new Payment();

        String unspentApiResponse = getTestData("transaction/bch_unspent_output.txt");
        UnspentOutputs unspentOutputs = UnspentOutputs.fromJson(unspentApiResponse);

        BigInteger sweepable = BigInteger.valueOf(327036L);
        BigInteger aboluteFee = BigInteger.valueOf(27);

        SpendableUnspentOutputs paymentBundle = subject.getSpendableCoins(unspentOutputs,
                sweepable,
                aboluteFee);

        String receiveAddress = "1JEggWq9VVaVnDmdTbYuHmJXN4icdF89Kq";

        final HashMap<String, BigInteger> receivers = new HashMap<>();
        receivers.put(receiveAddress, sweepable);

        Transaction tx = subject.makeSimpleTransaction(bitcoinCashMainNetParams,
                paymentBundle.getSpendableOutputs(),
                receivers,
                aboluteFee,
                null);

        Assert.assertEquals("0100000003cf759867fb887f188d4207f2f79582ed25ee00b9244f37a3e7555c13e1577a550000000000ffffffff5f2061d611a866145b99d75ac4d0885d399c7904b2851e443d0a19fddab86b8e0000000000ffffffff15261589a6a5d95842306db374b3a3edf77c3acfc46f77c023187b1830d5f7920100000000ffffffff017cfd0400000000001976a914bd10ab8b35f4343aa9c083d2b6217f2f33f1321288ac00000000",
                Hex.toHexString(tx.bitcoinSerialize()));
        Assert.assertEquals("e3b0013c04cb7169037d9c38065a852ba0de24a929e87e299c2fe773e3815f5d",
                tx.getHashAsString());

        List<ECKey> keys = new ArrayList<>();
        keys.add(new PrivateKeyFactory().getKey(PrivateKeyFactory.WIF_COMPRESSED, "Kyf1r2iNDikTTGEemDtsGP4jgcSqyYupsc4Rs2jQqHgwUZJNoyHK"));
        keys.add(new PrivateKeyFactory().getKey(PrivateKeyFactory.WIF_COMPRESSED, "L57gn4CnJMdJAiaKXwRL9zFGNqcrvJT4mSJLXPdkFwPBYdvfYLAG"));
        keys.add(new PrivateKeyFactory().getKey(PrivateKeyFactory.WIF_COMPRESSED, "KyGNbFSewezfpHfghxFfyoj3uscpFjLKih75PyU1ZoRkMwPuSWjb"));

        //Act
        subject.signBCHTransaction(bitcoinCashMainNetParams, tx, keys);

        //Assert
        Assert.assertEquals("0100000003cf759867fb887f188d4207f2f79582ed25ee00b9244f37a3e7555c13e1577a55000000006a473044022069e5f3d471baace21221038888a9594b875a3b37724e0fa1b0c5800b1fddb85d022051226d123de59e62ea17dfc11fc7beef625e977063507939997b0a44bfd702d94121034eeeae0afd407733476ec3b6d729ffaae408ffd678eeba8c4b8fd2fb4b716f87ffffffff5f2061d611a866145b99d75ac4d0885d399c7904b2851e443d0a19fddab86b8e000000006b483045022100a939cba6701f55499f4b8b8f777d820eaa626ab559a8752039b6c3717e297fc7022068d025a99b41886d6228efd0fdb57a039ec72362567cab77b792f565bd7912c841210248eb68f88e4a90df7159887c0acdb888c643d13fd2df45c3b4c45414f11d7635ffffffff15261589a6a5d95842306db374b3a3edf77c3acfc46f77c023187b1830d5f792010000006b483045022100b14958c39c42d20f52e36ba9f3fb8d6b7e97528ef5ea793bd4f108cb2d5f1cb9022002f3f9dcaca4de2f8c9a60527edcbd6e0740fd1031ee781197de46064d6f60dd412103183db6bf9edfa63716905fa267546ffd59647cc00448a7a6aa6c0c44bf4d0a87ffffffff017cfd0400000000001976a914bd10ab8b35f4343aa9c083d2b6217f2f33f1321288ac00000000",
                Hex.toHexString(tx.bitcoinSerialize()));
        Assert.assertEquals("e93cf0fb44ff3c3d9f2cde59d0a69a3b3daac0f7991f92bc8389f33c9a55f762",
                tx.getHashAsString());
    }
}