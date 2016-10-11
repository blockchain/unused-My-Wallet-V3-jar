package info.blockchain.wallet.payload;

import info.blockchain.bip44.Wallet;

import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.crypto.MnemonicException;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by riaanvos on 13/07/16.
 */
public class HDPayloadBridgeTest {

    @Test
    public void getHDWalletFromPayload_shouldReturnSameWallet() throws Exception {

        HDPayloadBridge hdPayloadBridge = new HDPayloadBridge();

        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.createHDWallet("Account 1");
        Wallet wallet = hdPayloadBridge.getHDWalletFromPayload(pair.payload);

        assertThat(pair.wallet.getSeedHex(), is(wallet.getSeedHex()));
        assertThat(pair.wallet.getMnemonic(), is(wallet.getMnemonic()));
    }

    @Test
    public void getHDWatchOnlyWalletFromPayload_shouldReturnSameXpub() throws Exception {

        HDPayloadBridge hdPayloadBridge = new HDPayloadBridge();

        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.createHDWallet("Account 1");
        Wallet watchOnlyWallet = hdPayloadBridge.getHDWatchOnlyWalletFromXpubs(getXPUBs(true, pair.payload));

        assertThat(pair.wallet.getAccount(0).xpubstr(), is(watchOnlyWallet.getAccount(0).xpubstr()));
    }

    @Test
    public void getHDWatchOnlyWalletFromPayload_shouldReturnNoXpriv() throws Exception {

        HDPayloadBridge hdPayloadBridge = new HDPayloadBridge();

        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.createHDWallet("Account 1");
        Wallet watchOnlyWallet = hdPayloadBridge.getHDWatchOnlyWalletFromXpubs(getXPUBs(true, pair.payload));

        assertThat("WatchOnlyWallet should not contain private key", watchOnlyWallet.getAccount(0).xprvstr() == null);
    }

    public String[] getXPUBs(boolean includeArchives, Payload payload) throws IOException, DecoderException, AddressFormatException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicChecksumException, MnemonicException.MnemonicWordException {

        ArrayList<String> xpubs = new ArrayList<String>();

        if (payload.getHdWallet() != null) {
            int nb_accounts = payload.getHdWallet().getAccounts().size();
            for (int i = 0; i < nb_accounts; i++) {
                boolean isArchived = payload.getHdWallet().getAccounts().get(i).isArchived();
                if (isArchived && !includeArchives) {
                    ;
                } else {
                    String s = payload.getHdWallet().getAccounts().get(i).getXpub();
                    if (s != null && s.length() > 0) {
                        xpubs.add(s);
                    }
                }
            }
        }

        return xpubs.toArray(new String[xpubs.size()]);
    }
}
