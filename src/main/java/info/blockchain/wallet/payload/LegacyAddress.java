package info.blockchain.wallet.payload;

import info.blockchain.api.PersistentUrls;
import info.blockchain.wallet.exceptions.PayloadException;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.DoubleEncryptionFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.json.JSONObject;

import java.math.BigInteger;

public class LegacyAddress {

    public static final long NORMAL_ADDRESS = 0L;
    public static final long ARCHIVED_ADDRESS = 2L;

    private static final String KEY_ADDR = "addr";
    private static final String KEY_PRIV = "priv";
    private static final String KEY_LABEL = "label";
    private static final String KEY_CREATED_TIME = "created_time";
    private static final String KEY_TAG = "tag";
    private static final String KEY_CREATED_DEVICE_NAME = "created_device_name";
    private static final String KEY_CREATED_DEVICE_VERSION = "created_device_version";

    private String strEncryptedKey = null;
    private long created = 0L;
    private String strAddress = null;
    private String strLabel = null;
    private String strCreatedDeviceName = null;
    private String strCreatedDeviceVersion = null;
    private long tag = 0L;
    private boolean watchOnly = false;

    public LegacyAddress() {
    }

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

    public static LegacyAddress fromJson(JSONObject legacyJsonObject) throws PayloadException {
        LegacyAddress legacyAddress = new LegacyAddress();

        if(legacyJsonObject.has(KEY_ADDR) && legacyJsonObject.get(KEY_ADDR).equals(JSONObject.NULL)){
            throw new PayloadException("Address is null");
        }

        legacyAddress.strAddress = legacyJsonObject.getString(KEY_ADDR);

        if (legacyAddress.strAddress == null || legacyAddress.strAddress.equals("null") || legacyAddress.strAddress.equals("")){
            throw new PayloadException("Address is null");
        }

        try {
            if (legacyJsonObject.has(KEY_PRIV)) {
                legacyAddress.strEncryptedKey = legacyJsonObject.getString(KEY_PRIV);
            }
            if (legacyAddress.strEncryptedKey != null && legacyAddress.strEncryptedKey.equals("null")) {
                legacyAddress.strEncryptedKey = null;
            }
        } catch (Exception e) {
            legacyAddress.strEncryptedKey = null;
        }

        if (legacyAddress.strEncryptedKey == null || legacyAddress.strEncryptedKey.length() == 0) {
            legacyAddress.watchOnly = true;
        }

        if (legacyJsonObject.has(KEY_CREATED_TIME)) {
            try {
                legacyAddress.created = legacyJsonObject.getLong(KEY_CREATED_TIME);
            } catch (Exception e) {
                legacyAddress.created = 0L;
            }
        } else {
            legacyAddress.created = 0L;
        }

        try {
            if (legacyJsonObject.has(KEY_LABEL)) {
                legacyAddress.strLabel = legacyJsonObject.getString(KEY_LABEL);
            }
            if (legacyAddress.strLabel != null && legacyAddress.strLabel.equals("null")) {
                legacyAddress.strLabel = null;
            }
        } catch (Exception e) {
            legacyAddress.strLabel = null;
        }

        if (legacyJsonObject.has(KEY_TAG)) {
            try {
                legacyAddress.tag = legacyJsonObject.getLong(KEY_TAG);
            } catch (Exception e) {
                legacyAddress.tag = 0L;
            }
        } else {
            legacyAddress.tag = 0L;
        }

        try {
            if (legacyJsonObject.has(KEY_CREATED_DEVICE_NAME)) {
                legacyAddress.strCreatedDeviceName = legacyJsonObject.getString(KEY_CREATED_DEVICE_NAME);
            }
            if (legacyAddress.strCreatedDeviceName != null && legacyAddress.strCreatedDeviceName.equals("null")) {
                legacyAddress.strCreatedDeviceName = null;
            }
        } catch (Exception e) {
            legacyAddress.strCreatedDeviceName = null;
        }

        try {
            if (legacyJsonObject.has(KEY_CREATED_DEVICE_VERSION)) {
                legacyAddress.strCreatedDeviceVersion = legacyJsonObject.getString(KEY_CREATED_DEVICE_VERSION);
            }
            if (legacyAddress.strCreatedDeviceVersion != null && legacyAddress.strCreatedDeviceVersion.equals("null")) {
                legacyAddress.strCreatedDeviceVersion = null;
            }
        } catch (Exception e) {
            legacyAddress.strCreatedDeviceVersion = null;
        }

        return legacyAddress;
    }

