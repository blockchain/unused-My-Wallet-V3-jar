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
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.params.BitcoinCashMainNetParams;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

public class BitcoinCashWalletTest extends MockedResponseTest{

    BitcoinCashWallet subject;

    @Mock BlockExplorer blockExplorer = new BlockExplorer();

    private TestVectorBip39List getTestVectors() throws Exception {
        URI uri = getClass().getClassLoader().getResource("hd/test_EN_BIP39.json").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        return TestVectorBip39List.fromJson(response);
    }

    /*
    Really not convenient to have a web call in a constructor, but what can we do.
     */
    private void mockMetadataFetchMagic() {
        int code = 404;
        String response = "{\"message\":\"Not Found\"}";
        mockInterceptor.setResponseList(new LinkedList<Pair>(Arrays.asList(Pair.of(code, response))));
    }

    @Test
    public void getPrivB58() throws Exception {

        TestVectorBip39 vector = getTestVectors().getVectors().get(24);
        mockMetadataFetchMagic();

        subject = BitcoinCashWallet.Companion
            .restore(blockExplorer, BitcoinCashMainNetParams.get(),
                BitcoinCashWallet.Companion.getBITCOINCASH_COIN_PATH(), split(vector.getMnemonic()), vector.getPassphrase());
        subject.addAccount();
        Assert.assertNotNull(subject.getAccountPrivB58(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getPrivB58_badIndex() throws Exception {
        mockMetadataFetchMagic();
        subject = BitcoinCashWallet.Companion.create(blockExplorer, BitcoinCashMainNetParams.get(),
            BitcoinCashWallet.Companion.getBITCOINCASH_COIN_PATH());
        Assert.assertNull(subject.getAccountPrivB58(1));
    }

    @Test
    public void testAddressDerivations() throws Exception {

        TestVectorBip39 vector = getTestVectors().getVectors().get(24);
        mockMetadataFetchMagic();

        subject = BitcoinCashWallet.Companion.restore(blockExplorer, BitcoinCashMainNetParams.get(),
            BitcoinCashWallet.Companion.getBITCOINCASH_COIN_PATH(), split(vector.getMnemonic()),
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
                    subject.getReceiveAddressAt(accountIndex, addressIndex));
                Assert.assertEquals(address.getChangeLegacy(),
                    subject.getChangeAddressAt(accountIndex, addressIndex));

                Assert.assertEquals(address.getReceiveCashAddress(),
                    subject.getReceiveCashAddressAt(accountIndex, addressIndex));
                Assert.assertEquals(address.getChangeCashAddress(),
                    subject.getChangeCashAddressAt(accountIndex, addressIndex));

                addressIndex++;
            }

            accountIndex++;
        }
    }

    public static List<String> split(String words) {
        return new ArrayList<>(Arrays.asList(words.split("\\s+")));
    }
}
