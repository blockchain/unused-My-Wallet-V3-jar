package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

public class Cache implements PayloadJsonKeys{

    protected String receiveAccount = null;
    protected String changeAccount = null;

    public Cache() {
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

        obj.put(KEY_HD_WALLET__RECEIVE_ACCOUNT, receiveAccount == null ? "" : receiveAccount);
        obj.put(KEY_HD_WALLET__CHANGE_ACCOUNT, changeAccount == null ? "" : changeAccount);

        return obj;
    }

}
