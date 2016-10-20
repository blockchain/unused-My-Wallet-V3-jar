package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

public class Cache {

    private final String KEY_RECEIVE_ACCOUNT = "receiveAccount";
    private final String KEY_CHANGE_ACCOUNT = "changeAccount";

    protected String receiveAccount = null;
    protected String changeAccount = null;

    public Cache(){

    }

    public Cache(JSONObject cacheObj) {

        parseJson(cacheObj);
    }

    private void parseJson(JSONObject cacheObj){

        if (cacheObj.has(KEY_RECEIVE_ACCOUNT)) {
            setReceiveAccount(cacheObj.getString(KEY_RECEIVE_ACCOUNT));
        }

        if (cacheObj.has(KEY_CHANGE_ACCOUNT)) {
            setChangeAccount(cacheObj.getString(KEY_CHANGE_ACCOUNT));
        }
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

        obj.put(KEY_RECEIVE_ACCOUNT, receiveAccount == null ? "" : receiveAccount);
        obj.put(KEY_CHANGE_ACCOUNT, changeAccount == null ? "" : changeAccount);

        return obj;
    }

}
