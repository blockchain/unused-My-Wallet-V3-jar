package info.blockchain.wallet.payload.data;

import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Deprecated
public class Account {

    private static final String KEY_LABEL = "label";
    private static final String KEY_ARCHIVED = "archived";
    private static final String KEY_XPRIV = "xpriv";
    private static final String KEY_XPUB = "xpub";
    private static final String KEY_CACHE = "cache";
    private static final String KEY_ADDRESS_LABELS = "address_labels";
    private static final String KEY_ADDRESS_INDEX = "index";
    private static final String KEY_ADDRESS_LABEL = KEY_LABEL;

    public boolean isArchived = false;
    protected String strLabel = null;
    protected String strXpub = null;
    protected String strXpriv = null;
    protected TreeMap<Integer, String> addressLabels = null;
    protected Cache cache = null;

    //Not in json
    protected int realIdx = -1;
    protected long amount = 0L;
    protected int idxChangeAddresses = 0;
    protected int idxReceiveAddresses = 0;

    @Deprecated
    public Account() {
        strXpub = "";
        strXpriv = "";
        addressLabels = new TreeMap<Integer, String>();
        cache = new Cache();
    }

    public Account(boolean isArchived, int change, int receive, String label) {
        this.isArchived = isArchived;
        this.idxChangeAddresses = change;
        this.idxReceiveAddresses = receive;
        this.strLabel = label;
        strXpub = "";
        strXpriv = "";
        addressLabels = new TreeMap<Integer, String>();
        cache = new Cache();
    }

    public Account(String label) {
        this.isArchived = false;
        this.idxChangeAddresses = 0;
        this.idxReceiveAddresses = 0;
        this.strLabel = label;
        strXpub = "";
        strXpriv = "";
        addressLabels = new TreeMap<Integer, String>();
        cache = new Cache();
    }

    @Deprecated
    public static Account fromJson(JSONObject accountJsonObj) throws Exception{

        Account account = new Account();

        if (accountJsonObj.has(KEY_ARCHIVED) && accountJsonObj.getBoolean(KEY_ARCHIVED)) {
            account.setArchived(true);
        } else {
            account.setArchived(false);
        }
        account.setLabel(accountJsonObj.has(KEY_LABEL) ? accountJsonObj.getString(KEY_LABEL) : null);
        if (accountJsonObj.has(KEY_XPUB) && (accountJsonObj.getString(KEY_XPUB)) != null && (accountJsonObj.getString(KEY_XPUB)).length() > 0) {
            account.setXpub((String) accountJsonObj.get(KEY_XPUB));
        } else {
            throw new Exception("HDAccount contains no xpub");
        }
        if (accountJsonObj.has(KEY_XPRIV) && (accountJsonObj.getString(KEY_XPRIV)) != null && (accountJsonObj.getString(KEY_XPRIV)).length() > 0) {
            account.setXpriv((String) accountJsonObj.get(KEY_XPRIV));
        } else {
            throw new Exception("HDAccount contains no private key");
        }

        if (accountJsonObj.has(KEY_ADDRESS_LABELS)) {
            JSONArray labels = accountJsonObj.getJSONArray(KEY_ADDRESS_LABELS);
            if (labels != null && labels.length() > 0) {
                TreeMap<Integer, String> addressLabels = new TreeMap<Integer, String>();
                for (int j = 0; j < labels.length(); j++) {
                    JSONObject obj = labels.getJSONObject(j);
                    addressLabels.put(obj.getInt(KEY_ADDRESS_INDEX), obj.getString(KEY_ADDRESS_LABEL));
                }
                account.setAddressLabels(addressLabels);
            }
        }

        if (accountJsonObj.has(KEY_CACHE)) {

            JSONObject cacheObj = accountJsonObj.getJSONObject(KEY_CACHE);
            account.setCache(Cache.fromJson(cacheObj));
        }

        return account;
    }

    public void incChange() {
        idxChangeAddresses++;
    }

    public void incReceive() {
        idxReceiveAddresses++;
    }

    @Deprecated
    public boolean isArchived() {
        return isArchived;
    }

    @Deprecated
    public void setArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public int getIdxChangeAddresses() {
        return idxChangeAddresses;
    }

    public void setIdxChangeAddresses(int nbChangeAddresses) {
        this.idxChangeAddresses = nbChangeAddresses;
    }

    public int getIdxReceiveAddresses() {
        return idxReceiveAddresses;
    }

    public void setIdxReceiveAddresses(int nbReceiveAddresses) {
        this.idxReceiveAddresses = nbReceiveAddresses;
    }

    @Deprecated
    public String getLabel() {
        if (strLabel == null) {
            return "";
        } else if (strLabel.length() > 32) {
            return strLabel.replaceAll("\n", " ").substring(0, 32) + "...";
        } else {
            return strLabel.replaceAll("\n", " ");
        }
    }

    @Deprecated
    public String getFullLabel() {
        return strLabel;
    }

    @Deprecated
    public void setLabel(String strLabel) {
        this.strLabel = strLabel;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Deprecated
    public String getXpub() {
        return strXpub;
    }

    @Deprecated
    public void setXpub(String strXpub) {
        this.strXpub = strXpub;
    }

    @Deprecated
    public String getXpriv() {
        return strXpriv;
    }

    @Deprecated
    public void setXpriv(String strXpriv) {
        this.strXpriv = strXpriv;
    }

    @Deprecated
    public TreeMap<Integer, String> getAddressLabels() {
        return addressLabels;
    }

    @Deprecated
    public void setAddressLabels(TreeMap<Integer, String> addressLabels) {
        this.addressLabels = addressLabels;
    }

    @Deprecated
    public String getAddressLabel(int index) {
        return addressLabels.get(index);
    }

    @Deprecated
    public void setAddressLabel(int index, String label) {
        this.addressLabels.put(index, label);
    }

    @Deprecated
    public Cache getCache() {
        return cache;
    }

    @Deprecated
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public int getRealIdx() {
        return realIdx;
    }

    public void setRealIdx(int realIdx) {
        this.realIdx = realIdx;
    }

    @Deprecated
    public JSONObject toJson() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put(KEY_ARCHIVED, isArchived);
        obj.put(KEY_LABEL, strLabel == null ? JSONObject.NULL : strLabel);
        obj.put(KEY_XPUB, strXpub == null ? JSONObject.NULL : strXpub);
        obj.put(KEY_XPRIV, strXpriv == null ? JSONObject.NULL : strXpriv);

        JSONArray labels = new JSONArray();
        if (addressLabels != null) {
            for (Integer key : addressLabels.keySet()) {
                JSONObject labelObj = new JSONObject();

                labelObj.put(KEY_ADDRESS_INDEX, key);
                String addressLabel = addressLabels.get(key);
                labelObj.put(KEY_ADDRESS_LABEL, addressLabel == null ? JSONObject.NULL : addressLabel);

                labels.put(labelObj);
            }
        }
        obj.put(KEY_ADDRESS_LABELS, labels);

        JSONObject _cache = cache.toJson();
        obj.put(KEY_CACHE, _cache);

        return obj;
    }

}
