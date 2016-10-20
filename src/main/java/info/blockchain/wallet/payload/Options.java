package info.blockchain.wallet.payload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Options {

    // TODO: 20/10/16 Simplify these keys again after refactor
    final String KEY_OPTIONS__FEE_PER_KB = "fee_per_kb";
    final String KEY_OPTIONS__LOGOUT_TIME = "logout_time";
    final String KEY_OPTIONS__HTML5_NOTIFICATIONS = "html5_notifications";
    final String KEY_OPTIONS__ADDITIONAL_SEED = "additional_seeds";
    final String KEY_OPTIONS__PBKDF2_ITERATIONS = "pbkdf2_iterations";
    final String KEY_OPTIONS__ENABLE_MULTIPLE_ACCOUNTS = "enable_multiple_accounts";// TODO: 20/10/16 Why is this written to payload?

    private int iterations = BlockchainWallet.DEFAULT_PBKDF2_ITERATIONS_V2;
    private long fee_per_kb = 10000L;
    private long logout_time = 600000L;
    private List<String> additionalSeeds = null;
    private boolean enable_multiple_accounts = true;
    private boolean html5_notifications = false;

    public Options() {
        additionalSeeds = new ArrayList<String>();
    }

    public Options(JSONObject optionsJson) {

        if (optionsJson.has(KEY_OPTIONS__PBKDF2_ITERATIONS)) {
            int val = optionsJson.getInt(KEY_OPTIONS__PBKDF2_ITERATIONS);
            setIterations(val);
        }
        if (optionsJson.has(KEY_OPTIONS__FEE_PER_KB)) {
            long val = optionsJson.getLong(KEY_OPTIONS__FEE_PER_KB);
            setFeePerKB(val);
        }
        if (optionsJson.has(KEY_OPTIONS__LOGOUT_TIME)) {
            long val = optionsJson.getLong(KEY_OPTIONS__LOGOUT_TIME);
            setLogoutTime(val);
        }
        if (optionsJson.has(KEY_OPTIONS__HTML5_NOTIFICATIONS)) {
            boolean val = optionsJson.getBoolean(KEY_OPTIONS__HTML5_NOTIFICATIONS);
            setHtml5Notifications(val);
        }
        if (optionsJson.has(KEY_OPTIONS__ADDITIONAL_SEED)) {
            JSONArray seedJsonArray = optionsJson.getJSONArray(KEY_OPTIONS__ADDITIONAL_SEED);
            List<String> additionalSeeds = new ArrayList<String>();
            for (int i = 0; i < seedJsonArray.length(); i++) {
                additionalSeeds.add(seedJsonArray.getString(i));
            }
            setAdditionalSeeds(additionalSeeds);
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

    public void setAdditionalSeeds(List<String> seeds) {
        this.additionalSeeds = seeds;
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

    public List<String> getAdditionalSeeds() {
        return additionalSeeds;
    }

    public boolean isEnableMultipleAccounts() {
        return enable_multiple_accounts;
    }

    public void setEnablMultipleAccounts(boolean enable) {
        this.enable_multiple_accounts = enable;
    }

    public boolean isHtml5Notifications() {
        return html5_notifications;
    }

    public void setHtml5Notifications(boolean enable) {
        this.html5_notifications = enable;
    }

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put(KEY_OPTIONS__PBKDF2_ITERATIONS, iterations);
        obj.put(KEY_OPTIONS__FEE_PER_KB, fee_per_kb);
        obj.put(KEY_OPTIONS__LOGOUT_TIME, logout_time);
        obj.put(KEY_OPTIONS__ENABLE_MULTIPLE_ACCOUNTS, enable_multiple_accounts);
        obj.put(KEY_OPTIONS__HTML5_NOTIFICATIONS, html5_notifications);

        JSONArray seeds = new JSONArray();
        for (String seed : additionalSeeds) {
            seeds.put(seed);
        }

        // TODO: 20/10/16 Confirm that we don't use this and remove
//        obj.put(KEY_OPTIONS__ADDITIONAL_SEED, seeds);

        return obj;
    }

}
