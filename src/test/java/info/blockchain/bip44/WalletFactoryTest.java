package info.blockchain.bip44;

import org.bitcoinj.crypto.MnemonicException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class WalletFactoryTest {

    WalletFactory walletFactory = new WalletFactory();

    @Test
    public void createWallet_shouldContainCorrectInfo() throws IOException, MnemonicException.MnemonicLengthException {

        String passphrase = "passphrase";
        int mnemonicLength = 12;
        String path = "M/44H";

        Wallet wallet = walletFactory.newWallet(mnemonicLength, passphrase, 1);

        assertThat(wallet.getMnemonic().split(" ").length, is(mnemonicLength));
        assertThat(wallet.getPassphrase(), is(passphrase));
        assertThat(wallet.getAccounts().size(), is(1));
        assertThat(wallet.getPath(), is(path));
    }

    @Test
    public void restoreWallet_withGoodMnemonic_shouldContainCorrectInfo() {

        String mnemonic = "all all all all all all all all all all all all";
        String passphrase = "myPassPhrase";
        int accountListSize = 4;

        Wallet wallet = null;

        try {
            wallet = walletFactory.restoreWallet(mnemonic, passphrase, accountListSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat("Wallet restore failed", wallet != null);
        assertThat(wallet.getMnemonic(), is(mnemonic));
        assertThat(wallet.getAccounts().size(), is(accountListSize));
        assertThat(wallet.getPassphrase(), is(passphrase));
    }

    @Test
    public void restoreWallet_withBadMnemonic_shouldFail() {

        Wallet wallet = null;

        try {
            wallet = walletFactory.restoreWallet("all all all all all all all all all all all all bogus", "", 1);
        } catch (Exception e) {
            ;
        } finally {
            assertThat("Bad mnemonic should not restore wallet", wallet == null);
        }
    }

    @Test
    public void restoreWallet_withGoodSeedHex_shouldPass() {

        String hexSeed = "0660cc198330660cc198330660cc1983";
        String passphrase = "myPassPhrase";
        int accountListSize = 4;

        Wallet wallet = null;

        try {
            wallet = walletFactory.restoreWallet(hexSeed, passphrase, accountListSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat("Wallet restore failed", wallet != null);
        assertThat(wallet.getSeedHex(), is(hexSeed));
        assertThat(wallet.getAccounts().size(), is(accountListSize));
        assertThat(wallet.getPassphrase(), is(passphrase));
    }

    @Test
    public void restoredWallet_addressChains_withSamePassphrase_shouldBeSame() {

        Wallet restoredWallet1 = null;
        Wallet restoredWallet2 = null;

        String passphrase1 = "passphrase1";

        try {
            restoredWallet1 = walletFactory.restoreWallet("all all all all all all all all all all all all", passphrase1, 1);
            restoredWallet2 = walletFactory.restoreWallet("all all all all all all all all all all all all", passphrase1, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat(restoredWallet1.getAccount(0).getReceive().getAddressAt(0).getAddressString(),
                is(restoredWallet2.getAccount(0).getReceive().getAddressAt(0).getAddressString()));

        assertThat(restoredWallet1.getAccount(0).getChange().getAddressAt(0).getAddressString(),
                is(restoredWallet2.getAccount(0).getChange().getAddressAt(0).getAddressString()));
    }

    @Test
    public void restoredWallet_addressChains_withDifferentPassphrase_shouldBeDifferent() {

        Wallet wallet1 = null;
        Wallet wallet2 = null;

        String passphrase1 = "passphrase1";
        String passphrase2 = "passphrase2";

        try {
            wallet1 = walletFactory.restoreWallet("all all all all all all all all all all all all", passphrase1, 1);
            wallet2 = walletFactory.restoreWallet("all all all all all all all all all all all all", passphrase2, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat(wallet1.getAccount(0).getReceive().getAddressAt(0).getAddressString(),
                not(wallet2.getAccount(0).getReceive().getAddressAt(0).getAddressString()));

        assertThat(wallet1.getAccount(0).getChange().getAddressAt(0).getAddressString(),
                not(wallet2.getAccount(0).getChange().getAddressAt(0).getAddressString()));
    }
}
