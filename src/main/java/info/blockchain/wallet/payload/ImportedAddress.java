package info.blockchain.wallet.payload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ImportedAddress extends Account {

    private final String KEY_LABEL = "label";
    private final String KEY_CHANGE_ADDRESSES = "change_addresses";//?
    private final String KEY_RECEIVE_ADDRESSES = "receive_addresses";
    private final String KEY_TAGS = "tags";//?
    private final String KEY_AMOUNT = "amount";
    private final String KEY_ARCHIVED = "archived";

    private List<LegacyAddress> legacyAddresses = null;

    public ImportedAddress() {
        super();
        this.legacyAddresses = new ArrayList<LegacyAddress>();
    }

    public ImportedAddress(String label, List<LegacyAddress> legacyAddresses, List<String> tags, long amount) {
        this.isArchived = false;
        this.idxChangeAddresses = 0;
        this.receiveAddresses = new ArrayList<ReceiveAddress>();

        this.strLabel = label;
        this.legacyAddresses = legacyAddresses;
        this.tags = tags;
        this.amount = amount;
    }

    public List<LegacyAddress> getLegacyAddresses() {
        return this.legacyAddresses;
    }

    public void setLegacyAddresses(List<LegacyAddress> addrs) {
        this.legacyAddresses = addrs;
    }

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = super.dumpJSON();

        obj.put(KEY_ARCHIVED, isArchived);
        obj.put(KEY_CHANGE_ADDRESSES, idxChangeAddresses);
        obj.put(KEY_LABEL, strLabel == null ? "" : strLabel);

        JSONArray receives = new JSONArray();
        for (ReceiveAddress receiveAddress : receiveAddresses) {
            receives.put(receiveAddress.dumpJSON());
        }
        obj.put(KEY_RECEIVE_ADDRESSES, receives);

        JSONArray _tags = new JSONArray();
        for (String tag : tags) {
            _tags.put(tag);
        }
        obj.put(KEY_TAGS, _tags);

        obj.put(KEY_AMOUNT, amount);

        return obj;
    }

}
