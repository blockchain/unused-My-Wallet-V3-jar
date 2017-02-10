package info.blockchain.wallet.crypto;



import org.bitcoinj.core.Sha256Hash;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.crypto.paddings.ISO10126d2Padding;
import org.spongycastle.crypto.paddings.ISO7816d4Padding;
import org.spongycastle.crypto.paddings.ZeroBytePadding;

/**
 * Created by riaanvos on 19/04/16.
 */
public class AESUtilTest {

    String cleartext = "test data";
    String pw = "password";
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

        Assert.assertTrue("Encryption failed", success);
    }

    @Test
    public void decryptTest() {
        try {
            String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
            String decrypted = AESUtil.decrypt(encrypted, pw, iterations);
            Assert.assertTrue("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed as expected.", false);
        }
    }

    @Test
    public void decryptFailIncorrectPasswordTest() {
        try {
            String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
            String decrypted = AESUtil.decrypt(encrypted, "bogus", iterations);
            Assert.assertNotEquals(cleartext, decrypted);
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed as expected.", true);
        }
    }

    @Test
    public void decryptFailIncorrectIterationsTest() {
        try {
            String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
            String decrypted = AESUtil.decrypt(encrypted, pw, iterations + 1);
            Assert.assertNotEquals(cleartext, decrypted);
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed as expected.", true);
        }
    }

    @Test
    public void decrypting_OFB_ISO10126_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, new ISO10126d2Padding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, new ISO10126d2Padding());

            Assert.assertTrue("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed as expected.", false);
        }
    }

    @Test
    public void decrypting_OFB_zeroPadding_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, new ZeroBytePadding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, new ZeroBytePadding());

            Assert.assertTrue("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_OFB_ISO7816_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, new ISO7816d4Padding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, new ISO7816d4Padding());

            Assert.assertTrue("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_OFB_NoPadding_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_OFB, null);
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_OFB, null);

            Assert.assertTrue("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_CBC_ISO10126_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_CBC, new ISO10126d2Padding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_CBC, new ISO10126d2Padding());

            Assert.assertTrue("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_CBC_zeroPadding_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_CBC, new ZeroBytePadding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_CBC, new ZeroBytePadding());

            Assert.assertTrue("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed", false);
        }
    }

    @Test
    public void decrypting_CBC_ISO7816_shouldPass() {
        try {
            String encrypted = AESUtil.encryptWithSetMode(cleartext, pw, iterations, AESUtil.MODE_CBC, new ISO7816d4Padding());
            String decrypted = AESUtil.decryptWithSetMode(encrypted, pw, iterations, AESUtil.MODE_CBC, new ISO7816d4Padding());

            Assert.assertTrue("Decryption failed", decrypted.equals(cleartext));
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed", false);
        }
    }

    @Test
    public void encrypt_withNullPassword_shouldFail() {

        try {
            AESUtil.encrypt(cleartext, null, iterations);
            Assert.assertTrue("Encryption successful with NULL password", false);
        } catch (Exception e) {
            Assert.assertTrue("Encryption failed as expected.", true);
        }
    }

    @Test
    public void decrypt_withNullPassword_shouldFail() {

        try {
            String encrypted = AESUtil.encrypt(cleartext, pw, iterations);
            String decrypted = AESUtil.decrypt(encrypted, null, iterations);
            Assert.assertTrue("Decryption successful with NULL password", false);
        } catch (Exception e) {
            Assert.assertTrue("Decryption failed as expected.", true);
        }
    }

    @Test
    public void decrypt_withKey() throws Exception {

        String key = "80a150a8aae2c159cdad74fd675f03238fe5b8c884b976f8abfe56e621fd7ee1";//"Dylan Thomas";
        String data = "Do not go gentle into that good night.";

        byte[] keyBytes = Sha256Hash.hash(key.getBytes());
        byte[] encrypted = AESUtil.encryptWithKey(keyBytes, data);
        String decrypted = AESUtil.decryptWithKey(keyBytes, new String(encrypted));

        Assert.assertTrue("Decryption", decrypted.equals(data));
    }
}
