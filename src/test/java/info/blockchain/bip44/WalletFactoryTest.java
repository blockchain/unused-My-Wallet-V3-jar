package info.blockchain.bip44;

import org.bitcoinj.crypto.MnemonicException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by riaanvos on 07/07/16.
 */
public class WalletFactoryTest {

    WalletFactory walletFactory = new WalletFactory();

    @Test
    public void newWallet_shouldPass() throws IOException, MnemonicException.MnemonicLengthException {

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
    public void restore_withGoodMnemonic_shouldPass() {

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
    public void restore_withBadMnemonic_shouldFail() {

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
    public void restore_withGoodSeedHex_shouldPass() {

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
    public void walletChains_withDifferentPassphrase_shouldBeDifferent() {

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
