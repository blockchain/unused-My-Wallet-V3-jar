package info.blockchain.wallet.payload;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by riaanvos on 07/07/16.
 */
public class PayloadManagerTest {

    @Test
    public void newWallet_shouldPass() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();

        String label = "Account 1";
        String passphrase = "passphrase";

        Payload payload = payloadManager.createHDWallet(passphrase, label);

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getAccounts().get(0).getLabel(), is(label));
        assertThat(payload, is(payloadManager.getPayload()));
        assertThat("Checksum should not be null", payloadManager.getCheckSum() != null);

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void restoreWallet_withMnemonic_shouldPass() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();

        String mnemonic = "all all all all all all all all all all all all";

        String seedHex = "0660cc198330660cc198330660cc1983";//All all ...
        String xpub1 = "xpub6BiVtCpG9fQPxnPmHXG8PhtzQdWC2Su4qWu6XW9tpWFYhxydCLJGrWBJZ5H6qTAHdPQ7pQhtpjiYZVZARo14qHiay2fvrX996oEP42u8wZy";
        String xpub2 = "xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL";
        String xpub3 = "xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x";
        String xpub4 = "xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH";
        String xpub5 = "xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP";

        Payload payload = payloadManager.restoreHDWallet(mnemonic, "", "");

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getSeedHex(), is(seedHex));

        assertThat(payload.getHdWallet().getAccounts().get(0).getXpub(), is(xpub1));
        assertThat(payload.getHdWallet().getAccounts().get(0).getXpriv().substring(4), is("9xj9UhHNKHr6kJKJBVj82ZxFrbfhczBDUHyVj7kHGAiZqAeUenz2JhrphnMMYVKcWcVPFJESngtKsVa4FYEvFfWUTtZThCoZdwDeS9qQnqm"));

        payloadManager.addAccount("",null);
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpub(), is(xpub2));
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpriv().substring(4), is("9xj9UhHNKHr6nkRg3ZpSBp2i3MgSazXa3LGet5MsVY3nTeE1zvnwVrjpnsJGEtEvvcm8fwoUBVpnHcioJfFqRUaZ6ijXEuwUuv2Q5RM6dGR"));

        payloadManager.addAccount("",null);
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpub(), is(xpub3));
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpriv().substring(4), is("9xj9UhHNKHr6rUDptMDdQhw5ccX8mzVQBopwYejpRt1NHpFvQMSG1a8RGRJjZRE8rRJJ6N9g1GcB6yWEgkXCzGBweq934jS9LfBuViQRxRw"));

        payloadManager.addAccount("",null);
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpub(), is(xpub4));
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpriv().substring(4), is("9xj9UhHNKHr6tdLP1UdrAJKKRUiGd92aBbsqkW28vtdmCzXTvns1aNKwh5uM1nSbdD8Y4x9VBnTLrDDEbREnu9KYnDyvt8QRPtPWQ78UgAG"));

        payloadManager.addAccount("",null);
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpub(), is(xpub5));
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpriv().substring(4), is("9xj9UhHNKHr6vLRGPDajPuoc2futBwhu93ZLCUuoGBya3uD4X5kDfMuUiEHz7HPWPpkgCHiwNbLWjxa6QrqfjmPmVr146GUt8D5shiXkQpC"));

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void restoreWallet_withMnemonic_shouldContainCorrectReceiveAddresses() throws Exception{

        PayloadManager payloadManager = PayloadManager.getInstance();

        String mnemonic = "all all all all all all all all all all all all";
        Payload payload = payloadManager.restoreHDWallet(mnemonic, "", "");

        int accountIndex = 0;
        String[] expectedAddresses0 = {"1JAd7XCBzGudGpJQSDSfpmJhiygtLQWaGL",
                "1GWFxtwWmNVqotUPXLcKVL2mUKpshuJYo",
                "1Eni8JFS4yA2wJkicc3yx3QzCNzopLybCM",
                "124dT55Jqpj9AKTyJnTX6G8RkUs7ReTzun",
                "15T9DSqc6wjkPxcr2MNVSzF9JAePdvS3n1",
                "1GA9u9TfCG7SWmKCveBumdA1TZpfom6ZdJ",
                "1PogPE3bXc84abzEuM2rJEZf2vCbCEZzXz",
                "176U2WABbj4h5PCrxE963wmxzXd2Mw6bP4",
                "1HRZDR7CmLnq59w6mtzNa7SHtVWPSxdgKA"};

        for(int i = 0; i < expectedAddresses0.length; i++) {
            String receiveAddress = payloadManager.getReceiveAddress(accountIndex);
            assertThat(receiveAddress, is(expectedAddresses0[i]));

            String changeAddress = payloadManager.getChangeAddress(accountIndex);
            //TODO
        }

        payloadManager.addAccount("", null);//add account - index 1
        payloadManager.addAccount("", null);//add account - index 2
        payloadManager.addAccount("", null);//add account - index 3
        payloadManager.addAccount("", null);//add account - index 4

        //Let's test index 4
        accountIndex = 4;
        String[] expectedAddresses4 = {"1HdTbfFfHLBbMB7Wr2pGmZUhEz9TMK6Q96",
                "1DqQYGbyPBCTh22EifruQ2CQdudayzXKaZ",
                "1CpBgovtuWNzwFVPBmfJehtytXEmYD7ST7",
                "167tYTuBweEux6EhiNA1g2fptQoqFdC5XY",
                "1AyvLEtEZ7RBKxrqbeC3UemVzkKhWNfBLN",
                "13TrnmcyZwM4pW2vAbzk2pzviV9NyvNN3q",
                "12yv1bjTqcztNWMpuyZBRgpwvjyaiKSMvz",
                "1LZEWagvzxg4vLQSVFmNksUvfQQ9N7yGqA",
                "19WEjX2zgXdn6FCLmRAJ5Ty593GkJ77pNj"};

        for(int i = 0; i < expectedAddresses4.length; i++) {
            String receiveAddress = payloadManager.getReceiveAddress(accountIndex);
            assertThat(receiveAddress, is(expectedAddresses4[i]));

            String changeAddress = payloadManager.getChangeAddress(accountIndex);
            //TODO
        }
    }




