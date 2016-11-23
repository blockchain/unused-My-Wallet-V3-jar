package info.blockchain.wallet.metadata;

import info.blockchain.wallet.payload.PayloadManager;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import io.jsonwebtoken.lang.Assert;

/**
 * Integration Test
 */
public class MetadataTest {

    private DeterministicKey getRandomECKey() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();
        payloadManager.createHDWallet("", "Account 1");
        return payloadManager.getMasterKey();
    }

    @Test
    public void testMetadata() throws Exception{

//        String web_mnemonic = "bicycle balcony prefer kid flower pole goose crouch century lady worry flavor";
//        PayloadManager payloadManager = PayloadManager.getInstance();
//        payloadManager.restoreHDWallet("", web_mnemonic, "Account 1");
//        DeterministicKey key = payloadManager.getMasterKey();

        DeterministicKey key = getRandomECKey();

        String message = "{hello: 'world'}";

        Metadata metadata5 = new Metadata(key, Metadata.PAYLOAD_TYPE_RESERVED);
        metadata5.putMetadata(message);

        String result1 = metadata5.getMetadata();
        Assert.isTrue(message.equals(result1));

        message = "{hello: 'mars'}";
        metadata5.putMetadata(message);

        String result2 = metadata5.getMetadata();
        Assert.isTrue(message.equals(result2));

        metadata5.deleteMetadata(message);

        try {
            metadata5.getMetadata();
            Assert.isTrue(false);
        }catch (Exception e){
            Assert.isTrue(true);
        }
    }

    @Test
    public void testGetMetadataNode() throws Exception {

        String web_mnemonic = "bicycle balcony prefer kid flower pole goose crouch century lady worry flavor";
        String web_seedHex = "15e23aa73d25994f1921a1256f93f72c";
        String web_address = "12sC9tqHzAhdoukhCbTnyx2MjYXNXBGHnF";

        PayloadManager payloadManager = PayloadManager.getInstance();
        payloadManager.restoreHDWallet("", web_mnemonic, "Account 1");

        //Ensure web_wallet and this restore wallet is same
        Assert.isTrue(web_seedHex.equals(payloadManager.getHDSeedHex()));
        String web_priv = "xprv9s21ZrQH143K2qnxcoP1RnRkxYvHT5ZDamV4B4UYTmAuANBnyWwVP7e3GYmEkt1chPWq264tiUxo21FiRKx3kVTpHLkkP65NRzHSAjS8nHA";
        Assert.isTrue(web_priv.equals(payloadManager.getMasterKey().serializePrivB58(MainNetParams.get())));

        Metadata metadata = new Metadata(payloadManager.getMasterKey(), 2);
        Assert.isTrue(metadata.getAddress().equals(web_address));
        payloadManager.wipe();
    }

    @Test
    public void testFetchExistingMagicHash() throws Exception {

        DeterministicKey key = getRandomECKey();

        Metadata metadata = new Metadata(key, 2);
        metadata.putMetadata("Yolo");

        metadata = new Metadata(key, 2);
        metadata.putMetadata("Yolo2");

        metadata = new Metadata(key, 2);
        metadata.putMetadata("Yolo3");
    }
}