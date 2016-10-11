package info.blockchain.util;

import info.blockchain.wallet.send.SendCoins;

import org.bitcoinj.core.Coin;

import java.math.BigInteger;

public class FeeUtil {

    private static final int ESTIMATED_INPUT_LEN = 148; // compressed key
    private static final int ESTIMATED_OUTPUT_LEN = 34;

    public static final BigInteger AVERAGE_ABSOLUTE_FEE = BigInteger.valueOf(Coin.parseCoin("0.0001").longValue());
    public static final BigInteger AVERAGE_FEE_PER_KB = BigInteger.valueOf(Coin.parseCoin("0.0003").longValue());

    public static BigInteger estimatedFee(int inputs, int outputs, BigInteger feePerKb) {

        int size = estimatedSize(inputs, outputs);
        return calculateFee(size, feePerKb);
    }

    public static int estimatedSize(int inputs, int outputs) {
        return (outputs * ESTIMATED_OUTPUT_LEN) + (inputs * ESTIMATED_INPUT_LEN) + 10;
    }

    private static BigInteger calculateFee(int size, BigInteger feePerKb) {

        double txBytes = ((double) size / 1000.0);
        long absoluteFee = (long) Math.ceil(feePerKb.doubleValue() * txBytes);
        return BigInteger.valueOf(absoluteFee);
    }

    public static boolean isAdequateFee(int inputs, int outputs, BigInteger absoluteFee) {

        double txBytes = ((double) estimatedSize(inputs, outputs) / 1000.0);
        long feePerkb = (long) Math.ceil(absoluteFee.doubleValue() / txBytes);
        return feePerkb > SendCoins.bMinimumFeePerKb.longValue();
    }

    /*
    // Future use
    // use unsigned tx here
    //
    public static long getPriority(Transaction tx, List<MyTransactionOutPoint> outputs)   {

        long priority = 0L;

        for(MyTransactionOutPoint output : outputs)   {
            priority += output.getValue().longValue() * output.getConfirmations();
        }
        //
        // calculate priority
        //
        long estimatedSize = tx.bitcoinSerialize().length + (114 * tx.getInputs().size());
        priority /= estimatedSize;

        return priority;
    }
     */
}