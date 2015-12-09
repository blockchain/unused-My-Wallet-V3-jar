package info.blockchain.wallet.payload;

import info.blockchain.wallet.crypto.AESUtil;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Options {

    private int iterations = PayloadFactory.WalletPbkdf2Iterations;
    private long fee_per_kb = 10000L;
    private long logout_time = 600000L;
    private List<String> additionalSeeds = null;
    private boolean enable_multiple_accounts = true;

    public Options() { additionalSeeds = new ArrayList<String>(); }

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

    public JSONObject dumpJSON() throws JSONException	 {

        JSONObject obj = new JSONObject();

        obj.put("pbkdf2_iterations", iterations);
        obj.put("fee_per_kb", fee_per_kb);
        obj.put("logout_time", logout_time);
        obj.put("enable_multiple_accounts", enable_multiple_accounts);

        JSONArray seeds = new JSONArray();
        for(String seed : additionalSeeds) {
            seeds.put(seed);
        }
//        obj.put("additional_seeds", seeds);

        return obj;
    }

}
