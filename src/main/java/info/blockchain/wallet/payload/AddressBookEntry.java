package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

public class AddressBookEntry implements PayloadJsonKeys{

    // TODO: 20/10/16 Simplify these keys again after refactor
    String KEY_ADDRESS_BOOK__LABEL = "label";
    String KEY_ADDRESS_BOOK__ADDR = "addr";

    private String strAddress = null;
    private String strLabel = null;

    public AddressBookEntry() {
    }

    public AddressBookEntry(JSONObject addressObject) {
        this.strAddress = addressObject.has(KEY_ADDRESS_BOOK__ADDR) ? (String) addressObject.get(KEY_ADDRESS_BOOK__ADDR) : null;
        this.strLabel = addressObject.has(KEY_ADDRESS_BOOK__LABEL) ? (String) addressObject.get(KEY_ADDRESS_BOOK__LABEL) : null;
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

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put(KEY_ADDRESS_BOOK__ADDR, strAddress);
        obj.put(KEY_ADDRESS_BOOK__LABEL, strLabel == null ? "" : strLabel);

        return obj;
    }

}