package info.blockchain.wallet.payload;

import org.json.JSONObject;
import org.json.JSONException;

public class XPut {

    private int accountId = -1;
    private String strAddress = null;
    private boolean multAddrs = false;
    private long amount = 0L;

    public XPut() {
    }

    public XPut(int id, String addr, boolean multi, long amount) {
        this.accountId = id;
        this.strAddress = addr;
        this.multAddrs = multi;
        this.amount = amount;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int id) {
        this.accountId = id;
    }

    public String getAddress() {
        return strAddress;
    }

    public void setAddress(String address) {
        this.strAddress = address;
    }

    public boolean isMultAddrs() {
        return multAddrs;
    }

    public void setMultAddrs(boolean multAddrs) {
        this.multAddrs = multAddrs;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put("account", accountId);
        obj.put("amount", amount);
        obj.put("multiple_addresses", multAddrs);
        obj.put("address", strAddress == null ? "" : strAddress);

        return obj;
    }

}