    public String getEncryptedKey() {
        return strEncryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        strEncryptedKey = encryptedKey;
    }

    public void setEncryptedKeyBytes(byte[] privKeyBytes) {
        strEncryptedKey = Base58.encode(privKeyBytes);
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

    public String getCreatedDeviceName() {
        return strCreatedDeviceName;
    }

    public void setCreatedDeviceName(String device_name) {
        this.strCreatedDeviceName = device_name;
    }

    public String getCreatedDeviceVersion() {
        return strCreatedDeviceVersion;
    }

    public void setCreatedDeviceVersion(String device_version) {
        this.strCreatedDeviceVersion = device_version;
    }

    public String getPrivateKey(String secondPassword) throws AddressFormatException {

        ECKey ecKey = getECKey(secondPassword);

        if (ecKey != null) {
            return ecKey.getPrivateKeyEncoded(PersistentUrls.getInstance().getCurrentNetworkParams()).toString();
        } else {
            return null;
        }
    }

    public ECKey getECKey(CharSequenceX secondPassword) throws Exception {

        String encryptedKey = DoubleEncryptionFactory.getInstance().decrypt(strEncryptedKey,
                PayloadManager.getInstance().getPayload().getSharedKey(),
                secondPassword.toString(),
                PayloadManager.getInstance().getPayload().getDoubleEncryptionPbkdf2Iterations());

        return getECKey(encryptedKey);
    }

    public ECKey getECKey() throws AddressFormatException {
        return getECKey(strEncryptedKey);
    }

    private ECKey getECKey(String strEncryptedKey) throws AddressFormatException {

        if (strEncryptedKey == null || strEncryptedKey.isEmpty())
            return null;

        byte[] privBytes = Base58.decode(strEncryptedKey);
        ECKey ecKey;

        ECKey keyCompressed;
        ECKey keyUnCompressed;
        BigInteger priv = new BigInteger(privBytes);
        if (priv.compareTo(BigInteger.ZERO) >= 0) {
            keyCompressed = ECKey.fromPrivate(priv, true);
            keyUnCompressed = ECKey.fromPrivate(priv, false);
        } else {
            byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
            BigInteger priv2 = new BigInteger(appendZeroByte);
            keyCompressed = ECKey.fromPrivate(priv2, true);
            keyUnCompressed = ECKey.fromPrivate(priv2, false);
        }

        if (keyCompressed.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString().equals(this.strAddress)) {
            ecKey = keyCompressed;
        } else if (keyUnCompressed.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString().equals(this.strAddress)) {
            ecKey = keyUnCompressed;
        } else {
            ecKey = null;
        }

        return ecKey;
    }

    public JSONObject toJson() throws Exception {

        JSONObject obj = new JSONObject();

        if (strAddress == null || "".equals(strAddress)) {
            throw new Exception("Address null or empty");
        } else {
            obj.put(KEY_ADDR, strAddress);
        }

        if (strEncryptedKey != null && !"".equals(strEncryptedKey)) {
            obj.put(KEY_PRIV, strEncryptedKey);
        } else {
            obj.put(KEY_PRIV, JSONObject.NULL);
        }

        obj.put(KEY_TAG, tag);

        if (strLabel != null && !"".equals(strLabel)) {
            obj.put(KEY_LABEL, strLabel);
        } else {
            obj.put(KEY_LABEL, JSONObject.NULL);
        }

        if (created >= 0L) {
            obj.put(KEY_CREATED_TIME, created);
        } else {
            obj.put(KEY_CREATED_TIME, 0L);
        }

        if (strCreatedDeviceName != null && !"".equals(strCreatedDeviceName)) {
            obj.put(KEY_CREATED_DEVICE_NAME, strCreatedDeviceName);
        } else {
            obj.put(KEY_CREATED_DEVICE_NAME, JSONObject.NULL);
        }

        if (strCreatedDeviceVersion != null && !"".equals(strCreatedDeviceVersion)) {
            obj.put(KEY_CREATED_DEVICE_VERSION, strCreatedDeviceVersion);
        } else {
            obj.put(KEY_CREATED_DEVICE_VERSION, JSONObject.NULL);
        }

        return obj;
    }

}