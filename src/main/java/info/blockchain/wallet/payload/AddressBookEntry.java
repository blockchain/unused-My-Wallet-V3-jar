package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

public class AddressBookEntry {

    private static final String KEY_LABEL = "label";
    private static final String KEY_ADDR = "addr";

    private String strAddress = null;
    private String strLabel = null;

    public static AddressBookEntry fromJson(JSONObject addressObject){

        AddressBookEntry addressBookEntry = new AddressBookEntry();
        addressBookEntry.strAddress = addressObject.has(KEY_ADDR) ? addressObject.getString(KEY_ADDR) : null;
        addressBookEntry.strLabel = addressObject.has(KEY_LABEL) ? addressObject.getString(KEY_LABEL) : null;
        return addressBookEntry;
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

    public JSONObject toJson() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put(KEY_ADDR, strAddress);
        obj.put(KEY_LABEL, strLabel == null ? JSONObject.NULL : strLabel);

        return obj;
    }

}