package info.blockchain.wallet.payload;

import info.blockchain.util.AssertJson;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HDWalletTest {

    @Test
    public void testSetSeedHex() throws Exception {
        HDWallet hdWallet = HDWallet.fromJson(new JSONObject(testString));
        hdWallet.setSeedHex("test hex");

        Assert.assertEquals(hdWallet.getSeedHex(), "test hex");
    }

    @Test
    public void testSetAccounts() throws Exception {
        HDWallet hdWallet = HDWallet.fromJson(new JSONObject(testString));

        List<Account> accounts = new ArrayList<Account>();
        accounts.add(new Account());
        accounts.add(new Account());
        hdWallet.setAccounts(accounts);

        Assert.assertEquals(hdWallet.getAccounts().size(), 2);
    }

    @Test
    public void testSetPassphrase() throws Exception {
        HDWallet hdWallet = HDWallet.fromJson(new JSONObject(testString));
        hdWallet.setPassphrase("test");

        Assert.assertEquals(hdWallet.getPassphrase(), "test");
    }

    @Test
    public void testMnemonic_verified() throws Exception {
        HDWallet hdWallet = HDWallet.fromJson(new JSONObject(testString));
        hdWallet.setMnemonicVerified(true);
        Assert.assertEquals(hdWallet.isMnemonicVerified(), true);
        hdWallet.setMnemonicVerified(false);
        Assert.assertEquals(hdWallet.isMnemonicVerified(), false);
    }

    @Test
    public void testSetDefaultIndex() throws Exception {
        HDWallet hdWallet = HDWallet.fromJson(new JSONObject(testString));
        hdWallet.setDefaultIndex(6);

        Assert.assertEquals(hdWallet.getDefaultIndex(), 6);
    }

    @Test
    public void testToJson() throws Exception {
        HDWallet hdWallet = HDWallet.fromJson(new JSONObject(testString));
        AssertJson.assertEqual(hdWallet.toJson().toString(), testString);

    }

    private String testString = "{\n" +
            "      \"seed_hex\": \"751d8471827550fd53f2\",\n" +
            "      \"passphrase\": \"\",\n" +
            "      \"mnemonic_verified\": true,\n" +
            "      \"default_account_idx\": 0,\n" +
            "      \"accounts\": []\n" +
            "    }";
}