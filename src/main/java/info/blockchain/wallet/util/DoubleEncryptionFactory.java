package info.blockchain.wallet.util;

import info.blockchain.wallet.crypto.AESUtil;
import java.security.MessageDigest;
import org.spongycastle.util.encoders.Hex;

public class DoubleEncryptionFactory {

    private static DoubleEncryptionFactory instance = null;

    private DoubleEncryptionFactory() {
    }

    public static DoubleEncryptionFactory getInstance() {

        if (instance == null) {
            instance = new DoubleEncryptionFactory();
        }

        return instance;
    }

    public String encrypt(String encrypted, String sharedKey, String password2, int iterations) throws Exception {
        return AESUtil.encrypt(encrypted, sharedKey + password2, iterations);
    }

    public String decrypt(String encrypted2, String sharedKey, String password2, int iterations) throws Exception {
        return AESUtil.decrypt(encrypted2, sharedKey + password2, iterations);
    }

    public String getHash(String sharedKey, String password2, int iterations) {

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
            e.printStackTrace();
        }

        if (data != null) {
            return new String(Hex.encode(data));
        } else {
            return null;
        }

    }

    public boolean validateSecondPassword(String dpasswordhash, String sharedKey, String password2, int iterations) {
        String dhash = getHash(sharedKey, password2, iterations);
        return dpasswordhash.equals(dhash);
    }

}