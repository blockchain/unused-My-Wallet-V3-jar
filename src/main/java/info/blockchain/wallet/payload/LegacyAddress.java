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

    // TODO: 20/10/16 Simplify these keys again after refactor
    final String KEY_LEGACY_KEYS__ADDR = "addr";
    final String KEY_LEGACY_KEYS__PRIV = "priv";
    final String KEY_LEGACY_KEYS__LABEL = "label";
    final String KEY_LEGACY_KEYS__CREATED_TIME = "created_time";
    final String KEY_LEGACY_KEYS__TAG = "tag";
    final String KEY_LEGACY_KEYS__CREATED_DEVICE_NAME = "created_device_name";
    final String KEY_LEGACY_KEYS__CREATED_DEVICE_VERSION = "created_device_version";
    final String KEY_LEGACY_KEYS__CHANGE_ADDRESS = "change_addresses";//?
    final String KEY_LEGACY_KEYS__RECEIVE_ADDRESS = "receive_addresses";
    final String KEY_LEGACY_KEYS__TAGS = "tags";//?
    final String KEY_LEGACY_KEYS__AMOUNT = "amount";

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

    public LegacyAddress(JSONObject key) {

        parseJson(key);
    }

    private void parseJson(JSONObject key) {

        String strAddress = key.getString(KEY_LEGACY_KEYS__ADDR);

        if (strAddress != null && !strAddress.equals("null")) {

            try {
                if (key.has(KEY_LEGACY_KEYS__PRIV)) {
                    strEncryptedKey = key.getString(KEY_LEGACY_KEYS__PRIV);
                }
                if (strEncryptedKey == null || strEncryptedKey.equals("null")) {
                    strEncryptedKey = "";// TODO: 20/10/16 Don't treat this as empty string
                }
            } catch (Exception e) {
                strEncryptedKey = "";
            }

            if (strEncryptedKey.length() == 0) {
                watchOnly = true;
            }

            if (key.has(KEY_LEGACY_KEYS__CREATED_TIME)) {
                try {
                    created = key.getLong(KEY_LEGACY_KEYS__CREATED_TIME);
                } catch (Exception e) {
                    created = 0L;
                }
            } else {
                created = 0L;
            }

            try {
                if (key.has(KEY_LEGACY_KEYS__LABEL)) {
                    strLabel = key.getString(KEY_LEGACY_KEYS__LABEL);
                }
                if (strLabel == null || strLabel.equals("null")) {
                    strLabel = "";// TODO: 20/10/16 Don't treat this as empty string
                }
            } catch (Exception e) {
                strLabel = "";
            }

            if (key.has(KEY_LEGACY_KEYS__TAG)) {
                try {
                    tag = key.getLong(KEY_LEGACY_KEYS__TAG);
                } catch (Exception e) {
                    tag = 0L;
                }
            } else {
                tag = 0L;
            }

            try {
                if (key.has(KEY_LEGACY_KEYS__CREATED_DEVICE_NAME)) {
                    strCreatedDeviceName = key.getString(KEY_LEGACY_KEYS__CREATED_DEVICE_NAME);
                }
                if (strCreatedDeviceName == null || strCreatedDeviceName.equals("null")) {
                    strCreatedDeviceName = "";// TODO: 20/10/16 Don't treat this as empty string
                }
            } catch (Exception e) {
                strCreatedDeviceName = "";
            }

            try {
                if (key.has(KEY_LEGACY_KEYS__CREATED_DEVICE_VERSION)) {
                    strCreatedDeviceVersion = key.getString(KEY_LEGACY_KEYS__CREATED_DEVICE_VERSION);
                }
                if (strCreatedDeviceVersion == null || strCreatedDeviceVersion.equals("null")) {
                    strCreatedDeviceVersion = "";// TODO: 20/10/16 Don't treat this as empty string
                }
            } catch (Exception e) {
                strCreatedDeviceVersion = "";
            }
        }
    }

    public String getEncryptedKey() {
        return strEncryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        strEncryptedKey = encryptedKey;
    }

    public void setEncryptedKey(byte[] privKeyBytes) {
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
            return ecKey.getPrivateKeyEncoded(MainNetParams.get()).toString();
        } else {
            return null;
        }
    }

    public ECKey getECKey(CharSequenceX secondPassword) throws Exception {

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

        if (keyCompressed.toAddress(MainNetParams.get()).toString().equals(this.strAddress)) {
            ecKey = keyCompressed;
        } else if (keyUnCompressed.toAddress(MainNetParams.get()).toString().equals(this.strAddress)) {
            ecKey = keyUnCompressed;
        } else {
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

        obj.put(KEY_LEGACY_KEYS__ADDR, strAddress);

        if (!"".equals(strEncryptedKey)) {
            obj.put(KEY_LEGACY_KEYS__PRIV, strEncryptedKey);
        } else {
            obj.put(KEY_LEGACY_KEYS__PRIV, JSONObject.NULL);
        }

        obj.put(KEY_LEGACY_KEYS__TAG, tag);

        if (strLabel != null && !"".equals(strLabel)) {
            obj.put(KEY_LEGACY_KEYS__LABEL, strLabel);
        }

        if (created >= 0L) {
            obj.put(KEY_LEGACY_KEYS__CREATED_TIME, created);
        } else {
            obj.put(KEY_LEGACY_KEYS__CREATED_TIME, 0L);
        }

        if (!"".equals(strCreatedDeviceName)) {
            obj.put(KEY_LEGACY_KEYS__CREATED_DEVICE_NAME, strCreatedDeviceName);
        } else {
            obj.put(KEY_LEGACY_KEYS__CREATED_DEVICE_NAME, JSONObject.NULL);
        }

        if (!"".equals(strCreatedDeviceVersion)) {
            obj.put(KEY_LEGACY_KEYS__CREATED_DEVICE_VERSION, strCreatedDeviceVersion);
        } else {
            obj.put(KEY_LEGACY_KEYS__CREATED_DEVICE_VERSION, JSONObject.NULL);
        }

        return obj;
    }

}