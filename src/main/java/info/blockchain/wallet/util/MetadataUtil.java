package info.blockchain.wallet.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

public class MetadataUtil {

    public static byte[] message(byte[] payload, byte[] prevMagicHash) throws IOException {
        if (prevMagicHash == null)
            return payload;
        else {
            final byte[] payloadHash = Sha256Hash.hash(payload);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(prevMagicHash);
            outputStream.write(payloadHash);

            return outputStream.toByteArray();
        }
    }

    public static byte[] magic(byte[] payload, byte[] prevMagicHash) throws IOException {
        byte[] msg = message(payload, prevMagicHash);
        return magicHash(msg);
    }

    private static byte[] magicHash(byte[] message) {
        byte[] messageBytes = Utils.formatMessageForSigning(Base64Util.encodeBase64String(message));
        return Sha256Hash.hashTwice(messageBytes);
    }

    public static DeterministicKey deriveMetadataNode(DeterministicKey node) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return HDKeyDerivation.deriveChildKey(node, getPurpose("metadata") | ChildNumber.HARDENED_BIT);
    }

    public static DeterministicKey deriveSharedMetadataNode(DeterministicKey node) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return HDKeyDerivation.deriveChildKey(node, getPurpose("mdid") | ChildNumber.HARDENED_BIT);
    }

    /**
     * BIP 43 purpose needs to be 31 bit or less. For lack of a BIP number we take the first 31 bits
     * of the SHA256 hash of a reverse domain.
     */
    private static int getPurpose(String sub) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String text = "info.blockchain."+sub;
        md.update(text.getBytes("UTF-8"));
        byte[] hash = md.digest();
        byte[] slice = Arrays.copyOfRange(hash, 0, 4);

        return (int) (Utils.readUint32BE(slice, 0) & 0x7FFFFFFF); // 510742
    }

    public static DeterministicKey deriveHardened(DeterministicKey node, int type) {
        return HDKeyDerivation.deriveChildKey(node, type | ChildNumber.HARDENED_BIT);
    }
}
