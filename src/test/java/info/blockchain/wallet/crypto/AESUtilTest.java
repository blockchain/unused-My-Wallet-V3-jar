package info.blockchain.wallet.crypto;

import info.blockchain.wallet.util.CharSequenceX;

import org.junit.Test;
import org.spongycastle.crypto.paddings.ISO10126d2Padding;
import org.spongycastle.crypto.paddings.ISO7816d4Padding;
import org.spongycastle.crypto.paddings.ZeroBytePadding;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by riaanvos on 19/04/16.
 */
public class AESUtilTest {

    String cleartext = "test data";
    CharSequenceX pw = new CharSequenceX("password");
    int iterations = AESUtil.QR_CODE_PBKDF_2ITERATIONS;

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

        assertThat("Decryption (cleartext 'test data') successful with incorrect password as : " + decrypted, decrypted == null || !decrypted.equals(cleartext));
    }

    @Test
    public void decryptFailIncorrectIterationsTest() {

        String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
        String decrypted = AESUtil.decrypt(encrypted, pw, iterations + 1);
        System.out.println("decrypted: " + decrypted);
        assertThat("Decryption (cleartext 'test data') successful with incorrect iterations as : " + decrypted, decrypted == null || !decrypted.equals(cleartext));
    }

    @Test
    public void decrypting_OFB_ISO10126_shouldPass() {

        String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, new ISO10126d2Padding());
        String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, new ISO10126d2Padding());

        assertThat("Decryption failed", decrypted.equals(cleartext));
    }

    @Test
    public void decrypting_OFB_zeroPadding_shouldPass() {

        String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, new ZeroBytePadding());
        String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, new ZeroBytePadding());

        assertThat("Decryption failed", decrypted.equals(cleartext));
    }

    @Test
    public void decrypting_OFB_ISO7816_shouldPass() {

        String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, new ISO7816d4Padding());
        String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, new ISO7816d4Padding());

        assertThat("Decryption failed", decrypted.equals(cleartext));
    }

    @Test
    public void decrypting_OFB_NoPadding_shouldPass() {

        String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, null);
        String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, null);

        assertThat("Decryption failed", decrypted.equals(cleartext));
    }

    @Test
    public void decrypting_CBC_ISO10126_shouldPass() {

        String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_CBC, new ISO10126d2Padding());
        String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_CBC, new ISO10126d2Padding());

        assertThat("Decryption failed", decrypted.equals(cleartext));
    }

    @Test
    public void decrypting_CBC_zeroPadding_shouldPass() {

        String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_CBC, new ZeroBytePadding());
        String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_CBC, new ZeroBytePadding());

        assertThat("Decryption failed", decrypted.equals(cleartext));
    }

    @Test
    public void decrypting_CBC_ISO7816_shouldPass() {

        String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_CBC, new ISO7816d4Padding());
        String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_CBC, new ISO7816d4Padding());

        assertThat("Decryption failed", decrypted.equals(cleartext));
    }
}
