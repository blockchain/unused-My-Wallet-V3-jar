package info.blockchain.wallet.payload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HDWallet {

    String KEY_PAYLOAD__ACCOUNTS = "accounts";
    String KEY_HD_WALLET__SEED_HEX = "seed_hex";
    String KEY_HD_WALLET__PASSPHRASE = "passphrase";
    String KEY_HD_WALLET__MNEMONIC_VERIFIED = "mnemonic_verified";
    String KEY_HD_WALLET__DEFAULT_ACCOUNT_INDEX = "default_account_idx";

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

    public HDWallet(String seed, List<Account> accounts, String passphrase, HashMap<String, PaidTo> paidTo) {
        this.strSeedHex = seed;
        this.accounts = accounts;
        this.strPassphrase = passphrase;
    }

    public HDWallet(JSONObject walletJsonObject) {
        parseJson(walletJsonObject);
    }

    private void parseJson(JSONObject walletJsonObject){

        if (walletJsonObject.has(KEY_HD_WALLET__SEED_HEX)) {
            setSeedHex((String) walletJsonObject.get(KEY_HD_WALLET__SEED_HEX));
        }
        if (walletJsonObject.has(KEY_HD_WALLET__PASSPHRASE)) {
            setPassphrase((String) walletJsonObject.get(KEY_HD_WALLET__PASSPHRASE));
        }
        if (walletJsonObject.has(KEY_HD_WALLET__MNEMONIC_VERIFIED)) {
            mnemonic_verified(walletJsonObject.getBoolean(KEY_HD_WALLET__MNEMONIC_VERIFIED));
        }
        if (walletJsonObject.has(KEY_HD_WALLET__DEFAULT_ACCOUNT_INDEX)) {
            int i;
            try {
                String val = (String) walletJsonObject.get(KEY_HD_WALLET__DEFAULT_ACCOUNT_INDEX);
                i = Integer.parseInt(val);
            } catch (java.lang.ClassCastException cce) {
                i = (Integer) walletJsonObject.get(KEY_HD_WALLET__DEFAULT_ACCOUNT_INDEX);
            }
            setDefaultIndex(i);
        }

        if (walletJsonObject.has(KEY_PAYLOAD__ACCOUNTS)) {

            JSONArray accountsJsonArray = walletJsonObject.getJSONArray(KEY_PAYLOAD__ACCOUNTS);
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

        obj.put(KEY_HD_WALLET__SEED_HEX, strSeedHex);
        obj.put(KEY_HD_WALLET__PASSPHRASE, strPassphrase);
        obj.put(KEY_HD_WALLET__DEFAULT_ACCOUNT_INDEX, default_account_idx);
        obj.put(KEY_HD_WALLET__MNEMONIC_VERIFIED, mnemonic_verified);

        JSONArray accs = new JSONArray();
        for (Account account : accounts) {
            if (!(account instanceof ImportedAccount)) {
                accs.put(account.dumpJSON());
            }
        }
        obj.put(KEY_PAYLOAD__ACCOUNTS, accs);

        return obj;
    }

}
