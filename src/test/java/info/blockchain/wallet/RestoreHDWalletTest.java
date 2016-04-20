package info.blockchain.wallet;

import org.bitcoinj.core.bip44.Wallet;
import org.bitcoinj.core.bip44.WalletFactory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

/**
 * Created by riaanvos on 19/04/16.
 */
public class RestoreHDWalletTest {

    @Test
    public void restoreGoodMnemonicTest() {

        WalletFactory walletFactory = WalletFactory.getInstance();
        Wallet wallet = null;

        try {
            wallet = walletFactory.restoreWallet("all all all all all all all all all all all all", "", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat("Wallet restore failed", wallet != null);
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
    public void restoreGoodMnemonicWithPassphraseTest() {

        WalletFactory walletFactory = WalletFactory.getInstance();
        Wallet wallet = null;

        try {
            wallet = walletFactory.restoreWallet("all all all all all all all all all all all all", "passphrase", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat("Wallet restore failed", wallet != null);
    }

    @Test
    public void restoreGoodHexSeedTest() {

        WalletFactory walletFactory = WalletFactory.getInstance();
        Wallet wallet = null;

        try {
            wallet = walletFactory.restoreWallet("0660cc198330660cc198330660cc1983", "", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat("Wallet restore failed", wallet != null);
    }

    @Test
    public void receiveChainShouldBeDifferentForWalletWithDifferentPassphraseTest() {

        WalletFactory walletFactory = WalletFactory.getInstance();
        Wallet wallet1 = null;
        Wallet wallet2 = null;

        try {
            wallet1 = walletFactory.restoreWallet("all all all all all all all all all all all all", "one", 1);
            wallet2 = walletFactory.restoreWallet("all all all all all all all all all all all all", "two", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat(wallet1.getAccount(0).getReceive().getAddressAt(0).getAddressString(), not(wallet2.getAccount(0).getReceive().getAddressAt(0).getAddressString()));
    }

    @Test
    public void changeChainShouldBeDifferentForWalletWithDifferentPassphraseTest() {

        WalletFactory walletFactory = WalletFactory.getInstance();
        Wallet wallet1 = null;
        Wallet wallet2 = null;

        try {
            wallet1 = walletFactory.restoreWallet("all all all all all all all all all all all all", "one", 1);
            wallet2 = walletFactory.restoreWallet("all all all all all all all all all all all all", "two", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat(wallet1.getAccount(0).getChange().getAddressAt(0).getAddressString(), not(wallet2.getAccount(0).getChange().getAddressAt(0).getAddressString()));
    }
}
