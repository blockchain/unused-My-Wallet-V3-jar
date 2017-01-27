package info.blockchain.wallet.bip44;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.MainNetParams;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class WalletFactoryTest {

    WalletFactory walletFactory = new WalletFactory(MainNetParams.get());

    @Test
    public void testCreateWallet() throws IOException, MnemonicException.MnemonicLengthException {

        String passphrase = "passphrase";
        int mnemonicLength = 12;
        String path = "M/44H";

        Wallet wallet = walletFactory.newWallet(mnemonicLength, passphrase, 1);

        Assert.assertEquals(mnemonicLength, wallet.getMnemonic().split(" ").length);
        Assert.assertEquals(passphrase, wallet.getPassphrase());
        Assert.assertEquals(1, wallet.getAccounts().size());
        Assert.assertEquals(path, wallet.getPath());
    }

    @Test
    public void testRestoreWallet_mnemonic() {

        String mnemonic = "all all all all all all all all all all all all";
        String passphrase = "myPassPhrase";
        int accountListSize = 4;

        Wallet wallet = null;

        try {
            wallet = walletFactory.restoreWallet(mnemonic, passphrase, accountListSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Wallet
        Assert.assertNotNull(wallet);
        Assert.assertEquals("edb3e309910eafe85e03c9067b82a04d59e040523810c92bac3aca8252d461d5",
            wallet.getMasterKey().getPrivateKeyAsHex());
        Assert.assertEquals(16, wallet.getSeed().length);
        Assert.assertEquals("0660cc198330660cc198330660cc1983", wallet.getSeedHex());
        Assert.assertEquals("M/44H", wallet.getPath());
        Assert.assertEquals(mnemonic, wallet.getMnemonic());
        Assert.assertEquals(passphrase, wallet.getPassphrase());

        //Account
        Assert.assertEquals(accountListSize, wallet.getAccounts().size());
        wallet.addAccount();
        Assert.assertEquals(accountListSize + 1, wallet.getAccounts().size());
    }

    @Test
    public void testRestoreWallet_badMnemonic_fail() {

        Wallet wallet = null;

        try {
            wallet = walletFactory
                .restoreWallet("all all all all all all all all all all all all bogus", null, 1);
        } catch (Exception e) {
            ;
        } finally {
            Assert.assertNull(wallet);
        }
    }

    @Test
    public void testRestoreWallet_seedHex() {

        String hexSeed = "0660cc198330660cc198330660cc1983";
        String passphrase = "myPassPhrase";
        int accountListSize = 4;

        Wallet wallet = null;

        try {
            wallet = walletFactory.restoreWallet(hexSeed, passphrase, accountListSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertNotNull(wallet);
        Assert.assertEquals(hexSeed, wallet.getSeedHex());
        Assert.assertEquals(accountListSize, wallet.getAccounts().size());
        Assert.assertEquals(passphrase, wallet.getPassphrase());
    }

    @Test
    public void testRestoredWallet_addressChains_withSamePassphrase_shouldBeSame() {

        Wallet restoredWallet1 = null;
        Wallet restoredWallet2 = null;

        String passphrase1 = "passphrase1";

        try {
            restoredWallet1 = walletFactory
                .restoreWallet("all all all all all all all all all all all all", passphrase1, 1);
            restoredWallet2 = walletFactory
                .restoreWallet("all all all all all all all all all all all all", passphrase1, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(
            restoredWallet2.getAccount(0).getReceive().getAddressAt(0).getAddressString(),
            restoredWallet1.getAccount(0).getReceive().getAddressAt(0).getAddressString());

        Assert.assertEquals(
            restoredWallet2.getAccount(0).getChange().getAddressAt(0).getAddressString(),
            restoredWallet1.getAccount(0).getChange().getAddressAt(0).getAddressString());
    }

    @Test
    public void testRestoredWallet_addressChains_withDifferentPassphrase_shouldBeDifferent() {

        Wallet wallet1 = null;
        Wallet wallet2 = null;

        String passphrase1 = "passphrase1";
        String passphrase2 = "passphrase2";

        try {
            wallet1 = walletFactory
                .restoreWallet("all all all all all all all all all all all all", passphrase1, 1);
            wallet2 = walletFactory
                .restoreWallet("all all all all all all all all all all all all", passphrase2, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertNotEquals(
            wallet2.getAccount(0).getReceive().getAddressAt(0).getAddressString(),
            wallet1.getAccount(0).getReceive().getAddressAt(0).getAddressString());

        Assert.assertNotEquals(
            wallet2.getAccount(0).getChange().getAddressAt(0).getAddressString(),
            wallet1.getAccount(0).getChange().getAddressAt(0).getAddressString());
    }

    @Test
    public void testAccount() throws AddressFormatException {
        Account account = new Account(MainNetParams.get(), "xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV", 1);
        Assert.assertEquals("xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV",account.xpubstr());
        Assert.assertEquals(1,account.getId());

        account = new Account(MainNetParams.get(), "xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV");
        Assert.assertEquals("xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV",account.xpubstr());
        Assert.assertEquals(0,account.getId());
    }
}
