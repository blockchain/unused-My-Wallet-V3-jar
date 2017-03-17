package info.blockchain.wallet.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    public static byte[] xor(byte[] a, byte[] b) {

        if (a.length != b.length) {
            return null;
        }

        byte[] ret = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            ret[i] = (byte) ((int) b[i] ^ (int) a[i]);
        }

        return ret;
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash
     * again. This is standard procedure in Bitcoin. The resulting hash is in big endian form.
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static byte[] doubleDigest(byte[] input, int offset, int length) {

        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("SHA-256");

            synchronized (digest) {
                digest.reset();
                digest.update(input, offset, length);
                byte[] first = digest.digest();
                return digest.digest(first);
            }

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }
}