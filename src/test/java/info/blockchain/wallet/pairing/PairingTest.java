package info.blockchain.wallet.pairing;

import info.blockchain.api.WalletPayload;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class PairingTest {

    Pairing pairing = new Pairing();

    final String guid = "a09910d9-1906-4ea1-a956-2508c3fe0661";
    final String qrRaw_Good = "1|" + guid + "|TGbFKLZQ+ZxaAyDwdUcMOAtzolqUYMdkjOYautXPNt41AXqjk67P9aDqRPMM4mmbZ0VPDEpr/xYBSBhjxDCye4L9/MwABu6S3NNV8x+Kn/Q=";
    final String qrRaw_GoodComponents_BadSharedKey = "1|" + guid + "|BOGUS_GbFKLZQ+ZxaAyDwdUcMOAtzolqUYMdkjOYautXPNt41AXqjk67P9aDqRPMM4mmbZ0VPDEpr/xYBSBhjxDCye4L9/MwABu6S3NNV8x+Kn/Q=";
    final String qrRaw_badComponents = "1524b5e9f-72ea-4690-b28c-8c1cfce65ca0MZfQWMPJHjUkAqlEOrm97qIryrXygiXlPNQGh3jppS6GXJZf5mmD2kti0Mf/Bwqw7+OCWWqUf8r19EB+YmgRcWmGxsstWPE2ZR4oJrKpmpo=";

    @Test
    public void getQRComponents_whenBadComponentString_shouldFail() {

        try {
            pairing.getQRComponentsFromRawString(qrRaw_badComponents);
        } catch (Exception e) {
            assertThat("Should fail", true);
        }
    }

    @Test
    public void getQRComponents_whenGoodComponentString_shouldPass() throws Exception {

        assertThat("Should pass", pairing.getQRComponentsFromRawString(qrRaw_Good) != null);
    }

    @Test
    public void getSharedKeyAndPassword_whenBadString_shouldFail() {

        String encryptionPassword = null;
        try {
            encryptionPassword = new WalletPayload().getPairingEncryptionPassword(guid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PairingQRComponents components = null;
        try {
            components = pairing.getQRComponentsFromRawString(qrRaw_GoodComponents_BadSharedKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pairing.getSharedKeyAndPassword(components.encryptedPairingCode, encryptionPassword);
            assertThat("Decryption should have failed", false);
        } catch (Exception e) {
            assertThat("Should fail", true);
        }
    }

//    @Test
//    public void getSharedKeyAndPassword_whenGoodString_shouldPass() throws Exception {
//
//        String encryptionPassword = new Wallet().getPairingEncryptionPassword(guid);
//
//        PairingQRComponents components = pairing.getQRComponentsFromRawString(qrRaw_Good);
//        String[] sharedKeyAndPassword = pairing.getSharedKeyAndPassword(components.encryptedPairingCode, encryptionPassword);
//
//        assertThat("Should pass", sharedKeyAndPassword != null);
//        assertThat("Should pass", sharedKeyAndPassword[1] != null && sharedKeyAndPassword[1].isEmpty());
//        assertThat("Should pass", sharedKeyAndPassword[2] != null && sharedKeyAndPassword[2].isEmpty());
//    }
}
