package info.blockchain.wallet.util;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.network.NetworkParams;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.crypto.KeyAgreement;

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

    public static KeyPair getKeyPair(ECKey ecKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidParameterSpecException {

        return new KeyPair(getPublicKey(ecKey), getPrivateKey(ecKey));
    }

    public static PrivateKey getPrivateKey(ECKey key) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {

        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC", "SunEC");
        parameters.init(new ECGenParameterSpec("secp256k1"));
        ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
        ECPrivateKeySpec specPrivate = new ECPrivateKeySpec(key.getPrivKey(), ecParameters);

        KeyFactory kf = KeyFactory.getInstance("EC");
        return  kf.generatePrivate(specPrivate);

    }

    public static PublicKey getPublicKey(ECKey key) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {

        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC", "SunEC");
        parameters.init(new ECGenParameterSpec("secp256k1"));
        ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
        ECPublicKeySpec specPublic = new ECPublicKeySpec(new ECPoint(key.getPubKeyPoint().getXCoord().toBigInteger(), key.getPubKeyPoint().getYCoord().toBigInteger()), ecParameters);

        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(specPublic);
    }

    public static byte[] getSharedSecret(KeyPair ourKeyPair, byte[] otherPublicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(otherPublicKeyBytes);
        PublicKey otherPublicKey = keyFactory.generatePublic(pkSpec);

        byte[] ourPublicKeyBytes = ourKeyPair.getPublic().getEncoded();

        // Perform key agreement
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(ourKeyPair.getPrivate());
        keyAgreement.doPhase(otherPublicKey, true);

        // Read shared secret
        byte[] sharedSecret = keyAgreement.generateSecret();

        // Derive a key from the shared secret and both public keys
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        hash.update(sharedSecret);

        // Simple deterministic ordering
        List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(ourPublicKeyBytes), ByteBuffer.wrap(otherPublicKeyBytes));
        Collections.sort(keys);
        hash.update(keys.get(0));
        hash.update(keys.get(1));

        return hash.digest();
    }

    public static byte[] encryptFor(ECKey myKey, String theirXpub, String message) throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchProviderException, InvalidParameterSpecException, InvalidKeyException,
            UnsupportedEncodingException, InvalidCipherTextException {

        // Read other's public key:
        DeterministicKey otherKey = DeterministicKey.deserializeB58(null, theirXpub, NetworkParams.getInstance().getCurrentParams());
        PublicKey otherPublicKey = MetadataUtil.getPublicKey(otherKey);

        // Generate ephemeral ECDH keypair
        KeyPair ourKeyPair = MetadataUtil.getKeyPair(myKey);

//        System.out.println("\tmy priv key: "+ Hex.toHexString(ourKeyPair.getPrivate().getEncoded()));
//        System.out.println("\tmy pub key: "+Hex.toHexString(ourKeyPair.getPublic().getEncoded()));
//        System.out.println("\ttheir pub key: "+Hex.toHexString(otherPublicKey.getEncoded()));

        byte[] secret = MetadataUtil.getSharedSecret(ourKeyPair, otherPublicKey.getEncoded());
//        System.out.println("Shared Secret: "+ Hex.toHexString(secret));

//        System.out.println("Encrypting message with secret...");

        byte[] keyBuffer = Sha256Hash.hash(secret);

        return AESUtil.encryptWithKey(keyBuffer, message);
    }

    public static String decryptFrom(DeterministicKey myKey, String theirXpub, String message) throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchProviderException, InvalidParameterSpecException, InvalidKeyException,
            UnsupportedEncodingException, InvalidCipherTextException {

        // Read other's public key:
        DeterministicKey otherKey = DeterministicKey.deserializeB58(null, theirXpub, NetworkParams.getInstance().getCurrentParams());
        PublicKey otherPublicKey = MetadataUtil.getPublicKey(otherKey);

        // Generate ephemeral ECDH keypair
        KeyPair ourKeyPair = MetadataUtil.getKeyPair(myKey);

//        System.out.println("\tmy priv key: "+Hex.toHexString(ourKeyPair.getPrivate().getEncoded()));
//        System.out.println("\tmy pub key: "+Hex.toHexString(ourKeyPair.getPublic().getEncoded()));
//        System.out.println("\ttheir pub key: "+Hex.toHexString(otherPublicKey.getEncoded()));

        byte[] secret = MetadataUtil.getSharedSecret(ourKeyPair, otherPublicKey.getEncoded());
//        System.out.println("Shared Secret: "+ Hex.toHexString(secret));

        byte[] keyBuffer = Sha256Hash.hash(secret);
        String decryptedMessage = AESUtil.decryptWithKey(keyBuffer, message);

//        System.out.println("Decrypting message with secret...");

        return decryptedMessage;
    }
}
