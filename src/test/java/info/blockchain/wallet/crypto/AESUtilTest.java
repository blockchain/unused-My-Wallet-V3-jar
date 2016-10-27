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

        boolean success;
        try {
            AESUtil.encrypt(cleartext, pw, iterations);
            success = true;
        } catch (Exception e) {
            success = false;
        }

        assertThat("Encryption failed", success);
    }

    @Test
    public void decryptTest() {
        try {
            String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
            String decrypted = AESUtil.decrypt(encrypted, pw, iterations);
            assertThat("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            assertThat("Encryption failed as expected.", false);
        }
    }

    @Test
    public void decryptFailIncorrectPasswordTest() {
        try {
            String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
            String decrypted = AESUtil.decrypt(encrypted, new CharSequenceX("bogus"), iterations);

            assertThat("Decryption (cleartext 'test data') successful with incorrect password as : " + decrypted, false);
        } catch (Exception e) {
            assertThat("Encryption failed as expected.", true);
        }
    }

    @Test
    public void decryptFailIncorrectIterationsTest() {
        try {
            String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
            String decrypted = AESUtil.decrypt(encrypted, pw, iterations + 1);
            assertThat("Decryption (cleartext 'test data') successful with incorrect iterations as : " + decrypted, cleartext.equals(decrypted));
        } catch (Exception e) {
            assertThat("Encryption failed as expected.", true);
        }
    }

    @Test
    public void decrypting_OFB_ISO10126_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, new ISO10126d2Padding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, new ISO10126d2Padding());

            assertThat("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            assertThat("Encryption failed as expected.", false);
        }
    }

    @Test
    public void decrypting_OFB_zeroPadding_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, new ZeroBytePadding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, new ZeroBytePadding());

            assertThat("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            assertThat("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_OFB_ISO7816_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, new ISO7816d4Padding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, new ISO7816d4Padding());

            assertThat("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            assertThat("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_OFB_NoPadding_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, null);
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, null);

            assertThat("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            assertThat("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_CBC_ISO10126_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_CBC, new ISO10126d2Padding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_CBC, new ISO10126d2Padding());

            assertThat("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            assertThat("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_CBC_zeroPadding_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_CBC, new ZeroBytePadding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_CBC, new ZeroBytePadding());

            assertThat("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            assertThat("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_CBC_ISO7816_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_CBC, new ISO7816d4Padding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_CBC, new ISO7816d4Padding());

            assertThat("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            assertThat("Encryption failed", false);
        }
    }

    @Test
    public void encrypt_withNullPassword_shouldFail() {

        try {
            AESUtil.encrypt(cleartext, null, iterations);
            assertThat("Encryption successful with NULL password", false);
        } catch (Exception e) {
            assertThat("Encryption failed as expected.", true);
        }
    }

    @Test
    public void decrypt_withNullPassword_shouldFail() {

        try {
            String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
            String decrypted = AESUtil.decrypt(encrypted, null, iterations);
            assertThat("Decryption successful with NULL password", false);
        } catch (Exception e) {
            assertThat("Decryption failed as expected.", true);
        }
    }
}
