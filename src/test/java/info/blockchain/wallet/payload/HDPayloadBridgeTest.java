package info.blockchain.wallet.payload;

import org.bitcoinj.crypto.MnemonicException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by riaanvos on 07/07/16.
 */
public class HDPayloadBridgeTest {

    HDPayloadBridge hdPayloadBridge = new HDPayloadBridge();

    @Test
    public void newWallet_shouldPass() throws IOException, MnemonicException.MnemonicLengthException {

//        hdPayloadBridge.


        assertThat("", is(""));
    }
}
