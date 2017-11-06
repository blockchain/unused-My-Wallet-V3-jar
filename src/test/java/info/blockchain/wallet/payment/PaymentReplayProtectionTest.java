package info.blockchain.wallet.payment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.MockedResponseTest;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

public class PaymentReplayProtectionTest extends MockedResponseTest {

    private Payment subject = new Payment();

    boolean addReplayProtection = true;

    private String getTestData(String file) throws Exception {
        URI uri = getClass().getClassLoader().getResource(file).toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
    }


    @Test
    public void getSpendableCoins_all_replayable() throws Exception {

        String response = getTestData("unspent/unspent_all_replayable.txt");

        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(response);
        subject = new Payment();

        long spendAmount = 1500000L;
        SpendableUnspentOutputs paymentBundle = subject.getSpendableCoins(
            unspentOutputs,
            BigInteger.valueOf(spendAmount),
            BigInteger.valueOf(30000L),
            addReplayProtection);

        assertFalse(paymentBundle.isReplayProtected());
    }

    @Test
    public void getSpendableCoins_1_replayable() throws Exception {

        String response = getTestData("unspent/unspent_1_replayable.txt");

        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(response);
        subject = new Payment();

        long spendAmount = 1500000L;
        SpendableUnspentOutputs paymentBundle = subject.getSpendableCoins(
            unspentOutputs,
            BigInteger.valueOf(spendAmount),
            BigInteger.valueOf(30000L),
            addReplayProtection);

        assertTrue(!paymentBundle.isReplayProtected());
        assertEquals(8324, paymentBundle.getSpendableOutputs().get(0).getValue().intValue());
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

        assertTrue(!paymentBundle.isReplayProtected());
        assertEquals(8324, paymentBundle.getSpendableOutputs().get(0).getValue().intValue());
    }
}