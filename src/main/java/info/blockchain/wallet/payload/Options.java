package info.blockchain.wallet.payload;

import org.json.JSONException;
import org.json.JSONObject;

public class Options {

    private final String KEY_FEE_PER_KB = "fee_per_kb";
    private final String KEY_LOGOUT_TIME = "logout_time";
    private final String KEY_HTML5_NOTIFICATIONS = "html5_notifications";
    private final String KEY_PBKDF2_ITERATIONS = "pbkdf2_iterations";

    private int iterations = BlockchainWallet.DEFAULT_PBKDF2_ITERATIONS_V2;
    private long fee_per_kb = 10000L;
    private long logout_time = 600000L;
    private boolean html5_notifications = false;

    public Options() {

    }

    public Options(JSONObject optionsJson) {

        parseJson(optionsJson);
    }

    private void parseJson(JSONObject optionsJson){

        if (optionsJson.has(KEY_PBKDF2_ITERATIONS)) {
            int val = optionsJson.getInt(KEY_PBKDF2_ITERATIONS);
            setIterations(val);
        }
        if (optionsJson.has(KEY_FEE_PER_KB)) {
            long val = optionsJson.getLong(KEY_FEE_PER_KB);
            setFeePerKB(val);
        }
        if (optionsJson.has(KEY_LOGOUT_TIME)) {
            long val = optionsJson.getLong(KEY_LOGOUT_TIME);
            setLogoutTime(val);
        }
        if (optionsJson.has(KEY_HTML5_NOTIFICATIONS)) {
            boolean val = optionsJson.getBoolean(KEY_HTML5_NOTIFICATIONS);
            setHtml5Notifications(val);
        }
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void setFeePerKB(long fee_per_kb) {
        this.fee_per_kb = fee_per_kb;
    }

    public void setLogoutTime(long logout_time) {
        this.logout_time = logout_time;
    }

    public int getIterations() {
        return iterations;
    }

    public long getFeePerKB() {
        return fee_per_kb;
    }

    public long getLogoutTime() {
        return logout_time;
    }

    public boolean isHtml5Notifications() {
        return html5_notifications;
    }

    public void setHtml5Notifications(boolean enable) {
        this.html5_notifications = enable;
    }

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put(KEY_PBKDF2_ITERATIONS, iterations);
        obj.put(KEY_FEE_PER_KB, fee_per_kb);
        obj.put(KEY_LOGOUT_TIME, logout_time);
        obj.put(KEY_HTML5_NOTIFICATIONS, html5_notifications);

        return obj;
    }

}
