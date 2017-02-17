package info.blockchain.wallet.util;




import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class UtilTest {

    @Test
    public void test_XOR() throws Exception {

        //http://xor.pw/
        byte[] data1 = Hex.decode("033cdcaecb07a62695963be9dbeec3362f1b26b048e8e15fb239c8f6967e8410");
        byte[] data2 = Hex.decode("3980d19c880f6cfecf97ba22e8d1e03c9566448e37de0b4757a898a76c71fa64");
        String expectedResult = "3abc0d324308cad85a0181cb333f230aba7d623e7f36ea18e5915051fa0f7e74";

        byte[] xor = Util.xor(data1, data2);

        Assert.assertEquals(expectedResult, Hex.toHexString(xor));
    }
}
