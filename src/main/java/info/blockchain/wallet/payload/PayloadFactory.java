package info.blockchain.wallet.payload;

import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.crypto.MnemonicException;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bitcoinj.core.bip44.Account;
import org.bitcoinj.core.bip44.Wallet;
import org.bitcoinj.core.bip44.WalletFactory;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.WebUtil;

/**
 *
 * PayloadFactory.java : singleton class for reading/writing/parsing Blockchain HD JSON payload
 *
 */
public class PayloadFactory	{

    public static final double SUPPORTED_ENCRYPTION_VERSION = 3.0;
    public static final long NORMAL_ADDRESS = 0L;
    public static final long ARCHIVED_ADDRESS = 2L;

    private static PayloadFactory instance = null;
    // active payload:
    private static Payload payload = null;
    // cached payload, compare to this payload to determine if changes have been made. Used to avoid needless remote saves to server
    private static String cached_payload = null;

    private static final int WalletDefaultPbkdf2Iterations = 5000;
    public static int WalletPbkdf2Iterations = WalletDefaultPbkdf2Iterations;

    private static CharSequenceX strTempPassword =  null;
    private static CharSequenceX strTempDoubleEncryptPassword =  null;
    private static String strCheckSum = null;
    private static boolean isNew = false;
    private static boolean syncPubKeys = true;
    private static String email = null;

    private static double version = 2.0;

    private PayloadFactory()	{ ; }

    /**
     * Return instance for a payload factory.
     *
     * @return PayloadFactory
     *
     */
    public static PayloadFactory getInstance() {

        if (instance == null) {
            instance = new PayloadFactory();
            payload = new Payload();
            cached_payload = "";
        }

        return instance;
    }

    /**
     * Return instance for a payload factory. Payload initialized using provided JSON string.
     *
     * @param  String json JSON string used to initialize this instance
     *
     * @return HD_WalletFactory
     *
     */
    public static PayloadFactory getInstance(String json) {

        if (instance == null) {
            instance = new PayloadFactory();
            payload = new Payload(json);
            try {
                cached_payload = payload.dumpJSON().toString();
            }
            catch(JSONException je) {
                cached_payload = "";
            }
        }

        return instance;
    }

    /**
     * Reset PayloadFactory to null instance.
     *
     */
    public void wipe() {
        instance = null;
    }

    /**
     * Get temporary password for user. Read password from here rather than reprompting user.
     *
     * @return CharSequenceX
     *
     */
    public CharSequenceX getTempPassword() {
        return strTempPassword;
    }

    /**
     * Set temporary password for user once it has been validated. Read password from here rather than reprompting user.
     *
     * @param CharSequenceX password Validated user password
     *
     */
    public void setTempPassword(CharSequenceX temp_password) {
        strTempPassword = temp_password;
    }

    /**
     * Get temporary double encrypt password for user. Read double encrypt password from here rather than reprompting user.
     *
     * @return CharSequenceX
     *
     */
    public CharSequenceX getTempDoubleEncryptPassword() {
        return strTempDoubleEncryptPassword;
    }

    /**
     * Set temporary double encrypt password for user once it has been validated. Read double encrypt password from here rather than reprompting user.
     *
     * @param CharSequenceX password Validated user double encrypt password
     *
     */
    public void setTempDoubleEncryptPassword(CharSequenceX temp_password2) {
        strTempDoubleEncryptPassword = temp_password2;
    }

    /**
     * Get checksum for this payload.
     *
     * @return String
     *
     */
    public String getCheckSum() {
        return strCheckSum;
    }

    /**
     * Set checksum for this payload.
     *
     * @param String checksum Checksum to be set for this payload
     *
     */
    public void setCheckSum(String checksum) {
        this.strCheckSum = checksum;
    }

