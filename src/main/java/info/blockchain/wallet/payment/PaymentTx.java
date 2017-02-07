package info.blockchain.wallet.payment;

import info.blockchain.api.data.UnspentOutput;
import info.blockchain.api.pushtx.PushTx;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.util.Hash;
import info.blockchain.wallet.util.Tools;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import okhttp3.ResponseBody;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.SendRequest;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Call;

public class PaymentTx {

    public static synchronized Transaction makeTransaction(List<UnspentOutput> unspentCoins,
        HashMap<String, BigInteger> receivingAddresses,
        @Nonnull BigInteger fee,
        @Nonnull String changeAddress)
        throws InsufficientMoneyException, AddressFormatException {

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

            Address address = new Address(PersistentUrls.getInstance()
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
                unspentCoin.getTxOutputN(),
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

        if (change.compareTo(BigInteger.ZERO) > 0) {

            Script changeScript = ScriptBuilder
                .createOutputScript(new Address(networkParams, changeAddress));

            TransactionOutput change_output = new TransactionOutput(networkParams,
                null,
                Coin.valueOf(change.longValue()),
                changeScript.getProgram());
            transaction.addOutput(change_output);
        }
    }

    public static synchronized void signTransaction(Transaction transaction, List<ECKey> keys) {

        Wallet keyBag = Wallet.fromKeys(PersistentUrls.getInstance()
            .getCurrentNetworkParams(), keys);

        SendRequest sendRequest = SendRequest.forTx(transaction);
        keyBag.signTransaction(sendRequest);
    }

    public static synchronized Call<ResponseBody> publishTransaction(Transaction transaction, String apiCode)
        throws IOException {
        PushTx pushTx = new PushTx(BlockchainFramework.getRetrofitServerInstance(), apiCode);
        return pushTx.pushTx(new String(Hex.encode(transaction.bitcoinSerialize())));
    }
}
