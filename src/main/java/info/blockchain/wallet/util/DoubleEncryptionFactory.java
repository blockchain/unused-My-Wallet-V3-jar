package info.blockchain.wallet.util;

import info.blockchain.wallet.crypto.AESUtil;

import org.spongycastle.util.encoders.Hex;

import java.security.MessageDigest;

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
        return AESUtil.encrypt(encrypted, new CharSequenceX(sharedKey + password2), iterations);
    }

    public String decrypt(String encrypted2, String sharedKey, String password2, int iterations) throws Exception {
        return AESUtil.decrypt(encrypted2, new CharSequenceX(sharedKey + password2), iterations);
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

    public boolean validateSecondPassword(String dpasswordhash, String sharedKey, CharSequenceX password2, int iterations) {
        String dhash = getHash(sharedKey, password2.toString(), iterations);
        return dpasswordhash.equals(dhash);
    }

}