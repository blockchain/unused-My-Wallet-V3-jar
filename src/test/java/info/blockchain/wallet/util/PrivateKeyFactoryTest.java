package info.blockchain.wallet.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PrivateKeyFactoryTest {

    @Test
    public void test_Mini_KeyFormat() throws Exception {

        String miniKey = "SmZxHc2PURmBHgKKXo97rEYWfnQKYu";
        String format = PrivateKeyFactory.getInstance().getFormat(miniKey);
        assertThat(format, is(PrivateKeyFactory.MINI));

        String miniKey2 = "SxuRMDrSNbwozww4twnedUPouUmGST";
        String format2 = PrivateKeyFactory.getInstance().getFormat(miniKey2);
        assertThat(format2, is(PrivateKeyFactory.MINI));
    }

    @Test
    public void test_BASE58_KeyFormat() throws Exception {

        String key = "22mPQQDMarsk4UcUuNH34PhebdftEtrQuftXDg5kA4QG";
        String format = PrivateKeyFactory.getInstance().getFormat(key);
        assertThat(format, is(PrivateKeyFactory.BASE58));
    }

    @Test
    public void test_BASE64_KeyFormat() throws Exception {

        String key = "vICceVGqzvxqnB7haMDSB1q+XtBJ2kYraP45sjPd3CA=";
        String format = PrivateKeyFactory.getInstance().getFormat(key);
        assertThat(format, is(PrivateKeyFactory.BASE64));
    }

    @Test
    public void test_HEX_UNCOMPRESSED_KeyFormat() throws Exception {

        String key = "C7C4AEE098C6EF6C8A9363E4D760F515FA27D67C219E7238510F458235B9870D";
        String format = PrivateKeyFactory.getInstance().getFormat(key);
        assertThat(format, is(PrivateKeyFactory.HEX_UNCOMPRESSED));
    }

    @Test
    public void test_WIF_COMPRESSED_KeyFormat() throws Exception {

        String key = "KyCHxZe68e5PNfqh8Ls8DrihMuweHKxvjtm3PGTrj43MyWuvN2aE";
        String format = PrivateKeyFactory.getInstance().getFormat(key);
        assertThat(format, is(PrivateKeyFactory.WIF_COMPRESSED));
    }

    @Test
    public void test_WIF_UNCOMPRESSED_KeyFormat() throws Exception {

        String key = "5JKxWHiBf1GX2A83BRVxYG4xpqsbfR3w9kQtppAUUJ6jnafURkm";
        String format = PrivateKeyFactory.getInstance().getFormat(key);
        assertThat(format, is(PrivateKeyFactory.WIF_UNCOMPRESSED));
    }

    @Test
    public void test_BIP38_KeyFormat() throws Exception {

        String key = "6PfY1oK1kJX7jYDPMGBkcECCYwzH2qTCHfMdz67cBJrL7oZvpH8H8jfH2j";
        String format = PrivateKeyFactory.getInstance().getFormat(key);
        assertThat(format, is(PrivateKeyFactory.BIP38));
    }
}