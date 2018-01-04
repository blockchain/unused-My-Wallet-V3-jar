package info.blockchain.wallet.crypto;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Joiner;
import info.blockchain.wallet.BitcoinWallet;
import info.blockchain.wallet.test_data.TestVectorBip39;
import info.blockchain.wallet.test_data.TestVectorBip39List;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.BitcoinMainNetParams;
import org.junit.Assert;
import org.junit.Test;

public class DeterministicWalletTest {

    BitcoinWallet subject; //Any DeterministicWallet will do
    NetworkParameters params = BitcoinMainNetParams.get(); //Won't affect this test but needed.

    private TestVectorBip39List getTestVectors() throws Exception {
        URI uri = getClass().getClassLoader().getResource("hd/test_EN_BIP39.json").toURI();
        String response = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
        return TestVectorBip39List.fromJson(response);
    }

    /**
     * Creates random wallet
     */
    @Test
    public void construct1() throws Exception {

        subject = new BitcoinWallet(params);

        Assert.assertNotNull(subject.getSeedHex());
        Assert.assertNotNull(subject.getEntropyHex());
        assertEquals(12, subject.getMnemonic().size());
        assertEquals("", subject.getPassphrase());
        assertEquals(1, subject.getAccountTotal());
    }

    /**
     * Creates random wallet with passphrase
     */
    @Test
    public void construct2() throws Exception {

        subject = new BitcoinWallet("somePassphrase", params);

        Assert.assertNotNull(subject.getSeedHex());
        Assert.assertNotNull(subject.getEntropyHex());
        assertEquals(12, subject.getMnemonic().size());
        assertEquals("somePassphrase", subject.getPassphrase());
        assertEquals(1, subject.getAccountTotal());
    }

    /**
     * Wallet from given entropy and passphrase
     *
     * @throws Exception
     */
    @Test
    public void construct3() throws Exception {
        TestVectorBip39List testVectors = getTestVectors();

        for (TestVectorBip39 vector : testVectors.getVectors()) {

            subject = new BitcoinWallet(vector.getEntropy(), vector.getPassphrase(), params);

            assertEquals(vector.getSeed(), subject.getSeedHex());
            assertEquals(vector.getEntropy(), subject.getEntropyHex());
            assertEquals(vector.getMnemonic(), Joiner.on(" ").join(subject.getMnemonic()));
            assertEquals(vector.getPassphrase(), subject.getPassphrase());
            assertEquals(1, subject.getAccountTotal());
        }
    }

    /**
     * Wallet from given mnemonic and passphrase
     *
     * @throws Exception
     */
    @Test
    public void construct4() throws Exception {
        TestVectorBip39List testVectors = getTestVectors();

        for (TestVectorBip39 vector : testVectors.getVectors()) {

            subject = new BitcoinWallet(split(vector.getMnemonic()), vector.getPassphrase(),
                params);

            assertEquals(vector.getSeed(), subject.getSeedHex());
            assertEquals(vector.getEntropy(), subject.getEntropyHex());
            assertEquals(vector.getMnemonic(), Joiner.on(" ").join(subject.getMnemonic()));
            assertEquals(vector.getPassphrase(), subject.getPassphrase());
            assertEquals(0, subject.getAccountTotal());
        }
    }

    /**
     * Ensure restored wallet with different passphrase results in different seed, but still same
     * entropy and mnemonic.
     *
     * @throws Exception
     */
    @Test
    public void passphrase() throws Exception {
        TestVectorBip39List testVectors = getTestVectors();

        TestVectorBip39 vector = testVectors.getVectors().get(24);

        BitcoinWallet subject1 = new BitcoinWallet(split(vector.getMnemonic()),
            vector.getPassphrase(), params);
        BitcoinWallet subject2 = new BitcoinWallet(split(vector.getMnemonic()), "Other passphrase",
            params);

        Assert.assertNotEquals(subject1.getSeedHex(), subject2.getSeedHex());
        assertEquals(subject1.getEntropyHex(), subject2.getEntropyHex());
        assertEquals(subject1.getMnemonic(), subject2.getMnemonic());
    }

    @Test
    public void addAccount() throws Exception {

        subject = new BitcoinWallet(params);

        Assert.assertNotNull(subject.getSeedHex());
        Assert.assertNotNull(subject.getEntropyHex());
        assertEquals(12, subject.getMnemonic().size());
        assertEquals("", subject.getPassphrase());
        assertEquals(1, subject.getAccountTotal());

        subject.addAccount();
        assertEquals(2, subject.getAccountTotal());
        subject.addAccount();
        assertEquals(3, subject.getAccountTotal());
    }

    @Test
    public void getReceiveECKeyAt() throws Exception {

        subject = new BitcoinWallet(params);
        TestVectorBip39List testVectors = getTestVectors();
        TestVectorBip39 vector = testVectors.getVectors().get(24);

        subject = new BitcoinWallet(split(vector.getMnemonic()),
            vector.getPassphrase(), params);

        assertEquals("0660cc198330660cc198330660cc1983", subject.getEntropyHex());
        System.out.println(subject.getMnemonic());

        subject.addAccount();
        assertEquals("03c6d9cc725bb7e19c026df03bf693ee1171371a8eaf25f04b7a58f6befabcd38c",
            Hex.encodeHexString(subject.getReceiveECKeyAt(0, 0).getPubKey()));
        assertEquals("02a7a079c1ef9916b289c2ff21a992c808d0de3dfcf8a9f163205c5c9e21f55d5c",
            Hex.encodeHexString(subject.getReceiveECKeyAt(0, 5).getPubKey()));
        assertEquals("038bac33bcdaeec5626e2f2c5680a9fdc5e551d4e1167f272825bea98e6158d4c8",
            Hex.encodeHexString(subject.getReceiveECKeyAt(0, 10).getPubKey()));

        subject.addAccount();
        assertEquals("034fd90850dfab2ae698c9cf58ce3182d4d06676e1abf012331659c9434098100a",
            Hex.encodeHexString(subject.getReceiveECKeyAt(1, 0).getPubKey()));
        assertEquals("020b1383f111bcc2c7af28107e08e1cc652f866bc762d5f39cad75102999cd1d05",
            Hex.encodeHexString(subject.getReceiveECKeyAt(1, 5).getPubKey()));
        assertEquals("022e3589f454389f18fc92b84ad717f6953c230a0cb63ffb8bb46dd8f3cdd5f8b3",
            Hex.encodeHexString(subject.getReceiveECKeyAt(1, 10).getPubKey()));
    }

    public static List<String> split(String words) {
        return new ArrayList<>(Arrays.asList(words.split("\\s+")));
    }
}
