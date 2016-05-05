package info.blockchain.wallet.payment;

import info.blockchain.util.FeeUtil;
import info.blockchain.wallet.send.MyTransactionOutPoint;
import info.blockchain.wallet.payment.data.SpendableUnspentOutputs;
import info.blockchain.wallet.payment.data.UnspentOutputs;
import info.blockchain.wallet.util.Hash;
import org.bitcoinj.core.Sha256Hash;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

/*
This class is still in development (26/04/2016)
 */
public class Payment {

    private HashMap<String, JSONObject> cachedUnspentOutputs;

    public Payment() {
        this.cachedUnspentOutputs = new HashMap<String, JSONObject>();
    }

    private JSONObject getCachedUnspentOutputs(String address) {
        return cachedUnspentOutputs.get(address);
    }

    /**
     *
     * Caches address-unspent key pair
     * @param address or xpub (pipe separated)
     * @param unspentOutputsJson unspent json from blockchain.info
     *
     */
    public void cacheUnspentOutputs(String address, JSONObject unspentOutputsJson) {

        if(!cachedUnspentOutputs.containsKey(address)) {
            cachedUnspentOutputs.put(address, unspentOutputsJson);
        }
    }

    /**
     *
     * Clears cache used for unspent api responses
     *
     */
    public void clearCachedUnspentOutputs() {
        cachedUnspentOutputs.clear();
    }

    public UnspentOutputs getCachedCoins(String address) throws Exception {
        if(cachedUnspentOutputs.containsKey(address)) {
            return getCoins(cachedUnspentOutputs.get(address));
        }else{
            throw new Exception("Address not cached.");
        }
    }

    /**
     *
     * If cacheUnspentOutputs() has been called prior to this, a cached state will be returned, otherwise - calls blockchain's unspent api and caches the result in a JSONObject
     * @param unspentsOutputJson
     * @return UnspentOutputs result of api response
     *
     */
    public UnspentOutputs getCoins(JSONObject unspentsOutputJson) {

        String notice = null;
        BigInteger balance = BigInteger.ZERO;
        List<MyTransactionOutPoint> outputPointList = new ArrayList<MyTransactionOutPoint>();

        JSONArray unspentsJsonArray = unspentsOutputJson.getJSONArray("unspent_outputs");

        if(unspentsOutputJson.has("notice")) {
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
            Sha256Hash txHash = new Sha256Hash(hash.getBytes());

            // Construct the output
            MyTransactionOutPoint outPoint = new MyTransactionOutPoint(txHash, txOutputN, value, scriptBytes);
            outPoint.setConfirmations(confirmations);
            outputPointList.add(outPoint);

        }

        return new UnspentOutputs(outputPointList, balance, notice);
    }

    public BigInteger getSweepFee(UnspentOutputs coins, BigInteger feePerKb) {

        //Filter usable coins
        ArrayList<MyTransactionOutPoint> worthyCoins = new ArrayList<MyTransactionOutPoint>();
        for(MyTransactionOutPoint output : coins.getOutputs()){
            if(output.getValue().compareTo(feePerKb) == 1){
                worthyCoins.add(output);
            }
        }

        //All inputs, 1 output = no change
        return FeeUtil.estimatedFee(worthyCoins.size(), 1, feePerKb);
    }

    public BigInteger getSweepBalance(UnspentOutputs coins, BigInteger feePerKb) {

        //Filter usable coins
        ArrayList<MyTransactionOutPoint> worthyCoins = new ArrayList<MyTransactionOutPoint>();
        BigInteger sweepBalance = BigInteger.ZERO;
        for(MyTransactionOutPoint output : coins.getOutputs()){
            if(output.getValue().compareTo(feePerKb) == 1){
                worthyCoins.add(output);
                sweepBalance = sweepBalance.add(output.getValue());
            }
        }

        //All inputs, 1 output = no change
        BigInteger feeForAll = FeeUtil.estimatedFee(worthyCoins.size(), 1, feePerKb);
        return sweepBalance.subtract(feeForAll);
    }

    public BigInteger getRecommendedFee(UnspentOutputs coins, BigInteger spendAmount, BigInteger feePerKb) {

        //Filter usable coins
        ArrayList<MyTransactionOutPoint> worthyCoins = new ArrayList<MyTransactionOutPoint>();
        for(MyTransactionOutPoint output : coins.getOutputs()){
            if(output.getValue().compareTo(feePerKb) == 1){
                worthyCoins.add(output);
            }
        }

        //All inputs, 1 output = no change
        return FeeUtil.estimatedFee(worthyCoins.size(), 1, feePerKb);
    }

    public SpendableUnspentOutputs getSpendableCoins(UnspentOutputs coins, BigInteger spendAmount, BigInteger feePerKb) {

        SpendableUnspentOutputs result = new SpendableUnspentOutputs();

        // select the minimum number of outputs necessary
        Collections.sort(coins.getOutputs(), new UnspentOutputAmountComparator());
        List<MyTransactionOutPoint> minimumUnspentOutputsList = new ArrayList<MyTransactionOutPoint>();
        BigInteger totalValue = BigInteger.ZERO;
        int outputCount = 2;
        for (MyTransactionOutPoint output : coins.getOutputs()) {
            totalValue = totalValue.add(output.getValue());
            minimumUnspentOutputsList.add(output);

            //No change = 1 output
            BigInteger spendAmountNoChange = spendAmount.add(FeeUtil.estimatedFee(minimumUnspentOutputsList.size(), 1, feePerKb));
            if (spendAmountNoChange.compareTo(totalValue) == 0) {
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

            int ret = 0;

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
