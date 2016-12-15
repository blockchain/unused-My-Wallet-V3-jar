package info.blockchain.wallet.send;

import info.blockchain.api.PersistentUrls;
import info.blockchain.wallet.util.Hash;

import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * SendCoins.java : singleton class for spending from Blockchain Android HD wallet
 */
public class SendCoins {

    private static SendCoins instance = null;

    private SendCoins() {
    }

    public static final BigInteger bDust = BigInteger.valueOf(Coin.parseCoin("0.000005460").longValue());

    //Minimum fee accepted by push_tc endpoint - 10000 satoshis
    public static final BigInteger bMinimumFeePerKb = BigInteger.valueOf(Coin.parseCoin("0.00001").longValue());

    public static SendCoins getInstance() {

        if (instance == null) {
            instance = new SendCoins();
        }

        return instance;
    }

    /**
     * Creates, populates, and returns transaction instance for this spend and returns it with
     * calculated priority. Change output is positioned randomly.
     *
     * @param boolean                     isSimpleSend Always true, not currently used
     * @param List<MyTransactionOutPoint> unspent Unspent outputs
     * @param BigInteger                  amount Spending amount (not including fee)
     * @param HashMap<String,             BigInteger> receivingAddresses
     * @param BigInteger                  fee Miner's fee for this spend
     * @param String                      changeAddress Change address for this spend
     * @return Pair<Transaction, Long>
     */
    public Pair<Transaction, Long> makeTransaction(boolean isSimpleSend, List<MyTransactionOutPoint> unspent, HashMap<String, BigInteger> receivingAddresses, BigInteger fee, final String changeAddress) throws Exception {

        long priority = 0;

        if (unspent == null || unspent.size() == 0) {
//			throw new InsufficientFundsException("No free outputs to spend.");
            return null;
        }

        if (fee == null) {
            fee = BigInteger.ZERO;
        }

        // Construct a new transaction
        Transaction tx = new Transaction(PersistentUrls.getInstance().getCurrentNetworkParams());
        List<MyTransactionInput> inputs = new ArrayList<MyTransactionInput>();
        List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
        BigInteger outputValueSum = BigInteger.ZERO;

        for (Entry<String, BigInteger> mapEntry : receivingAddresses.entrySet()) {
            String toAddress = mapEntry.getKey();
            BigInteger amount = mapEntry.getValue();

            if (amount == null || amount.compareTo(BigInteger.ZERO) <= 0) {
                throw new Exception("Invalid amount");
            }

            if (amount.compareTo(bDust) < 1) {
                throw new Exception("Dust amount");
            }

            outputValueSum = outputValueSum.add(amount);
            // Add the output
            BitcoinScript toOutputScript = BitcoinScript.createSimpleOutBitcoinScript(new BitcoinAddress(toAddress));
            TransactionOutput output = new TransactionOutput(PersistentUrls.getInstance().getCurrentNetworkParams(), null, Coin.valueOf(amount.longValue()), toOutputScript.getProgram());
            outputs.add(output);
        }

        // Now select the appropriate inputs
        BigInteger valueSelected = BigInteger.ZERO;
        BigInteger valueNeeded = outputValueSum.add(fee);
        BigInteger minFreeOutputSize = BigInteger.valueOf(1000000);

        for (MyTransactionOutPoint outPoint : unspent) {

            BitcoinScript script = new BitcoinScript(outPoint.getScriptBytes());

            if (script.getOutType() == BitcoinScript.ScriptOutTypeStrange) {
                continue;
            }

            BitcoinScript inputScript = new BitcoinScript(outPoint.getConnectedPubKeyScript());
            String address = inputScript.getAddress().toString();

            // if isSimpleSend don't use address as input if is output
            if (isSimpleSend && receivingAddresses.get(address) != null) {
                continue;
            }

            MyTransactionInput input = new MyTransactionInput(PersistentUrls.getInstance().getCurrentNetworkParams(), null, new byte[0], outPoint, outPoint.getTxHash().toString(), outPoint.getTxOutputN());
            inputs.add(input);
            valueSelected = valueSelected.add(outPoint.getValue());
            priority += outPoint.getValue().longValue() * outPoint.getConfirmations();

            if (valueSelected.compareTo(valueNeeded) == 0 || valueSelected.compareTo(valueNeeded.add(minFreeOutputSize)) >= 0) {
                break;
            }
        }

        if (valueSelected.compareTo(BigInteger.valueOf(2100000000000000L)) > 0) {
            throw new Exception("21m limit exceeded");
        }

        // Check the amount we have selected is greater than the amount we need
        if (valueSelected.compareTo(valueNeeded) < 0) {
            throw new Exception("Insufficient Funds");
        }

        BigInteger change = valueSelected.subtract(outputValueSum).subtract(fee);
        // Now addContact the change if there is any
        if (change.compareTo(BigInteger.ZERO) > 0) {

            // Consume the change if it would create a very small none standard output
            if (change.compareTo(bDust) >= 0) {
                BitcoinScript change_script;
                if (changeAddress != null) {
                    change_script = BitcoinScript.createSimpleOutBitcoinScript(new BitcoinAddress(changeAddress));
                } else {
                    throw new Exception("Change address null");
                }
                TransactionOutput change_output = new TransactionOutput(PersistentUrls.getInstance().getCurrentNetworkParams(), null, Coin.valueOf(change.longValue()), change_script.getProgram());
                outputs.add(change_output);
            }
        }

        //
        // deterministically sort inputs and outputs, see OBPP BIP69
        //
        Collections.sort(inputs, new InputComparator());
        for (MyTransactionInput ti : inputs) {
            tx.addInput(ti);
        }

        Collections.sort(outputs, new OutputComparator());
        for (TransactionOutput to : outputs) {
            tx.addOutput(to);
        }

        long estimatedSize = tx.bitcoinSerialize().length + (114 * tx.getInputs().size());
        priority /= estimatedSize;

        return Pair.of(tx, priority);
    }

