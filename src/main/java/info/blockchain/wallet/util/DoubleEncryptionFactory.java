package info.blockchain.wallet.util;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.EncryptionException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;

/**
 * Double encryption uses concatenated sharedKey+second password to encrypt data
 */
public class DoubleEncryptionFactory {

    private static final Logger log = LoggerFactory.getLogger(DoubleEncryptionFactory.class);

    public static String encrypt(String encrypted, String sharedKey, String password2, int iterations)
        throws UnsupportedEncodingException, EncryptionException {
        log.info("Encrypting");
        return AESUtil.encrypt(encrypted, sharedKey + password2, iterations);
    }

    public static String decrypt(String encrypted2, String sharedKey, String password2, int iterations)
        throws UnsupportedEncodingException, DecryptionException, InvalidCipherTextException {
        log.info("Decrypting");
        return AESUtil.decrypt(encrypted2, sharedKey + password2, iterations);
    }

    public static String getHash(String sharedKey, String password2, int iterations) {

        byte[] data = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            {
                // n rounds of SHA256
                data = md.digest((sharedKey + password2).getBytes("UTF-8"));
                // first hash already done above
                for (int i = 1; i < iterations; i++) {
                    data = md.digest(data);
                }

            }
        } catch (Exception e) {
            //Second pw or iterations incorrect
            log.error("", e);
            e.printStackTrace();
        }

        if (data != null) {
            return new String(Hex.encode(data));
        } else {
            return null;
        }

    }

    public static void validateSecondPassword(String dpasswordhash, String sharedKey, String password2, int iterations)
        throws DecryptionException {
        log.info("Validating second password");
        String dhash = getHash(sharedKey, password2, iterations);
        if(!dpasswordhash.equals(dhash)) {
            throw new DecryptionException("Double encryption password error!!");
        }
    }
}