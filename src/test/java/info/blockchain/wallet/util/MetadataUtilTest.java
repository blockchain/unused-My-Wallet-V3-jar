package info.blockchain.wallet.util;

import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.security.KeyPair;

import io.jsonwebtoken.lang.Assert;

public class MetadataUtilTest {

    @Test
    public void testGetMessage() throws Exception {

        String message = "{\"hello\":\"world\"}";
        String expected1 = "eyJoZWxsbyI6IndvcmxkIn0=";
        String expected2 = "LxR+2CipfgdIdi4EZgNOKTT+96WbppXnPZZjdZJ2vwCTojlxqRTl6svwqNJRVM2jCcPBxy+7mRTUfGDzy2gViA==";

        byte[] result = MetadataUtil.message(message.getBytes(), null);
        Assert.isTrue(expected1.equals(Base64.encodeBase64String(result)));
        byte[] magic = MetadataUtil.magic(message.getBytes(), null);
        byte[] nextResult = MetadataUtil.message(message.getBytes(), magic);
        Assert.isTrue(expected2.equals(Base64.encodeBase64String(nextResult)));
    }

    @Test
    public void testMagic() throws Exception {

        String message = "{\"hello\":\"world\"}";
        String expected1 = "LxR+2CipfgdIdi4EZgNOKTT+96WbppXnPZZjdZJ2vwA=";
        String expected2 = "skkJOHg9L6/1OVztbUohjcvVR3cNdRDZ/OJOUdQI41M=";

        byte[] magic = MetadataUtil.magic(message.getBytes(), null);
        Assert.isTrue(expected1.equals(Base64.encodeBase64String(magic)));

        byte[] nextMagic = MetadataUtil.magic(message.getBytes(), magic);
        Assert.isTrue(expected2.equals(Base64.encodeBase64String(nextMagic)));
    }

    @Test
    public void testSharedSecret() throws Exception {

        // Generate ephemeral ECDH keypair
        Wallet walletA = new WalletFactory().restoreWallet("15e23aa73d25994f1921a1256f93f72c",
                "",
                1);
        KeyPair keyPairA = MetadataUtil.getKeyPair(walletA.getMasterKey());
        byte[] publicKeyA = keyPairA.getPublic().getEncoded();

        // Read other's public key:
        Wallet walletB = new WalletFactory().restoreWallet("0660cc198330660cc198330660cc1983",
                "",
                1);
        KeyPair keyPairB = MetadataUtil.getKeyPair(walletB.getMasterKey());
        byte[] publicKeyB = keyPairB.getPublic().getEncoded();

        byte[] secretA = MetadataUtil.getSharedSecret(keyPairA, publicKeyB);
        byte[] secretB = MetadataUtil.getSharedSecret(keyPairB, publicKeyA);

        Assert.isTrue(Hex.toHexString(secretA).equals(Hex.toHexString(secretB)));
    }
}