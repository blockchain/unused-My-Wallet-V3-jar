package info.blockchain.wallet.coin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.exceptions.WalletMetadataException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class BlockchainStoreWalletTest extends MockedResponseTest {

    BlockchainStoreWallet subject;
    private static final int METADATA_TYPE_EXTERNAL = 123;

    class TestBlockchainStoreWallet extends BlockchainStoreWallet {

        public TestBlockchainStoreWallet(String coinPath, int mnemonicLength, String passphrase)
            throws WalletMetadataException {
            super(METADATA_TYPE_EXTERNAL, coinPath, mnemonicLength, passphrase);
        }

        public TestBlockchainStoreWallet(String coinPath, String entropyHex, String passphrase)
            throws WalletMetadataException {
            super(METADATA_TYPE_EXTERNAL, coinPath, entropyHex, passphrase);
        }

        public TestBlockchainStoreWallet(String coinPath, List<String> mnemonic, String passphrase)
            throws WalletMetadataException {
            super(METADATA_TYPE_EXTERNAL, coinPath, mnemonic, passphrase);
        }
    }

    @Test
    public void testConstructors() throws WalletMetadataException {

        int code = 404;
        String response = "{\"message\":\"Not Found\"}";

        mockInterceptor.setResponseList(new LinkedList<Pair>(Arrays.asList(Pair.of(code, response))));
        subject = new TestBlockchainStoreWallet("M/44H/145H", 12, "");
        assertNotNull(subject);
        assertNotNull(subject.metadata);
        assertNull(subject.data);

        mockInterceptor.setResponseList(new LinkedList<Pair>(Arrays.asList(Pair.of(code, response))));
        subject = new TestBlockchainStoreWallet("M/44H/145H", "0660cc198330660cc198330660cc1983", "");
        assertNotNull(subject);
        assertNotNull(subject.metadata);
        assertNull(subject.data);

        mockInterceptor.setResponseList(new LinkedList<Pair>(Arrays.asList(Pair.of(code, response))));
        subject = new TestBlockchainStoreWallet("M/44H/145H", split("all all all all all all all all all all all all"), "");
        assertNotNull(subject);
        assertNotNull(subject.metadata);
        assertNull(subject.data);
    }

    @Test
    public void testLoadEmptyWalletData() throws Exception {

        //Arrange
        LinkedList<Pair> responses = new LinkedList<>();
        responses.add(Pair.of(404, "{\"message\":\"Not Found\"}")); // fetch magic hash
        responses.add(Pair.of(200, "{\"payload\":\"q68bADKt+3zKeU0eH2PIw8dc43Ew6XZRpoPpd33mKBzuA4IvvqP/zbJkR7CIaiziWBBqgPeIoDHKXrFgZYoZ4k5IVKjfP1LRiP9QzD/5Dd0=\",\"version\":1,\"type_id\":123,\"signature\":\"IAZaPoWmNSmJJYxK7+3nGon34Tk1CrpZlI7Qa45lSMLybiaIT1v8fwBKlvqvwlVlxw7V4oQKgv7jJFn0pDfc8TQ=\",\"address\":\"13jEfRA5W979JN9vLquZ65USde8dtfa6eG\",\"created_at\":1515170103000,\"updated_at\":1515170103000}"));
        mockInterceptor.setResponseList(responses);

        subject = new TestBlockchainStoreWallet("M/44H/145H", split("all all all all all all all all all all all all"), "");

        //Act
        subject.loadWalletMetadata();

        //Assert
        assertNotNull(subject);
        assertNotNull(subject.metadata);
        assertNotNull(subject.data);
        assertEquals(0, subject.data.getAccounts().size());
    }

    @Test
    public void testLoadFullWalletData() throws Exception {

        //Arrange
        LinkedList<Pair> responses = new LinkedList<>();
        responses.add(Pair.of(404, "{\"message\":\"Not Found\"}")); // fetch magic hash
        responses.add(Pair.of(200, "{\"payload\":\"dr4yvOQwZH9/HxhCY3iOG1Ns8IE/4yUbMZDTUZQ2AfFYWMtBue20DkrwXoi1dfwoj/15DsaFUPkoQx9f53/F3ZzSEjLcpEwwB/4HD3OEVN3hqTjp/Fb6gFOhxmAMWLKgVAhvG2Ahih5j8s86QFj9EhASQD36VVvHbI1i9KRGLYE=\",\"version\":1,\"type_id\":123,\"signature\":\"H3I24OTCZAC8Fg+k/H2c5XEFh84z4KsZkf8dDFAvJGANOXu3AfBI+DHLcGkEZ51j909OoNalxM5OtTqpduww8vc=\",\"prev_magic_hash\":\"f1251dcd68279b9761302db32c3ba47c496070013e90962ddecba831517c2bf4\",\"address\":\"13jEfRA5W979JN9vLquZ65USde8dtfa6eG\",\"created_at\":1515170103000,\"updated_at\":1515171744000}"));
        mockInterceptor.setResponseList(responses);

        subject = new TestBlockchainStoreWallet("M/44H/145H", split("all all all all all all all all all all all all"), "");

        //Act
        subject.loadWalletMetadata();

        //Assert
        assertNotNull(subject);
        assertNotNull(subject.metadata);
        assertNotNull(subject.data);
        assertEquals(1, subject.data.getAccounts().size());
        assertEquals("First account", subject.data.getAccounts().get(0).getLabel());
        assertFalse(subject.data.getAccounts().get(0).isArchived());
    }

    @Test
    public void testSaveWalletData() throws Exception {

        //Arrange
        LinkedList<Pair> responses = new LinkedList<>();
        responses.add(Pair.of(404, "{\"message\":\"Not Found\"}")); // fetch magic hash
        responses.add(Pair.of(200, "{\"payload\":\"Gp07ysDlmKNEnLqZMZuosydySbyo5Nkci9Gu2RSftwZSbK3Yq7OghxDSRF5L0C7YBW0xDuy1V/pyldpvHn7KOMaFcg4ZbQ/lAN76CD00sPf0a7Fp3EfGAUGqOgT24cAj9KW3Ou+LxRmw8bOkQpiXFmsPbsTfr22B1vwciu2iklIjN4bWVjCL18JhYVzEqVX96nePAGdvOIcLvDbOoMlQfQ==\",\"version\":1,\"type_id\":123,\"signature\":\"HzrY8gI7thidrFDonV1iknf8oBaDJMvKNsd0wB8nc5uoJBCZa3EyHlGS3zqUEr2y1a3uE36thLt3ub5Vfcl26tI=\",\"prev_magic_hash\":\"3c19a050eb6c809533de19655ea361b849faa1f23600bc37edecfca7775bd024\",\"address\":\"13jEfRA5W979JN9vLquZ65USde8dtfa6eG\",\"created_at\":1515170103000,\"updated_at\":1515171934000}"));
        responses.add(Pair.of(200, "{\"payload\":\"TSq1S8nXXz9zGN6WzwFLAtBHEtKZKtq9waIoHKsy/v3xOCO61xktg2B8eTLVc5iqcE2nQ9tgc3rWnOOeRluSd5GZ4l2zN+MocRXmHZRI7cuHzZbzcHsJrpXoxaAn7/XcYKZsXKt4dCwsPwFAvR/UHFUvDN2fQn+qJO7cm8X8tBW406rBmIafIj8cSF4cu+Pc6gPcMZtQDso3jDZKSR1TLuzWcrldNrx2ohoujtpLlXdpDeuyGTPfOVM8AIpT5sMpbQsaqhgCs6onbP3Ujdsj4A==\",\"version\":1,\"type_id\":123,\"signature\":\"H28dxl9Cc1/J1PKCNox8iVO5jDDqvKnpBb2amZvUAGRTNNxogwVSgGQNn/W90dZhE/blpPPAJpPth+PgYLfC34g=\",\"prev_magic_hash\":\"219ac8e14530b64252e46a524a6f40af18117d0f53c437f15a9eb50af37a593d\",\"address\":\"13jEfRA5W979JN9vLquZ65USde8dtfa6eG\",\"created_at\":1515170103000,\"updated_at\":1515171967541}"));
        mockInterceptor.setResponseList(responses);

        subject = new TestBlockchainStoreWallet("M/44H/145H", split("all all all all all all all all all all all all"), "");

        subject.loadWalletMetadata();
        assertEquals(2, subject.data.getAccounts().size());
        subject.addAccount("Second account");

        //Act
        subject.saveWalletMetadata();

        //Assert
        assertEquals(3, subject.data.getAccounts().size());
    }

    public static List<String> split(String words) {
        return new ArrayList<>(Arrays.asList(words.split("\\s+")));
    }
}
