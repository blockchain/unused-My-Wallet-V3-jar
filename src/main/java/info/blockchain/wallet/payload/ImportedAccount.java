package info.blockchain.wallet.payload;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class ImportedAccount extends Account {

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

    public List<LegacyAddress> getLegacyAddresses()	{
        return this.legacyAddresses;
    }

    public void setLegacyAddresses(List<LegacyAddress> addrs)	{
        this.legacyAddresses = addrs;
    }

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = super.dumpJSON();

        obj.put("archived", isArchived);
        obj.put("change_addresses", idxChangeAddresses);
        obj.put("label", strLabel == null ? "" : strLabel);

        JSONArray receives = new JSONArray();
        for(ReceiveAddress receiveAddress : receiveAddresses) {
            receives.put(receiveAddress.dumpJSON());
        }
        obj.put("receive_addresses", receives);

        JSONArray _tags = new JSONArray();
        for(String tag : tags) {
            _tags.put(tag);
        }
        obj.put("tags", _tags);

        obj.put("amount", amount);

        return obj;
    }

}
