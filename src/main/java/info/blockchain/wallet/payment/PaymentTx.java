package info.blockchain.wallet.payment;

import info.blockchain.api.data.UnspentOutput;
import info.blockchain.api.pushtx.PushTx;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.api.data.DustServiceInput;
import info.blockchain.wallet.util.Hash;
import info.blockchain.wallet.util.PrivateKeyFactory;
import info.blockchain.wallet.util.Tools;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import okhttp3.ResponseBody;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Call;

public class PaymentTx {

    private static final Logger log = LoggerFactory.getLogger(PaymentTx.class);

    public static synchronized Transaction makeSimpleTransaction(List<UnspentOutput> unspentCoins,
        HashMap<String, BigInteger> receivingAddresses,
        @Nonnull BigInteger fee,
        @Nonnull String changeAddress)
        throws InsufficientMoneyException, AddressFormatException {

        log.info("Making transaction");

        Transaction transaction = new Transaction(PersistentUrls.getInstance()
            .getCurrentNetworkParams());

        //Outputs
        BigInteger outputValueSum = addTransactionOutputs(transaction, receivingAddresses);
        BigInteger valueNeeded = outputValueSum.add(fee);

        //Inputs
        BigInteger inputValueSum = addTransactionInputList(transaction, unspentCoins, valueNeeded);

        //Add Change
        addChange(transaction, fee, changeAddress, outputValueSum, inputValueSum);

        //Bip69
        Transaction sortedTx = Tools.applyBip69(transaction);

        return sortedTx;
    }

    private static BigInteger addTransactionOutputs(Transaction transaction,
        HashMap<String, BigInteger> receivingAddresses) throws AddressFormatException {

        BigInteger outputValueSum = BigInteger.ZERO;

        Set<Entry<String, BigInteger>> set = receivingAddresses.entrySet();
        for (Entry<String, BigInteger> mapEntry : set) {

            String toAddress = mapEntry.getKey();
            BigInteger amount = mapEntry.getValue();

            //Don't allow less than dust value
            if (amount == null
                || amount.compareTo(BigInteger.ZERO) <= 0
                || amount.compareTo(Payment.DUST) == -1) {
                continue;
            }

            Address address = Address.fromBase58(PersistentUrls.getInstance()
                .getCurrentNetworkParams(), toAddress);
            Coin coin = Coin.valueOf(amount.longValue());

            transaction.addOutput(coin, address);
            outputValueSum = outputValueSum.add(amount);
        }

        return outputValueSum;
    }

    private static BigInteger addTransactionInputList(Transaction transaction,
        List<UnspentOutput> unspentCoins, BigInteger valueNeeded)
        throws InsufficientMoneyException {

        AbstractBitcoinNetParams networkParams = PersistentUrls.getInstance()
            .getCurrentNetworkParams();

        BigInteger inputValueSum = BigInteger.ZERO;
        BigInteger minFreeOutputSize = BigInteger.valueOf(1000000);

        for (UnspentOutput unspentCoin : unspentCoins) {

            Hash hash = new Hash(Hex.decode(unspentCoin.getTxHash()));
            hash.reverse();
            Sha256Hash txHash = Sha256Hash.wrap(hash.getBytes());

            TransactionOutPointConnected outPoint = new TransactionOutPointConnected(networkParams,
                unspentCoin.getTxOutputCount(),
                txHash);

            //outPoint needs connected output here
            TransactionOutput output = new TransactionOutput(networkParams,
                null,
                Coin.valueOf(unspentCoin.getValue().longValue()),
                Hex.decode(unspentCoin.getScript()));
            outPoint.setConnectedOutput(output);

            TransactionInput input = new TransactionInput(networkParams,
                null,
                new byte[0],
                outPoint);

            transaction.addInput(input);
            inputValueSum = inputValueSum.add(unspentCoin.getValue());

            if (inputValueSum.compareTo(valueNeeded) == 0
                || inputValueSum.compareTo(valueNeeded.add(minFreeOutputSize)) >= 0) {
                break;
            }
        }

        if (inputValueSum.compareTo(BigInteger.valueOf(2100000000000000L)) > 0) {
            throw new ProtocolException("21m limit exceeded");
        }

        if (inputValueSum.compareTo(valueNeeded) < 0) {
            throw new InsufficientMoneyException(valueNeeded.subtract(inputValueSum));
        }

        return inputValueSum;
    }

    private static void addChange(Transaction transaction, @Nonnull BigInteger fee,
        @Nonnull String changeAddress,
        BigInteger outputValueSum, BigInteger inputValueSum) throws AddressFormatException {

        AbstractBitcoinNetParams networkParams = PersistentUrls.getInstance()
            .getCurrentNetworkParams();

        BigInteger change = inputValueSum.subtract(outputValueSum).subtract(fee);

        //Consume dust if needed
        if (change.compareTo(BigInteger.ZERO) > 0 && (change.compareTo(Payment.DUST) == 1)) {

            Script changeScript = ScriptBuilder
                .createOutputScript(Address.fromBase58(networkParams, changeAddress));

            TransactionOutput change_output = new TransactionOutput(networkParams,
                null,
                Coin.valueOf(change.longValue()),
                changeScript.getProgram());
            transaction.addOutput(change_output);
        }
    }