    /**
     * <p>Calculate signatures for inputs of a transaction.
     *
     * @param Transaction transaction  Transaction for which the inputs must be signed
     * @param Wallet      wallet Wallet used as key bag, not for actual spending
     */
    public synchronized void signTx(Transaction transaction, Wallet wallet) throws ScriptException {

        List<TransactionInput> inputs = transaction.getInputs();

        TransactionSignature[] sigs = new TransactionSignature[inputs.size()];
        ECKey[] keys = new ECKey[inputs.size()];

        for (int i = 0; i < inputs.size(); i++) {

            TransactionInput input = inputs.get(i);

            // Find the signing key
            ECKey key = input.getOutpoint().getConnectedKey(wallet);
            // Keep key for script creation step below
            keys[i] = key;
            byte[] connectedPubKeyScript = input.getOutpoint().getConnectedPubKeyScript();
            assert key != null;
            if (key.hasPrivKey() || key.isEncrypted()) {
                sigs[i] = transaction.calculateSignature(i, key, connectedPubKeyScript, SigHash.ALL, false);
            } else {
                sigs[i] = TransactionSignature.dummy();   // watch only ?
            }
        }

        for (int i = 0; i < inputs.size(); i++) {
            if (sigs[i] == null) {
                continue;
            }
            TransactionInput input = inputs.get(i);
            final TransactionOutput connectedOutput = input.getOutpoint().getConnectedOutput();
            assert connectedOutput != null;
            Script scriptPubKey = connectedOutput.getScriptPubKey();
            if (scriptPubKey.isSentToAddress()) {
                input.setScriptSig(ScriptBuilder.createInputScript(sigs[i], keys[i]));
            } else if (scriptPubKey.isSentToRawPubKey()) {
                input.setScriptSig(ScriptBuilder.createInputScript(sigs[i]));
            } else {
                throw new RuntimeException("Unknown script type: " + scriptPubKey);
            }
        }

    }

    public String encodeHex(Transaction tx) {
        return new String(Hex.encode(tx.bitcoinSerialize()));
    }

    private class InputComparator implements Comparator<MyTransactionInput> {

        public int compare(MyTransactionInput i1, MyTransactionInput i2) {

            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            Hash hash1 = new Hash(Hex.decode(i1.getTxHash()));
            Hash hash2 = new Hash(Hex.decode(i2.getTxHash()));
            byte[] h1 = hash1.getBytes();
            byte[] h2 = hash2.getBytes();

            int pos = 0;
            while (pos < h1.length && pos < h2.length) {

                byte b1 = h1[pos];
                byte b2 = h2[pos];

                if ((b1 & 0xff) < (b2 & 0xff)) {
                    return BEFORE;
                } else if ((b1 & 0xff) > (b2 & 0xff)) {
                    return AFTER;
                } else {
                    pos++;
                }

            }

            if (i1.getTxPos() < i2.getTxPos()) {
                return BEFORE;
            } else if (i1.getTxPos() > i2.getTxPos()) {
                return AFTER;
            } else {
                return EQUAL;
            }

        }

    }

    private class OutputComparator implements Comparator<TransactionOutput> {

        public int compare(TransactionOutput o1, TransactionOutput o2) {

            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            if (o1.getValue().compareTo(o2.getValue()) > 0) {
                return AFTER;
            } else if (o1.getValue().compareTo(o2.getValue()) < 0) {
                return BEFORE;
            } else {

                byte[] b1 = o1.getScriptBytes();
                byte[] b2 = o2.getScriptBytes();

                int pos = 0;
                while (pos < b1.length && pos < b2.length) {

                    if ((b1[pos] & 0xff) < (b2[pos] & 0xff)) {
                        return BEFORE;
                    } else if ((b1[pos] & 0xff) > (b2[pos] & 0xff)) {
                        return AFTER;
                    }

                    pos++;
                }

                if (b1.length < b2.length) {
                    return BEFORE;
                } else if (b1.length > b2.length) {
                    return AFTER;
                } else {
                    return EQUAL;
                }

            }

        }

    }

}