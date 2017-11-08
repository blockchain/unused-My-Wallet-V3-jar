package info.blockchain.wallet.payment;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.UnspentOutput;
import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.BlockchainFramework;

import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.Script.ScriptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Call;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class Coins {

    private static final Logger log = LoggerFactory.getLogger(Coins.class);

    public static final int SEGWIT_TX_SIZE_ADAPT = 150; //Size added to combined tx using dust-service to approximate fee

    public static Call<UnspentOutputs> getUnspentCoins(List<String> addresses) throws IOException {
        log.info("Fetching unspent coins");
        BlockExplorer blockExplorer = new BlockExplorer(BlockchainFramework.getRetrofitExplorerInstance(), BlockchainFramework.getApiCode());
        return blockExplorer.getUnspentOutputs(addresses);
    }

    /**
     *
     * @param coins
     * @param feePerKb
     * @param addReplayProtection If true. Include at least one non-replayable output or add a dust output from dust-service api.
     * @return
     */
    public static Pair<BigInteger, BigInteger> getMaximumAvailable(UnspentOutputs coins,
        BigInteger feePerKb, boolean addReplayProtection) {

        BigInteger sweepBalance = BigInteger.ZERO;
        BigInteger sweepFee;

        ArrayList<UnspentOutput> unspentOutputs;

        //Sort inputs
        if (addReplayProtection) {
            unspentOutputs = getSortedCoins(coins.getUnspentOutputs());
        } else {
            unspentOutputs = coins.getUnspentOutputs();
            Collections.sort(unspentOutputs, new UnspentOutputAmountComparatorDesc());
        }

        ArrayList<UnspentOutput> usableCoins = new ArrayList<>();

        double inputCost = inputCost(feePerKb);

        //1st input will be non-replayable if possible
        boolean hasReplayProtection = !unspentOutputs.get(0).isReplayable();

        if(addReplayProtection && !hasReplayProtection) {
            log.info("Calculating maximum available with non-replayable dust included.");
            unspentOutputs.add(0, getMockDustCoin());
        }

        for (int i = 0; i < unspentOutputs.size(); i++) {

            UnspentOutput output = unspentOutputs.get(i);

            //Filter usable coins
            if (output.isForceInclude() || output.getValue().doubleValue() >= inputCost) {
                usableCoins.add(output);
                sweepBalance = sweepBalance.add(output.getValue());
            }
        }

        //All inputs, 1 output = no change. (Correct way)
        //sweepFee = Fees.estimatedFee(usableCoins.size(), 1, feePerKb);

        //Assume 2 outputs to line up with web. Not 100% correct but acceptable to
        //keep values across platforms constant.
        int outputCount = 2;
        int inputCount = usableCoins.size();

        if(addReplayProtection && !hasReplayProtection) {
            log.info("Modifying tx size for segwit fee calculation.");
            int size = Fees.estimatedSize(inputCount, outputCount) + SEGWIT_TX_SIZE_ADAPT;
            sweepFee = Fees.calculateFee(size, feePerKb);
        } else {
            sweepFee = Fees.estimatedFee(inputCount, outputCount, feePerKb);
        }

        sweepBalance = sweepBalance.subtract(sweepFee);

        sweepBalance = BigInteger.valueOf(Math.max(sweepBalance.longValue(), 0));

        log.info("Filtering sweepable coins. Sweepable Balance = {}, Fee required for sweep = {}", sweepBalance, sweepFee);
        return Pair.of(sweepBalance, sweepFee);
    }

    /**
     * Sort in order - 1 smallest non-replayable coin, descending replayable, descending non-relayable
     * @param unspentOutputs
     * @return
     */
    private static ArrayList<UnspentOutput> getSortedCoins(
        ArrayList<UnspentOutput> unspentOutputs) {

        ArrayList<UnspentOutput> sortedCoins = new ArrayList<>();

        //Select 1 smallest non-replayable coin
        Collections.sort(unspentOutputs, new UnspentOutputAmountComparatorAsc());
        for(UnspentOutput coin : unspentOutputs) {
            if (!coin.isReplayable()) {
                coin.setForceInclude(true);
                sortedCoins.add(coin);
                break;
            }
        }

        //Descending value. Add all replayable coins.
        Collections.reverse(unspentOutputs);
        for(UnspentOutput coin : unspentOutputs) {
            if (!sortedCoins.contains(coin) && coin.isReplayable()) {
                sortedCoins.add(coin);
            }
        }

        //Still descending. Add all non-replayable coins.
        for(UnspentOutput coin : unspentOutputs) {
            if (!sortedCoins.contains(coin) && !coin.isReplayable()) {
                sortedCoins.add(coin);
            }
        }

        return sortedCoins;
    }

    /**
     *
     * @param coins
     * @param paymentAmount
     * @param feePerKb
     * @param addReplayProtection If true: Add at least one non-replayable output or add a dust output from dust-service api.
     * @return
     */
    public static SpendableUnspentOutputs getMinimumCoinsForPayment(UnspentOutputs coins,
        BigInteger paymentAmount, BigInteger feePerKb, boolean addReplayProtection) {

        log.info("Select the minimum number of outputs necessary for payment");
        List<UnspentOutput> spendWorthyList = new ArrayList<>();

        ArrayList<UnspentOutput> unspentOutputs;

        //Sort inputs
        if (addReplayProtection) {
            unspentOutputs = getSortedCoins(coins.getUnspentOutputs());
        } else {
            unspentOutputs = coins.getUnspentOutputs();
            Collections.sort(unspentOutputs, new UnspentOutputAmountComparatorDesc());
        }

        BigInteger collectedAmount = BigInteger.ZERO;
        BigInteger consumedAmount = BigInteger.ZERO;

        double inputCost = inputCost(feePerKb);

        int outputCount = 2;//initially assume change

        //1st input will be non-replayable if possible
        boolean hasReplayProtection = !unspentOutputs.get(0).isReplayable();

        if(addReplayProtection && !hasReplayProtection) {
            log.info("Adding non-replayable dust to selected coins.");
            unspentOutputs.add(0, getMockDustCoin());
        }

        for (int i = 0; i < unspentOutputs.size(); i++) {

            UnspentOutput output = unspentOutputs.get(i);

            //Filter coins not worth spending
            if (output.getValue().doubleValue() < inputCost && !output.isForceInclude()) {
                continue;
            }

            //Skip script with no type
            if (!output.isForceInclude() &&
                new Script(Hex.decode(output.getScript().getBytes())).getScriptType() == Script.ScriptType.NO_TYPE) {
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

        BigInteger absoluteFee;
        int inputCount = spendWorthyList.size();
        if(addReplayProtection && !hasReplayProtection) {
            //No non-replayable outputs in wallet - a dust input and output will be added to tx later
            log.info("Modifying tx size for segwit fee calculation.");
            int size = Fees.estimatedSize(inputCount, outputCount) + SEGWIT_TX_SIZE_ADAPT;
            absoluteFee = Fees.calculateFee(size, feePerKb);
        } else {
            absoluteFee = Fees.estimatedFee(inputCount, outputCount, feePerKb);
        }

        SpendableUnspentOutputs paymentBundle = new SpendableUnspentOutputs();
        paymentBundle.setSpendableOutputs(spendWorthyList);
        paymentBundle.setAbsoluteFee(absoluteFee);
        paymentBundle.setConsumedAmount(consumedAmount);
        paymentBundle.setReplayProtected(hasReplayProtection);
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
    private static class UnspentOutputAmountComparatorDesc implements Comparator<UnspentOutput> {

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

    /**
     * Sort unspent outputs by amount in ascending order.
     */
    private static class UnspentOutputAmountComparatorAsc implements Comparator<UnspentOutput> {

        public int compare(UnspentOutput o1, UnspentOutput o2) {

            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            int ret;

            if (o1.getValue().compareTo(o2.getValue()) > 0) {
                ret = AFTER;
            } else if (o1.getValue().compareTo(o2.getValue()) < 0) {
                ret = BEFORE;
            } else {
                ret = EQUAL;
            }

            return ret;
        }

    }

    private static UnspentOutput getMockDustCoin() {

        UnspentOutput dust = new UnspentOutput();
        dust.setValue(Payment.DUST);
        dust.setForceInclude(true);
        return dust;
    }
}
