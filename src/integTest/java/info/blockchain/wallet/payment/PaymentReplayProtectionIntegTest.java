package info.blockchain.wallet.payment;

import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.FrameworkInterface;
import info.blockchain.wallet.api.Environment;
import info.blockchain.wallet.api.PersistentUrls;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import okhttp3.ResponseBody;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Call;
import retrofit2.Retrofit;

public class PaymentReplayProtectionIntegTest extends BaseIntegTest {

    private Payment subject = new Payment();

    boolean addReplayProtection = true;

    @BeforeClass
    public static void init() {

        //Initialize framework
        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {
                return getRetrofit("https://api.staging.blockchain.info/", getOkHttpClient());
            }

            @Override
            public Retrofit getRetrofitExplorerInstance() {
                return getRetrofit("https://explorer.staging.blockchain.info/", getOkHttpClient());
            }

            @Override
            public Environment getEnvironment() {
                return Environment.STAGING;
            }

            @Override
            public AbstractBitcoinNetParams getNetworkParameters() {
                return MainNetParams.get();
            }

            @Override
            public String getApiCode() {
                return "123123123-android-test";
            }

            @Override
            public String getDevice() {
                return "Android Integration test";
            }

            @Override
            public String getAppVersion() {
                return "1.0";
            }
        });
    }

    private String getTestData(String file) throws Exception {
        URI uri = getClass().getClassLoader().getResource(file).toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
    }

    @Test
    public void signTransaction() throws Exception {

        //Arrange
        subject = new Payment();

        String unspentApiResponse = getTestData("unspent/unspent_segwit_test.txt");
        UnspentOutputs unspentOutputs = new UnspentOutputs().fromJson(unspentApiResponse);

        String dustApiResponse = getTestData("dust/dust_1.txt");
        DustServiceInput dustServiceInput = new DustServiceInput().fromJson(dustApiResponse);

        //Set up transaction details
        AbstractBitcoinNetParams params = PersistentUrls.getInstance()
            .getCurrentNetworkParams();

        Coin value = Coin.valueOf(112229);
        long feePerByte = 50;
        int size = 376;
        long fee = size * feePerByte;
        long receiveValue = value.longValue() - fee;
        SpendableUnspentOutputs paymentBundle = subject.getSpendableCoins(
            unspentOutputs,
            BigInteger.valueOf(receiveValue),
            BigInteger.valueOf(fee),
            addReplayProtection);

        String receiveAddress = "18tj6jQTnJYoU4MWsRTQvoCLGGfGgn8qkn";
        BigInteger bigIntFee = BigInteger.valueOf(fee);
        BigInteger bigIntAmount = BigInteger.valueOf(receiveValue);

        final HashMap<String, BigInteger> receivers = new HashMap<>();
        receivers.put(receiveAddress, bigIntAmount);

        System.out.println("Creating transaction.");
        System.out.println("Spending from " + paymentBundle.getSpendableOutputs().get(0).getTxHash() + ", amount " + value.toFriendlyString() + " to " + receiveAddress);

        System.out.println("Using dust from " + dustServiceInput.getTransactionOutPoint(params));

        //Build transaction
        Transaction tx = subject.makeNonReplayableTransaction(
            paymentBundle.getSpendableOutputs(),
            receivers,
            bigIntFee,
            null,
            dustServiceInput);

        //Add keys
        List<ECKey> keys = new ArrayList<>();
//        String privateKey = "5JBzbKTaosfyaVnVLvyCH3skWdtNTMRLP1UPVovKcSNHZzSV78D";
//        String format = new PrivateKeyFactory().getFormat(privateKey);
//        ECKey key = new PrivateKeyFactory().getKey(format, privateKey);
//        keys.add(key);
        keys.add(ECKey.fromPrivate(Hex.decode("bda0efb617f4a664706c1b5ae60ed15784c3878e510e55d23fbe3b915e8e94a7")));

        //Sign transaction
        subject.signNonReplayableTransaction(tx, keys);

        System.out.println("\n\nTransaction built! \n " + tx + "\n" + Hex.toHexString(tx.bitcoinSerialize()));

        Assert.assertEquals("ca4e3c636bc9879d7284d1dc99e958dc436a31b3cca859a63427d1d7c292daa5", tx.getHashAsString());
        Call<ResponseBody> call = subject.publishTransactionWithSecret(tx, dustServiceInput.getLockSecret());
        call.execute();
    }
}
