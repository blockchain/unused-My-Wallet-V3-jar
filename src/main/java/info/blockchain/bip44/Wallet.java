package info.blockchain.bip44;

import com.google.common.base.Joiner;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Wallet.java : BIP44 wallet
 */
public class Wallet {

    private byte[] seed = null;
    private String strPassphrase = null;
    private List<String> wordList = null;

    private DeterministicKey dkKey = null;
    private DeterministicKey dkRoot = null;

    private ArrayList<Account> accounts = null;

    private String strPath = null;

    private NetworkParameters params = null;

    private Wallet() {
    }

    /**
     * Constructor for wallet.
     *
     * @param mc         mnemonic code object
     * @param seed       seed for this wallet
     * @param passphrase optional BIP39 passphrase
     * @param nbAccounts number of accounts to create
     */
    public Wallet(MnemonicCode mc, NetworkParameters params, byte[] seed, String passphrase, int nbAccounts) throws MnemonicException.MnemonicLengthException {

        this.params = params;
        this.seed = seed;
        strPassphrase = passphrase;

        wordList = mc.toMnemonic(seed);
        byte[] hd_seed = MnemonicCode.toSeed(wordList, strPassphrase);
        dkKey = HDKeyDerivation.createMasterPrivateKey(hd_seed);
        DeterministicKey dKey = HDKeyDerivation.deriveChildKey(dkKey, 44 | ChildNumber.HARDENED_BIT);
        dkRoot = HDKeyDerivation.deriveChildKey(dKey, ChildNumber.HARDENED_BIT);

        accounts = new ArrayList<Account>();
        for (int i = 0; i < nbAccounts; i++) {
            accounts.add(new Account(params, dkRoot, i));
        }

        strPath = dKey.getPathAsString();
    }

    public Wallet(JSONObject jsonobj, NetworkParameters params, Locale locale) throws DecoderException, JSONException, IOException, MnemonicException.MnemonicLengthException {

        this.params = params;
        seed = Hex.decodeHex(((String) jsonobj.get("hex_seed")).toCharArray());
        strPassphrase = jsonobj.getString("passphrase");
        int nbAccounts = jsonobj.getJSONArray("accounts").length();

        InputStream wis = this.getClass()
                .getClassLoader()
                .getResourceAsStream("wordlist/" + locale.toString() + ".txt");
        MnemonicCode mc = null;
        if (wis != null) {
            mc = new MnemonicCode(wis, WalletFactory.BIP39_ENGLISH_SHA256);
            wis.close();
        }

        assert mc != null;
        wordList = mc.toMnemonic(seed);
        byte[] hd_seed = MnemonicCode.toSeed(wordList, strPassphrase);
        dkKey = HDKeyDerivation.createMasterPrivateKey(hd_seed);
        DeterministicKey dKey = HDKeyDerivation.deriveChildKey(dkKey, 44 | ChildNumber.HARDENED_BIT);
        dkRoot = HDKeyDerivation.deriveChildKey(dKey, ChildNumber.HARDENED_BIT);

        accounts = new ArrayList<Account>();
        for (int i = 0; i < nbAccounts; i++) {
            accounts.add(new Account(params, dkRoot, i));
        }

        strPath = dKey.getPathAsString();
    }

    /**
     * Constructor for watch-only wallet initialized from submitted XPUB(s).
     *
     * @param xpub array of XPUB strings
     */
    public Wallet(NetworkParameters params, String[] xpub) throws AddressFormatException {

        this.params = params;
        accounts = new ArrayList<Account>();
        for (int i = 0; i < xpub.length; i++) {
            accounts.add(new Account(params, xpub[i], i));
        }

    }

    /**
     * Return wallet seed as byte array.
     *
     * @return byte[]
     */
    public byte[] getSeed() {
        return seed;
    }

    /**
     * Return wallet seed as hex string.
     *
     * @return String
     */
    public String getSeedHex() {
        return new String(Hex.encodeHex(seed));
    }

    /**
     * Return wallet BIP39 mnemonic as string containing space separated words.
     *
     * @return String
     */
    public String getMnemonic() {
        return Joiner.on(" ").join(wordList);
    }

    /**
     * Return wallet BIP39 passphrase.
     *
     * @return String
     */
    public String getPassphrase() {
        return strPassphrase;
    }

    /**
     * Return accounts for this wallet.
     *
     * @return List<Account>
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Return account for submitted account id.
     *
     * @return Account
     */
    public Account getAccount(int accountId) {
        return accounts.get(accountId);
    }

    /**
     * Add new account.
     */
    public void addAccount() {
        accounts.add(new Account(params, dkRoot, accounts.size()));
    }

    /**
     * Return BIP44 path for this wallet (m / purpose').
     *
     * @return String
     */
    public String getPath() {
        return strPath;
    }


    public DeterministicKey getMasterKey() {
        return dkKey;
    }

    /**
     * Write entire wallet to JSONObject. For debugging only.
     *
     * @return JSONObject
     */
    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();

            if (seed != null) {
                obj.put("hex_seed", Hex.encodeHexString(seed));
                obj.put("passphrase", strPassphrase);

                JSONArray words = new JSONArray();
                for (String w : wordList) {
                    words.put(w);
                }
                obj.put("mnemonic", words);
            }

            JSONArray accts = new JSONArray();
            for (Account acct : accounts) {
                accts.put(acct.toJSON());
            }
            obj.put("accounts", accts);

            obj.put("path", getPath());

            return obj;
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

}