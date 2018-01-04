package info.blockchain.wallet.util;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.api.PersistentUrls;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.BitcoinMainNetParams;
import org.bitcoinj.params.BitcoinTestNet3Params;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrivateKeyFactoryTest extends MockedResponseTest {

    private PrivateKeyFactory privateKeyFactory;

    //Mini key
    private String miniKey = "SxuRMDrSNbwozww4twnedUPouUmGST";
    private String miniUncompressedAddress = "16FFsrfKxeKt7JWhtpB4VrGBjQ1kKv5o3p";
    private String miniCompressedAddress = "1H2E6b2Ny6UeQ6bM5V2pSxAwZaVYAaaYUH";

    //Hex key
    private String hexKey = "C7C4AEE098C6EF6C8A9363E4D760F515FA27D67C219E7238510F458235B9870D";
    private String hexUncompressedAddress = "1NZUGwdmQJ7AA6QrEuBz4jeT6z7yjty5dM";
    private String hexCompressedAddress = "1NLLkARpefxpXaMb7ZhHmc2DYNoVUnzBAz";

    private String balanceApiResponse = "{\n" +
            "    \"%s\": {\n" +
            "        \"final_balance\": %d,\n" +
            "        \"n_tx\": 22,\n" +
            "        \"total_received\": 259526\n" +
            "    },\n" +
            "    \"%s\": {\n" +
            "        \"final_balance\": %d,\n" +
            "        \"n_tx\": 51,\n" +
            "        \"total_received\": 622078\n" +
            "    }\n" +
            "}";

    @Before
    public void setup() throws Exception {
        privateKeyFactory = new PrivateKeyFactory();
    }

    @Test
    public void test_Mini_KeyFormat() throws Exception {

        String miniKey = "SmZxHc2PURmBHgKKXo97rEYWfnQKYu";
        String format = privateKeyFactory.getFormat(miniKey);
        Assert.assertEquals(PrivateKeyFactory.MINI, format);

        String miniKey2 = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format2 = privateKeyFactory.getFormat(miniKey2);
        Assert.assertEquals(PrivateKeyFactory.MINI, format2);
    }

    @Test
    public void test_BASE58_KeyFormat() throws Exception {

        String key = "22mPQQDMarsk4UcUuNH34PhebdftEtrQuftXDg5kA4QG";
        String format = privateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.BASE58, format);
    }

    @Test
    public void test_BASE64_KeyFormat() throws Exception {

        String key = "vICceVGqzvxqnB7haMDSB1q+XtBJ2kYraP45sjPd3CA=";
        String format = privateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.BASE64, format);
    }

    @Test
    public void test_HEX_KeyFormat() throws Exception {

        String format = privateKeyFactory.getFormat(hexKey);
        Assert.assertEquals(PrivateKeyFactory.HEX, format);
    }

    @Test
    public void test_WIF_COMPRESSED_KeyFormat() throws Exception {

        String key = "KyCHxZe68e5PNfqh8Ls8DrihMuweHKxvjtm3PGTrj43MyWuvN2aE";

        if(PersistentUrls.getInstance().getBitcoinParams() instanceof BitcoinTestNet3Params) {
            key = "cUQEjQs1kQ5MdrKfKwV3GLq5onJ7tQ2uBmMuqWHvdfwru7vCj3jT";
        }

        String format = privateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.WIF_COMPRESSED, format);
    }

    @Test
    public void test_WIF_UNCOMPRESSED_KeyFormat() throws Exception {

        String key = "5JKxWHiBf1GX2A83BRVxYG4xpqsbfR3w9kQtppAUUJ6jnafURkm";

        if(PersistentUrls.getInstance().getBitcoinParams() instanceof BitcoinTestNet3Params) {
            key = "938XkbQZo5mwX6jk81ZdmFv2ziytri1tFDmcXvmAS5HxiMZeBkn";
        }

        String format = privateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.WIF_UNCOMPRESSED, format);
    }

    @Test
    public void test_BIP38_KeyFormat() throws Exception {

        String key = "6PfY1oK1kJX7jYDPMGBkcECCYwzH2qTCHfMdz67cBJrL7oZvpH8H8jfH2j";
        String format = privateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.BIP38, format);
    }

    @Test
    public void test_Mini_KeyFormat_shouldReturnCompressed_byDefault() throws Exception {

        //Arrange
        String compressedByDefault = String.format(balanceApiResponse, miniUncompressedAddress, 0, miniCompressedAddress,0);
        mockInterceptor.setResponseString(compressedByDefault);

        //Act
        String format = privateKeyFactory.getFormat(miniKey);
        ECKey ecKey = privateKeyFactory.getKey(format, miniKey);
        Address address = ecKey.toAddress(BitcoinMainNetParams.get());

        //Assert
        Assert.assertEquals(miniCompressedAddress, address.toString());
        Assert.assertTrue(ecKey.isCompressed());
    }

    @Test
    public void test_Mini_KeyFormat_shouldReturnUncompressed_ifHasBalance() throws Exception {

        //Arrange
        String uncompressedWithBalance = String.format(balanceApiResponse, miniUncompressedAddress, 1000, miniCompressedAddress ,0);
        mockInterceptor.setResponseString(uncompressedWithBalance);

        //Act
        String format = privateKeyFactory.getFormat(miniKey);
        ECKey ecKey = privateKeyFactory.getKey(format, miniKey);
        Address address = ecKey.toAddress(BitcoinMainNetParams.get());

        //Assert
        if(PersistentUrls.getInstance().getBitcoinParams() instanceof BitcoinMainNetParams) {
            Assert.assertEquals(miniUncompressedAddress, address.toString());
            Assert.assertTrue(!ecKey.isCompressed());
        }
    }

    @Test
    public void test_Mini_KeyFormat_shouldReturnCompressed_ifBothHaveFunds() throws Exception {

        //Arrange
        String compressedWithBalance = String.format(balanceApiResponse, miniUncompressedAddress, 0, miniCompressedAddress ,1000);
        mockInterceptor.setResponseString(compressedWithBalance);

        //Act
        String format = privateKeyFactory.getFormat(miniKey);
        ECKey ecKey = privateKeyFactory.getKey(format, miniKey);
        Address address = ecKey.toAddress(BitcoinMainNetParams.get());

        //Assert
        Assert.assertEquals(miniCompressedAddress, address.toString());
        Assert.assertTrue(ecKey.isCompressed());
    }

    @Test
    public void test_HEX_KeyFormat_shouldReturnCompressed_byDefault() throws Exception {

        //Arrange
        String compressedByDefault = String.format(balanceApiResponse, hexUncompressedAddress, 0, hexCompressedAddress ,0);
        mockInterceptor.setResponseString(compressedByDefault);

        //Act
        String format = privateKeyFactory.getFormat(hexKey);
        ECKey ecKey = privateKeyFactory.getKey(format, hexKey);
        Address address = ecKey.toAddress(BitcoinMainNetParams.get());

        //Assert
        Assert.assertEquals(hexCompressedAddress, address.toString());
        Assert.assertTrue(ecKey.isCompressed());
        Assert.assertEquals(PrivateKeyFactory.HEX, format);
    }

    @Test
    public void test_HEX_KeyFormat_shouldReturnCompressed_ifBothHaveFunds() throws Exception {

        //Arrange
        String compressedWithBalance = String.format(balanceApiResponse, hexUncompressedAddress, 0, hexCompressedAddress ,1000);
        mockInterceptor.setResponseString(compressedWithBalance);

        //Act
        String format = privateKeyFactory.getFormat(hexKey);
        ECKey ecKey = privateKeyFactory.getKey(format, hexKey);
        Address address = ecKey.toAddress(BitcoinMainNetParams.get());

        //Assert
        Assert.assertEquals(hexCompressedAddress, address.toString());
        Assert.assertTrue(ecKey.isCompressed());
        Assert.assertEquals(PrivateKeyFactory.HEX, format);
    }

    @Test
    public void test_HEX_KeyFormat_shouldReturnUncompressed_ifHasBalance() throws Exception {

        //Arrange
        String uncompressedWithBalance = String.format(balanceApiResponse, hexUncompressedAddress, 1000 ,hexCompressedAddress, 0);
        mockInterceptor.setResponseString(uncompressedWithBalance);

        //Act
        String format = privateKeyFactory.getFormat(hexKey);
        ECKey ecKey = privateKeyFactory.getKey(format, hexKey);
        Address address = ecKey.toAddress(BitcoinMainNetParams.get());

        //Assert
        Assert.assertEquals(PrivateKeyFactory.HEX, format);
        if(PersistentUrls.getInstance().getBitcoinParams() instanceof BitcoinMainNetParams) {
            Assert.assertEquals(hexUncompressedAddress, address.toString());
            Assert.assertTrue(!ecKey.isCompressed());
        }
    }

}