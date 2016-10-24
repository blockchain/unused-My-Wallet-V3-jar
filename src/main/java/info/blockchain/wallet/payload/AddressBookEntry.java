package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

public class AddressBookEntry {

    private final String KEY_LABEL = "label";
    private final String KEY_ADDR = "addr";

    private String strAddress = null;
    private String strLabel = null;

    public AddressBookEntry() {
    }

    public AddressBookEntry(JSONObject addressObject) {
        parseJson(addressObject);
    }

    private void parseJson(JSONObject addressObject) {
        this.strAddress = addressObject.has(KEY_ADDR) ? addressObject.getString(KEY_ADDR) : null;
        this.strLabel = addressObject.has(KEY_LABEL) ? addressObject.getString(KEY_LABEL) : null;
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