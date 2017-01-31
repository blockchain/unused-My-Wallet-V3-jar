package info.blockchain.wallet.util;

import com.lambdaworks.codec.Base64;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nonnull;

public final class Base64Util {

    private Base64Util() {
        throw new AssertionError("Cannot be instantiated");
    }

    /**
     * Returns a UTF-8 String from a byte[] input in Base64 format. This helper method replaces
     * {@link org.apache.commons.codec.binary.Base64#encodeBase64String(byte[])}, as Android devices
     * have commons.codec V1.3 in the classpath and the method was added in 1.4. Because the lib is
     * in the boot classpath, the packaged library here (1.10) isn't loaded, and as such invoking
     * the method throws a {@link NoSuchMethodError} exception.
     *
     * @param input A byte array in Base64
     * @return A String in UTF-8
     */
    public static String encodeBase64String(byte[] input) {
        return newUtf8String(getStringToBytes(encode(input)));
    }

    /**
     * Returns a byte array from a Base64 encoded String. This helper method replaces {@link
     * org.apache.commons.codec.binary.Base64#decodeBase64(byte[])}
     *
     * @param input A String in Base64 format
     * @return A decoded byte array
     */
    public static byte[] decodeBase64(@Nonnull String input) {
        return newUtf8String(decode(input)).getBytes();
    }

    private static byte[] decode(@Nonnull String input) {
        return Base64.decode(input.toCharArray());
    }

    private static char[] encode(byte[] bytes) {
        return Base64.encode(bytes);
    }

    private static byte[] getStringToBytes(char[] chars) {
        return new String(chars).getBytes();
    }

    private static String newUtf8String(final byte[] bytes) {
        try {
            return bytes == null ? null : new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
