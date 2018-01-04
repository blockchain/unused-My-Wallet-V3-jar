package info.blockchain.wallet;

import info.blockchain.wallet.test_data.TestVectorAccount;
import info.blockchain.wallet.test_data.TestVectorAddress;
import info.blockchain.wallet.test_data.TestVectorBip39;
import info.blockchain.wallet.test_data.TestVectorBip39List;
import info.blockchain.wallet.test_data.TestVectorCoin;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.BitcoinCashMainNetParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BitcoinCashWalletBtcChainTest {

    BitcoinCashWallet subject;
    NetworkParameters params = BitcoinCashMainNetParams.get();

    private TestVectorBip39List getTestVectors() throws Exception {
        URI uri = getClass().getClassLoader().getResource("hd/test_EN_BIP39.json").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        return TestVectorBip39List.fromJson(response);
    }

    @Test
    public void getPrivB58() throws Exception {

        TestVectorBip39 vector = getTestVectors().getVectors().get(24);

        subject = new BitcoinCashWallet(split(vector.getMnemonic()),
            vector.getPassphrase(), BitcoinWallet.COIN_PATH, params);
        subject.addAccount();
        Assert.assertNotNull(subject.getPrivB58(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getPrivB58_badIndex() throws Exception {
        subject = new BitcoinCashWallet(params, BitcoinWallet.COIN_PATH);
        Assert.assertNull(subject.getPrivB58(1));
    }

    @Test
    public void testAddressDerivations() throws Exception {

        TestVectorBip39 vector = getTestVectors().getVectors().get(24);

        subject = new BitcoinCashWallet(split(vector.getMnemonic()),
            vector.getPassphrase(), BitcoinWallet.COIN_PATH, params);

        //m / purpose' / coin_type' / account' / change / address_index
        //m/44H/0H/0H/0/0
        TestVectorCoin coin = vector.getCoinTestVectors(subject.getUriScheme(), subject.getPath());

        int accountIndex = 0;
        for (TestVectorAccount account : coin.getAccountList()) {

            subject.addAccount();

            int addressIndex = 0;
            for(TestVectorAddress address : account.getAddresses()) {

                Assert.assertEquals(address.getReceiveLegacy(),
                    subject.getReceiveLegacyAddressAt(accountIndex, addressIndex));
                Assert.assertEquals(address.getChangeLegacy(),
                    subject.getChangeLegacyAddressAt(accountIndex, addressIndex));

                Assert.assertEquals(address.getReceiveSegwit(),
                    subject.getReceiveAddressAt(accountIndex, addressIndex));
                Assert.assertEquals(address.getChangeSegwit(),
                    subject.getChangeAddressAt(accountIndex, addressIndex));

                addressIndex++;
            }

            accountIndex++;
        }
    }

    public static List<String> split(String words) {
        return new ArrayList<>(Arrays.asList(words.split("\\s+")));
    }
}
