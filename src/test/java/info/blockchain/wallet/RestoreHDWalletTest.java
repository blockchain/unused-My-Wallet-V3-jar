package info.blockchain.wallet;

import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by riaanvos on 19/04/16.
 */
public class RestoreHDWalletTest {

    @Test
    public void restoreGoodMnemonicTest() {

        String mnemonic = "all all all all all all all all all all all all";
        String passphrase = "myPassPhrase";
        int accountListSize = 4;

        WalletFactory walletFactory = WalletFactory.getInstance();
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
    public void restoreBadMnemonicTest() {

        WalletFactory walletFactory = WalletFactory.getInstance();
        Wallet wallet = null;

        try {
            wallet = walletFactory.restoreWallet("all all all all all all all all all all all all bogus", "", 1);
        } catch (Exception e) {
            ;
        }finally {
            assertThat("Bad mnemonic should not restore wallet", wallet == null);
        }
    }

    @Test
    public void restoreGoodHexSeedTest() {

        String hexSeed = "0660cc198330660cc198330660cc1983";
        String passphrase = "myPassPhrase";
        int accountListSize = 4;

        WalletFactory walletFactory = WalletFactory.getInstance();
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
    public void chainsShouldBeDifferentForWalletWithDifferentPassphraseTest() {

        WalletFactory walletFactory = WalletFactory.getInstance();
        Wallet wallet1 = null;
        Wallet wallet2 = null;

        try {
            wallet1 = walletFactory.restoreWallet("all all all all all all all all all all all all", "one", 1);
            wallet2 = walletFactory.restoreWallet("all all all all all all all all all all all all", "two", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat(wallet1.getAccount(0).getReceive().getAddressAt(0).getAddressString(),
                not(wallet2.getAccount(0).getReceive().getAddressAt(0).getAddressString()));

        assertThat(wallet1.getAccount(0).getChange().getAddressAt(0).getAddressString(),
                not(wallet2.getAccount(0).getChange().getAddressAt(0).getAddressString()));
    }
}
