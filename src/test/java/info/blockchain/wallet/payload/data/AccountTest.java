package info.blockchain.wallet.payload.data;

import info.blockchain.util.AssertJson;
import info.blockchain.wallet.payload.data.Account;
import java.util.TreeMap;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class AccountTest {

    @Test
    public void testToJSON() throws Exception {

        Account account = Account.fromJson(new JSONObject(testString));
        AssertJson.assertEqual(account.toJson().toString(), testString);
    }

    @Test
    public void testIncChange() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        int changeIndex = account.getIdxChangeAddresses();

        account.incChange();

        Assert.assertEquals(account.getIdxChangeAddresses(), changeIndex + 1);
    }

    @Test
    public void testIncReceive() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        int changeIndex = account.getIdxReceiveAddresses();

        account.incReceive();

        Assert.assertEquals(account.getIdxReceiveAddresses(), changeIndex + 1);
    }

    @Test
    public void testSetArchived() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        account.setArchived(false);
        Assert.assertFalse(account.isArchived);
        account.setArchived(true);
        Assert.assertTrue(account.isArchived);
    }

    @Test
    public void testSetIdxChangeAddresses() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        account.setIdxChangeAddresses(10);
        Assert.assertEquals( account.getIdxChangeAddresses(), 10);
    }

    @Test
    public void testSetIdxReceiveAddresses() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        account.setIdxReceiveAddresses(10);
        Assert.assertEquals(account.getIdxReceiveAddresses(), 10);
    }

    @Test
    public void testSetLabel() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        account.setLabel("Label 1");
        Assert.assertEquals(account.getLabel(), "Label 1");
    }

    @Test
    public void testGetAmount() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        account.setAmount(2000);
        Assert.assertEquals(account.getAmount(), 2000);
    }

    @Test
    public void testSetXpub() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        account.setXpub("xpub a");
        Assert.assertEquals(account.getXpub(), "xpub a");
    }

    @Test
    public void testSetXpriv() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        account.setXpriv("xpriv a");
        Assert.assertEquals(account.getXpriv(), "xpriv a");
    }

    @Test
    public void testSetAddressLabels() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        TreeMap<Integer, String> tree = new TreeMap<Integer, String>();
        tree.put(0, "label 1");
        account.setAddressLabels(tree);
        Assert.assertEquals(account.getAddressLabel(0), "label 1");
    }

    @Test
    public void testSetRealIdx() throws Exception {
        Account account = Account.fromJson(new JSONObject(testString));
        account.setRealIdx(65);
        Assert.assertEquals(account.getRealIdx(), 65);
    }

    String testString = "{\n" +
            "          \"label\": \"My Bitcoin HDWallet\",\n" +
            "          \"archived\": false,\n" +
            "          \"xpriv\": \"xpriv1\",\n" +
            "          \"xpub\": \"xpub1\",\n" +
            "          \"address_labels\": [],\n" +
            "          \"cache\": {\n" +
            "            \"receiveAccount\": \"xpubabc\",\n" +
            "            \"changeAccount\": \"xpunxyz\"\n" +
            "          }\n" +
            "        }";
}