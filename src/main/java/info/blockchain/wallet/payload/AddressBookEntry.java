package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

public class AddressBookEntry {

    private String strAddress = null;
    private String strLabel = null;

    public AddressBookEntry() {
        ;
    }

    public AddressBookEntry(String address, String label) {
        this.strAddress = address;
        this.strLabel = label;
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

        obj.put("addr", strAddress);
        obj.put("label", strLabel == null ? "" : strLabel);

        return obj;
    }

}
