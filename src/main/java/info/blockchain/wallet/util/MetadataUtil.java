package info.blockchain.wallet.util;

import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
        byte[] messageBytes = Utils.formatMessageForSigning(Base64.encodeBase64String(message));
        return Sha256Hash.hashTwice(messageBytes);
    }
}
