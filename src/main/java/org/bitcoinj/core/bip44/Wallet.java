package org.bitcoinj.core.bip44;

import com.google.common.base.Joiner;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.ChildNumber;
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

public class Wallet {

    private byte[] seed = null;
    private String strPassphrase = null;
    private List<String> wordList = null;

    private DeterministicKey dkKey = null;
    private DeterministicKey dkRoot = null;

    private ArrayList<Account> accounts = null;

    private String strPath = null;

    private NetworkParameters params = null;

    private Wallet() { ; }

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
        for(int i = 0; i < nbAccounts; i++) {
            accounts.add(new Account(params, dkRoot, i));
        }

        strPath = dKey.getPathAsString();
    }

    public Wallet(JSONObject jsonobj, NetworkParameters params) throws DecoderException, JSONException, IOException, MnemonicException.MnemonicLengthException {

        this.params = params;
        seed = Hex.decodeHex(((String)jsonobj.get("hex_seed")).toCharArray());
        strPassphrase = (String)jsonobj.getString("passphrase");
        int nbAccounts = jsonobj.getJSONArray("accounts").length();

        InputStream wis = Main.class.getResourceAsStream("/wordlist/english.txt");
        MnemonicCode mc = null;
        if(wis != null) {
            mc = new MnemonicCode(wis, WalletFactory.BIP39_ENGLISH_SHA256);
            wis.close();
        }

        wordList = mc.toMnemonic(seed);
        byte[] hd_seed = MnemonicCode.toSeed(wordList, strPassphrase);
        dkKey = HDKeyDerivation.createMasterPrivateKey(hd_seed);
        DeterministicKey dKey = HDKeyDerivation.deriveChildKey(dkKey, 44 | ChildNumber.HARDENED_BIT);
        dkRoot = HDKeyDerivation.deriveChildKey(dKey, ChildNumber.HARDENED_BIT);

        accounts = new ArrayList<Account>();
        for(int i = 0; i < nbAccounts; i++) {
            accounts.add(new Account(params, dkRoot, i));
        }

        strPath = dKey.getPathAsString();
    }

    /*
    create from account xpub key(s)
     */
    public Wallet(NetworkParameters params, String[] xpub) throws AddressFormatException {

        this.params = params;
        DeterministicKey aKey = null;
        accounts = new ArrayList<Account>();
        for(int i = 0; i < xpub.length; i++) {
            accounts.add(new Account(params, xpub[i], i));
        }

    }

    public byte[] getSeed() {
        return seed;
    }

    public String getSeedHex() {
        return Hex.encodeHexString(seed);
    }

    public String getMnemonic() {
        return Joiner.on(" ").join(wordList);
    }

    public String getPassphrase() {
        return strPassphrase;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Account getAccount(int accountId) {
        return accounts.get(accountId);
    }

    public void addAccount() {
        accounts.add(new Account(params, dkRoot, accounts.size()));
    }

    public void addAccount(String label) {

        if(label == null) {
            addAccount();
        }
        else {
            accounts.add(new Account(params, dkRoot, accounts.size()));
        }

    }

    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();

            if(seed != null) {
                obj.put("hex_seed", Hex.encodeHexString(seed));
                obj.put("passphrase", strPassphrase);

                JSONArray words = new JSONArray();
                for(String w : wordList) {
                    words.put(w);
                }
                obj.put("mnemonic", words);
            }

            JSONArray accts = new JSONArray();
            for(Account acct : accounts) {
                accts.put(acct.toJSON());
            }
            obj.put("accounts", accts);

            obj.put("path", strPath);

            return obj;
        }
        catch(JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

}
