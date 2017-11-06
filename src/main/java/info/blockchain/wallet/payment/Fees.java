package info.blockchain.wallet.payment;

import java.math.BigInteger;

class Fees {

    private static final int ESTIMATED_INPUT_LEN = 148; // compressed key
    private static final int ESTIMATED_OUTPUT_LEN = 34;

    public static BigInteger estimatedFee(int inputs, int outputs, BigInteger feePerKb) {

        int size = estimatedSize(inputs, outputs);
        return calculateFee(size, feePerKb);
    }

    public static int estimatedSize(int inputs, int outputs) {
        return (outputs * ESTIMATED_OUTPUT_LEN) + (inputs * ESTIMATED_INPUT_LEN) + 10;
    }

    public static BigInteger calculateFee(int size, BigInteger feePerKb) {

        double txBytes = ((double) size / 1000.0);
        long absoluteFee = (long) Math.ceil(feePerKb.doubleValue() * txBytes);
        return BigInteger.valueOf(absoluteFee);
    }

    public static boolean isAdequateFee(int inputs, int outputs, BigInteger absoluteFee) {

        double txBytes = ((double) estimatedSize(inputs, outputs) / 1000.0);
        long feePerkb = (long) Math.ceil(absoluteFee.doubleValue() / txBytes);
        return feePerkb > Payment.PUSHTX_MIN.longValue();
    }
}