    /**
     * Check if this payload is for a new Blockchain account.
     *
     * @return boolean
     *
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * Set if this payload is for a new Blockchain account.
     *
     * @param boolean isNew
     *
     */
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Remote get(). Get refreshed payload from server.
     *
     * @param  String guid User's wallet 'guid'
     * @param  String sharedKey User's sharedKey value
     * @param  CharSequenceX password User password
     *
     * @return Payload
     *
     */
    public Payload get(String guid, String sharedKey, CharSequenceX password) {

        String checksum = null;

        try {
            String response = WebUtil.getInstance().postURL(WebUtil.PAYLOAD_URL,"method=wallet.aes.json&guid=" + guid + "&sharedKey=" + sharedKey + "&format=json");
            JSONObject jsonObject = new JSONObject(response);

            if(jsonObject.has("payload_checksum")) {
                checksum = jsonObject.get("payload_checksum").toString();
            }

            if(jsonObject.has("payload")) {
                String encrypted_payload = null;
                JSONObject _jsonObject = null;
                try {
                    _jsonObject = new JSONObject((String)jsonObject.get("payload"));
                }
                catch(Exception e) {
                    _jsonObject = null;
                }
                if(_jsonObject != null && _jsonObject.has("payload")) {
                    if(_jsonObject.has("pbkdf2_iterations")) {
                        WalletPbkdf2Iterations = Integer.valueOf(_jsonObject.get("pbkdf2_iterations").toString());
                    }
                    if(_jsonObject.has("version")) {
                        version = Double.valueOf(_jsonObject.get("version").toString());
                    }
                    encrypted_payload = (String)_jsonObject.get("payload");
                }
                else {
                    if(jsonObject.has("pbkdf2_iterations")) {
                        WalletPbkdf2Iterations = Integer.valueOf(jsonObject.get("pbkdf2_iterations").toString());
                    }
                    if(jsonObject.has("version")) {
                        version = Double.valueOf(jsonObject.get("version").toString());
                    }
                    encrypted_payload = (String)jsonObject.get("payload");
                }

                String decrypted = null;
                try {
                    decrypted = AESUtil.decrypt(encrypted_payload, password, WalletPbkdf2Iterations);
                }
                catch(Exception e) {
                    payload.lastErrorMessage = e.getMessage();
                    e.printStackTrace();
                    return null;
                }
                if(decrypted == null) {
                    try {
                        // v1 wallet fixed PBKDF2 iterations at 10
                        decrypted = AESUtil.decrypt(encrypted_payload, password, 10);
                    }
                    catch(Exception e) {
                        payload.lastErrorMessage = e.getMessage();
                        e.printStackTrace();
                        return null;
                    }
                    if(decrypted == null) {
                        payload.lastErrorMessage = "Empty after decrypt";
                        return null;
                    }
                }
                payload = new Payload(decrypted);
                if(payload.getJSON() == null) {
                    payload.lastErrorMessage = "Can't parse JSON";
                    return null;
                }

                // Default to wallet pbkdf2 iterations in case the double encryption pbkdf2 iterations is not set in wallet.json > options
                payload.setDoubleEncryptionPbkdf2Iterations(WalletPbkdf2Iterations);

                try {
                    payload.parseJSON();
                }
                catch(JSONException je) {
                    payload.lastErrorMessage = je.getMessage();
                    je.printStackTrace();
                    return null;
                }
            }
            else {
//                Log.i("PayloadFactory", "jsonObject has no payload");
                return null;
            }
        }
        catch(JSONException e) {
            payload.lastErrorMessage = e.getMessage();
            e.printStackTrace();
            return null;
        }
        catch(Exception e) {
            payload.lastErrorMessage = e.getMessage();
            e.printStackTrace();
            return null;
        }

        if (StringUtils.isNotEmpty(checksum)) {
            strCheckSum = checksum;
        }
        return payload;
    }

    /**
     * Local get(). Returns current payload from the client.
     *
     * @return Payload
     *
     */
    public Payload get() {
        return payload;
    }

    /**
     * Local set(). Sets current payload on the client.
     *
     * @param p Payload to be assigned
     *
     */
    public void set(Payload p) {
        payload = p;
    }

    /**
     * Remote save of current client payload to server. Will not save if no change as compared to cached payload.
     *
     * @param CharSequenceX password User password
     *
     * @return boolean
     *
     */
    public boolean put() {

        String strOldCheckSum = strCheckSum;
        String payloadCleartext = null;

        StringBuilder args = new StringBuilder();
        try	{

            if(cached_payload != null && cached_payload.equals(payload.dumpJSON().toString())) {
                return true;
            }

            payloadCleartext = payload.dumpJSON().toString();
            String payloadEncrypted = AESUtil.encrypt(payloadCleartext, new CharSequenceX(strTempPassword), WalletPbkdf2Iterations);
            JSONObject rootObj = new JSONObject();
            rootObj.put("version", payload.isUpgraded() ? 3.0 : 2.0);
            rootObj.put("pbkdf2_iterations", WalletPbkdf2Iterations);
            rootObj.put("payload", payloadEncrypted);

            strCheckSum  = new String(Hex.encode(MessageDigest.getInstance("SHA-256").digest(rootObj.toString().getBytes("UTF-8"))));

            String method = isNew ? "insert" : "update";

            String urlEncodedPayload = URLEncoder.encode(rootObj.toString());

            args.append("guid=");
            args.append(URLEncoder.encode(payload.getGuid(), "utf-8"));
            args.append("&sharedKey=");
            args.append(URLEncoder.encode(payload.getSharedKey(), "utf-8"));
            args.append("&payload=");
            args.append(urlEncodedPayload);
            args.append("&method=");
            args.append(method);
            args.append("&length=");
            args.append(rootObj.toString().length());
            args.append("&checksum=");
            args.append(URLEncoder.encode(strCheckSum, "utf-8"));

        }
        catch(NoSuchAlgorithmException nsae)	{
            nsae.printStackTrace();
            return false;
        }
        catch(UnsupportedEncodingException uee)	{
            uee.printStackTrace();
            return false;
        }
        catch(JSONException je)	{
            je.printStackTrace();
            return false;
        }

        if (syncPubKeys) {
            args.append("&active=");

            String[] legacyAddrs = null;
            List<LegacyAddress> legacyAddresses = payload.getLegacyAddresses();
            List<String> addrs = new ArrayList<String>();
            for(LegacyAddress addr : legacyAddresses) {
                if(addr.getTag() == 0L) {
                    addrs.add(addr.getAddress());
                }
            }

            args.append(StringUtils.join(addrs.toArray(new String[addrs.size()]), "|"));
        }

        if (email != null && email.length() > 0) {
            try {
                args.append("&email=");
                args.append(URLEncoder.encode(email, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        args.append("&device=");
        args.append("android");

        if(strOldCheckSum != null && strOldCheckSum.length() > 0)	{
            args.append("&old_checksum=");
            args.append(strOldCheckSum);
        }

        try	{
            String response = WebUtil.getInstance().postURL(WebUtil.PAYLOAD_URL, args.toString());
            isNew = false;
            if(response.contains("Wallet successfully synced")){
                cache();
                return true;
            }
        }
        catch(Exception e)	{
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Write to current client payload to cache.
     *
     */
    public void cache() {
        try {
            cached_payload = payload.dumpJSON().toString();
        }
        catch(JSONException je) {
            je.printStackTrace();
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        PayloadFactory.email = email;
    }

    public double getVersion()  {
      return version;
    }
}
