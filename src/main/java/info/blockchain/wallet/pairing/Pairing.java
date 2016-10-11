package info.blockchain.wallet.pairing;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.util.CharSequenceX;

import java.util.regex.Pattern;

public class Pairing {

    public PairingQRComponents getQRComponentsFromRawString(String rawString) throws Exception {

        PairingQRComponents result = new PairingQRComponents();

        if (rawString == null || rawString.length() == 0 || rawString.charAt(0) != '1') {
            throw new Exception("QR string null or empty.");
        }

        String[] components = rawString.split("\\|", Pattern.LITERAL);

        if (components.length != 3) {
            throw new Exception("QR string does not have 3 components.");
        }

        result.guid = components[1];
        if (result.guid.length() != 36) {
            throw new Exception("GUID should be 36 characters in length.");
        }

        result.encryptedPairingCode = components[2];

        return result;
    }

    public String[] getSharedKeyAndPassword(String encryptedPairingCode, String encryptionPassword) throws Exception {

        String decryptedPairingCode = AESUtil.decrypt(encryptedPairingCode, new CharSequenceX(encryptionPassword), AESUtil.QR_CODE_PBKDF_2ITERATIONS);

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

