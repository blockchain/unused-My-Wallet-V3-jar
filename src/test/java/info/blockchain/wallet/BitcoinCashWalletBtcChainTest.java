package info.blockchain.wallet;

import info.blockchain.api.blockexplorer.BlockExplorer;
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
import org.junit.Test;
import org.mockito.Mock;

public class BitcoinCashWalletBtcChainTest {

    private BitcoinCashWallet subject;
    private NetworkParameters params = BitcoinCashMainNetParams.get();
    @Mock private BlockExplorer blockExplorer = new BlockExplorer();

    private TestVectorBip39List getTestVectors() throws Exception {
        URI uri = getClass().getClassLoader().getResource("hd/test_EN_BIP39.json").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        return TestVectorBip39List.fromJson(response);
    }

    @Test
    public void getPrivB58() throws Exception {

        TestVectorBip39 vector = getTestVectors().getVectors().get(24);

        subject = BitcoinCashWallet.Companion
            .restore(blockExplorer, params, BitcoinCashWallet.BITCOINCASH_COIN_PATH, split(vector.getMnemonic()), vector.getPassphrase());
        subject.addAccount();
        Assert.assertNotNull(subject.getAccountPrivB58(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getPrivB58_badIndex() {
        subject = BitcoinCashWallet.Companion
            .create(blockExplorer, params, BitcoinCashWallet.BITCOIN_COIN_PATH);
        Assert.assertNull(subject.getAccountPrivB58(1));
    }

    @Test
    public void testAddressDerivations() throws Exception {

        TestVectorBip39 vector = getTestVectors().getVectors().get(24);

        subject = BitcoinCashWallet.Companion
            .restore(blockExplorer, params, BitcoinCashWallet.BITCOIN_COIN_PATH, split(vector.getMnemonic()),
            vector.getPassphrase());

        //m / purpose' / coin_type' / account' / change / address_index
        //m/44H/0H/0H/0/0
        TestVectorCoin coin = vector.getCoinTestVectors(subject.getUriScheme(), subject.getPath());

        int accountIndex = 0;
        for (TestVectorAccount account : coin.getAccountList()) {

            subject.addAccount();

            int addressIndex = 0;
            for(TestVectorAddress address : account.getAddresses()) {

                Assert.assertEquals(address.getReceiveLegacy(),
                    subject.getReceiveAddressAtArbitraryPosition(accountIndex, addressIndex));
                Assert.assertEquals(address.getChangeLegacy(),
                    subject.getChangeAddressAtArbitraryPosition(accountIndex, addressIndex));

                Assert.assertEquals(address.getReceiveCashAddress(),
                    subject.getReceiveCashAddressAt(accountIndex, addressIndex));
                Assert.assertEquals(address.getChangeCashAddress(),
                    subject.getChangeCashAddressAt(accountIndex, addressIndex));

                addressIndex++;
            }

            accountIndex++;
        }
    }

    private static List<String> split(String words) {
        return new ArrayList<>(Arrays.asList(words.split("\\s+")));
    }
}
