package info.blockchain.wallet.pairing;

import info.blockchain.wallet.crypto.AESUtil;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;

public class Pairing {

    /**
     *
     * @param rawString
     * @return Pair. Left = guid, Right = encryptedPairingCode
     * @throws Exception
     */
    public static Pair getQRComponentsFromRawString(String rawString) throws Exception {

        if (rawString == null || rawString.length() == 0 || rawString.charAt(0) != '1') {
            throw new Exception("QR string null or empty.");
        }

        String[] components = rawString.split("\\|", Pattern.LITERAL);

        if (components.length != 3) {
            throw new Exception("QR string does not have 3 components.");
        }

        if (components[1].length() != 36) {
            throw new Exception("GUID should be 36 characters in length.");
        }

        return Pair.of(components[1], components[2]);
    }

    public static String[] getSharedKeyAndPassword(String encryptedPairingCode, String encryptionPassword) throws Exception {

        String decryptedPairingCode = AESUtil.decrypt(encryptedPairingCode, encryptionPassword, AESUtil.QR_CODE_PBKDF_2ITERATIONS);

        if (decryptedPairingCode == null) {
            throw new Exception("Pairing code decryption failed.");
        }
        String[] sharedKeyAndPassword = decryptedPairingCode.split("\\|", Pattern.LITERAL);

        if (sharedKeyAndPassword.length < 2) {
            throw new Exception("Invalid decrypted pairing code.");
        }

        String sharedKey = sharedKeyAndPassword[0];
        if (sharedKey.length() != 36) {
            throw new Exception("Invalid shared key.");
        }

        return sharedKeyAndPassword;
    }
}