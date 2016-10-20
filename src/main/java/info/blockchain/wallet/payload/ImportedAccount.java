package info.blockchain.wallet.payload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ImportedAccount extends Account implements PayloadJsonKeys{

    private List<LegacyAddress> legacyAddresses = null;

    public ImportedAccount() {
        super();
        this.legacyAddresses = new ArrayList<LegacyAddress>();
    }

    public ImportedAccount(String label, List<LegacyAddress> legacyAddresses, List<String> tags, long amount) {
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

        obj.put(KEY_HD_WALLET__ARCHIVED, isArchived);
        obj.put(KEY_LEGACY_KEYS__CHANGE_ADDRESS, idxChangeAddresses);
        obj.put(KEY_LEGACY_KEYS__LABEL, strLabel == null ? "" : strLabel);

        JSONArray receives = new JSONArray();
        for (ReceiveAddress receiveAddress : receiveAddresses) {
            receives.put(receiveAddress.dumpJSON());
        }
        obj.put(KEY_LEGACY_KEYS__RECEIVE_ADDRESS, receives);

        JSONArray _tags = new JSONArray();
        for (String tag : tags) {
            _tags.put(tag);
        }
        obj.put(KEY_LEGACY_KEYS__TAGS, _tags);

        obj.put(KEY_LEGACY_KEYS__AMOUNT, amount);

        return obj;
    }

}
