package info.blockchain.wallet.crypto;

import info.blockchain.wallet.util.CharSequenceX;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by riaanvos on 19/04/16.
 */
public class AESUtilTest {

    String cleartext = "test data";
    CharSequenceX pw = new CharSequenceX("password");
    int iterations = AESUtil.QrCodePbkdf2Iterations;

    @Test
    public void encryptTest() {

        String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
        assertThat("Encryption failed", encrypted != null);
    }

    @Test
    public void decryptTest() {

        String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
        String decrypted = AESUtil.decrypt(encrypted, pw, iterations);

        assertThat("Decryption failed", decrypted.equals(cleartext));
    }

    @Test
    public void decryptFailIncorrectPasswordTest() {

        String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
        String decrypted = AESUtil.decrypt(encrypted, new CharSequenceX("bogus"), iterations);

        assertThat("Decryption successful with incorrect password", decrypted == null);
    }

    @Test
    public void decryptFailIncorrectIterationsTest() {

        String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
        String decrypted = AESUtil.decrypt(encrypted, pw, iterations + 1);

        assertThat("Decryption successful with incorrect iterations", decrypted == null);
    }
}
