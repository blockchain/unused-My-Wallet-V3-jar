package info.blockchain.wallet.payload;

import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PayloadManagerTest {

    PayloadManager payloadManager;
    String password = "password";
    String label = "Account 1";
    Payload payload;

    @Before
    public void setUp() throws Exception {
        payloadManager = PayloadManager.getInstance();
        payload = payloadManager.createHDWallet(password,label);
    }

    @After
    public void tearDown() throws Exception{
        PayloadManager.getInstance().wipe();
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withValidVars_shouldPass() throws Exception{

        payloadManager.initiatePayload(payload.getSharedKey(), payload.getGuid(), new CharSequenceX(password), new PayloadManager.InitiatePayloadListener() {
            public void onSuccess() {
                assertThat("Payload successfully fetch and decrypted", true);
            }

            public void onInvalidGuidOrSharedKey() {
                assertThat("onInvalidGuidOrSharedKey", false);
            }

            public void onServerError(String error) {
                assertThat("onServerConnectionFail", false);
            }

            public void onEmptyPayloadReturned() {
                assertThat("onEmptyPayloadReturned", false);
            }

            public void onDecryptionFail() {
                assertThat("onDecryptionFail", false);
            }

            public void onWalletSyncFail() {
                assertThat("onWalletSyncFail", false);
            }

            public void onWalletVersionNotSupported() {
                assertThat("onWalletVersionNotSupported", false);
            }
        });
        try{Thread.sleep(500);}catch (Exception e){}
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withInvalidGuid_shouldCall_onInvalidGuid(){

        payloadManager.initiatePayload(payload.getSharedKey(), payload.getGuid()+"a", new CharSequenceX(password), new PayloadManager.InitiatePayloadListener() {
            public void onSuccess() {
                assertThat("onSuccess", false);
            }

            public void onInvalidGuidOrSharedKey() {
                assertThat("Invalid Guid successfully detected", true);
            }

            public void onServerError(String error) {
                assertThat("onServerConnectionFail", false);
            }

            public void onEmptyPayloadReturned() {
                assertThat("onEmptyPayloadReturned", false);
            }

            public void onDecryptionFail() {
                assertThat("onDecryptionFail", false);
            }

            public void onWalletSyncFail() {
                assertThat("onWalletSyncFail", false);
            }

            public void onWalletVersionNotSupported() {
                assertThat("onWalletVersionNotSupported", false);
            }
        });
        try{Thread.sleep(500);}catch (Exception e){}
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withInvalidPassword_shouldCall_onDecryptionFail(){

        payloadManager.initiatePayload(payload.getSharedKey(), payload.getGuid(), new CharSequenceX(password+"a"), new PayloadManager.InitiatePayloadListener() {
            public void onSuccess() {
                assertThat("onSuccess", false);
            }

            public void onInvalidGuidOrSharedKey() {
                assertThat("onInvalidGuidOrSharedKey", false);
            }

            public void onServerError(String error) {
                assertThat("onServerConnectionFail", false);
            }

            public void onEmptyPayloadReturned() {
                assertThat("onEmptyPayloadReturned", false);
            }

            public void onDecryptionFail() {
                assertThat("Decryption failed as expected", true);
            }

            public void onWalletSyncFail() {
                assertThat("onWalletSyncFail", false);
            }

            public void onWalletVersionNotSupported() {
                assertThat("onWalletVersionNotSupported", false);
            }
        });
        try{Thread.sleep(500);}catch (Exception e){}
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withInvalidShaeredKey_shouldCall_onDecryptionFail(){

        payloadManager.initiatePayload(payload.getSharedKey()+"a", payload.getGuid(), new CharSequenceX(password), new PayloadManager.InitiatePayloadListener() {
            public void onSuccess() {
                assertThat("onSuccess", false);
            }

            public void onInvalidGuidOrSharedKey() {
                assertThat("Invalid shared key successfully detected", true);
            }

            public void onServerError(String error) {
                assertThat("onServerConnectionFail", false);
            }

            public void onEmptyPayloadReturned() {
                assertThat("onEmptyPayloadReturned", false);
            }

            public void onDecryptionFail() {
                assertThat("onDecryptionFail", false);
            }

            public void onWalletSyncFail() {
                assertThat("onWalletSyncFail", false);
            }

            public void onWalletVersionNotSupported() {
                assertThat("onWalletVersionNotSupported", false);
            }
        });
        try{Thread.sleep(500);}catch (Exception e){}
    }

    @Test
    public void getPayloadFromServerAndDecrypt_withIncompatibleVersion_shouldCall_OnWalletVersionNotSupported() throws Exception{

        payloadManager.setVersion(4.0);

        payloadManager.savePayloadToServer();

        payloadManager.initiatePayload(payload.getSharedKey(), payload.getGuid(), new CharSequenceX(password), new PayloadManager.InitiatePayloadListener() {
            public void onSuccess() {
                assertThat("Incompatible version should not pass", false);
            }

            public void onInvalidGuidOrSharedKey() {
                assertThat("onInvalidGuidOrSharedKey", false);
            }

            public void onServerError(String error) {
                assertThat("onServerConnectionFail", false);
            }

            public void onEmptyPayloadReturned() {
                assertThat("onEmptyPayloadReturned", false);
            }

            public void onDecryptionFail() {
                assertThat("onDecryptionFail", false);
            }

            public void onWalletSyncFail() {
                assertThat("onWalletSyncFail", false);
            }

            public void onWalletVersionNotSupported() {
                assertThat("onWalletVersionNotSupported", true);
            }
        });
        try{Thread.sleep(500);}catch (Exception e){}
    }
    
    @Test
    public void upgradeV2PayloadToV3_shouldPass() throws Exception {

        final PayloadManager payloadManager = PayloadManager.getInstance();

        //Create HD
        String label = "Account 1";
        Payload payload = payloadManager.createHDWallet("password",label);
        payload.setHdWallets(new ArrayList<HDWallet>());//remove hd

        //Add legacy (way too much extra to docleanup newLegacyAddress() soon)
        LegacyAddress legacyAddress = payloadManager.generateLegacyAddress("android","6.6", null);
        if(payloadManager.addLegacyAddress(legacyAddress)) {
            final String guidOriginal = payloadManager.getPayload().getGuid();

            //Now we have legacy wallet (only addresses)
            payloadManager.upgradeV2PayloadToV3(new CharSequenceX(""), true, "My Bci Wallet", new PayloadManager.UpgradePayloadListener() {
                public void onDoubleEncryptionPasswordError() {
                    assertThat("upgradeV2PayloadToV3 failed", false);
                }

                public void onUpgradeSuccess() {

                    assertThat(payloadManager.getPayload().getGuid(), is(guidOriginal));
                    assertThat("Payload not flagged as upgraded", payloadManager.getPayload().isUpgraded());

                    String xpriv = payloadManager.getPayload().getHdWallet().getAccounts().get(0).getXpriv();
                    assertThat("Xpriv may not be null or empty after upgrade", xpriv != null && !xpriv.isEmpty());
                    try {
                        assertThat(payloadManager.getHDMnemonic().split(" ").length, is(12));
                    } catch (Exception e) {
                        e.printStackTrace();
                        assertThat("upgradeV2PayloadToV3 failed", false);
                    }
                }

                public void onUpgradeFail() {
                    assertThat("upgradeV2PayloadToV3 failed", false);
                }
            });
        }else{
            assertThat("adding new Legacy address failed failed", false);
        }

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void upgradeV2PayloadToV3_withSecondPassword_shouldPass() throws Exception {

        final String secondPassword = "password2";
        final PayloadManager payloadManager = PayloadManager.getInstance();

        //Create HD
        String label = "Account 1";
        Payload payload = payloadManager.createHDWallet("password",label);
        payload.setHdWallets(new ArrayList<HDWallet>());//remove hd

        //Set second password
        String hash = DoubleEncryptionFactory.getInstance().getHash(payload.getSharedKey(), secondPassword, payload.getDoubleEncryptionPbkdf2Iterations());
        payload.setDoublePasswordHash(hash);
        payload.setDoubleEncrypted(true);

        //Add legacy (way too much extra to docleanup newLegacyAddress() soon)
        LegacyAddress legacyAddress = payloadManager.generateLegacyAddress("android","6.6", secondPassword);
        if(payloadManager.addLegacyAddress(legacyAddress)) {
            final String guidOriginal = payloadManager.getPayload().getGuid();

            //Now we have legacy wallet (only addresses)
            payloadManager.upgradeV2PayloadToV3(new CharSequenceX(secondPassword), true, "My Bci Wallet", new PayloadManager.UpgradePayloadListener() {
                public void onDoubleEncryptionPasswordError() {
                    assertThat("upgradeV2PayloadToV3 failed", false);
                }

                public void onUpgradeSuccess() {

                    assertThat(payloadManager.getPayload().getGuid(), is(guidOriginal));
                    assertThat("Payload not flagged as upgraded", payloadManager.getPayload().isUpgraded());

                    String xpriv = payloadManager.getPayload().getHdWallet().getAccounts().get(0).getXpriv();
                    assertThat("Xpriv may not be null or empty after upgrade", xpriv != null && !xpriv.isEmpty());
                    try {
                        assertThat(payloadManager.getHDMnemonic().split(" ").length, is(12));
                    } catch (Exception e) {
                        e.printStackTrace();
                        assertThat("upgradeV2PayloadToV3 failed", false);
                    }
                }

                public void onUpgradeFail() {
                    assertThat("upgradeV2PayloadToV3 failed", false);
                }
            });
        }else{
            assertThat("adding new Legacy address failed failed", false);
        }

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void createWallet_shouldPass() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();

        String label = "Account 1";
        Payload payload = payloadManager.createHDWallet("password",label);

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getAccounts().get(0).getLabel(), is(label));
        assertThat(payload, is(payloadManager.getPayload()));
        assertThat("Checksum should not be null", payloadManager.getCheckSum() != null);

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void restoreWallet_withMnemonicNoPassphrase_shouldPass() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();

        String mnemonic = "all all all all all all all all all all all all";

        String seedHex = "0660cc198330660cc198330660cc1983";//All all ...
        String xpub1 = "xpub6BiVtCpG9fQPxnPmHXG8PhtzQdWC2Su4qWu6XW9tpWFYhxydCLJGrWBJZ5H6qTAHdPQ7pQhtpjiYZVZARo14qHiay2fvrX996oEP42u8wZy";
        String xpub2 = "xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL";
        String xpub3 = "xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x";
        String xpub4 = "xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH";
        String xpub5 = "xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP";

        Payload payload = payloadManager.restoreHDWallet("password", mnemonic, "");

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getSeedHex(), is(seedHex));

        assertThat(payload.getHdWallet().getAccounts().get(0).getXpub(), is(xpub1));
        assertThat(payload.getHdWallet().getAccounts().get(0).getXpriv().substring(4), is("9xj9UhHNKHr6kJKJBVj82ZxFrbfhczBDUHyVj7kHGAiZqAeUenz2JhrphnMMYVKcWcVPFJESngtKsVa4FYEvFfWUTtZThCoZdwDeS9qQnqm"));

        payloadManager.addAccount("",null, null);
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpub(), is(xpub2));
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpriv().substring(4), is("9xj9UhHNKHr6nkRg3ZpSBp2i3MgSazXa3LGet5MsVY3nTeE1zvnwVrjpnsJGEtEvvcm8fwoUBVpnHcioJfFqRUaZ6ijXEuwUuv2Q5RM6dGR"));

        payloadManager.addAccount("",null, null);
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpub(), is(xpub3));
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpriv().substring(4), is("9xj9UhHNKHr6rUDptMDdQhw5ccX8mzVQBopwYejpRt1NHpFvQMSG1a8RGRJjZRE8rRJJ6N9g1GcB6yWEgkXCzGBweq934jS9LfBuViQRxRw"));

        payloadManager.addAccount("",null, null);
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpub(), is(xpub4));
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpriv().substring(4), is("9xj9UhHNKHr6tdLP1UdrAJKKRUiGd92aBbsqkW28vtdmCzXTvns1aNKwh5uM1nSbdD8Y4x9VBnTLrDDEbREnu9KYnDyvt8QRPtPWQ78UgAG"));

        payloadManager.addAccount("",null, null);
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

        Payload payload = payloadManager.restoreHDWallet("password", mnemonic, "", passphrase);

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getSeedHex(), is(seedHex));

        assertThat(payload.getHdWallet().getAccounts().get(0).getXpub(), is(xpub1));
        assertThat(payload.getHdWallet().getAccounts().get(0).getXpriv().substring(4), is("9z4inCUBXyHCbzmU3jN1YUNCY8V5gJxcgSgCqZjVKGC9yibzTv5W1D91kRvVoaqPGNj9CosizY3nLnZheTYqZ4aYYWfAqMw9vz4F8mxj3KG"));

        payloadManager.addAccount("",null, null);
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpub(), is(xpub2));
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpriv().substring(4), is("9z4inCUBXyHCdr8m9pGmXuc7syJcmtZWGXENAfvCTg99LBq3NrhYZR47Umizc4tUtm8meaD58sTLuAyfNoTLWL7ELKtLKCSRuBnCgFfr2KX"));

        payloadManager.addAccount("",null, null);
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpub(), is(xpub3));
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpriv().substring(4), is("9z4inCUBXyHCgSz7Fcv9b4b2g6i2eyToGwtPn9s2eLgQSL7nwgL6PU6SJfAdunPLraJbaPWLHzGBxu78ETqBPk36JgBiUxUB1hfeMVaci1q"));

        payloadManager.addAccount("",null, null);
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpub(), is(xpub4));
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpriv().substring(4), is("9z4inCUBXyHCj1fXNVHQjEzH3bU5JmZyyT99LyQdnvFMxNyJtU4q2BSb2PfLNBMLDCgkC9Fv7cyCstkc1AyWZW8YXZc1aPJFTpJkcL9MpF7"));

        payloadManager.addAccount("",null, null);
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpub(), is(xpub5));
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpriv().substring(4), is("9z4inCUBXyHCkyYdB7FYtyNpYJtoBUKepRFJ4t5gWUkysmJaa7YcchzSoTJQ9TgEG78i3LcnWvkxr5eiYbxUkDN7s8NWPVwf7bgx7DGYFqF"));

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void restoreWallet_withMnemonic_shouldContainCorrectReceiveAddresses() throws Exception{

        PayloadManager payloadManager = PayloadManager.getInstance();

        String mnemonic = "all all all all all all all all all all all all";
        payloadManager.restoreHDWallet("password",mnemonic, "");

        assertThat(payloadManager.getReceiveAddress(0), is("1JAd7XCBzGudGpJQSDSfpmJhiygtLQWaGL"));

        payloadManager.addAccount("",null, null);
        assertThat(payloadManager.getReceiveAddress(1), is("1Dgews942GZs2GV7JT5v1t4KxuaDZpJgG9"));

        payloadManager.addAccount("",null, null);
        assertThat(payloadManager.getReceiveAddress(2), is("1N4rfuysGPvWuKHFnEeVdv8NE8QCNPZ9v3"));

        payloadManager.addAccount("",null, null);
        assertThat(payloadManager.getReceiveAddress(3), is("19LcKJTDYuF8B3p4bgDoW2XXn5opPqutx3"));

        PayloadManager.getInstance().wipe();
    }
}
