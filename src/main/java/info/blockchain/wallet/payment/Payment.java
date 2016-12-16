package info.blockchain.wallet.payment;

import info.blockchain.api.PersistentUrls;
import info.blockchain.api.PushTx;
import info.blockchain.util.FeeUtil;
import info.blockchain.wallet.payment.data.SpendableUnspentOutputs;
import info.blockchain.wallet.payment.data.SweepBundle;
import info.blockchain.wallet.payment.data.UnspentOutputs;
import info.blockchain.wallet.send.MyTransactionOutPoint;
import info.blockchain.wallet.send.SendCoins;
import info.blockchain.wallet.util.Hash;

import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Payment {

    /**
     * Parses Blockchain's unspent api json and return all unspent data
     *
     * @return UnspentOutputs result of api response
     */
    public UnspentOutputs getCoins(JSONObject unspentsOutputJson) {

        String notice = null;
        BigInteger balance = BigInteger.ZERO;
        List<MyTransactionOutPoint> outputPointList = new ArrayList<MyTransactionOutPoint>();

        JSONArray unspentsJsonArray = unspentsOutputJson.getJSONArray("unspent_outputs");

        if (unspentsOutputJson.has("notice")) {
            notice = unspentsOutputJson.getString("notice");
        }

        for (int i = 0; i < unspentsJsonArray.length(); i++) {

            JSONObject unspentJson = unspentsJsonArray.getJSONObject(i);

            byte[] hashBytes = Hex.decode(unspentJson.getString("tx_hash"));
            BigInteger value = BigInteger.valueOf(unspentJson.getLong("value"));
            int txOutputN = unspentJson.getInt("tx_output_n");
            byte[] scriptBytes = Hex.decode(unspentJson.getString("script"));
            int confirmations = unspentJson.getInt("confirmations");

            balance = balance.add(value);

            Hash hash = new Hash(hashBytes);
            hash.reverse();
            Sha256Hash txHash = Sha256Hash.wrap(hash.getBytes());

            // Construct the output
            MyTransactionOutPoint outPoint = new MyTransactionOutPoint(txHash, txOutputN, value, scriptBytes);
            outPoint.setConfirmations(confirmations);

            if (unspentJson.has("xpub")) {
                JSONObject xpubJsonObject = unspentJson.getJSONObject("xpub");
                outPoint.setPath(xpubJsonObject.getString("path"));
            }

            outputPointList.add(outPoint);

        }

        return new UnspentOutputs(outputPointList, balance, notice);
    }

    public SweepBundle getSweepBundle(UnspentOutputs coins, BigInteger feePerKb) {

        SweepBundle sweepBundle = new SweepBundle();

        Collections.sort(coins.getOutputs(), new UnspentOutputAmountComparator());
        ArrayList<MyTransactionOutPoint> allCoins = new ArrayList<MyTransactionOutPoint>();
        BigInteger sweepBalance = BigInteger.ZERO;

        double inputCost = inputCost(feePerKb);

        for (MyTransactionOutPoint output : coins.getOutputs()) {

            //Filter usable coins
            if (output.getValue().doubleValue() >= inputCost) {
                allCoins.add(output);
                sweepBalance = sweepBalance.add(output.getValue());
            }
        }

        //All inputs, 1 output = no change
        BigInteger feeForAll = FeeUtil.estimatedFee(allCoins.size(), 1, feePerKb);
        BigInteger balanceAfterFee = sweepBalance.subtract(feeForAll);
        if (balanceAfterFee.compareTo(BigInteger.ZERO) == -1) {
            sweepBundle.setSweepAmount(BigInteger.ZERO);
        } else {
            sweepBundle.setSweepAmount(balanceAfterFee);
        }
        sweepBundle.setSweepFee(feeForAll);
        return sweepBundle;
    }

    private double inputCost(BigInteger feePerKb) {
        double d = Math.ceil(feePerKb.doubleValue() * 0.148);
        return Math.ceil(d);
    }

    public SpendableUnspentOutputs getSpendableCoins(UnspentOutputs coins, BigInteger spendAmount, BigInteger feePerKb) {

        SpendableUnspentOutputs result = new SpendableUnspentOutputs();

        // Select the minimum number of outputs necessary
        Collections.sort(coins.getOutputs(), new UnspentOutputAmountComparator());
        List<MyTransactionOutPoint> minimumUnspentOutputsList = new ArrayList<MyTransactionOutPoint>();
        BigInteger totalValue = BigInteger.ZERO;
        BigInteger consumedBalance = BigInteger.ZERO;
        double inputCost = inputCost(feePerKb);

        int outputCount = 2;//initially assume change

        for (MyTransactionOutPoint output : coins.getOutputs()) {

            // Filter usable coins
            if (output.getValue().doubleValue() < inputCost) {
                continue;
            }

            totalValue = totalValue.add(output.getValue());
            minimumUnspentOutputsList.add(output);

            //No change = 1 output (Exact amount)
            BigInteger spendAmountNoChange = spendAmount.add(FeeUtil.estimatedFee(minimumUnspentOutputsList.size(), 1, feePerKb));
            if (spendAmountNoChange.compareTo(totalValue) == 0) {
                outputCount = 1;
                break;
            }

            //No change = 1 output (Don't allow dust to be sent back as change - consume it rather)
            BigInteger spendAmountNoChangeWithDustInclusion = spendAmount.add(FeeUtil.estimatedFee(minimumUnspentOutputsList.size(), 1, feePerKb));
            if (spendAmountNoChangeWithDustInclusion.compareTo(totalValue) == -1
                    && spendAmountNoChangeWithDustInclusion.compareTo(totalValue.subtract(SendCoins.bDust)) >= 0) {
                consumedBalance = consumedBalance.add(spendAmountNoChangeWithDustInclusion.subtract(totalValue));
                outputCount = 1;
                break;
            }

            //Expect change = 2 outputs
            BigInteger spendAmountWithChange = spendAmount.add(FeeUtil.estimatedFee(minimumUnspentOutputsList.size(), 2, feePerKb));
            if (totalValue.compareTo(spendAmountWithChange) >= 0) {
                outputCount = 2;//[multiple inputs, 2 outputs] - assume change
                break;
            }
        }

        result.setSpendableOutputs(minimumUnspentOutputsList);
        result.setAbsoluteFee(FeeUtil.estimatedFee(minimumUnspentOutputsList.size(), outputCount, feePerKb));
        result.setConsumedAmount(consumedBalance);

        return result;
    }

    /**
     * Sort unspent outputs by amount in descending order.
     */
    private class UnspentOutputAmountComparator implements Comparator<MyTransactionOutPoint> {

        public int compare(MyTransactionOutPoint o1, MyTransactionOutPoint o2) {

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

    public interface SubmitPaymentListener {
        void onSuccess(String hash);

        void onFail(String error);
    }

    public void submitPayment(SpendableUnspentOutputs unspentOutputBundle,
                              List<ECKey> keys,
                              String toAddress,
                              String changeAddress,
                              BigInteger bigIntFee,
                              BigInteger bigIntAmount,
                              SubmitPaymentListener listener) throws Exception {

        final HashMap<String, BigInteger> receivers = new HashMap<String, BigInteger>();
        receivers.put(toAddress, bigIntAmount);

        Pair<Transaction, Long> pair;
        pair = SendCoins.getInstance().makeTransaction(true,
                unspentOutputBundle.getSpendableOutputs(),
                receivers,
                bigIntFee,
                changeAddress);

        // Transaction cancelled
        if (pair == null) {
            throw new Exception("Transaction cancelled");
        }
        Transaction tx = pair.getLeft();
        Long priority = pair.getRight();

        Wallet wallet = new Wallet(PersistentUrls.getInstance().getCurrentNetworkParams());
        wallet.importKeys(keys);

        SendCoins.getInstance().signTx(tx, wallet);
        String hexString = SendCoins.getInstance().encodeHex(tx);

        if (hexString.length() > (100 * 1024)) {
            //Transaction maximum length cannot exceed 100KB
            //Try splitting transaction
            throw new Exception("Transaction maximum length cannot exceed 100KB");
        }

        PushTx pushTxApi = new PushTx();

        String response = pushTxApi.submitTransaction(tx);
        if (response.contains("Transaction Submitted")) {

            listener.onSuccess(tx.getHashAsString());
        } else {
            listener.onFail(response);
        }
    }
}
