package info.blockchain.wallet_rafactored.transaction;

import info.blockchain.wallet.send.MyTransactionOutPoint;
import info.blockchain.wallet.send.UnspentOutputsBundle;
import info.blockchain.wallet.util.Hash;
import info.blockchain.wallet.util.WebUtil;
import org.bitcoinj.core.Sha256Hash;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

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

    private JSONObject getUnspentOutputs(String address){
        String response = null;
        try {
            response = WebUtil.getInstance().getURL(WebUtil.UNSPENT_OUTPUTS_URL + address);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(response != null) {
            JSONObject result = new JSONObject(response);
            return result;
        }else{
            return null;
        }
    }

    public void cacheUnspentOutputs(String address) {

        JSONObject result = getUnspentOutputs(address);

        if(!cachedUnspentOutputs.containsKey(address)) {
            cachedUnspentOutputs.put(address, result);
        }
    }

    public void clearCachedUnspentOutputs() {
        cachedUnspentOutputs.clear();
    }

    public UnspentOutputsBundle getCachedCoins(String address) {
        return  getCoins(address, false);
    }

    public void someExtraMethodForAdvancedTx() {

    }

    public UnspentOutputsBundle getFreshCoins(String address) {
        return  getCoins(address, true);
    }

    private UnspentOutputsBundle getCoins(String address, boolean fetchFresh) {

        JSONObject unspentsJson = null;

        if(fetchFresh) {
            unspentsJson = getUnspentOutputs(address);
        }else{
            unspentsJson = getCachedUnspentOutputs(address);
            if(unspentsJson == null)
                return null;
        }

        UnspentOutputsBundle ret = new UnspentOutputsBundle();

        List<MyTransactionOutPoint> outputPointList = new ArrayList<MyTransactionOutPoint>();

        JSONArray unspentsJsonArray = unspentsJson.getJSONArray("unspent_outputs");

        if(unspentsJson.has("notice")) {
            ret.setNotice(unspentsJson.getString("notice"));
        }

        BigInteger totalAvailableBalance = 	BigInteger.ZERO;
        for (int i = 0; i < unspentsJsonArray.length(); i++) {

            JSONObject unspentJson = unspentsJsonArray.getJSONObject(i);

            byte[] hashBytes = Hex.decode(unspentJson.getString("tx_hash"));
            BigInteger value = BigInteger.valueOf(unspentJson.getLong("value"));
            int txOutputN = unspentJson.getInt("tx_output_n");
            byte[] scriptBytes = Hex.decode(unspentJson.getString("script"));
            int confirmations = unspentJson.getInt("confirmations");

            ret.setSweepAmount(ret.getSweepAmount().add(value));

            Hash hash = new Hash(hashBytes);
            hash.reverse();
            Sha256Hash txHash = new Sha256Hash(hash.getBytes());

            totalAvailableBalance = totalAvailableBalance.add(value);

            // Construct the output
            MyTransactionOutPoint outPoint = new MyTransactionOutPoint(txHash, txOutputN, value, scriptBytes);
            outPoint.setConfirmations(confirmations);
            outputPointList.add(outPoint);

        }
        ret.setOutputs(outputPointList);

        return ret;
    }
}
