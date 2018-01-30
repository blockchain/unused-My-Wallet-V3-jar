package info.blockchain.wallet.payment;

import info.blockchain.api.data.UnspentOutput;
import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.data.Fee;

import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import retrofit2.Call;

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class Payment {

    private static final Logger log = LoggerFactory.getLogger(Payment.class);

    public static final BigInteger PUSHTX_MIN = BigInteger.valueOf(Coin.parseCoin("0.00001").longValue());
    public static final BigInteger DUST = BigInteger.valueOf(Coin.parseCoin("0.000005460").longValue());

    public Payment() {
        // Empty constructor for injection
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fee Handling
    ///////////////////////////////////////////////////////////////////////////
    public BigInteger estimatedFee(int inputs, int outputs, @Nonnull BigInteger feePerKb) {
        return Fees.estimatedFee(inputs, outputs, feePerKb);
    }

    public int estimatedSize(int inputs, int outputs) {
        return Fees.estimatedSize(inputs, outputs);
    }

    public boolean isAdequateFee(int inputs, int outputs, @Nonnull BigInteger absoluteFee) {
        return Fees.isAdequateFee(inputs, outputs, absoluteFee);
    }

    public Fee getDefaultFee() {
        log.info("Using hardcoded default fee");
        try {
            return Fee.fromJson(""
                    + "{\n"
                    + "     \"fee\": 35000,\n"
                    + "     \"surge\": false,\n"
                    + "     \"ok\": true\n"
                    + "}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Coin selection
    ///////////////////////////////////////////////////////////////////////////
    public Call<UnspentOutputs> getUnspentCoins(@Nonnull List<String> addresses) {
        return Coins.getUnspentCoins(addresses);
    }

    public Call<UnspentOutputs> getUnspentBchCoins(@Nonnull List<String> addresses) {
        return Coins.getUnspentBchCoins(addresses);
    }

    /**
     * @return Pair left = sweepable amount, right = absolute fee needed for sweep
     */
    public Pair<BigInteger, BigInteger> getMaximumAvailable(@Nonnull UnspentOutputs unspentCoins,
                                                            @Nonnull BigInteger feePerKb) {
        return Coins.getMaximumAvailable(unspentCoins, feePerKb);
    }

    public SpendableUnspentOutputs getSpendableCoins(@Nonnull UnspentOutputs unspentCoins,
                                                     @Nonnull BigInteger paymentAmount,
                                                     @Nonnull BigInteger feePerKb) {
        return Coins.getMinimumCoinsForPayment(unspentCoins, paymentAmount, feePerKb);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Simple Transaction
    ///////////////////////////////////////////////////////////////////////////
    public Transaction makeSimpleTransaction(NetworkParameters networkParameters,
                                             List<UnspentOutput> unspentCoins,
                                             HashMap<String, BigInteger> receivingAddresses,
                                             BigInteger fee,
                                             @Nullable String changeAddress)
            throws InsufficientMoneyException, AddressFormatException {
        return PaymentTx.makeSimpleTransaction(networkParameters,
                unspentCoins,
                receivingAddresses,
                fee,
                changeAddress);
    }

    public void signSimpleTransaction(NetworkParameters networkParameters,
                                      Transaction transaction,
                                      List<ECKey> keys) {
        PaymentTx.signSimpleTransaction(networkParameters, transaction, keys, false);
    }

    public void signBCHTransaction(NetworkParameters networkParameters,
                                   Transaction transaction,
                                   List<ECKey> keys) {
        PaymentTx.signSimpleTransaction(networkParameters, transaction, keys, true);
    }

    public Call<ResponseBody> publishSimpleTransaction(@Nonnull Transaction transaction) {
        return PaymentTx.publishSimpleBtcTransaction(transaction, BlockchainFramework.getApiCode());
    }

    public Call<ResponseBody> publishSimpleBchTransaction(@Nonnull Transaction transaction) {
        return PaymentTx.publishSimpleBchTransaction(transaction, BlockchainFramework.getApiCode());
    }

}
