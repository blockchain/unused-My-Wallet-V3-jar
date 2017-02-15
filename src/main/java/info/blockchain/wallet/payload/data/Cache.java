package info.blockchain.wallet.payload.data;

import org.json.JSONException;
import org.json.JSONObject;

@Deprecated
public class Cache{

    private static final String KEY_RECEIVE_ACCOUNT = "receiveAccount";
    private static final String KEY_CHANGE_ACCOUNT = "changeAccount";

    protected String receiveAccount = null;
    protected String changeAccount = null;

    @Deprecated
    public static Cache fromJson(JSONObject cacheObj){
        Cache cache = new Cache();

        if (cacheObj.has(KEY_RECEIVE_ACCOUNT) && !cacheObj.isNull(KEY_RECEIVE_ACCOUNT)) {
            cache.receiveAccount = cacheObj.getString(KEY_RECEIVE_ACCOUNT);
        }

        if (cacheObj.has(KEY_CHANGE_ACCOUNT) && !cacheObj.isNull(KEY_CHANGE_ACCOUNT)) {
            cache.changeAccount = cacheObj.getString(KEY_CHANGE_ACCOUNT);
        }

        return cache;
    }

    @Deprecated
    public String getReceiveAccount() {
        return receiveAccount;
    }

    @Deprecated
    public void setReceiveAccount(String receiveAccount) {
        this.receiveAccount = receiveAccount;
    }

    @Deprecated
    public String getChangeAccount() {
        return changeAccount;
    }

    @Deprecated
    public void setChangeAccount(String changeAccount) {
        this.changeAccount = changeAccount;
    }

    @Deprecated
    public JSONObject toJson() throws JSONException {

        JSONObject obj = new JSONObject();

        if(receiveAccount != null) {
            obj.put(KEY_RECEIVE_ACCOUNT, receiveAccount);
        }

        if(changeAccount != null) {
            obj.put(KEY_CHANGE_ACCOUNT, changeAccount);
        }

        return obj;
    }

}
