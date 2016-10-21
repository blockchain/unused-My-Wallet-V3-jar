package info.blockchain.wallet.payload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.TreeMap;

public class Account {

    private final String KEY_LABEL = "label";
    private final String KEY_ARCHIVED = "archived";
    private final String KEY_XPRIV = "xpriv";
    private final String KEY_XPUB = "xpub";
    private final String KEY_CACHE = "cache";
    private final String KEY_ADDRESS_LABELS = "address_labels";
    private final String KEY_ADDRESS_INDEX = "index";
    private final String KEY_ADDRESS_LABEL = KEY_LABEL;

    protected boolean isArchived = false;
    protected int idxChangeAddresses = 0;
    protected int idxReceiveAddresses = 0;
    protected String strLabel = null;
    protected long amount = 0L;
    protected String strXpub = null;
    protected String strXpriv = null;
    protected TreeMap<Integer, String> addressLabels = null;
    protected Cache cache = null;
    protected int realIdx = -1;

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

    public Account(JSONObject accountJsonObj, int index) throws Exception {

        parseJson(accountJsonObj, index);
    }

    private void parseJson(JSONObject accountJsonObj, int index) throws Exception{

        setRealIdx(index);
        setArchived(accountJsonObj.has(KEY_ARCHIVED) ? accountJsonObj.getBoolean(KEY_ARCHIVED) : false);
        if (accountJsonObj.has(KEY_ARCHIVED) && accountJsonObj.getBoolean(KEY_ARCHIVED)) {
            setArchived(true);
        } else {
            setArchived(false);
        }
        setLabel(accountJsonObj.has(KEY_LABEL) ? accountJsonObj.getString(KEY_LABEL) : "");// TODO: 20/10/16 Don't treat as blank
        if (accountJsonObj.has(KEY_XPUB) && (accountJsonObj.getString(KEY_XPUB)) != null && (accountJsonObj.getString(KEY_XPUB)).length() > 0) {
            setXpub((String) accountJsonObj.get(KEY_XPUB));
        } else {
            throw new Exception("Account contains no xpub");
        }
        if (accountJsonObj.has(KEY_XPRIV) && (accountJsonObj.getString(KEY_XPRIV)) != null && (accountJsonObj.getString(KEY_XPRIV)).length() > 0) {
            setXpriv((String) accountJsonObj.get(KEY_XPRIV));
        } else {
            throw new Exception("Account contains no private key");
        }

        if (accountJsonObj.has(KEY_ADDRESS_LABELS)) {
            JSONArray labels = accountJsonObj.getJSONArray(KEY_ADDRESS_LABELS);
            if (labels != null && labels.length() > 0) {
                TreeMap<Integer, String> addressLabels = new TreeMap<Integer, String>();
                for (int j = 0; j < labels.length(); j++) {
                    JSONObject obj = labels.getJSONObject(j);
                    addressLabels.put(obj.getInt(KEY_ADDRESS_INDEX), obj.getString(KEY_ADDRESS_LABEL));
                }
                setAddressLabels(addressLabels);
            }
        }

        if (accountJsonObj.has(KEY_CACHE)) {

            JSONObject cacheObj = accountJsonObj.getJSONObject(KEY_CACHE);

            Cache cache = new Cache(cacheObj);
            setCache(cache);

        }
    }

    public void incChange() {
        idxChangeAddresses++;
    }

    public void incReceive() {
        idxReceiveAddresses++;
    }

    public boolean isArchived() {
        return isArchived;
    }

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

    // TODO: 20/10/16 This should be done UI side
    public String getLabel() {
        if (strLabel == null) {
            return "";
        } else if (strLabel.length() > 32) {
            return strLabel.replaceAll("\n", " ").substring(0, 32) + "...";
        } else {
            return strLabel.replaceAll("\n", " ");
        }
    }

    public String getFullLabel() {
        if (strLabel == null) {
            return "";
        } else {
            return strLabel;
        }
    }

    public void setLabel(String strLabel) {
        this.strLabel = strLabel;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getXpub() {
        return strXpub;
    }

    public void setXpub(String strXpub) {
        this.strXpub = strXpub;
    }

    public String getXpriv() {
        return strXpriv;
    }

    public void setXpriv(String strXpriv) {
        this.strXpriv = strXpriv;
    }

    public TreeMap<Integer, String> getAddressLabels() {
        return addressLabels;
    }

    public void setAddressLabels(TreeMap<Integer, String> addressLabels) {
        this.addressLabels = addressLabels;
    }

    public String getAddressLabel(int index) {
        return addressLabels.get(index);
    }

    public void setAddressLabel(int index, String label) {
        this.addressLabels.put(index, label);
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public int getRealIdx() {
        return realIdx;
    }

    public void setRealIdx(int realIdx) {
        this.realIdx = realIdx;
    }

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put(KEY_ARCHIVED, isArchived);
        obj.put(KEY_LABEL, strLabel == null ? "" : strLabel);// TODO: 20/10/16 Don't treat as blank
        obj.put(KEY_XPUB, strXpub == null ? "" : strXpub);// TODO: 20/10/16 Don't treat as blank
        obj.put(KEY_XPRIV, strXpriv == null ? "" : strXpriv);// TODO: 20/10/16 Don't treat as blank

        JSONArray labels = new JSONArray();
        for (Integer key : addressLabels.keySet()) {
            JSONObject labelObj = new JSONObject();

            labelObj.put(KEY_ADDRESS_INDEX, key);
            labelObj.put(KEY_ADDRESS_LABEL, addressLabels.get(key));

            labels.put(labelObj);
        }
        obj.put(KEY_ADDRESS_LABELS, labels);

        JSONObject _cache = cache.dumpJSON();
        obj.put(KEY_CACHE, _cache);

        return obj;
    }

}
