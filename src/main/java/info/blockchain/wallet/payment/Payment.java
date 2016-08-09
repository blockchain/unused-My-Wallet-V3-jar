package info.blockchain.wallet.payment;

import info.blockchain.api.PushTx;
import info.blockchain.bip44.Address;
import info.blockchain.util.FeeUtil;
import info.blockchain.wallet.payload.Account;
import info.blockchain.wallet.payload.LegacyAddress;
import info.blockchain.wallet.payload.PayloadManager;
import info.blockchain.wallet.payment.data.SpendableUnspentOutputs;
import info.blockchain.wallet.payment.data.SweepBundle;
import info.blockchain.wallet.payment.data.UnspentOutputs;
import info.blockchain.wallet.send.BitcoinScript;
import info.blockchain.wallet.send.MyTransactionOutPoint;
import info.blockchain.wallet.send.SendCoins;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.Hash;
import info.blockchain.wallet.util.PrivateKeyFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by riaanvos on 26/04/2016.
 */
public class Payment {

    /**
     * Parses Blockchain's unspent api json and return all unspent data
     *
     * @param unspentsOutputJson
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
            Sha256Hash txHash = new Sha256Hash(hash.getBytes());

            // Construct the output
            MyTransactionOutPoint outPoint = new MyTransactionOutPoint(txHash, txOutputN, value, scriptBytes);
            outPoint.setConfirmations(confirmations);

            if(unspentJson.has("xpub")){
                JSONObject xpubJsonObject = unspentJson.getJSONObject("xpub");
                outPoint.setPath(xpubJsonObject.getString("path"));
            }

            outputPointList.add(outPoint);

        }

        return new UnspentOutputs(outputPointList, balance, notice);
    }

    public SweepBundle getSweepBundle(UnspentOutputs coins, BigInteger feePerKb) {

        SweepBundle sweepBundle = new SweepBundle();

        //Filter usable coins
        ArrayList<MyTransactionOutPoint> worthyCoins = new ArrayList<MyTransactionOutPoint>();
        BigInteger sweepBalance = BigInteger.ZERO;
        BigInteger consumedBalance = BigInteger.ZERO;
        for (MyTransactionOutPoint output : coins.getOutputs()) {
//            if(output.getValue().compareTo(feePerKb) == 1){
                worthyCoins.add(output);
                sweepBalance = sweepBalance.add(output.getValue());
//            }

            if(output.getValue().compareTo(feePerKb) <= 1){
                consumedBalance.add(output.getValue());
            }
        }

        //All inputs, 1 output = no change
        //TODO- assume change to line up with web wallet. This should be 1
        BigInteger feeForAll = FeeUtil.estimatedFee(worthyCoins.size(), 2, feePerKb);
        sweepBundle.setSweepAmount(sweepBalance.subtract(feeForAll));
        sweepBundle.setSweepFee(feeForAll);
        sweepBundle.setConsumedAmount(consumedBalance);
        return sweepBundle;
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

    public interface SubmitPaymentListener {
        void onSuccess(String hash);
        void onFail(String error);
    }

    public void submitPayment(SpendableUnspentOutputs unspentOutputBundle,
                                      Account account,
                                      LegacyAddress legacyAddress,
                                      String toAddress,
                                      String changeAddress,
                                      String note,
                                      BigInteger bigIntFee,
                                      BigInteger bigIntAmount,
                                      boolean isWatchOnly,
                                      String secondPassword,
                                      SubmitPaymentListener listener) throws Exception {

        //TODO - PayloadManager needs to be refactored out to make this method testable
        //TODO - This method was pretty much just coppied out from android and modified slightly to retain stability
        PayloadManager payloadManager = PayloadManager.getInstance();

        final boolean isHD = account == null ? false : true;

        //Get keys
        HashMap<String, Address> keyMap = new HashMap<String, Address>();
        if (isHD) {

            for(MyTransactionOutPoint a : unspentOutputBundle.getSpendableOutputs()){
                String[] split = a.getPath().split("/");
                int chain = Integer.parseInt(split[1]);
                int addressIndex = Integer.parseInt(split[2]);

                Address address = payloadManager.getAddressAt(account.getRealIdx(), chain, addressIndex);
                keyMap.put(address.getAddressString(), address);
            }
        }

        final HashMap<String, BigInteger> receivers = new HashMap<String, BigInteger>();
        receivers.put(toAddress, bigIntAmount);

        Pair<Transaction, Long> pair = null;
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

        Wallet wallet = new Wallet(MainNetParams.get());
        for (TransactionInput input : tx.getInputs()) {
            byte[] scriptBytes = input.getOutpoint().getConnectedPubKeyScript();
            String address = new BitcoinScript(scriptBytes).getAddress().toString();
            ECKey walletKey = null;
            try {

                if (isHD) {
                    Address hd_address = keyMap.get(address);
                    walletKey =  PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.WIF_COMPRESSED, hd_address.getPrivateKeyString());

                } else {
                    if (!isWatchOnly && payloadManager.getPayload().isDoubleEncrypted()) {
                        walletKey = legacyAddress.getECKey(new CharSequenceX(secondPassword));
                    } else {
                        walletKey = legacyAddress.getECKey();
                    }
                }
            } catch (AddressFormatException afe) {
                // skip add Watch Only Bitcoin Address key because already accounted for later with tempKeys
                afe.printStackTrace();
                continue;
            }

            if (walletKey != null) {
                wallet.addKey(walletKey);
            } else {
                throw new Exception("Wallet key error");
            }

        }

        if (payloadManager.isNotUpgraded()) {
            wallet = new Wallet(MainNetParams.get());
            List<LegacyAddress> addrs = payloadManager.getPayload().getActiveLegacyAddresses();
            for (LegacyAddress addr : addrs) {
                ECKey ecKey = null;
                if (!isWatchOnly && payloadManager.getPayload().isDoubleEncrypted()) {
                    ecKey = addr.getECKey(new CharSequenceX(secondPassword));
                } else {
                    ecKey = addr.getECKey();
                }
                if (addr != null && ecKey != null && ecKey.hasPrivKey()) {
                    wallet.addKey(ecKey);
                }
            }
        }

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

            if (note != null && note.length() > 0) {
                Map<String, String> notes = payloadManager.getPayload().getNotes();
                notes.put(tx.getHashAsString(), note);
                payloadManager.getPayload().setNotes(notes);
            }

            if (account != null) {
                // increment change address counter
                account.incChange();
            }

        } else {
            listener.onFail(response);
        }
    }
}
