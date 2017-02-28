package info.blockchain.wallet.payment;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.UnspentOutput;
import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.BlockchainFramework;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.script.Script;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Call;

class Coins {

    public static synchronized Call<UnspentOutputs> getUnspentCoins(List<String> addresses) throws IOException {
        BlockExplorer blockExplorer = new BlockExplorer(BlockchainFramework.getRetrofitServerInstance(), BlockchainFramework.getApiCode());
        return blockExplorer.getUnspentOutputs(addresses);
    }

    public static synchronized Pair<BigInteger, BigInteger> getSweepableCoins(UnspentOutputs coins, BigInteger feePerKb){

        BigInteger sweepBalance = BigInteger.ZERO;
        BigInteger sweepFee;

        ArrayList<UnspentOutput> unspentOutputs = coins.getUnspentOutputs();

        Collections.sort(unspentOutputs, new UnspentOutputAmountComparator());

        ArrayList<UnspentOutput> usableCoins = new ArrayList<UnspentOutput>();

        double inputCost = inputCost(feePerKb);

        for (UnspentOutput output : unspentOutputs) {

            //Filter usable coins
            if (output.getValue().doubleValue() >= inputCost) {
                usableCoins.add(output);
                sweepBalance = sweepBalance.add(output.getValue());
            }
        }

        //All inputs, 1 output = no change
        sweepFee = Fees.estimatedFee(usableCoins.size(), 1, feePerKb);
        sweepBalance = sweepBalance.subtract(sweepFee);

        sweepBalance = BigInteger.valueOf(Math.max(sweepBalance.longValue(), 0));

        return Pair.of(sweepBalance, sweepFee);
    }

    public static synchronized SpendableUnspentOutputs getMinimumCoinsForPayment(UnspentOutputs coins, BigInteger paymentAmount, BigInteger feePerKb)
        throws UnsupportedEncodingException {

        List<UnspentOutput> unspentOutputs = coins.getUnspentOutputs();
        List<UnspentOutput> spendWorthyList = new ArrayList<>();

        // Descending order - Select the minimum number of outputs necessary
        Collections.sort(unspentOutputs, new UnspentOutputAmountComparator());

        BigInteger collectedAmount = BigInteger.ZERO;
        BigInteger consumedAmount = BigInteger.ZERO;

        double inputCost = inputCost(feePerKb);

        int outputCount = 2;//initially assume change

        for (UnspentOutput output : coins.getUnspentOutputs()) {

            // Skip coins not worth spending
            if (output.getValue().doubleValue() < inputCost) {
                continue;
            }

            //Skip script with no type
            Script script = new Script(Hex.decode(output.getScript().getBytes()));
            if (script.getScriptType() == Script.ScriptType.NO_TYPE) {
                continue;
            }

            //Collect coin
            spendWorthyList.add(output);
            collectedAmount = collectedAmount.add(output.getValue());

            //Fee
            int coinCount = spendWorthyList.size();
            BigInteger paymentAmountNoChange = estimateAmount(coinCount, paymentAmount, feePerKb, 1);
            BigInteger paymentAmountWithChange = estimateAmount(coinCount, paymentAmount, feePerKb, 2);

            //No change = 1 output (Exact amount)
            if (paymentAmountNoChange.compareTo(collectedAmount) == 0) {
                outputCount = 1;
                break;
            }

            //No change = 1 output (Don't allow dust to be sent back as change - consume it rather)
            if (paymentAmountNoChange.compareTo(collectedAmount) == -1
                && paymentAmountNoChange.compareTo(collectedAmount.subtract(Payment.DUST)) >= 0) {
                consumedAmount = consumedAmount.add(paymentAmountNoChange.subtract(collectedAmount));
                outputCount = 1;
                break;
            }

            //Expect change = 2 outputs
            if (collectedAmount.compareTo(paymentAmountWithChange) >= 0) {
                outputCount = 2;//[multiple inputs, 2 outputs] - assume change
                break;
            }
        }

        SpendableUnspentOutputs paymentBundle = new SpendableUnspentOutputs();
        paymentBundle.setSpendableOutputs(spendWorthyList);
        paymentBundle.setAbsoluteFee(Fees.estimatedFee(spendWorthyList.size(), outputCount, feePerKb));
        paymentBundle.setConsumedAmount(consumedAmount);
        return paymentBundle;
    }

    private static BigInteger estimateAmount(int CoinCount, BigInteger paymentAmount, BigInteger feePerKb, int outputCount){
        BigInteger fee = Fees.estimatedFee(CoinCount, outputCount, feePerKb);
        return paymentAmount.add(fee);
    }

    private static double inputCost(BigInteger feePerKb) {
        double d = Math.ceil(feePerKb.doubleValue() * 0.148);
        return Math.ceil(d);
    }

    /**
     * Sort unspent outputs by amount in descending order.
     */
    private static class UnspentOutputAmountComparator implements Comparator<UnspentOutput> {

        public int compare(UnspentOutput o1, UnspentOutput o2) {

            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            int ret;

            if (o1.getValue().compareTo(o2.getValue()) > 0) {
                ret = BEFORE;
            } else if (o1.getValue().compareTo(o2.getValue()) < 0) {
                ret = AFTER;
            } else {
                ret = EQUAL;
            }

            return ret;
        }

    }
}
