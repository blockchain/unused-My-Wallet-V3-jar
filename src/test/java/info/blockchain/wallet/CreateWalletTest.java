package info.blockchain.wallet;

import org.bitcoinj.core.bip44.Wallet;
import org.bitcoinj.core.bip44.WalletFactory;
import org.bitcoinj.crypto.MnemonicException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by riaanvos on 19/04/16.
 */
public class CreateWalletTest {

    @Test
    public void createDefaultWalletTest() {

        WalletFactory walletFactory = WalletFactory.getInstance();
        Wallet wallet = null;

        try {
            wallet = walletFactory.newWallet(12, "", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertThat("Wallet creation failed", wallet != null);
    }

    @Test
    public void isFactoryHoldingWalletTest() {

        WalletFactory walletFactory = WalletFactory.getInstance();

        try {
            walletFactory.newWallet(12, "", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            assertThat("Wallet creation failed", walletFactory.get() != null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MnemonicException.MnemonicLengthException e) {
            e.printStackTrace();
        }
    }
}
