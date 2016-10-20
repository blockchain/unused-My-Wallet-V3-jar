package info.blockchain.wallet.payload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Account {

    // TODO: 20/10/16 Simplify these keys again after refactor
    final String KEY_HD_WALLET__ARCHIVED = "archived";
    final String KEY_HD_WALLET__LABEL = "label";
    final String KEY_HD_WALLET__XPUB = "xpub";
    final String KEY_HD_WALLET__XPRIV = "xpriv";
    final String KEY_HD_WALLET__RECEIVE_ADDRESSES = "receive_addresses";
    final String KEY_HD_WALLET__INDEX = "index";
    final String KEY_HD_WALLET__AMOUNT = "amount";
    final String KEY_HD_WALLET__PAID = "paid";
    final String KEY_HD_WALLET__CANCELLED = "cancelled";
    final String KEY_HD_WALLET__TAGS = "tags";
    final String KEY_HD_WALLET__ADDRESS_LABELS = "address_labels";
    final String KEY_HD_WALLET__CACHE = "cache";

    protected boolean isArchived = false;
    protected int idxChangeAddresses = 0;
    protected int idxReceiveAddresses = 0;
    protected String strLabel = null;
    protected List<ReceiveAddress> receiveAddresses = null;
    protected List<String> tags = null;
    protected long amount = 0L;
    protected String strXpub = null;
    protected String strXpriv = null;
    protected TreeMap<Integer, String> addressLabels = null;
    protected Cache cache = null;
    protected int realIdx = -1;

    public Account() {
        receiveAddresses = new ArrayList<ReceiveAddress>();
        tags = new ArrayList<String>();
        strXpub = "";
        strXpriv = "";
        addressLabels = new TreeMap<Integer, String>();
        cache = new Cache();
    }

    /*
    public Account(boolean isArchived, int change, int receive, String label, List<ReceiveAddress> receiveAddresses, List<String> tags, long amount) {
        this.isArchived = isArchived;
        this.nbChangeAddresses = change;
        this.nbReceiveAddresses = receive;
        this.strLabel = label;
        this.receiveAddresses = receiveAddresses;
        this.tags = tags;
        this.amount = amount;
    }
    */

    public Account(boolean isArchived, int change, int receive, String label) {
        this.isArchived = isArchived;
        this.idxChangeAddresses = change;
        this.idxReceiveAddresses = receive;
        this.strLabel = label;
        receiveAddresses = new ArrayList<ReceiveAddress>();
        tags = new ArrayList<String>();
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
        this.receiveAddresses = new ArrayList<ReceiveAddress>();
        tags = new ArrayList<String>();
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
        setArchived(accountJsonObj.has(KEY_HD_WALLET__ARCHIVED) ? (Boolean) accountJsonObj.get(KEY_HD_WALLET__ARCHIVED) : false);
        if (accountJsonObj.has(KEY_HD_WALLET__ARCHIVED) && (Boolean) accountJsonObj.get(KEY_HD_WALLET__ARCHIVED)) {
            setArchived(true);
        } else {
            setArchived(false);
        }
        setLabel(accountJsonObj.has(KEY_HD_WALLET__LABEL) ? (String) accountJsonObj.get(KEY_HD_WALLET__LABEL) : "");// TODO: 20/10/16 Don't treat as blank
        if (accountJsonObj.has(KEY_HD_WALLET__XPUB) && (accountJsonObj.getString(KEY_HD_WALLET__XPUB)) != null && (accountJsonObj.getString(KEY_HD_WALLET__XPUB)).length() > 0) {
            setXpub((String) accountJsonObj.get(KEY_HD_WALLET__XPUB));
        } else {
            throw new Exception("Account contains no xpub");
        }
        if (accountJsonObj.has(KEY_HD_WALLET__XPRIV) && (accountJsonObj.getString(KEY_HD_WALLET__XPRIV)) != null && (accountJsonObj.getString(KEY_HD_WALLET__XPRIV)).length() > 0) {
            setXpriv((String) accountJsonObj.get(KEY_HD_WALLET__XPRIV));
        } else {
            throw new Exception("Account contains no private key");
        }

        if (accountJsonObj.has(KEY_HD_WALLET__RECEIVE_ADDRESSES)) {
            JSONArray receives = (JSONArray) accountJsonObj.get(KEY_HD_WALLET__RECEIVE_ADDRESSES);
            List<ReceiveAddress> receiveAddresses = new ArrayList<ReceiveAddress>();
            for (int j = 0; j < receives.length(); j++) {
                JSONObject receiveObj = (JSONObject) receives.get(j);
                ReceiveAddress receiveAddress = new ReceiveAddress();
                if (receiveObj.has(KEY_HD_WALLET__INDEX)) {
                    int val = (Integer) receiveObj.get(KEY_HD_WALLET__INDEX);
                    receiveAddress.setIndex(val);
                }
                receiveAddress.setLabel(receiveObj.has(KEY_HD_WALLET__LABEL) ? (String) receiveObj.get(KEY_HD_WALLET__LABEL) : "");// TODO: 20/10/16 Don't treat as blank
                receiveAddress.setAmount(receiveObj.has(KEY_HD_WALLET__AMOUNT) ? receiveObj.getLong(KEY_HD_WALLET__AMOUNT) : 0L);
                receiveAddress.setPaid(receiveObj.has(KEY_HD_WALLET__PAID) ? receiveObj.getLong(KEY_HD_WALLET__PAID) : 0L);
//                                    receiveAddress.setCancelled(receiveObj.has(KEY_HD_WALLET__CANCELLED) ? (Boolean)receiveObj.get(KEY_HD_WALLET__CANCELLED) : false);
//                                    receiveAddress.setComplete(receiveAddress.getPaid() >= receiveAddress.getAmount());
                receiveAddresses.add(receiveAddress);
            }
            setReceiveAddresses(receiveAddresses);
        }

        if (accountJsonObj.has(KEY_HD_WALLET__TAGS)) {
            JSONArray tags = (JSONArray) accountJsonObj.get(KEY_HD_WALLET__TAGS);
            if (tags != null && tags.length() > 0) {
                List<String> accountTags = new ArrayList<String>();
                for (int j = 0; j < tags.length(); j++) {
                    accountTags.add((String) tags.get(j));
                }
                setTags(accountTags);
            }
        }

        if (accountJsonObj.has(KEY_HD_WALLET__ADDRESS_LABELS)) {
            JSONArray labels = (JSONArray) accountJsonObj.get(KEY_HD_WALLET__ADDRESS_LABELS);
            if (labels != null && labels.length() > 0) {
                TreeMap<Integer, String> addressLabels = new TreeMap<Integer, String>();
                for (int j = 0; j < labels.length(); j++) {
                    JSONObject obj = labels.getJSONObject(j);
                    addressLabels.put(obj.getInt(KEY_HD_WALLET__INDEX), obj.getString(KEY_HD_WALLET__LABEL));
                }
                setAddressLabels(addressLabels);
            }
        }

        if (accountJsonObj.has(KEY_HD_WALLET__CACHE)) {

            JSONObject cacheObj = (JSONObject) accountJsonObj.get(KEY_HD_WALLET__CACHE);

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

    public List<ReceiveAddress> getReceiveAddresses() {
        return receiveAddresses;
    }

    public void setReceiveAddresses(List<ReceiveAddress> receiveAddresses) {
        this.receiveAddresses = receiveAddresses;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

        obj.put(KEY_HD_WALLET__ARCHIVED, isArchived);
//        obj.put("change_addresses", nbChangeAddresses);// TODO: 20/10/16 Why is this commented out?
//        obj.put("receive_addresses_count", nbReceiveAddresses);
        obj.put(KEY_HD_WALLET__LABEL, strLabel == null ? "" : strLabel);// TODO: 20/10/16 Don't treat as blank
        obj.put(KEY_HD_WALLET__XPUB, strXpub == null ? "" : strXpub);// TODO: 20/10/16 Don't treat as blank
        obj.put(KEY_HD_WALLET__XPRIV, strXpriv == null ? "" : strXpriv);// TODO: 20/10/16 Don't treat as blank

        JSONArray receives = new JSONArray();
        for (ReceiveAddress receiveAddress : receiveAddresses) {
            receives.put(receiveAddress.dumpJSON());
        }
        /*
         * temporary: do not write out receive addresses/payment requests until further notice
         */
//        obj.put("receive_addresses", receives);

        /*
        JSONArray _tags = new JSONArray();
        for(String tag : tags) {
            _tags.put(tag);
        }
        obj.put("tags", _tags);

        obj.put("amount", amount);
        */

        JSONArray labels = new JSONArray();
        for (Integer key : addressLabels.keySet()) {
            JSONObject labelObj = new JSONObject();

            labelObj.put(KEY_HD_WALLET__INDEX, key);
            labelObj.put(KEY_HD_WALLET__LABEL, addressLabels.get(key));

            labels.put(labelObj);
        }
        obj.put(KEY_HD_WALLET__ADDRESS_LABELS, labels);

        JSONObject _cache = cache.dumpJSON();
        obj.put(KEY_HD_WALLET__CACHE, _cache);

        return obj;
    }

}