    public static synchronized void signSimpleTransaction(Transaction transaction, List<ECKey> keys) {

        log.info("Signing transaction");
        Wallet keyBag = Wallet.fromKeys(PersistentUrls.getInstance()
            .getCurrentNetworkParams(), keys);

        SendRequest sendRequest = SendRequest.forTx(transaction);
        keyBag.signTransaction(sendRequest);
    }

    public static synchronized Call<ResponseBody> publishSimpleTransaction(Transaction transaction, String apiCode)
        throws IOException {

        log.info("Publishing transaction");
        PushTx pushTx = new PushTx(BlockchainFramework.getRetrofitExplorerInstance(), apiCode);
        return pushTx.pushTx(new String(Hex.encode(transaction.bitcoinSerialize())));
    }

    public static synchronized Transaction makeNonReplayableTransaction(List<UnspentOutput> unspentCoins,
        HashMap<String, BigInteger> receivingAddresses,
        @Nonnull BigInteger fee,
        @Nullable String changeAddress,
        @Nonnull DustServiceInput dustServiceInput)
        throws InsufficientMoneyException, AddressFormatException {

        log.info("Making transaction");
        AbstractBitcoinNetParams params = PersistentUrls.getInstance()
            .getCurrentNetworkParams();

        Transaction transaction = new Transaction(params);

        //Outputs
        BigInteger outputValueSum = addTransactionOutputs(transaction, receivingAddresses);
        BigInteger valueNeeded = outputValueSum.add(fee);

        if(unspentCoins.get(0).getValue().compareTo(Payment.DUST) == 0
            && unspentCoins.get(0).isForceInclude()) {
            log.info("Remove forced dust input");
            unspentCoins.remove(0);
            valueNeeded = valueNeeded.subtract(Payment.DUST);
        }

        //Inputs
        BigInteger inputValueSum = addTransactionInputList(transaction, unspentCoins, valueNeeded);

        //Add Change
        if (changeAddress != null) {
            addChange(transaction, fee, changeAddress, outputValueSum, inputValueSum);
        }

        //Add dust input/output
        Script dustOutput = new Script(Hex.decode(dustServiceInput.getOutputScript()));
        Coin dustCoin = Coin.valueOf(dustServiceInput.getValue().longValue());

        TransactionOutPoint dustOutpoint = dustServiceInput.getTransactionOutPoint(params);
        transaction.addInput(dustOutpoint.getHash(), dustOutpoint.getIndex(), new Script(new byte[0]));
        transaction.addOutput(dustCoin, dustOutput);

        //Bip69
        Transaction sortedTx = Tools.applyBip69(transaction);

        return sortedTx;
    }

    public static synchronized void signNonReplayableTransaction(Transaction transaction, List<ECKey> keys)
        throws Exception {

        log.info("Signing transaction");
        AbstractBitcoinNetParams networkParams = PersistentUrls.getInstance()
            .getCurrentNetworkParams();

        int numInputs = transaction.getInputs().size();
        for (int i = 0; i < numInputs; i++) {

            TransactionInput txIn = transaction.getInput(i);
            System.out.println("txIn: "+txIn);

            if (txIn.getConnectedOutput() == null) {
                // Missing connected output, assuming already signed.
                continue;
            }

            Script script = txIn.getConnectedOutput().getScriptPubKey();
            Address address = script.getToAddress(networkParams);

            ECKey key = getECKey(keys, address, networkParams);
            if(key == null) {
                continue;
            }

            // Create signature for our key
            Sha256Hash hash = transaction.hashForSignature(
                i,
                ScriptBuilder.createOutputScript(key.toAddress(PersistentUrls.getInstance()
                    .getCurrentNetworkParams())),
                Transaction.SigHash.ALL,
                false);

            TransactionSignature signature = new TransactionSignature(
                key.sign(hash),
                Transaction.SigHash.ALL,
                false
            );

            txIn.setScriptSig(
                ScriptBuilder.createInputScript(signature, key)
            );
        }
    }

    private static ECKey getECKey(List<ECKey> keys, Address address, AbstractBitcoinNetParams networkParams) throws Exception {
        ECKey key = null;
        for(ECKey k : keys) {

            String format = new PrivateKeyFactory().getFormat(k.getPrivateKeyAsHex());
            key = new PrivateKeyFactory().getKey(format, k.getPrivateKeyAsHex());

            if(key != null && !key.toAddress(networkParams).toBase58().equals(address.toBase58())) {
                key = null;
            }
        }

        return key;
    }

    public static synchronized Call<ResponseBody> publishTransactionWithSecret(Transaction transaction, String lockSecret, String apiCode)
        throws IOException {

        log.info("Publishing transaction");
        PushTx pushTx = new PushTx(BlockchainFramework.getRetrofitExplorerInstance(), apiCode);
        return pushTx.pushTxWithSecret(new String(Hex.encode(transaction.bitcoinSerialize())), lockSecret);
    }
}
