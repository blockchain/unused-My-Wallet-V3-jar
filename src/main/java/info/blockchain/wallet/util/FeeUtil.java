package info.blockchain.wallet.util;

import info.blockchain.wallet.send.MyTransactionOutPoint;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

/**
 * @deprecated use {@link info.blockchain.util.FeeUtil} instead.
 */
@Deprecated
public class FeeUtil {

    private static final int ESTIMATED_INPUT_LEN = 148; // compressed key
    private static final int ESTIMATED_OUTPUT_LEN = 34;

//    private static BigInteger bAvgFee = null;
//    private static double dPriorityMultiplier = 1.5;

    private static FeeUtil instance = null;

    public static final BigInteger AVERAGE_FEE = BigInteger.valueOf(Coin.parseCoin("0.0001").longValue());
    public static final BigInteger AVERAGE_FEE_PER_KB = BigInteger.valueOf(Coin.parseCoin("0.0003").longValue());

    //
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

    //
    // use signed tx here
    //
    public static BigInteger calculatedFee(Transaction tx, BigInteger feePerKb)   {

        String hexString = new String(Hex.encode(tx.bitcoinSerialize()));
        int size = hexString.length();

        return feeCalculation(size, feePerKb);
    }

    //
    // use unsigned tx here
    //
    public static BigInteger estimatedFee(Transaction tx, BigInteger feePerKb)   {

        int size = estimatedSize(tx.getOutputs().size(), tx.getInputs().size());

        return feeCalculation(size, feePerKb);
    }

    public static BigInteger estimatedFee(int inputs, int outputs, BigInteger feePerKb)   {

        int size = estimatedSize(inputs, outputs);
        return feeCalculation(size, feePerKb);
    }

    private static BigInteger feeCalculation(int size, BigInteger feePerKb)   {

        double txBytes = ((double)size / 1000.0);
        long fee = (long)Math.ceil(feePerKb.doubleValue() * txBytes);
        return BigInteger.valueOf(fee);
    }

    private static int estimatedSize(int inputs, int outputs)   {
        return (outputs * ESTIMATED_OUTPUT_LEN) + (inputs * ESTIMATED_INPUT_LEN) + 10;
    }

//    private static BigInteger calcPriority()   {
//        return BigInteger.valueOf((long)Math.round(bAvgFee.doubleValue() * dPriorityMultiplier));
//    }

    public static JSONObject getDynamicFee(){

        JSONObject resultJson = null;

        try {
            String result =  WebUtil.getInstance().getURL(WebUtil.DYNAMIC_FEE);

            if(result != null) {
                resultJson = new JSONObject(result);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return resultJson;
    }
}
