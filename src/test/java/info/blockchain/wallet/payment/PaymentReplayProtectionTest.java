package info.blockchain.wallet.payment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import info.blockchain.api.data.UnspentOutput;
import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.test_data.UnspentTestData;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class PaymentReplayProtectionTest extends MockedResponseTest {

    private Payment subject = new Payment();

    boolean addReplayProtection = true;

    private String getTestData(String file) throws Exception {
        URI uri = getClass().getClassLoader().getResource(file).toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
    }

    private long calculateFee(int outputs, int inputs, BigInteger feePerKb, boolean addSegwit) {
        //Manually calculated fee
        long size = (outputs * 34) + (inputs * 148) + 10;
        if (addSegwit) {
            size = size + Coins.SEGWIT_TX_SIZE_ADAPT;
        }
        double txBytes = ((double) size / 1000.0) ;
        return (long) Math.ceil(feePerKb.doubleValue() * txBytes);
    }

    @Test
    public void getMaximumAvailable_simple() throws Exception {

        UnspentOutputs unspentOutputs = new UnspentOutputs();

        ArrayList<UnspentOutput> list = new ArrayList<>();
        UnspentOutput coin = new UnspentOutput();
        coin.setValue(BigInteger.valueOf(1323));
        list.add(coin);
        unspentOutputs.setUnspentOutputs(list);

        BigInteger feePerKb = BigInteger.valueOf(200);

        Pair<BigInteger, BigInteger> sweepBundle = subject
            .getMaximumAvailable(unspentOutputs, feePerKb, addReplayProtection);

        //Added extra input and output for dust-service
        long feeManual = calculateFee(2, 2, feePerKb, true);

        assertEquals(feeManual, sweepBundle.getRight().longValue());
        //Available would be our amount + fake dust
        assertEquals(1323 + 546 - feeManual, sweepBundle.getLeft().longValue());
    }


    @Test
    public void getSpendableCoins_all_replayable() throws Exception {

        String response = getTestData("unspent/unspent_all_replayable.txt");

        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(response);
        subject = new Payment();

        long spendAmount = 26000L;
        BigInteger feePKb = BigInteger.valueOf(200);
        SpendableUnspentOutputs paymentBundle = subject.getSpendableCoins(
            unspentOutputs,
            BigInteger.valueOf(spendAmount),
            feePKb,
            addReplayProtection);

        List<UnspentOutput> unspentList = paymentBundle.getSpendableOutputs();

        //Descending values (only spend worthy)
        assertEquals(5, unspentList.size());
        assertEquals(546, unspentList.get(0).getValue().intValue());
        assertEquals(8324, unspentList.get(1).getValue().intValue());
        assertEquals(8140, unspentList.get(2).getValue().intValue());
        assertEquals(8139, unspentList.get(3).getValue().intValue());
        assertEquals(6600, unspentList.get(4).getValue().intValue());

        //All replayable except first dust coin
        assertFalse( !unspentList.get(0).isReplayable());
        assertTrue( unspentList.get(1).isReplayable());
        assertTrue( unspentList.get(2).isReplayable());
        assertTrue( unspentList.get(3).isReplayable());
        assertTrue( unspentList.get(4).isReplayable());

        assertFalse(paymentBundle.isReplayProtected());
    }

    @Test
    public void getSpendableCoins_1_non_worthy_non_replayable() throws Exception {

        String response = getTestData("unspent/unspent_1_replayable.txt");

        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(response);
        subject = new Payment();

        long spendAmount = 1500000L;
        SpendableUnspentOutputs paymentBundle = subject.getSpendableCoins(
            unspentOutputs,
            BigInteger.valueOf(spendAmount),
            BigInteger.valueOf(30000L),
            addReplayProtection);

        List<UnspentOutput> unspentList = paymentBundle.getSpendableOutputs();

        //Descending values (only spend worthy)
        assertEquals(7, unspentList.size());
        assertEquals(1323, unspentList.get(0).getValue().intValue());
        assertEquals(8324, unspentList.get(1).getValue().intValue());
        assertEquals(8140, unspentList.get(2).getValue().intValue());
        assertEquals(8139, unspentList.get(3).getValue().intValue());
        assertEquals(6600, unspentList.get(4).getValue().intValue());
        assertEquals(5000, unspentList.get(5).getValue().intValue());
        assertEquals(4947, unspentList.get(6).getValue().intValue());

        //Only first not replayable
        assertFalse( unspentList.get(0).isReplayable());
        assertTrue( unspentList.get(1).isReplayable());
        assertTrue( unspentList.get(2).isReplayable());
        assertTrue( unspentList.get(3).isReplayable());
        assertTrue( unspentList.get(4).isReplayable());
        assertTrue( unspentList.get(5).isReplayable());
        assertTrue( unspentList.get(6).isReplayable());

        assertTrue(paymentBundle.isReplayProtected());
    }

    @Test
    public void getSpendableCoins_3_replayable() throws Exception {

        String response = getTestData("unspent/unspent_3_replayable.txt");

        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(response);
        subject = new Payment();

        long spendAmount = 1500000L;
        SpendableUnspentOutputs paymentBundle = subject.getSpendableCoins(
            unspentOutputs,
            BigInteger.valueOf(spendAmount),
            BigInteger.valueOf(30000L),
            addReplayProtection);

        List<UnspentOutput> unspentList = paymentBundle.getSpendableOutputs();

        //Descending values (only spend worthy)
        assertEquals(6, unspentList.size());
        assertEquals(6600, unspentList.get(0).getValue().intValue());

        assertEquals(8324, unspentList.get(1).getValue().intValue());
        assertEquals(5000, unspentList.get(2).getValue().intValue());
        assertEquals(4947, unspentList.get(3).getValue().intValue());

        assertEquals(8140, unspentList.get(4).getValue().intValue());
        assertEquals(8139, unspentList.get(5).getValue().intValue());

        //First + two last = not replayable
        assertFalse( unspentList.get(0).isReplayable());
        assertTrue( unspentList.get(1).isReplayable());
        assertTrue( unspentList.get(2).isReplayable());
        assertTrue( unspentList.get(3).isReplayable());
        assertFalse( unspentList.get(4).isReplayable());
        assertFalse( unspentList.get(5).isReplayable());

        assertTrue(paymentBundle.isReplayProtected());
    }
}