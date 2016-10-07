package info.blockchain.wallet.payload;

import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONObject;

import java.math.BigInteger;

public class LegacyAddress {

    public static final long NORMAL_ADDRESS = 0L;
    public static final long ARCHIVED_ADDRESS = 2L;

    private String strEncryptedKey = null;
    private long created = 0L;
    private String strAddress = null;
    private String strLabel = null;
    private String strCreatedDeviceName = null;
    private String strCreatedDeviceVersion = null;
    private long tag = 0L;
    private boolean watchOnly = false;

    public LegacyAddress() { ; }

    public LegacyAddress(String encryptedKey, long created, String address, String label, long tag, String device_name, String device_version) {
        this.strEncryptedKey = encryptedKey;
        this.created = created;
        this.strAddress = address;
        this.strLabel = label;
        this.tag = tag;
        this.strCreatedDeviceName = device_name;
        this.strCreatedDeviceVersion = device_version;
    }

    public LegacyAddress(String encryptedKey, long created, String address, String label, long tag, String device_name, String device_version, boolean watchOnly) {
        this.strEncryptedKey = encryptedKey;
        this.created = created;
        this.strAddress = address;
        this.strLabel = label;
        this.tag = tag;
        this.strCreatedDeviceName = device_name;
        this.strCreatedDeviceVersion = device_version;
        this.watchOnly = watchOnly;
    }

    public LegacyAddress(String encryptedKey, String address) {
        this.strEncryptedKey = encryptedKey;
        this.created = 0L;
        this.strAddress = address;
        this.strLabel = "";
        this.tag = 0L;
    }

    public String getEncryptedKey() {
        return strEncryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        strEncryptedKey = encryptedKey;
    }

    public void setEncryptedKey(byte[] privKeyBytes) {
    	strEncryptedKey = new String(Base58.encode(privKeyBytes));
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getAddress() {
        return strAddress;
    }

    public void setAddress(String address) {
        strAddress = address;
    }

    public String getLabel() {
        return strLabel;
    }

    public void setLabel(String label) {
        strLabel = label;
    }

    public long getTag() {
        return tag;
    }

    public void setTag(long tag) {
        this.tag = tag;
    }

    public boolean isWatchOnly() {
        return watchOnly;
    }

    public void setWatchOnly(boolean watchOnly) {
        this.watchOnly = watchOnly;
    }

    public String getCreatedDeviceName() { return strCreatedDeviceName; }

    public void setCreatedDeviceName(String device_name) { this.strCreatedDeviceName = device_name; }

    public String getCreatedDeviceVersion() { return strCreatedDeviceVersion; }

    public void setCreatedDeviceVersion(String device_version) { this.strCreatedDeviceVersion = device_version; }

    public String getPrivateKey(String secondPassword) throws AddressFormatException {

        ECKey ecKey = getECKey(secondPassword);

        if(ecKey != null) {
            return ecKey.getPrivateKeyEncoded(MainNetParams.get()).toString();
        }
        else {
            return null;
        }
    }

    public ECKey getECKey(CharSequenceX secondPassword)  throws AddressFormatException{

        /*
        Log.i("LegacyAddress double encryptedPairingCode", strEncryptedKey);
        Log.i("LegacyAddress double encryptedPairingCode", PayloadManager.getInstance().get().getSharedKey());
        Log.i("LegacyAddress double encryptedPairingCode", PayloadManager.getInstance().getTempDoubleEncryptPassword().toString());
        Log.i("LegacyAddress double encryptedPairingCode", "hash:" + DoubleEncryptionFactory.getInstance().validateSecondPassword(PayloadManager.getInstance().get().getDoublePasswordHash(), PayloadManager.getInstance().get().getSharedKey(), PayloadManager.getInstance().getTempDoubleEncryptPassword(), PayloadManager.getInstance().get().getIterations()));
        Log.i("LegacyAddress double encryptedPairingCode", PayloadManager.getInstance().get().getDoublePasswordHash());
        Log.i("LegacyAddress double encryptedPairingCode", "" + PayloadManager.getInstance().get().getIterations());
        */

        String encryptedKey = DoubleEncryptionFactory.getInstance().decrypt(strEncryptedKey,
                PayloadManager.getInstance().getPayload().getSharedKey(),
                secondPassword.toString(),
                PayloadManager.getInstance().getPayload().getDoubleEncryptionPbkdf2Iterations());
//    		Log.i("LegacyAddress double encryptedPairingCode", encryptedKey);

        return getECKey(encryptedKey);
    }

    public ECKey getECKey()  throws AddressFormatException{
        return getECKey(strEncryptedKey);
    }

    private ECKey getECKey(String strEncryptedKey) throws AddressFormatException {

        if(strEncryptedKey == null || strEncryptedKey.isEmpty())
            return null;

        byte[] privBytes = Base58.decode(strEncryptedKey);
    	ECKey ecKey = null;

    	ECKey keyCompressed = null;
		ECKey keyUnCompressed = null;
		BigInteger priv = new BigInteger(privBytes);
		if(priv.compareTo(BigInteger.ZERO) >= 0) {
			keyCompressed = new ECKey(priv, null, true);
			keyUnCompressed = new ECKey(priv, null, false);
		}
		else {
			byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
			BigInteger priv2 = new BigInteger(appendZeroByte);
			keyCompressed = new ECKey(priv2, null, true);
			keyUnCompressed = new ECKey(priv2, null, false);
		}

		if(keyCompressed != null && keyCompressed.toAddress(MainNetParams.get()).toString().equals(this.strAddress)) {
			ecKey = keyCompressed;
		}
		else if(keyUnCompressed != null && keyUnCompressed.toAddress(MainNetParams.get()).toString().equals(this.strAddress)) {
			ecKey = keyUnCompressed;
		}
		else {
			ecKey = null;
		}

		return ecKey;
    }

    public JSONObject dumpJSON() {

        JSONObject obj = new JSONObject();

        if (strAddress == null || "".equals(strAddress)) {
            // TODO should probably throw an error here and not sync
            return obj;
        }

        obj.put("addr", strAddress);

        if (!"".equals(strEncryptedKey)) {
            obj.put("priv", strEncryptedKey);
        }else{
            obj.put("priv", JSONObject.NULL);
        }

        obj.put("tag", tag);

        if (strLabel != null && !"".equals(strLabel)) {
            obj.put("label", strLabel);
        }

        if (created >= 0L) {
          obj.put("created_time", created);
        }else{
          obj.put("created_time", 0L);
        }

        if (!"".equals(strCreatedDeviceName)) {
            obj.put("created_device_name", strCreatedDeviceName);
        }else{
            obj.put("created_device_name", JSONObject.NULL);
        }

        if (!"".equals(strCreatedDeviceVersion)) {
            obj.put("created_device_version", strCreatedDeviceVersion);
        }else{
            obj.put("created_device_version", JSONObject.NULL);
        }

        return obj;
    }

}
