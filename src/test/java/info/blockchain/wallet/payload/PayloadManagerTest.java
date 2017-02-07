package info.blockchain.wallet.payload;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import info.blockchain.BaseTest;
import info.blockchain.util.RestClient;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.FrameworkInterface;
import info.blockchain.wallet.exceptions.InvalidCredentialsException;
import info.blockchain.wallet.exceptions.UnsupportedVersionException;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import retrofit2.Retrofit;

public class PayloadManagerTest extends BaseTest{

    @Mock
    private PayloadManager mockPayloadManager;

    // TODO: 30/09/16 Investigate changing integration tests to unit tests
    PayloadManager payloadManager;
    String password = "password";
    String label = "Account 1";
    Payload payload;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        payloadManager = PayloadManager.getInstance();
        payload = payloadManager.createHDWallet(password, label);
    }

    @After
    public void tearDown() throws Exception {
        PayloadManager.getInstance().wipe();
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withValidVars_shouldPass() throws Exception {

        payloadManager.initiatePayload(payload.getSharedKey(), payload.getGuid(), password, new PayloadManager.InitiatePayloadListener() {
            public void onSuccess() {
                assertThat("Payload successfully fetch and decrypted", true);
            }
        });
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withInvalidGuid_shouldThrow_AuthenticationException() {

        try {
            payloadManager.initiatePayload(payload.getSharedKey(), payload.getGuid() + "addSomeTextToFail", password, new PayloadManager.InitiatePayloadListener() {
                public void onSuccess() {
                    assertThat("onSuccess", false);
                }
            });
        } catch (Exception e) {
            if (e instanceof InvalidCredentialsException) {
                assertThat("Invalid Guid successfully detected", true);
            } else {
                assertThat("Auth should not pass with invalid guid", false);
            }
        }
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withInvalidPassword_shouldThrow_DecryptionException() {

        try {
            payloadManager.initiatePayload(payload.getSharedKey(), payload.getGuid(), password + "addSomeTextToFail", new PayloadManager.InitiatePayloadListener() {
                public void onSuccess() {
                    assertThat("onSuccess", false);
                }
            });
        } catch (Exception e) {
            assertThat("InitiatePayload failed as expected", true);
        }
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withInvalidSharedKey_shouldThrow_AuthenticationException() {

        try {
            payloadManager.initiatePayload(payload.getSharedKey() + "addSomeTextToFail", payload.getGuid(), password, new PayloadManager.InitiatePayloadListener() {
                public void onSuccess() {
                    assertThat("onSuccess", false);
                }
            });
        } catch (Exception e) {
            if (e instanceof InvalidCredentialsException) {
                assertThat("Invalid shared key successfully detected", true);
            } else {
                assertThat("Auth should not pass with invalid shared key", false);
            }
        }
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withIncompatibleVersion_shouldThrow_UnsupportedVersionException() throws Exception {

        payloadManager.setVersion(4.0);

        payloadManager.savePayloadToServer();

        try {
            payloadManager.initiatePayload(payload.getSharedKey(), payload.getGuid(), password, new PayloadManager.InitiatePayloadListener() {
                public void onSuccess() {
                    assertThat("Incompatible version should not pass", false);
                }
            });
        } catch (Exception e) {
            if (e instanceof UnsupportedVersionException) {
                assertThat("Unsupported version detected", true);
            } else {
                assertThat("Unsupported version should not pass", false);
            }
        } finally {
            payloadManager.setVersion(3.0);
        }
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
    }

    @Test
    @Ignore
    public void upgradeV2PayloadToV3_shouldPass() throws Exception {

        final PayloadManager payloadManager = PayloadManager.getInstance();
        getRestoredWallet_All_All(payloadManager);

        //Remove HD
        Payload payload = payloadManager.getPayload();
        payload.setHdWalletList(new ArrayList<HDWallet>());

        //Add legacy (way too much extra to docleanup newLegacyAddress() soon)
        LegacyAddress legacyAddress = payloadManager.generateLegacyAddress("android", "6.6", null);

        payloadManager.addLegacyAddress(legacyAddress);

        final String guidOriginal = payloadManager.getPayload().getGuid();

        //Now we have legacy wallet (only addresses)
        payloadManager.upgradeV2PayloadToV3("", true, "My Bci Wallet", new PayloadManager.UpgradePayloadListener() {
            public void onDoubleEncryptionPasswordError() {
                assertThat("upgradeV2PayloadToV3 failed", false);
            }

            public void onUpgradeSuccess() {

                assertThat(payloadManager.getPayload().getGuid(), is(guidOriginal));
                assertThat("Payload not flagged as upgraded", payloadManager.getPayload().isUpgraded());

                String xpriv = payloadManager.getPayload().getHdWallet().getAccounts().get(0).getXpriv();
                assertThat("Xpriv may not be null or empty after upgrade", xpriv != null && !xpriv.isEmpty());
                try {
                    assertThat(payloadManager.getMnemonic().length, is(12));
                } catch (Exception e) {
                    e.printStackTrace();
                    assertThat("upgradeV2PayloadToV3 failed", false);
                }
            }

            public void onUpgradeFail() {
                assertThat("upgradeV2PayloadToV3 failed", false);
            }
        });

        PayloadManager.getInstance().wipe();
    }

    @Test
    @Ignore
    public void upgradeV2PayloadToV3_withSecondPassword_shouldPass() throws Exception {

        final String secondPassword = "password2";
        final PayloadManager payloadManager = PayloadManager.getInstance();

        //Create HD
        String label = "Account 1";
        Payload payload = payloadManager.createHDWallet("password", label);
        payload.setHdWalletList(new ArrayList<HDWallet>());//remove hd

        //Set second password
        String hash = DoubleEncryptionFactory.getInstance().getHash(payload.getSharedKey(), secondPassword, payload.getDoubleEncryptionPbkdf2Iterations());
        payload.setDoublePasswordHash(hash);
        payload.setDoubleEncrypted(true);

        //Add legacy (way too much extra to docleanup newLegacyAddress() soon)
        LegacyAddress legacyAddress = payloadManager.generateLegacyAddress("android", "6.6", secondPassword);
        payloadManager.addLegacyAddress(legacyAddress);

        final String guidOriginal = payloadManager.getPayload().getGuid();

        //Now we have legacy wallet (only addresses)
        payloadManager.upgradeV2PayloadToV3(secondPassword, true, "My Bci Wallet", new PayloadManager.UpgradePayloadListener() {
            public void onDoubleEncryptionPasswordError() {
                assertThat("upgradeV2PayloadToV3 failed", false);
            }

            public void onUpgradeSuccess() {

                assertThat(payloadManager.getPayload().getGuid(), is(guidOriginal));
                assertThat("Payload not flagged as upgraded", payloadManager.getPayload().isUpgraded());

                String xpriv = payloadManager.getPayload().getHdWallet().getAccounts().get(0).getXpriv();
                assertThat("Xpriv may not be null or empty after upgrade", xpriv != null && !xpriv.isEmpty());
                try {
                    assertThat(payloadManager.getMnemonic().length, is(12));
                } catch (Exception e) {
                    e.printStackTrace();
                    assertThat("upgradeV2PayloadToV3 failed", false);
                }
            }

            public void onUpgradeFail() {
                assertThat("upgradeV2PayloadToV3 failed", false);
            }
        });

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void createWallet_shouldPass() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();

        String label = "Account 1";
        Payload payload = payloadManager.createHDWallet("password", label);

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getAccounts().get(0).getLabel(), is(label));
        assertThat(payload, is(payloadManager.getPayload()));
        assertThat("Checksum should not be null", payloadManager.getCheckSum() != null);

        PayloadManager.getInstance().wipe();
    }

    private Payload getRestoredWallet_All_All(PayloadManager payloadManager) throws Exception {

        String mnemonic = "all all all all all all all all all all all all";

        LinkedList<String> xpubs = new LinkedList<>();
        xpubs.add("{\"xpub6BiVtCpG9fQPxnPmHXG8PhtzQdWC2Su4qWu6XW9tpWFYhxydCLJGrWBJZ5H6qTAHdPQ7pQhtpjiYZVZARo14qHiay2fvrX996oEP42u8wZy\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQCgxA541qm9qZ9VrGLScde4zsAMj2d15ewiMysCAnbgvSDSZXhFUdsyA2BfzzMrMFJbC4VSkXbzrXLZRitAmUVURmivxxqMJ\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQDvwDNekCEzAr3gYcoGXEF27bMwSBsCVP3bJYdUZ6m3jhv9vSG7hVxff3VEfnfK4fcMr2YRwfTfHcJwM4ioS6Eiwnrm1wcuf\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQGq7bXBjjf5zyguEXHrmxDu4t7pdTFUtDWD5epi4ecKmWBTMHvPQtRmQnby8gET7ArTzxjL4SNYdD2RYSdjk7fwYeEDMzkce\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQJXDcLwQU1cXECNqaGYb3nNSu1ZEuwFKMXjDbCni6eMhN6rFkdxQsgF1amKAqeLSN63zrYPKJ3GU2ppowBWZSdGBk7QUxgLV\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6BiVtCpG9fQQNBuKZoKzhzmENDKdCeXQsNVPF2Ynt8rhyYznmPURQNDmnNnX9SYahZ1DVTaNtsh3pJ4b2jKvsZhpv2oVj76YETCGztKJ3LM\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        mockInterceptor.setResponseStringList(xpubs);

        return payloadManager.restoreHDWallet("password", mnemonic, "");
    }

    @Test
    public void restoreWallet_withMnemonicNoPassphrase_shouldPass() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();

        String seedHex = "0660cc198330660cc198330660cc1983";//All all ...
        String xpub1 = "xpub6BiVtCpG9fQPxnPmHXG8PhtzQdWC2Su4qWu6XW9tpWFYhxydCLJGrWBJZ5H6qTAHdPQ7pQhtpjiYZVZARo14qHiay2fvrX996oEP42u8wZy";
        String xpub2 = "xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL";
        String xpub3 = "xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x";
        String xpub4 = "xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH";
        String xpub5 = "xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP";

        Payload payload = getRestoredWallet_All_All(payloadManager);

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getSeedHex(), is(seedHex));

        assertThat(payload.getHdWallet().getAccounts().get(0).getXpub(), is(xpub1));
        assertThat(payload.getHdWallet().getAccounts().get(0).getXpriv().substring(4), is("9xj9UhHNKHr6kJKJBVj82ZxFrbfhczBDUHyVj7kHGAiZqAeUenz2JhrphnMMYVKcWcVPFJESngtKsVa4FYEvFfWUTtZThCoZdwDeS9qQnqm"));

        payloadManager.addAccount("", null);
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpub(), is(xpub2));
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpriv().substring(4), is("9xj9UhHNKHr6nkRg3ZpSBp2i3MgSazXa3LGet5MsVY3nTeE1zvnwVrjpnsJGEtEvvcm8fwoUBVpnHcioJfFqRUaZ6ijXEuwUuv2Q5RM6dGR"));

        payloadManager.addAccount("", null);
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpub(), is(xpub3));
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpriv().substring(4), is("9xj9UhHNKHr6rUDptMDdQhw5ccX8mzVQBopwYejpRt1NHpFvQMSG1a8RGRJjZRE8rRJJ6N9g1GcB6yWEgkXCzGBweq934jS9LfBuViQRxRw"));

        payloadManager.addAccount("", null);
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpub(), is(xpub4));
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpriv().substring(4), is("9xj9UhHNKHr6tdLP1UdrAJKKRUiGd92aBbsqkW28vtdmCzXTvns1aNKwh5uM1nSbdD8Y4x9VBnTLrDDEbREnu9KYnDyvt8QRPtPWQ78UgAG"));

        payloadManager.addAccount("", null);
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpub(), is(xpub5));
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpriv().substring(4), is("9xj9UhHNKHr6vLRGPDajPuoc2futBwhu93ZLCUuoGBya3uD4X5kDfMuUiEHz7HPWPpkgCHiwNbLWjxa6QrqfjmPmVr146GUt8D5shiXkQpC"));

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void restoreWallet_withMnemonicWithPassphrase_shouldPass() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();

        String mnemonic = "all all all all all all all all all all all all";
        String passphrase = "myPassphrase";

        String seedHex = "0660cc198330660cc198330660cc1983";//All all ...
        String xpub1 = "xpub6D45Bi15NLqVpUqw9ku1ucJw6AKa5mgU3fbodx96sbj8rWw91TPkZ1TVbhMNUVihSLHGJk5pdCXBb56r2tiPRrvp439uxq4S3D8Emxs8WiZ";
        String xpub2 = "xpub6D45Bi15NLqVrLDEFqomu3YrS197BMHMdk9xy4Kp21g8CzABvQ1o7DNbL5aiFoksJYvpaJGW5aEXF7qFHFHejgvSV4UXu9LXoRaNfcek7sN";
        String xpub3 = "xpub6D45Bi15NLqVtw4aMeT9xCXmE8YX4SBeeAozaYGeCgDPK8SwVDeLwGQv9wW6x8Ex41ERUHwT9KKgKF2d9jXaYe9M4XUitPGTgk7UERTJ1dm";
        String xpub4 = "xpub6D45Bi15NLqVwVjzUWpR6Nw1bdJZiEHqLg4k9MpFMFnLqBJTS1P5Zym4sgfdYMTP87yJFK1MkeXUxviHE3emBfd8k3NyZf2GPLoaKYfEQti";
        String xpub5 = "xpub6D45Bi15NLqVyTd6H8nZG7KZ6LjHaw3WBeAtsGVJ4pHxkZdj7ersAWJvejFtNHCe95JGXjVJuZ8pvSMmRdf84FUbrnpPCfhJj5SoEk67Um6";

        LinkedList<String> xpubs = new LinkedList<>();
        xpubs.add("{\"xpub6D45Bi15NLqVpUqw9ku1ucJw6AKa5mgU3fbodx96sbj8rWw91TPkZ1TVbhMNUVihSLHGJk5pdCXBb56r2tiPRrvp439uxq4S3D8Emxs8WiZ\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6D45Bi15NLqVrLDEFqomu3YrS197BMHMdk9xy4Kp21g8CzABvQ1o7DNbL5aiFoksJYvpaJGW5aEXF7qFHFHejgvSV4UXu9LXoRaNfcek7sN\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6D45Bi15NLqVtw4aMeT9xCXmE8YX4SBeeAozaYGeCgDPK8SwVDeLwGQv9wW6x8Ex41ERUHwT9KKgKF2d9jXaYe9M4XUitPGTgk7UERTJ1dm\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6D45Bi15NLqVwVjzUWpR6Nw1bdJZiEHqLg4k9MpFMFnLqBJTS1P5Zym4sgfdYMTP87yJFK1MkeXUxviHE3emBfd8k3NyZf2GPLoaKYfEQti\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6D45Bi15NLqVyTd6H8nZG7KZ6LjHaw3WBeAtsGVJ4pHxkZdj7ersAWJvejFtNHCe95JGXjVJuZ8pvSMmRdf84FUbrnpPCfhJj5SoEk67Um6\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6D45Bi15NLqW1iv8utKQs2T6X3dXWNLXabdUdC2qP4xWMANJ7myAk3tKrdzptmkjxwDFWEdEKWGhEmw3yLEos5PLzUppzddgoiXnmxYcciV\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6D45Bi15NLqW6Yf22xqpWRMMzNmhabDChWdT48s7qJmqFCBnziRzftDWnBMF2yEVt4A2gWwvbFyrJQKWqibGFDyFd43fqz194kVinYK27Wj\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6D45Bi15NLqW7RYzFUQe8oqiBNB9DPtmERMCgi7Rk7WsZBZnUEUxojq39Tfw7cfTtMs3pb8ur4HVDwVaYZNf8UP1WzWwZiQhsSBKFcrQXF4\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6D45Bi15NLqWB1JKmPM6hgu54zofJAMLywvyCt29bj18u36Z6epCmCQayJaCe54SWg3e3Na7n217QfyZ6zvV7gEsgcCxLW8KZPKohqk6SED\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        xpubs.add("{\"xpub6D45Bi15NLqWCCqwmhSQoXNekogmYmMNKqAk9vNGg8uTRCRMB5XtBCWZv9nMMhLc3qVwSbgydUg4qfuBJZzjYmmG9y3o3Ta3k75a8x9iBMk\":{\"final_balance\":0,\"n_tx\":0,\"total_received\":20000}}");
        mockInterceptor.setResponseStringList(xpubs);

        Payload payload = payloadManager.restoreHDWallet("password", mnemonic, "", passphrase);

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getSeedHex(), is(seedHex));

        assertThat(payload.getHdWallet().getAccounts().get(0).getXpub(), is(xpub1));
        assertThat(payload.getHdWallet().getAccounts().get(0).getXpriv().substring(4), is("9z4inCUBXyHCbzmU3jN1YUNCY8V5gJxcgSgCqZjVKGC9yibzTv5W1D91kRvVoaqPGNj9CosizY3nLnZheTYqZ4aYYWfAqMw9vz4F8mxj3KG"));

        payloadManager.addAccount("", null);
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpub(), is(xpub2));
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpriv().substring(4), is("9z4inCUBXyHCdr8m9pGmXuc7syJcmtZWGXENAfvCTg99LBq3NrhYZR47Umizc4tUtm8meaD58sTLuAyfNoTLWL7ELKtLKCSRuBnCgFfr2KX"));

        payloadManager.addAccount("", null);
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpub(), is(xpub3));
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpriv().substring(4), is("9z4inCUBXyHCgSz7Fcv9b4b2g6i2eyToGwtPn9s2eLgQSL7nwgL6PU6SJfAdunPLraJbaPWLHzGBxu78ETqBPk36JgBiUxUB1hfeMVaci1q"));

        payloadManager.addAccount("", null);
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpub(), is(xpub4));
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpriv().substring(4), is("9z4inCUBXyHCj1fXNVHQjEzH3bU5JmZyyT99LyQdnvFMxNyJtU4q2BSb2PfLNBMLDCgkC9Fv7cyCstkc1AyWZW8YXZc1aPJFTpJkcL9MpF7"));

        payloadManager.addAccount("", null);
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpub(), is(xpub5));
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpriv().substring(4), is("9z4inCUBXyHCkyYdB7FYtyNpYJtoBUKepRFJ4t5gWUkysmJaa7YcchzSoTJQ9TgEG78i3LcnWvkxr5eiYbxUkDN7s8NWPVwf7bgx7DGYFqF"));

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void restoreWallet_withMnemonic_shouldContainCorrectReceiveAddresses() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();
        getRestoredWallet_All_All(payloadManager);

        assertThat(payloadManager.getNextReceiveAddress(0), is("1JAd7XCBzGudGpJQSDSfpmJhiygtLQWaGL"));

        payloadManager.addAccount("", null);
        assertThat(payloadManager.getNextReceiveAddress(1), is("1Dgews942GZs2GV7JT5v1t4KxuaDZpJgG9"));

        payloadManager.addAccount("", null);
        assertThat(payloadManager.getNextReceiveAddress(2), is("1N4rfuysGPvWuKHFnEeVdv8NE8QCNPZ9v3"));

        payloadManager.addAccount("", null);
        assertThat(payloadManager.getNextReceiveAddress(3), is("19LcKJTDYuF8B3p4bgDoW2XXn5opPqutx3"));

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void generateNewLegacyAddress_withWrongSecondPassword_shouldFail() throws Exception {

        when(mockPayloadManager.validateSecondPassword(anyString()))
                .thenReturn(false);

        LegacyAddress legacyAddress = mockPayloadManager.generateLegacyAddress("Jar", "1.0", "second_password");

        assertThat("Address should be null", legacyAddress == null);
    }

    @Test
    public void generateNewLegacyAddress_withFailedRandomECKey_shouldFail() throws Exception {

        when(mockPayloadManager.getRandomECKey())
                .thenReturn(null);

        LegacyAddress legacyAddress = mockPayloadManager.generateLegacyAddress("Jar", "1.0", "second_password");

        assertThat("Address should be null", legacyAddress == null);
    }

    @Test
    public void testIsEncryptionConsistent_not_doubleEncrypted() throws Exception {

        ArrayList<String> keyList = new ArrayList<>();
        keyList.add("o9azUsu8QrsjtKe54Ah3ep6FEGj6v1fA3S8nrYt6UFd");
        keyList.add("9DWpEiXYQ9wkYQA3UpZezsATW6sP3N9fcCetgRyfWUn4");
        keyList.add("xprv9xgDCL6n9Y3x9njMKWiLaYY3XDeEjY8duMyreHk5WhKNxVKeYjNt85SmeZYpctXAdAgodwnWpCknp5HwNB5Np1hxeqw6dMZDgcWexU4uTcH");
        keyList.add("xprv9xgDCL6n9Y3xDRCSvsGTnub1z1z1s1vbpsS7esVELAV7166hs2m6fj5ibth9ejDyowPEau2n7FbQVZCyVhg7KSrgy5a9VoqEge387KTwjws");

        Assert.assertTrue(payloadManager.isEncryptionConsistent(false, keyList));
        Assert.assertTrue(!payloadManager.isEncryptionConsistent(true, keyList));
    }

    @Test
    public void testIsEncryptionConsistent_doubleEncrypted() throws Exception {

        ArrayList<String> keyList = new ArrayList<>();
        keyList.add("kG4+ziqbauAwP+wbGT2UM9d5f7yGL2kazOSs4KlYempP68qTQ9YtV5od5dQg3Jxpi4t5Q4faCMqraKpnQ1Gq3Q==");
        keyList.add("6icDz7UDWlESNqNiGEFolP3UqTVES9xIZQ9Ld4E0ND6PF/4KStmoGP9ADOV43a+KIMyR1Ook+ap7zJ8l8Wn0bA==");
        keyList.add("Dn2ZVo45cuAEnf6Mj4d22EqYfyUJSLsuhfx2khqAv1DTcNJKNH9SAqBAK2zhmLWUvdQFNOtQu0o+RqLpTzOuiFVz6myIsmDTXh0+BZPUw29LkSflIei6+NgCa2HhvOL4jHjXhO4emu/UM5GeV/4PaYKN0qA+hnFT/DuV8pllfTE=");
        keyList.add("NQyZWdgZmJ4k8NBZjw6n64UIiXtW3zhnnfsZnfzU6YXHovucf2pLOtHq/lxqPzxWMzZuThmWBsdI+ns/HNAGB9s6Rad5Q6R55j5wx+Gd8KvSEK57U+3VZah5oWusdMZNz2hfKIn49EoZszw9bKsEjGzRxxd1D1FLny1pci5gtD4=");

        Assert.assertTrue(payloadManager.isEncryptionConsistent(true, keyList));
        Assert.assertTrue(!payloadManager.isEncryptionConsistent(false, keyList));
    }

    @Test
    public void testIsEncryptionConsistent_not_doubleEncrypted_corrupt() throws Exception {

        ArrayList<String> keyList = new ArrayList<>();
        keyList.add("o9azUsu8QrsjtKe54Ah3ep6FEGj6v1fA3S8nrYt6UFd");
        keyList.add("6icDz7UDWlESNqNiGEFolP3UqTVES9xIZQ9Ld4E0ND6PF/4KStmoGP9ADOV43a+KIMyR1Ook+ap7zJ8l8Wn0bA==");

        Assert.assertTrue(!payloadManager.isEncryptionConsistent(false, keyList));

        keyList = new ArrayList<>();
        keyList.add("xprv9xgDCL6n9Y3x9njMKWiLaYY3XDeEjY8duMyreHk5WhKNxVKeYjNt85SmeZYpctXAdAgodwnWpCknp5HwNB5Np1hxeqw6dMZDgcWexU4uTcH");
        keyList.add("Dn2ZVo45cuAEnf6Mj4d22EqYfyUJSLsuhfx2khqAv1DTcNJKNH9SAqBAK2zhmLWUvdQFNOtQu0o+RqLpTzOuiFVz6myIsmDTXh0+BZPUw29LkSflIei6+NgCa2HhvOL4jHjXhO4emu/UM5GeV/4PaYKN0qA+hnFT/DuV8pllfTE=");

        Assert.assertTrue(!payloadManager.isEncryptionConsistent(false, keyList));
    }

    @Test
    public void testIsEncryptionConsistent_doubleEncrypted_corrupt() throws Exception {

        ArrayList<String> keyList = new ArrayList<>();
        keyList.add("kG4+ziqbauAwP+wbGT2UM9d5f7yGL2kazOSs4KlYempP68qTQ9YtV5od5dQg3Jxpi4t5Q4faCMqraKpnQ1Gq3Q==");
        keyList.add("9DWpEiXYQ9wkYQA3UpZezsATW6sP3N9fcCetgRyfWUn4");
        keyList.add("Dn2ZVo45cuAEnf6Mj4d22EqYfyUJSLsuhfx2khqAv1DTcNJKNH9SAqBAK2zhmLWUvdQFNOtQu0o+RqLpTzOuiFVz6myIsmDTXh0+BZPUw29LkSflIei6+NgCa2HhvOL4jHjXhO4emu/UM5GeV/4PaYKN0qA+hnFT/DuV8pllfTE=");
        keyList.add("NQyZWdgZmJ4k8NBZjw6n64UIiXtW3zhnnfsZnfzU6YXHovucf2pLOtHq/lxqPzxWMzZuThmWBsdI+ns/HNAGB9s6Rad5Q6R55j5wx+Gd8KvSEK57U+3VZah5oWusdMZNz2hfKIn49EoZszw9bKsEjGzRxxd1D1FLny1pci5gtD4=");

        Assert.assertTrue(!payloadManager.isEncryptionConsistent(true, keyList));

        keyList = new ArrayList<>();
        keyList.add("Dn2ZVo45cuAEnf6Mj4d22EqYfyUJSLsuhfx2khqAv1DTcNJKNH9SAqBAK2zhmLWUvdQFNOtQu0o+RqLpTzOuiFVz6myIsmDTXh0+BZPUw29LkSflIei6+NgCa2HhvOL4jHjXhO4emu/UM5GeV/4PaYKN0qA+hnFT/DuV8pllfTE=");
        keyList.add("xprv9xgDCL6n9Y3x9njMKWiLaYY3XDeEjY8duMyreHk5WhKNxVKeYjNt85SmeZYpctXAdAgodwnWpCknp5HwNB5Np1hxeqw6dMZDgcWexU4uTcH");

        Assert.assertTrue(!payloadManager.isEncryptionConsistent(true, keyList));
    }
}
