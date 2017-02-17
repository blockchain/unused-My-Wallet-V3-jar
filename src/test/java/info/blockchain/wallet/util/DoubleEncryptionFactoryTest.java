package info.blockchain.wallet.util;



import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.DecryptionException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by riaanvos on 20/04/16.
 */
public class DoubleEncryptionFactoryTest {

    String cleartext = "test data";
    String pw = "password";
    int iterations = AESUtil.PIN_PBKDF2_ITERATIONS;
    String sharedKey = "524b5e9f-72ea-4690-b28c-8c1cfce65ca0";

    @Test
    public void validateSecondPasswordTest() throws DecryptionException {

        String hash = DoubleEncryptionFactory.getHash(sharedKey, pw, iterations);
        DoubleEncryptionFactory.validateSecondPassword(hash, sharedKey, pw, iterations);
        Assert.assertTrue(true);
    }

    @Test
    public void doubleEncryptionTest() {

        String encrypted = null;
        try {
            encrypted = DoubleEncryptionFactory.encrypt(cleartext, sharedKey, pw.toString(), iterations);
            Assert.assertNotNull(encrypted);
        } catch (Exception e) {
            Assert.fail();
        }

    }

    @Test
    public void doubleDecryptionTest() {

        try {
            String encrypted = DoubleEncryptionFactory.encrypt(cleartext, sharedKey, pw.toString(), iterations);
            String decrypted = DoubleEncryptionFactory.decrypt(encrypted, sharedKey, pw.toString(), iterations);

            Assert.assertTrue("Double decryption failed", cleartext.equals(decrypted));

        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void doubleDecryptionFailPasswordTest() {

        try {
            String encrypted = DoubleEncryptionFactory.encrypt(cleartext, sharedKey, pw.toString(), iterations);
            String decrypted = DoubleEncryptionFactory.decrypt(encrypted, sharedKey, "bogus", iterations);
            Assert.assertNotEquals(cleartext, decrypted);
        } catch (Exception e) {
            Assert.assertTrue("Double decryption failed", true);
        }
    }

    @Test
    public void doubleDecryptionFailIterationTest() {

        try{
            String encrypted = DoubleEncryptionFactory.encrypt(cleartext, sharedKey, pw.toString(), iterations);
            String decrypted = DoubleEncryptionFactory.decrypt(encrypted, sharedKey, pw.toString(), iterations + 1);
            Assert.assertNotEquals(cleartext, decrypted);
        } catch (Exception e) {
            Assert.assertTrue("Double decryption failed", true);
        }
    }
}
