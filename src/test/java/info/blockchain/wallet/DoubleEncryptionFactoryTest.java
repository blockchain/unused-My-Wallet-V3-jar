package info.blockchain.wallet;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by riaanvos on 20/04/16.
 */
public class DoubleEncryptionFactoryTest {

    String cleartext = "test data";
    CharSequenceX pw = new CharSequenceX("password");
    int iterations = AESUtil.PinPbkdf2Iterations;
    String sharedKey = "524b5e9f-72ea-4690-b28c-8c1cfce65ca0";

    @Test
    public void validateSecondPasswordTest() {

        DoubleEncryptionFactory doubleEncryptionFactory = DoubleEncryptionFactory.getInstance();
        String hash = doubleEncryptionFactory.getHash(sharedKey, pw.toString(), iterations);
        assertThat("Validate second password failed", doubleEncryptionFactory.validateSecondPassword(hash, sharedKey, pw, iterations));
    }

    @Test
    public void doubleEncryptionTest() {

        DoubleEncryptionFactory doubleEncryptionFactory = DoubleEncryptionFactory.getInstance();
        String encrypted = doubleEncryptionFactory.encrypt(cleartext, sharedKey, pw.toString(), iterations);
        assertThat("Double encryption failed", encrypted != null);
    }

    @Test
    public void doubleDecryptionTest() {

        DoubleEncryptionFactory doubleEncryptionFactory = DoubleEncryptionFactory.getInstance();
        String encrypted = doubleEncryptionFactory.encrypt(cleartext, sharedKey, pw.toString(), iterations);
        String decrypted = doubleEncryptionFactory.decrypt(encrypted, sharedKey, pw.toString(), iterations);
        assertThat("Double decryption failed", cleartext.equals(decrypted));
    }

    @Test
    public void doubleDecryptionFailPasswordTest() {

        DoubleEncryptionFactory doubleEncryptionFactory = DoubleEncryptionFactory.getInstance();
        String encrypted = doubleEncryptionFactory.encrypt(cleartext, sharedKey, pw.toString(), iterations);
        String decrypted = doubleEncryptionFactory.decrypt(encrypted, sharedKey, "bogus", iterations);
        assertThat("Double decryption failed", !cleartext.equals(decrypted));
    }

    @Test
    public void doubleDecryptionFailIterationTest() {

        DoubleEncryptionFactory doubleEncryptionFactory = DoubleEncryptionFactory.getInstance();
        String encrypted = doubleEncryptionFactory.encrypt(cleartext, sharedKey, pw.toString(), iterations);
        String decrypted = doubleEncryptionFactory.decrypt(encrypted, sharedKey, pw.toString(), iterations + 1);
        assertThat("Double decryption failed", !cleartext.equals(decrypted));
    }
}
