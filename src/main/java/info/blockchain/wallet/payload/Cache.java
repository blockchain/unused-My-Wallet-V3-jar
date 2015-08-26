package info.blockchain.wallet.payload;

import org.json.JSONObject;
import org.json.JSONException;

public class Cache {

    protected String receiveAccount = null;
    protected String changeAccount = null;

    public Cache() {
        ;
    }

    public String getReceiveAccount() {
        return receiveAccount;
    }

    public void setReceiveAccount(String receiveAccount) {
        this.receiveAccount = receiveAccount;
    }

    public String getChangeAccount() {
        return changeAccount;
    }

    public void setChangeAccount(String changeAccount) {
        this.changeAccount = changeAccount;
    }

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put("receiveAccount", receiveAccount == null ? "" : receiveAccount);
        obj.put("changeAccount", changeAccount == null ? "" : changeAccount);

        return obj;
    }

}
