package info.blockchain.wallet.transaction;

import info.blockchain.wallet.send.MyTransactionOutPoint;
import info.blockchain.wallet.transaction.data.UnspentOutputs;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseTransaction implements SimpleTransaction, AdvancedTransaction {

    private HashMap<String, JSONObject> cachedUnspentOutputs;

    public BaseTransaction() {
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

    public void someExtraMethodForAdvancedTx() {

    }

    public UnspentOutputs getCachedCoins(String address) throws Exception {
        if(cachedUnspentOutputs.containsKey(address)) {
            return getCoins(address, cachedUnspentOutputs.get(address));
        }else{
            throw new Exception("Address not cached.");
        }
    }

    /**
     *
     * If cacheUnspentOutputs() has been called prior to this, a cached state will be returned, otherwise - calls blockchain's unspent api and caches the result in a JSONObject
     * @param address or xpub (pipe separated)
     * @return UnspentOutputs result of api response
     *
     */
    public UnspentOutputs getCoins(String address, JSONObject unspentsOutputJson) {

        String notice = null;
        BigInteger balance = BigInteger.ZERO;
        List<MyTransactionOutPoint> outputPointList = new ArrayList<MyTransactionOutPoint>();

//        JSONArray unspentsJsonArray = unspentsOutputJson.getJSONArray("unspent_outputs");
//
//        if(unspentsOutputJson.has("notice")) {
//            notice = unspentsOutputJson.getString("notice");
//        }
//
//        BigInteger totalAvailableBalance = 	BigInteger.ZERO;
//        for (int i = 0; i < unspentsJsonArray.length(); i++) {
//
//            JSONObject unspentJson = unspentsJsonArray.getJSONObject(i);
//
//            byte[] hashBytes = Hex.decode(unspentJson.getString("tx_hash"));
//            BigInteger value = BigInteger.valueOf(unspentJson.getLong("value"));
//            int txOutputN = unspentJson.getInt("tx_output_n");
//            byte[] scriptBytes = Hex.decode(unspentJson.getString("script"));
//            int confirmations = unspentJson.getInt("confirmations");
//
//            balance.add(value);
//
//            Hash hash = new Hash(hashBytes);
//            hash.reverse();
//            Sha256Hash txHash = new Sha256Hash(hash.getBytes());
//
//            totalAvailableBalance = totalAvailableBalance.add(value);
//
//            // Construct the output
//            MyTransactionOutPoint outPoint = new MyTransactionOutPoint(txHash, txOutputN, value, scriptBytes);
//            outPoint.setConfirmations(confirmations);
//            outputPointList.add(outPoint);
//
//        }

        return new UnspentOutputs(outputPointList, balance, notice);
    }
}
