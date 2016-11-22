package info.blockchain.wallet.util;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.spongycastle.util.encoders.Base64;

public class MetadataUtil {

    public static byte[] getMessage(byte[] payload, byte[] prevMagicHash) {
        if (prevMagicHash == null)
            return payload;
        else {
            final byte[] payloadHash = Sha256Hash.hash(payload);
            final byte[] message = new byte[64];
            System.arraycopy(prevMagicHash, 0, message, 0, 32);
            System.arraycopy(payloadHash, 0, message, 32, 32);
            return message;
        }
    }

    public static byte[] magic(byte[] payload, byte[] prevMagicHash){
        byte[] msg = getMessage(payload, prevMagicHash);
        return magicHash(msg);
    }

    private static byte[] magicHash(byte[] message) {
        byte[] messageBytes = Utils.formatMessageForSigning(Base64.toBase64String(message));
        return Sha256Hash.hashTwice(messageBytes);
    }
}
