package info.blockchain.wallet.util;

import info.blockchain.api.Balance;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PrivateKeyFactoryTest {

    PrivateKeyFactory privateKeyFactory;

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

    @Before
    public void setUp() throws Exception {
        privateKeyFactory = new PrivateKeyFactory(mock(Balance.class));
    }

    @Test
    public void test_Mini_KeyFormat() throws Exception {

        String miniKey = "SmZxHc2PURmBHgKKXo97rEYWfnQKYu";
        String format = privateKeyFactory.getFormat(miniKey);
        assertThat(format, is(PrivateKeyFactory.MINI));

        String miniKey2 = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format2 = privateKeyFactory.getFormat(miniKey2);
        assertThat(format2, is(PrivateKeyFactory.MINI));
    }

    @Test
    public void test_BASE58_KeyFormat() throws Exception {

        String key = "22mPQQDMarsk4UcUuNH34PhebdftEtrQuftXDg5kA4QG";
        String format = privateKeyFactory.getFormat(key);
        assertThat(format, is(PrivateKeyFactory.BASE58));
    }

    @Test
    public void test_BASE64_KeyFormat() throws Exception {

        String key = "vICceVGqzvxqnB7haMDSB1q+XtBJ2kYraP45sjPd3CA=";
        String format = privateKeyFactory.getFormat(key);
        assertThat(format, is(PrivateKeyFactory.BASE64));
    }

    @Test
    public void test_HEX_UNCOMPRESSED_KeyFormat() throws Exception {

        String key = "C7C4AEE098C6EF6C8A9363E4D760F515FA27D67C219E7238510F458235B9870D";
        String format = privateKeyFactory.getFormat(key);
        assertThat(format, is(PrivateKeyFactory.HEX_UNCOMPRESSED));
    }

    @Test
    public void test_WIF_COMPRESSED_KeyFormat() throws Exception {

        String key = "KyCHxZe68e5PNfqh8Ls8DrihMuweHKxvjtm3PGTrj43MyWuvN2aE";
        String format = privateKeyFactory.getFormat(key);
        assertThat(format, is(PrivateKeyFactory.WIF_COMPRESSED));
    }

    @Test
    public void test_WIF_UNCOMPRESSED_KeyFormat() throws Exception {

        String key = "5JKxWHiBf1GX2A83BRVxYG4xpqsbfR3w9kQtppAUUJ6jnafURkm";
        String format = privateKeyFactory.getFormat(key);
        assertThat(format, is(PrivateKeyFactory.WIF_UNCOMPRESSED));
    }

    @Test
    public void test_BIP38_KeyFormat() throws Exception {

        String key = "6PfY1oK1kJX7jYDPMGBkcECCYwzH2qTCHfMdz67cBJrL7oZvpH8H8jfH2j";
        String format = privateKeyFactory.getFormat(key);
        assertThat(format, is(PrivateKeyFactory.BIP38));
    }

    @Test
    public void test_Mini_KeyFormat_shouldReturnCompressed_byDefault() throws Exception {

        //Arrange
        String compressedByDefault = String.format(balanceApiResponse, 0 ,0);

        Balance mockApi = mock(Balance.class);
        PrivateKeyFactory privateKeyFactory = new PrivateKeyFactory(mockApi);
        when(mockApi.getBalance(any(ArrayList.class))).thenReturn(new JSONObject(compressedByDefault));

        //Act
        String miniKey = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format = privateKeyFactory.getFormat(miniKey);
        ECKey ecKey = privateKeyFactory.getKey(format, miniKey);
        Address address = ecKey.toAddress(MainNetParams.get());

        //Assert
        assertThat(address.toString(), is(compressedAddress));
        Assert.assertTrue(ecKey.isCompressed());
    }

    @Test
    public void test_Mini_KeyFormat_shouldReturnUncompressed_ifHasBalance() throws Exception {

        //Arrange
        String uncompressedWithBalance = String.format(balanceApiResponse, 1000 ,0);
        Balance mockApi = mock(Balance.class);
        PrivateKeyFactory privateKeyFactory = new PrivateKeyFactory(mockApi);
        when(mockApi.getBalance(any(ArrayList.class))).thenReturn(new JSONObject(uncompressedWithBalance));

        //Act
        String miniKey = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format = privateKeyFactory.getFormat(miniKey);
        ECKey ecKey = privateKeyFactory.getKey(format, miniKey);
        Address address = ecKey.toAddress(MainNetParams.get());

        //Assert
        assertThat(address.toString(), is(uncompressedAddress));
        Assert.assertTrue(!ecKey.isCompressed());
    }

    @Test
    public void test_Mini_KeyFormat_shouldReturnCompressed_ifBothHaveFunds() throws Exception {

        //Arrange
        String compressedWithBalance = String.format(balanceApiResponse, 0 ,1000);
        Balance mockApi = mock(Balance.class);
        PrivateKeyFactory privateKeyFactory = new PrivateKeyFactory(mockApi);
        when(mockApi.getBalance(any(ArrayList.class))).thenReturn(new JSONObject(compressedWithBalance));

        //Act
        String miniKey = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format = privateKeyFactory.getFormat(miniKey);
        ECKey ecKey = privateKeyFactory.getKey(format, miniKey);
        Address address = ecKey.toAddress(MainNetParams.get());

        //Assert
        assertThat(address.toString(), is(compressedAddress));
        Assert.assertTrue(ecKey.isCompressed());
    }
}