package info.blockchain.util;

import org.bitcoinj.core.Coin;

import java.math.BigInteger;

public class FeeUtil  {

    private static final int ESTIMATED_INPUT_LEN = 148; // compressed key
    private static final int ESTIMATED_OUTPUT_LEN = 34;

    public static final BigInteger AVERAGE_ABSOLUTE_FEE = BigInteger.valueOf(Coin.parseCoin("0.0001").longValue());
    public static final BigInteger AVERAGE_FEE_PER_KB = BigInteger.valueOf(Coin.parseCoin("0.0003").longValue());

    public static BigInteger estimatedFee(int inputs, int outputs, BigInteger feePerKb)   {

        int size = estimatedSize(inputs, outputs);
        return calculateFee(size, feePerKb);
    }

    private static int estimatedSize(int inputs, int outputs)   {
        return (outputs * ESTIMATED_OUTPUT_LEN) + (inputs * ESTIMATED_INPUT_LEN) + 10;
    }

    private static BigInteger calculateFee(int size, BigInteger feePerKb)   {

        double txBytes = ((double)size / 1000.0);
        long fee = (long)Math.ceil(feePerKb.doubleValue() * txBytes);
        return BigInteger.valueOf(fee);
    }
}