//    @Test
//    public void createWallet_withPassphrase_shouldPass() throws Exception {
//
//        String passphrase = "passphrase";
//
//        Wallet wallet = bciWalletFactory.createWallet(passphrase);
//
//        assertThat(wallet.getMnemonic().split(" ").length, is(12));//All Bci mnemonics are 12
//        assertThat(wallet.getAccounts().size(), is(1));//Newly created wallets will contain 1 account
//        assertThat(wallet.getPassphrase(), is(passphrase));
//    }
//
//    @Test
//    public void createWallet_withEmptyPassphrase_shouldPass() throws Exception {
//
//        String passphrase = "";
//
//        Wallet wallet = bciWalletFactory.createWallet(passphrase);
//
//        assertThat(wallet.getMnemonic().split(" ").length, is(12));//All Bci mnemonics are 12
//        assertThat(wallet.getAccounts().size(), is(1));//Newly created wallets will contain 1 account
//        assertThat(wallet.getPassphrase(), is(passphrase));
//    }
//
//    @Test
//    public void restoreWallet_withGoodMnemonic_shouldPass() throws Exception {
//
//        String goodMnemonic = "all all all all all all all all all all all all";
//        String passphrase = "myPassPhrase";
//        int accountListSize = 4;
//
//        Wallet wallet = bciWalletFactory.restoreWallet(goodMnemonic, passphrase, accountListSize);
//
//        String[] restoredMnemonic = wallet.getMnemonic().split(" ");
//        assertThat(restoredMnemonic.length, is(12));//All Bci mnemonics are 12
//        assertThat(wallet.getAccounts().size(), is(accountListSize));
//        assertThat(wallet.getPassphrase(), is(passphrase));
//        for (int i = 0; i < restoredMnemonic.length; i++){
//            assertThat(restoredMnemonic[i], is("all"));
//        }
//    }
//
//    @Test
//    public void restoreWallet_withBadMnemonic_shouldFail() {
//
//        String passphrase = "myPassPhrase";
//        int accountListSize = 4;
//
//        try {
//            String badMnemonic_Length_notMultipleOf3 = "all all all all all all all all all all all";
//            bciWalletFactory.restoreWallet(badMnemonic_Length_notMultipleOf3, passphrase, accountListSize);
//        } catch (MnemonicException.MnemonicLengthException e) {
//            assertThat("Mnemonic with incorrect length should not produce wallet", true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//            String badMnemonic_incorrectWord = "all all all all all all all all all all all fakeWord";
//            bciWalletFactory.restoreWallet(badMnemonic_incorrectWord, passphrase, accountListSize);
//        } catch (Exception e) {
//            assertThat("Mnemonic with incorrect word should not produce wallet", true);
//        }
//
//        try {
//            String badMnemonic_badChecksum = "all all all all all all all all all all all all all all all all all all all all all all all all all all all all all all all all all";
//            bciWalletFactory.restoreWallet(badMnemonic_badChecksum, passphrase, accountListSize);
//        } catch (Exception e) {
//            assertThat("Mnemonic with bad checksum should not produce wallet", true);
//        }
//    }
//
//    @Test
//    public void createWatchOnlyWallet_shouldPass() throws Exception {
//
//        String[] xpubs = {"xpub6BiVtCpG9fQPxnPmHXG8PhtzQdWC2Su4qWu6XW9tpWFYhxydCLJGrWBJZ5H6qTAHdPQ7pQhtpjiYZVZARo14qHiay2fvrX996oEP42u8wZy",
//                "xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL"};
//
//        Wallet wallet = bciWalletFactory.createWatchOnlyWallet(xpubs);
//
//        assertThat(wallet.getAccounts().size(), is(2));
//    }
}
