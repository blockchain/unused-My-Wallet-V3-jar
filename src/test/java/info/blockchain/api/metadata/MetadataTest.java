package info.blockchain.api.metadata;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;

import io.jsonwebtoken.lang.Assert;

/**
 * Integration Test
 */
public class MetadataTest {

    Metadata mds;

    ECKey senderKey;//extended public senderKey derived from master senderKey
    String senderMdid;

    ECKey recipientKey;
    String recipientMdid;

    @Before
    public void setup() throws Exception {

        mds = new Metadata();

        senderKey = getRandomECKey();
        senderMdid = senderKey.toAddress(MainNetParams.get()).toString();

        recipientKey = getRandomECKey();
        recipientMdid = recipientKey.toAddress(MainNetParams.get()).toString();
    }

    private ECKey getRandomECKey() throws Exception {

        byte[] rdata = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(rdata);
        return ECKey.fromPrivate(rdata, true);
    }

    @Test
    public void testGetNonce() throws Exception {
        String nonce = mds.getNonce();
        Assert.hasText(nonce);
    }

    @Test
    public void testGetToken() throws Exception {
        String token = mds.getToken(senderKey);
        Assert.hasText(token);
    }

    @Test
    public void testGetTrustedList() throws Exception {
        String token = mds.getToken(senderKey);
        String result = mds.getTrustedList(token);
        Assert.isTrue(result.equals(senderMdid));
    }

    @Test
    public void testGetTrusted() throws Exception {
        String token = mds.getToken(senderKey);
        String result = mds.getTrusted(token, senderMdid);
        Assert.isTrue(result.equals(senderMdid));
    }

    @Test
    public void testPutTrusted() throws Exception {
        ECKey otherKey = getRandomECKey();
        String otherMdid = otherKey.toAddress(MainNetParams.get()).toString();

        String token = mds.getToken(senderKey);
        String result = mds.putTrusted(token, otherMdid);
        System.out.println(result);
        Assert.isTrue(result.equals(otherMdid));
    }

    @Test
    public void testDeleteTrusted() throws Exception {
        ECKey otherKey = getRandomECKey();
        String otherMdid = otherKey.toAddress(MainNetParams.get()).toString();

        String token = mds.getToken(senderKey);
        mds.putTrusted(token, otherMdid);

        boolean result = mds.deleteTrusted(token, otherMdid);
        Assert.isTrue(result);
    }

    @Test
    public void testPostMessage() throws Exception {
        //Add senderMdid to trust
        String recipientToken = mds.getToken(recipientKey);

        ECKey senderKey = this.senderKey;
        String senderToken = mds.getToken(this.senderKey);
        String senderMdid = this.senderMdid;

        mds.putTrusted(recipientToken, senderMdid);

        //Send that senderMdid a message
        String message = "Any fool can paint a picture, but it takes a wise person to be able to sell it.";
        mds.postMessage(senderToken, senderKey, recipientMdid, message, 1);
    }

    @Test
    public void testGetMessage() throws Exception {
        String senderToken = mds.getToken(senderKey);
        String result = mds.getMessage(senderToken, true);

        System.out.println(result);
    }
}