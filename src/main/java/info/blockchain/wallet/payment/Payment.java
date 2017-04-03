package info.blockchain.wallet.payment;

import info.blockchain.api.data.UnspentOutput;
import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.WalletApi;
import info.blockchain.wallet.api.data.Fee;
import info.blockchain.wallet.api.data.FeeList;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

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

    public Observable<FeeList> getDynamicFee() {
        log.info("Fetching dynamic fee list");
        return new WalletApi().getDynamicFee();
    }

    public Fee getDefaultFee() {

        log.info("Using hardcoded default fee");
        Fee fee = null;
        try {
            fee = Fee.fromJson(""
                    + "{\n"
                    + "     \"fee\": 35000,\n"
                    + "     \"surge\": false,\n"
                    + "     \"ok\": true\n"
                    + "}");
        } catch (IOException e) {
            log.error("This should never happen");
            e.printStackTrace();
        }
        return fee;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Coin selection
    ///////////////////////////////////////////////////////////////////////////
    public Call<UnspentOutputs> getUnspentCoins(@Nonnull List<String> addresses) throws IOException {
        return Coins.getUnspentCoins(addresses);
    }

    /**
     * @param unspentCoins
     * @param feePerKb
     * @return Pair left = sweepable amount, right = absolute fee needed for sweep
     */
    public Pair<BigInteger, BigInteger> getSweepableCoins(@Nonnull UnspentOutputs unspentCoins,
                                                          @Nonnull BigInteger feePerKb) {
        return Coins.getSweepableCoins(unspentCoins, feePerKb);
    }

    public SpendableUnspentOutputs getSpendableCoins(@Nonnull UnspentOutputs unspentCoins,
                                                            @Nonnull BigInteger paymentAmount,
                                                            @Nonnull BigInteger feePerKb)  {
        return Coins.getMinimumCoinsForPayment(unspentCoins, paymentAmount, feePerKb);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Transaction
    ///////////////////////////////////////////////////////////////////////////
    public Transaction makeTransaction(@Nonnull List<UnspentOutput> unspentCoins,
                                              @Nonnull HashMap<String, BigInteger> receivingAddresses,
                                              @Nonnull BigInteger fee,
                                              @Nonnull String changeAddress)
            throws InsufficientMoneyException, AddressFormatException {
        return PaymentTx.makeTransaction(unspentCoins, receivingAddresses, fee, changeAddress);
    }

    public void signTransaction(@Nonnull Transaction transaction, @Nonnull List<ECKey> keys) {
        PaymentTx.signTransaction(transaction, keys);
    }

    public Call<ResponseBody> publishTransaction(@Nonnull Transaction transaction)
            throws IOException {
        return PaymentTx.publishTransaction(transaction, BlockchainFramework.getApiCode());
    }
}
