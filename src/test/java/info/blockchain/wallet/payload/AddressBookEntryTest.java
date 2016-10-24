package info.blockchain.wallet.payload;

import info.blockchain.util.AssertJson;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class AddressBookEntryTest {

    @Test
    public void testSetAddress() throws Exception {
        AddressBookEntry addressBookEntry = AddressBookEntry.fromJson(new JSONObject(testString));
        addressBookEntry.setAddress("Address 1");
        Assert.assertEquals(addressBookEntry.getAddress(), "Address 1");
    }

    @Test
    public void testSetLabel() throws Exception {
        AddressBookEntry addressBookEntry = AddressBookEntry.fromJson(new JSONObject(testString));
        addressBookEntry.setLabel("Label 1");
        Assert.assertEquals(addressBookEntry.getLabel(), "Label 1");
    }

    @Test
    public void testToJSON() throws Exception {
        AddressBookEntry addressBookEntry = AddressBookEntry.fromJson(new JSONObject(testString));
        AssertJson.assertEqual(addressBookEntry.toJson().toString(), testString);
    }

    String testString = "{\n" +
            "          \"label\": \"My Bitcoin Wallet\",\n" +
            "          \"addr\": \"1bkasfgkasfghkajsf\"\n" +
            "        }";
}