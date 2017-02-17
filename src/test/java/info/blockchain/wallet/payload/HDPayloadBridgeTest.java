//package info.blockchain.wallet.payload;
//
//
//import info.blockchain.MockedResponseTest;
//import info.blockchain.wallet.bip44.HDWallet;
//import info.blockchain.wallet.payload.data.Payload;
//import java.io.IOException;
//import java.util.ArrayList;
//import org.apache.commons.codec.DecoderException;
//import org.bitcoinj.core.AddressFormatException;
//import org.bitcoinj.crypto.MnemonicException;
//import org.junit.Assert;
//import org.junit.Test;
//
///**
// * Created by riaanvos on 13/07/16.
// */
//public class HDPayloadBridgeTest extends MockedResponseTest {
//
//    @Test
//    public void getHDWalletFromPayload_shouldReturnSameWallet() throws Exception {
//
//        HDPayloadBridge hdPayloadBridge = new HDPayloadBridge();
//
//        HDPayloadBridge.HDWalletPayloadPair pair = hdPayloadBridge.createHDWallet("HDAccount 1");
//        HDWallet wallet = hdPayloadBridge.getHDWalletFromPayload(pair.payload);
//
//        Assert.assertEquals(pair.wallet.getSeedHex(), wallet.getSeedHex());
//        Assert.assertEquals(pair.wallet.getMnemonicOld(), wallet.getMnemonicOld());
//    }
//
//    public String[] getXPUBs(boolean includeArchives, Payload payload) throws IOException, DecoderException, AddressFormatException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicChecksumException, MnemonicException.MnemonicWordException {
//
//        ArrayList<String> xpubs = new ArrayList<String>();
//
//        if (payload.getHdWallet() != null) {
//            int nb_accounts = payload.getHdWallet().getAccounts().size();
//            for (int i = 0; i < nb_accounts; i++) {
//                boolean isArchived = payload.getHdWallet().getAccounts().get(i).isArchived();
//                if (isArchived && !includeArchives) {
//                    ;
//                } else {
//                    String s = payload.getHdWallet().getAccounts().get(i).getXpub();
//                    if (s != null && s.length() > 0) {
//                        xpubs.add(s);
//                    }
//                }
//            }
//        }
//
//        return xpubs.toArray(new String[xpubs.size()]);
//    }
//}
