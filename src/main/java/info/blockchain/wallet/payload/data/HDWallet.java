package info.blockchain.wallet.payload.data;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Deprecated
public class HDWallet {

    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_SEED_HEX = "seed_hex";
    private static final String KEY_PASSPHRASE = "passphrase";
    private static final String KEY_MNEMONIC_VERIFIED = "mnemonic_verified";
    private static final String KEY_DEFAULT_ACCOUNT_INDEX = "default_account_idx";

    private String strSeedHex = null;
    private List<Account> accounts = null;
    private String strPassphrase = "";
    private boolean mnemonic_verified = false;
    private int default_account_idx = 0;

    @Deprecated
    public HDWallet() {
        accounts = new ArrayList<Account>();
    }

    public HDWallet(String seed, List<Account> accounts, String passphrase) {
        this.strSeedHex = seed;
        this.accounts = accounts;
        this.strPassphrase = passphrase;
    }

    @Deprecated
    public static HDWallet fromJson(JSONObject walletJsonObject){
        HDWallet hdWallet = new HDWallet();

        if (walletJsonObject.has(KEY_SEED_HEX)) {
            hdWallet.setSeedHex(walletJsonObject.getString(KEY_SEED_HEX));
        }
        if (walletJsonObject.has(KEY_PASSPHRASE)) {
            hdWallet.setPassphrase(walletJsonObject.getString(KEY_PASSPHRASE));
        }
        if (walletJsonObject.has(KEY_MNEMONIC_VERIFIED)) {
            hdWallet.setMnemonicVerified(walletJsonObject.getBoolean(KEY_MNEMONIC_VERIFIED));
        }
        if (walletJsonObject.has(KEY_DEFAULT_ACCOUNT_INDEX)) {
            int i;
            try {
                String val = walletJsonObject.getString(KEY_DEFAULT_ACCOUNT_INDEX);
                i = Integer.parseInt(val);
            } catch (Exception cce) {
                i = walletJsonObject.getInt(KEY_DEFAULT_ACCOUNT_INDEX);
            }
            hdWallet.setDefaultIndex(i);
        }

        if (walletJsonObject.has(KEY_ACCOUNTS)) {

            JSONArray accountsJsonArray = walletJsonObject.getJSONArray(KEY_ACCOUNTS);
            if (accountsJsonArray != null && accountsJsonArray.length() > 0) {
                List<Account> walletAccounts = new ArrayList<Account>();
                for (int i = 0; i < accountsJsonArray.length(); i++) {

                    JSONObject accountJsonObj = accountsJsonArray.getJSONObject(i);

                    try {
                        Account account = Account.fromJson(accountJsonObj);
                        account.setRealIdx(i);
                        walletAccounts.add(account);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                hdWallet.setAccounts(walletAccounts);

            }
        }

        return hdWallet;
    }

    @Deprecated
    public String getSeedHex() {
        return strSeedHex;
    }

    @Deprecated
    public void setSeedHex(String seed) {
        this.strSeedHex = seed;
    }

    @Deprecated
    public List<Account> getAccounts() {
        return accounts;
    }

    @Deprecated
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    @Deprecated
    public String getPassphrase() {
        return strPassphrase;
    }

    @Deprecated
    public void setPassphrase(String passphrase) {
        this.strPassphrase = passphrase;
    }

    @Deprecated
    public boolean isMnemonicVerified() {
        return mnemonic_verified;
    }

    @Deprecated
    public void setMnemonicVerified(boolean verified) {
        this.mnemonic_verified = verified;
    }

    @Deprecated
    public int getDefaultIndex() {
        return default_account_idx;
    }

    @Deprecated
    public void setDefaultIndex(int idx) {
        this.default_account_idx = idx;
    }

    @Deprecated
    public JSONObject toJson() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put(KEY_SEED_HEX, strSeedHex == null ? JSONObject.NULL : strSeedHex);
        obj.put(KEY_PASSPHRASE, strPassphrase == null ? JSONObject.NULL : strPassphrase);
        obj.put(KEY_DEFAULT_ACCOUNT_INDEX, default_account_idx);
        obj.put(KEY_MNEMONIC_VERIFIED, mnemonic_verified);

        JSONArray accs = new JSONArray();
        if (accounts != null) {
            for (Account account : accounts) {
                if (!(account instanceof ImportedAccount)) {
                    accs.put(account.toJson());
                }
            }
        }
        obj.put(KEY_ACCOUNTS, accs);

        return obj;
    }

}
