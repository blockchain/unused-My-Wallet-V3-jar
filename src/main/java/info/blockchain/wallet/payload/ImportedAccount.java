package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
Could be used to represent a collection of all legacy addresses as a single account
 */
public class ImportedAccount extends Account {

    private final String KEY_LABEL = "label";
    private final String KEY_AMOUNT = "amount";
    private final String KEY_ARCHIVED = "archived";

    private List<LegacyAddress> legacyAddresses = null;

    public ImportedAccount() {
        super();
        this.legacyAddresses = new ArrayList<LegacyAddress>();
    }

    public ImportedAccount(String label, List<LegacyAddress> legacyAddresses, long amount) {
        this.isArchived = false;
        this.strLabel = label;
        this.legacyAddresses = legacyAddresses;
        this.amount = amount;
    }

    public List<LegacyAddress> getLegacyAddresses() {
        return this.legacyAddresses;
    }

    public void setLegacyAddresses(List<LegacyAddress> addrs) {
        this.legacyAddresses = addrs;
    }

    public JSONObject toJson() throws JSONException {

        JSONObject obj = super.toJson();

        obj.put(KEY_ARCHIVED, isArchived);
        obj.put(KEY_LABEL, strLabel == null ? JSONObject.NULL : strLabel);
        obj.put(KEY_AMOUNT, amount);

        return obj;
    }

}
