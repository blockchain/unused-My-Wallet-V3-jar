package info.blockchain.wallet.payload;

import info.blockchain.util.AssertJson;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LegacyAddressTest {

    private String testString = "{\n" +
            "      \"addr\": \"someaddress\",\n" +
            "      \"priv\": \"someprivatekey\",\n" +
            "      \"tag\": 0,\n" +
            "      \"label\": \"Label 1\",\n" +
            "      \"created_time\": 1469624014,\n" +
            "      \"created_device_name\": \"android\",\n" +
            "      \"created_device_version\": 6.1.31\n" +
            "    }";

    private String testStringNull = "{\n" +
            "      \"addr\": \"someaddress\",\n" +
            "      \"priv\": null,\n" +
            "      \"tag\": 0,\n" +
            "      \"label\": null,\n" +
            "      \"created_time\": 0,\n" +
            "      \"created_device_name\": null,\n" +
            "      \"created_device_version\": null\n" +
            "    }";

    @Test
    public void generateLegacyAddress_address_shouldMatchECKeyAddress() throws Exception {

        String device = "device";
        String version = "version";

        LegacyAddress legacyAddress = PayloadManager.getInstance().generateLegacyAddress(device, version, null);

        assertThat(legacyAddress.getCreatedDeviceName(), is(device));
        assertThat(legacyAddress.getCreatedDeviceVersion(), is(version));

        ECKey ecKey = legacyAddress.getECKey();
        String address = ecKey.toAddress(MainNetParams.get()).toString();
        assertThat(address, is(legacyAddress.getAddress()));
    }

    @Test
    public void testSetEncryptedKey() throws Exception {

        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));
        legacyAddress.setEncryptedKey("test");

        Assert.assertEquals(legacyAddress.getEncryptedKey(), "test");
    }

    @Test
    public void testSetEncryptedKey1() throws Exception {

        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));
        legacyAddress.setEncryptedKeyBytes(Base58.decode("test"));

        Assert.assertEquals(legacyAddress.getEncryptedKey(), "test");
    }

    @Test
    public void testSetCreated() throws Exception {

        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));

        long now = System.currentTimeMillis();
        legacyAddress.setCreated(now);

        Assert.assertEquals(legacyAddress.getCreated(), now);
    }

    @Test
    public void testSetAddress() throws Exception {

        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));
        legacyAddress.setAddress("test");

        Assert.assertEquals(legacyAddress.getAddress(), "test");
    }

    @Test
    public void testSetLabel() throws Exception {
        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));
        legacyAddress.setLabel("test");

        Assert.assertEquals(legacyAddress.getLabel(), "test");
    }

    @Test
    public void testSetTag() throws Exception {
        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));
        legacyAddress.setTag(321);

        Assert.assertEquals(legacyAddress.getTag(), 321);
    }

    @Test
    public void testSetWatchOnly() throws Exception {
        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));
        legacyAddress.setWatchOnly(true);
        Assert.assertEquals(legacyAddress.isWatchOnly(), true);
        legacyAddress.setWatchOnly(false);
        Assert.assertEquals(legacyAddress.isWatchOnly(), false);
    }

    @Test
    public void testSetCreatedDeviceName() throws Exception {
        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));
        legacyAddress.setCreatedDeviceName("test");

        Assert.assertEquals(legacyAddress.getCreatedDeviceName(), "test");
    }

    @Test
    public void testSetCreatedDeviceVersion() throws Exception {
        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));
        legacyAddress.setCreatedDeviceVersion("test");

        Assert.assertEquals(legacyAddress.getCreatedDeviceVersion(), "test");
    }

    @Test
    public void testToJson() throws Exception {
        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString));
        AssertJson.assertEqual(testString, legacyAddress.toJson().toString());
    }

    @Test
    public void testToJsonNull() throws Exception {
        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testStringNull));
        AssertJson.assertEqual(testStringNull, legacyAddress.toJson().toString());
    }

    @Test
    public void testToJson_shouldPass_with_only_address() throws Exception {

        String testString2 = "{\n" +
                "      \"addr\": \"someaddress\"\n" +
                "    }";

        LegacyAddress legacyAddress = LegacyAddress.fromJson(new JSONObject(testString2));
        AssertJson.assertEqual(testString2, legacyAddress.toJson().toString());
    }

    @Test
    public void testToJson_shouldFail_if_null_address()  {

        String testString2 = "{\n" +
                "      \"addr\": null\n" +
                "    }";

        try {
            LegacyAddress.fromJson(new JSONObject(testString2));
            Assert.assertTrue("Should not parse legacy address with null or empty address.", false);
        }catch (Exception e){
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testToJson_shouldFail_if_empty_address()  {

        String testString2 = "{\n" +
                "      \"addr\": \"\",\n" +
                "    }";

        try {
            LegacyAddress.fromJson(new JSONObject(testString2));
            Assert.assertTrue("Should not parse legacy address with null or empty address.", false);
        }catch (Exception e){
            Assert.assertTrue(true);
        }
    }
}
