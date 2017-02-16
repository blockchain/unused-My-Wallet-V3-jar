package info.blockchain.wallet.util;

import info.blockchain.MockedResponseTest;
import info.blockchain.wallet.api.PersistentUrls;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrivateKeyFactoryTest extends MockedResponseTest {

    //Mini key
    String uncompressedAddress = "16FFsrfKxeKt7JWhtpB4VrGBjQ1kKv5o3p";
    String compressedAddress = "1H2E6b2Ny6UeQ6bM5V2pSxAwZaVYAaaYUH";
    String balanceApiResponse = "{\n" +
            "    \""+uncompressedAddress+"\": {\n" +
            "        \"final_balance\": %d,\n" +
            "        \"n_tx\": 22,\n" +
            "        \"total_received\": 259526\n" +
            "    },\n" +
            "    \""+compressedAddress+"\": {\n" +
            "        \"final_balance\": %d,\n" +
            "        \"n_tx\": 51,\n" +
            "        \"total_received\": 622078\n" +
            "    }\n" +
            "}";

    @Test
    public void test_Mini_KeyFormat() throws Exception {

        String miniKey = "SmZxHc2PURmBHgKKXo97rEYWfnQKYu";
        String format = PrivateKeyFactory.getFormat(miniKey);
        Assert.assertEquals(PrivateKeyFactory.MINI, format);

        String miniKey2 = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format2 = PrivateKeyFactory.getFormat(miniKey2);
        Assert.assertEquals(PrivateKeyFactory.MINI, format2);
    }

    @Test
    public void test_BASE58_KeyFormat() throws Exception {

        String key = "22mPQQDMarsk4UcUuNH34PhebdftEtrQuftXDg5kA4QG";
        String format = PrivateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.BASE58, format);
    }

    @Test
    public void test_BASE64_KeyFormat() throws Exception {

        String key = "vICceVGqzvxqnB7haMDSB1q+XtBJ2kYraP45sjPd3CA=";
        String format = PrivateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.BASE64, format);
    }

    @Test
    public void test_HEX_COMPRESSED_KeyFormat() throws Exception {

        String key = "C7C4AEE098C6EF6C8A9363E4D760F515FA27D67C219E7238510F458235B9870D";
        String format = PrivateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.HEX_COMPRESSED, format);
    }

    @Test
    public void test_WIF_COMPRESSED_KeyFormat() throws Exception {

        String key = "KyCHxZe68e5PNfqh8Ls8DrihMuweHKxvjtm3PGTrj43MyWuvN2aE";

        if(PersistentUrls.getInstance().getCurrentNetworkParams() instanceof TestNet3Params) {
            key = "cUQEjQs1kQ5MdrKfKwV3GLq5onJ7tQ2uBmMuqWHvdfwru7vCj3jT";
        }

        String format = PrivateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.WIF_COMPRESSED, format);
    }

    @Test
    public void test_WIF_UNCOMPRESSED_KeyFormat() throws Exception {

        String key = "5JKxWHiBf1GX2A83BRVxYG4xpqsbfR3w9kQtppAUUJ6jnafURkm";

        if(PersistentUrls.getInstance().getCurrentNetworkParams() instanceof TestNet3Params) {
            key = "938XkbQZo5mwX6jk81ZdmFv2ziytri1tFDmcXvmAS5HxiMZeBkn";
        }

        String format = PrivateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.WIF_UNCOMPRESSED, format);
    }

    @Test
    public void test_BIP38_KeyFormat() throws Exception {

        String key = "6PfY1oK1kJX7jYDPMGBkcECCYwzH2qTCHfMdz67cBJrL7oZvpH8H8jfH2j";
        String format = PrivateKeyFactory.getFormat(key);
        Assert.assertEquals(PrivateKeyFactory.BIP38, format);
    }

    @Test
    public void test_Mini_KeyFormat_shouldReturnCompressed_byDefault() throws Exception {

        //Arrange
        String compressedByDefault = String.format(balanceApiResponse, 0 ,0);
        mockInterceptor.setResponseString(compressedByDefault);

        //Act
        String miniKey = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format = PrivateKeyFactory.getFormat(miniKey);
        ECKey ecKey = PrivateKeyFactory.getKey(format, miniKey);
        Address address = ecKey.toAddress(MainNetParams.get());

        //Assert
        Assert.assertEquals(compressedAddress, address.toString());
        Assert.assertTrue(ecKey.isCompressed());
    }

    @Test
    public void test_Mini_KeyFormat_shouldReturnUncompressed_ifHasBalance() throws Exception {

        //Arrange
        String uncompressedWithBalance = String.format(balanceApiResponse, 1000 ,0);
        mockInterceptor.setResponseString(uncompressedWithBalance);

        //Act
        String miniKey = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format = PrivateKeyFactory.getFormat(miniKey);
        ECKey ecKey = PrivateKeyFactory.getKey(format, miniKey);
        Address address = ecKey.toAddress(MainNetParams.get());

        //Assert
        if(PersistentUrls.getInstance().getCurrentNetworkParams() instanceof MainNetParams) {
            Assert.assertEquals(uncompressedAddress, address.toString());
            Assert.assertTrue(!ecKey.isCompressed());
        }
    }

    @Test
    public void test_Mini_KeyFormat_shouldReturnCompressed_ifBothHaveFunds() throws Exception {

        //Arrange
        String compressedWithBalance = String.format(balanceApiResponse, 0 ,1000);
        mockInterceptor.setResponseString(compressedWithBalance);

        //Act
        String miniKey = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format = PrivateKeyFactory.getFormat(miniKey);
        ECKey ecKey = PrivateKeyFactory.getKey(format, miniKey);
        Address address = ecKey.toAddress(MainNetParams.get());

        //Assert
        Assert.assertEquals(compressedAddress, address.toString());
        Assert.assertTrue(ecKey.isCompressed());
    }

    @Test
    public void test_HEX_KeyFormat_shouldReturnCompressed() throws Exception {

        String key = "C7C4AEE098C6EF6C8A9363E4D760F515FA27D67C219E7238510F458235B9870D";
        String format = PrivateKeyFactory.getFormat(key);
        ECKey key1 = PrivateKeyFactory.getKey(PrivateKeyFactory.HEX_COMPRESSED, key);

        //Assert
        Assert.assertEquals(PrivateKeyFactory.HEX_COMPRESSED, format);
        Assert.assertEquals("1NLLkARpefxpXaMb7ZhHmc2DYNoVUnzBAz", key1.toAddress(MainNetParams.get()).toString());
    }
}