package info.blockchain.wallet.payload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HDWallet {

    private final String KEY_ACCOUNTS = "accounts";
    private final String KEY_SEED_HEX = "seed_hex";
    private final String KEY_PASSPHRASE = "passphrase";
    private final String KEY_MNEMONIC_VERIFIED = "mnemonic_verified";
    private final String KEY_DEFAULT_ACCOUNT_INDEX = "default_account_idx";

    private String strSeedHex = null;
    private List<Account> accounts = null;
    private String strPassphrase = "";
    private boolean mnemonic_verified = false;
    private int default_account_idx = 0;

    public HDWallet() {
        accounts = new ArrayList<Account>();
    }

    public HDWallet(String seed, List<Account> accounts, String passphrase) {
        this.strSeedHex = seed;
        this.accounts = accounts;
        this.strPassphrase = passphrase;
    }

    public HDWallet(JSONObject walletJsonObject) {
        parseJson(walletJsonObject);
    }

    private void parseJson(JSONObject walletJsonObject){

        if (walletJsonObject.has(KEY_SEED_HEX)) {
            setSeedHex(walletJsonObject.getString(KEY_SEED_HEX));
        }
        if (walletJsonObject.has(KEY_PASSPHRASE)) {
            setPassphrase(walletJsonObject.getString(KEY_PASSPHRASE));
        }
        if (walletJsonObject.has(KEY_MNEMONIC_VERIFIED)) {
            mnemonic_verified(walletJsonObject.getBoolean(KEY_MNEMONIC_VERIFIED));
        }
        if (walletJsonObject.has(KEY_DEFAULT_ACCOUNT_INDEX)) {
            int i;
            try {
                String val = walletJsonObject.getString(KEY_DEFAULT_ACCOUNT_INDEX);
                i = Integer.parseInt(val);
            } catch (Exception cce) {
                i = walletJsonObject.getInt(KEY_DEFAULT_ACCOUNT_INDEX);
            }
            setDefaultIndex(i);
        }

        if (walletJsonObject.has(KEY_ACCOUNTS)) {

            JSONArray accountsJsonArray = walletJsonObject.getJSONArray(KEY_ACCOUNTS);
            if (accountsJsonArray != null && accountsJsonArray.length() > 0) {
                List<Account> walletAccounts = new ArrayList<Account>();
                for (int i = 0; i < accountsJsonArray.length(); i++) {

                    JSONObject accountJsonObj = accountsJsonArray.getJSONObject(i);

                    Account account = null;
                    try {
                        account = new Account(accountJsonObj, i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    walletAccounts.add(account);
                }

                setAccounts(walletAccounts);

            }
        }
    }

    public String getSeedHex() {
        return strSeedHex;
    }

    public void setSeedHex(String seed) {
        this.strSeedHex = seed;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public String getPassphrase() {
        return strPassphrase;
    }

    public void setPassphrase(String passphrase) {
        this.strPassphrase = passphrase;
    }

    public boolean isMnemonicVerified() {
        return mnemonic_verified;
    }

    public void mnemonic_verified(boolean verified) {
        this.mnemonic_verified = verified;
    }

    public int getDefaultIndex() {
        return default_account_idx;
    }

    public void setDefaultIndex(int idx) {
        this.default_account_idx = idx;
    }

    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put(KEY_SEED_HEX, strSeedHex == null ? JSONObject.NULL : strSeedHex);
        obj.put(KEY_PASSPHRASE, strPassphrase == null ? JSONObject.NULL : strPassphrase);
        obj.put(KEY_DEFAULT_ACCOUNT_INDEX, default_account_idx);
        obj.put(KEY_MNEMONIC_VERIFIED, mnemonic_verified);

        JSONArray accs = new JSONArray();
        for (Account account : accounts) {
            if (!(account instanceof ImportedAccount)) {
                accs.put(account.dumpJSON());
            }
        }
        obj.put(KEY_ACCOUNTS, accs);

        return obj;
    }

}
