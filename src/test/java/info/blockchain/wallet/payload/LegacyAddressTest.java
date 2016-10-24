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
        legacyAddress.setEncryptedKey(Base58.decode("test"));

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

    private String testString = "{\n" +
            "      \"addr\": \"someaddress\",\n" +
            "      \"priv\": \"someprivatekey\",\n" +
            "      \"tag\": 0,\n" +
            "      \"label\": \"Label 1\",\n" +
            "      \"created_time\": 1469624014,\n" +
            "      \"created_device_name\": \"android\",\n" +
            "      \"created_device_version\": 6.1.31\n" +
            "    }";
}
