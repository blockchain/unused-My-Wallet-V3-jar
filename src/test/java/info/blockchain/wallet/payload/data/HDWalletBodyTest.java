package info.blockchain.wallet.payload.data;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class HDWalletBodyTest {

    @Test
    public void fromJson_1() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);
        HDWalletBody hdWallet = wallet.getHdWallet();

        Assert.assertEquals(68, hdWallet.getAccounts().size());
        Assert.assertEquals("i3gtswW35zfbS/23fnh3IzKzcrpD04Tp+zeKbj++rODMOGRMO1aMQukwE3Q+63ds8pUMzBFnzomkjntprhisrQ==", hdWallet.getSeedHex());
        Assert.assertEquals("", hdWallet.getPassphrase());
        Assert.assertTrue(hdWallet.isMnemonicVerified());
        Assert.assertEquals(0, hdWallet.getDefaultAccountIdx());
    }

    @Test
    public void fromJson_2() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_2.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);
        Assert.assertNull(wallet.getHdWallets());
    }

    @Test
    public void fromJson_6() throws Exception {
        URI uri = getClass().getClassLoader().getResource("wallet_body_6.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);
        HDWalletBody hdWallet = wallet.getHdWallet();

        Assert.assertEquals(1, hdWallet.getAccounts().size());
        Assert.assertEquals("bfb70136ef9f973e866dff00817b8070", hdWallet.getSeedHex());
        Assert.assertEquals("somePassPhrase", hdWallet.getPassphrase());
        Assert.assertFalse(hdWallet.isMnemonicVerified());
        Assert.assertEquals(2, hdWallet.getDefaultAccountIdx());
    }

    @Test
    public void testToJSON() throws Exception {

        //Ensure toJson doesn't write any unintended fields
        URI uri = getClass().getClassLoader().getResource("wallet_body_1.txt").toURI();
        String body = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        WalletBody wallet = WalletBody.fromJson(body);
        HDWalletBody hdWallet = wallet.getHdWallet();
        String jsonString = hdWallet.toJson();

        JSONObject jsonObject = new JSONObject(jsonString);
        Assert.assertEquals(5, jsonObject.keySet().size());
    }
}