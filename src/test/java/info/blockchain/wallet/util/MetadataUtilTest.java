package info.blockchain.wallet.util;

import org.junit.Test;

import io.jsonwebtoken.lang.Assert;

public class MetadataUtilTest {

    @Test
    public void testGetMessage() throws Exception {

        String message = "{\"hello\":\"world\"}";
        String expected1 = "eyJoZWxsbyI6IndvcmxkIn0=";
        String expected2 = "LxR+2CipfgdIdi4EZgNOKTT+96WbppXnPZZjdZJ2vwCTojlxqRTl6svwqNJRVM2jCcPBxy+7mRTUfGDzy2gViA==";

        byte[] result = MetadataUtil.message(message.getBytes(), null);
        Assert.isTrue(expected1.equals(Base64Util.encodeBase64String(result)));
        byte[] magic = MetadataUtil.magic(message.getBytes(), null);
        byte[] nextResult = MetadataUtil.message(message.getBytes(), magic);
        Assert.isTrue(expected2.equals(Base64Util.encodeBase64String(nextResult)));
    }

    @Test
    public void testMagic() throws Exception {

        String message = "{\"hello\":\"world\"}";
        String expected1 = "LxR+2CipfgdIdi4EZgNOKTT+96WbppXnPZZjdZJ2vwA=";
        String expected2 = "skkJOHg9L6/1OVztbUohjcvVR3cNdRDZ/OJOUdQI41M=";

        byte[] magic = MetadataUtil.magic(message.getBytes(), null);
        Assert.isTrue(expected1.equals(Base64Util.encodeBase64String(magic)));

        byte[] nextMagic = MetadataUtil.magic(message.getBytes(), magic);
        Assert.isTrue(expected2.equals(Base64Util.encodeBase64String(nextMagic)));
    }
}