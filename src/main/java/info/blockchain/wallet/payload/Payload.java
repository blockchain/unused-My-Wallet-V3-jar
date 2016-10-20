package info.blockchain.wallet.payload;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.PayloadException;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.FormatsUtil;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Payload.java : java class for encapsulating Blockchain HD wallet payload
 *
 * <p>The Blockchain HD wallet payload is read from/written to the server in an encryptedPairingCode
 * JSON file
 *
 * <p>The Blockchain HD wallet payload format was previously fully documented on Basecamp but the
 * latest documentation there is out-of-date. This java class encapsulates the JSON format as most
 * recently updated by @Sjors and is subject to change.
 *
 * <p>Unused portions of the payload (either previously defined and unused, or previously used and
 * now deprecated) have been deliberately left in this code pending decisions concerning their use
 * in future versions of the Blockchain HD wallet as well as pending a full documentation of the
 * Blockchain HD wallet payload format. Such portions might be commented out in the other payload
 * member classes of this package.
 */
public class Payload implements PayloadJsonKeys, Serializable {

    private String guid = null;
    private String sharedKey = null;
    private String secondPasswordHash = null;
    private String decryptedPayload = null;
    private Options options = null;
    private boolean isDoubleEncrypted = false;
    private boolean isUpgraded = false;
    private List<LegacyAddress> legacyAddressList = null;
    private List<AddressBookEntry> addressBookEntryList = null;
    private List<HDWallet> hdWalletList = null;
    private Map<String, String> transactionNotesMap = null;
    private Map<String, List<Integer>> transactionTagsMap = null;
    private Map<Integer, String> tagNamesMap = null;
    private Map<String, PaidTo> paidToMap = null;

    //Maps used to find xpub index and visa versa
    private Map<String, Integer> xpubToAccountIndexMap = null;
    private Map<Integer, String> accountIndexToXpubMap = null;

    public Payload() {
        legacyAddressList = new ArrayList<LegacyAddress>();
        addressBookEntryList = new ArrayList<AddressBookEntry>();
        hdWalletList = new ArrayList<HDWallet>();
        transactionNotesMap = new HashMap<String, String>();
        transactionTagsMap = new HashMap<String, List<Integer>>();
        tagNamesMap = new HashMap<Integer, String>();
        paidToMap = new HashMap<String, PaidTo>();
        options = new Options();
        xpubToAccountIndexMap = new HashMap<String, Integer>();
        accountIndexToXpubMap = new HashMap<Integer, String>();
    }

    public Payload(String decryptedPayload, int pdfdf2Iterations) throws PayloadException {
        legacyAddressList = new ArrayList<LegacyAddress>();
        addressBookEntryList = new ArrayList<AddressBookEntry>();
        hdWalletList = new ArrayList<HDWallet>();
        transactionNotesMap = new HashMap<String, String>();
        transactionTagsMap = new HashMap<String, List<Integer>>();
        tagNamesMap = new HashMap<Integer, String>();
        paidToMap = new HashMap<String, PaidTo>();

        xpubToAccountIndexMap = new HashMap<String, Integer>();
        accountIndexToXpubMap = new HashMap<Integer, String>();

        options = new Options();
        options.setIterations(pdfdf2Iterations);

        this.decryptedPayload = decryptedPayload;

        parseWalletData(decryptedPayload, pdfdf2Iterations);
    }

    private void parseWalletData(final String decryptedPayload, int pdfdf2Iterations) throws PayloadException {

        if (FormatsUtil.getInstance().isValidJson(decryptedPayload)) {

            parsePayload(new JSONObject(decryptedPayload));

            // Default to wallet pbkdf2 iterations in case the double encryption pbkdf2 iterations is not set in wallet.json > options
            setDoubleEncryptionPbkdf2Iterations(pdfdf2Iterations);

        } else {
            //Iterations might be incorrect
            throw new PayloadException("Payload not valid json");
        }
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(String key) {
        this.sharedKey = key;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public List<LegacyAddress> getLegacyAddressList() {
        return legacyAddressList;
    }

    public List<LegacyAddress> getLegacyAddresses(long tag) {

        List<LegacyAddress> addrs = new ArrayList<LegacyAddress>();
        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.getTag() == tag) {
                addrs.add(legacyAddress);
            }
        }

        return addrs;
    }

    public List<LegacyAddress> getActiveLegacyAddresses() {
        List<LegacyAddress> addrs = new ArrayList<LegacyAddress>();

        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.getTag() == LegacyAddress.NORMAL_ADDRESS &&
                    !legacyAddress.isWatchOnly()) {
                addrs.add(legacyAddress);
            }
        }

        return addrs;
    }

    public List<String> getLegacyAddressStrings() {

        List<String> addrs = new ArrayList<String>();
        for (LegacyAddress legacyAddress : legacyAddressList) {
            addrs.add(legacyAddress.getAddress());
        }

        return addrs;
    }

    public List<String> getWatchOnlyAddressStrings() {

        List<String> addrs = new ArrayList<String>();
        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.isWatchOnly()) {
                addrs.add(legacyAddress.getAddress());
            }
        }

        return addrs;
    }

    public List<String> getLegacyAddressStrings(long tag) {

        List<String> addrs = new ArrayList<String>();
        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.getTag() == tag) {
                addrs.add(legacyAddress.getAddress());
            }
        }

        return addrs;
    }

    public List<String> getActiveLegacyAddressStrings() {
        List<String> addrs = new ArrayList<String>();

        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.getTag() == LegacyAddress.NORMAL_ADDRESS) {
                addrs.add(legacyAddress.getAddress());
            }
        }

        return addrs;
    }

    public void setLegacyAddressList(List<LegacyAddress> legacyAddressList) {
        this.legacyAddressList = legacyAddressList;
    }

    public boolean containsLegacyAddress(String addr) {

        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.getAddress().equals(addr)) {
                return true;
            }
        }

        return false;
    }

    public List<AddressBookEntry> getAddressBookEntryList() {
        return addressBookEntryList;
    }

    public void setAddressBookEntryList(List<AddressBookEntry> addressBookEntryList) {
        this.addressBookEntryList = addressBookEntryList;
    }

    public Map<String, String> getNotes() {
        return transactionNotesMap;
    }

    public void setNotes(Map<String, String> notes) {
        this.transactionNotesMap = notes;
    }

    public Map<String, List<Integer>> getTags() {
        return transactionTagsMap;
    }

    public void setTags(Map<String, List<Integer>> tags) {
        this.transactionTagsMap = tags;
    }

    public List<HDWallet> getHdWalletList() {
        return hdWalletList;
    }

    public void setHdWalletList(List<HDWallet> hdWalletList) {
        this.hdWalletList = hdWalletList;
    }

    public HDWallet getHdWallet() {
        return hdWalletList.size() > 0 ? hdWalletList.get(0) : null;
    }

    public void setHdWallets(HDWallet hdWallet) {
        this.hdWalletList.clear();
        this.hdWalletList.add(hdWallet);
    }

    public Map<Integer, String> getTagNamesMap() {
        return tagNamesMap;
    }

    public void setTagNamesMap(Map<Integer, String> tag_names) {
        this.tagNamesMap = tag_names;
    }

    public Map<String, PaidTo> getPaidToMap() {
        return paidToMap;
    }

    public void setPaidToMap(Map<String, PaidTo> paidToMap) {
        this.paidToMap = paidToMap;
    }

    public int getDoubleEncryptionPbkdf2Iterations() {
        return options.getIterations();
    }

    public void setDoubleEncryptionPbkdf2Iterations(int doubleEncryptionPbkdf2Iterations) {
        options.setIterations(doubleEncryptionPbkdf2Iterations);
    }

    public boolean isDoubleEncrypted() {
        return isDoubleEncrypted;
    }

    public void setDoubleEncrypted(boolean encrypted2) {
        this.isDoubleEncrypted = encrypted2;
    }

    public String getDoublePasswordHash() {
        return secondPasswordHash;
    }

    public void setDoublePasswordHash(String hash2) {
        this.secondPasswordHash = hash2;
    }

    /**
     * Parser for Blockchain HD JSON object.
     *
     * <p>Parses the JSONObject passed as an argument and populates the payload instance with all
     * payload data for legacy and HD parts of the wallet.
     *
     * @param payloadJson JSON object to be parsed
     */
    public void parsePayload(@Nonnull JSONObject payloadJson) throws PayloadException {

        guid = payloadJson.getString(KEY_PAYLOAD__GUID);

        if (!payloadJson.has(KEY_PAYLOAD__SHAREDKEY)) {
            throw new PayloadException("Payload contains no shared key!");
        }

        sharedKey = payloadJson.getString(KEY_PAYLOAD__SHAREDKEY);
        isDoubleEncrypted = payloadJson.has(KEY_PAYLOAD__DOUBLE_ENCRYPTION) ? payloadJson.getBoolean(KEY_PAYLOAD__DOUBLE_ENCRYPTION) : false;
        secondPasswordHash = payloadJson.has(KEY_PAYLOAD__DPASSWORDHASH) ? payloadJson.getString(KEY_PAYLOAD__DPASSWORDHASH) : "";

        //
        // "options" or "wallet_options" ?
        //
        JSONObject optionsJson = null;
        options = new Options();
        if (payloadJson.has(KEY_PAYLOAD__OPTION)) {
            optionsJson = (JSONObject) payloadJson.get(KEY_PAYLOAD__OPTION);
        }
        if (optionsJson == null && payloadJson.has(KEY_PAYLOAD__WALLET_OPTIONS)) {
            optionsJson = (JSONObject) payloadJson.get(KEY_PAYLOAD__WALLET_OPTIONS);
        }
        if (optionsJson != null) {
            options = new Options(optionsJson);
        }

        if (payloadJson.has(KEY_PAYLOAD__TX_NOTES)) {
            JSONObject txNotesJson = (JSONObject) payloadJson.get(KEY_PAYLOAD__TX_NOTES);
            transactionNotesMap = new HashMap<String, String>();

            for (Iterator keys = txNotesJson.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                String note = (String) txNotesJson.get(key);
                transactionNotesMap.put(key, note);
            }
        }

        if (payloadJson.has(KEY_PAYLOAD__TX_TAGS)) {
            JSONObject txTagsJson = (JSONObject) payloadJson.get(KEY_PAYLOAD__TX_TAGS);
            transactionTagsMap = new HashMap<String, List<Integer>>();
            for (Iterator keys = txTagsJson.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                JSONArray tagsJsonArray = (JSONArray) txTagsJson.get(key);
                List<Integer> tags = new ArrayList<Integer>();
                for (int i = 0; i < tagsJsonArray.length(); i++) {
                    long val = (Long) tagsJsonArray.get(i);
                    tags.add((int) val);
                }
                transactionTagsMap.put(key, tags);
            }
        }

        if (payloadJson.has(KEY_PAYLOAD__TAG_NAMES)) {
            JSONArray tagNamesJsonArray = (JSONArray) payloadJson.get(KEY_PAYLOAD__TAG_NAMES);
            tagNamesMap = new HashMap<Integer, String>();
            for (int i = 0; i < tagNamesJsonArray.length(); i++) {
                tagNamesMap.put(i, (String) tagNamesJsonArray.get(i));
            }
        }

        if (payloadJson.has(KEY_PAYLOAD__PAIDTO)) {
            JSONObject paidTo = (JSONObject) payloadJson.get(KEY_PAYLOAD__PAIDTO);
            paidToMap = new HashMap<String, PaidTo>();
            for (Iterator keys = paidTo.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                paidToMap.put(key, new PaidTo(paidTo.getJSONObject(key)));
            }
        }

        if (payloadJson.has(KEY_PAYLOAD__HD_WALLET)) {
            isUpgraded = true;

            JSONArray wallets = (JSONArray) payloadJson.get(KEY_PAYLOAD__HD_WALLET);
            JSONObject wallet = (JSONObject) wallets.get(0);
            HDWallet hdw = new HDWallet();

            if (wallet.has(KEY_HD_WALLET__SEED_HEX)) {
                hdw.setSeedHex((String) wallet.get(KEY_HD_WALLET__SEED_HEX));
            }
            if (wallet.has(KEY_HD_WALLET__PASSPHRASE)) {
                hdw.setPassphrase((String) wallet.get(KEY_HD_WALLET__PASSPHRASE));
            }
            if (wallet.has(KEY_HD_WALLET__MNEMONIC_VERIFIED)) {
                hdw.mnemonic_verified(wallet.getBoolean(KEY_HD_WALLET__MNEMONIC_VERIFIED));
            }
            if (wallet.has(KEY_HD_WALLET__DEFAULT_ACCOUNT_INDEX)) {
                int i;
                try {
                    String val = (String) wallet.get(KEY_HD_WALLET__DEFAULT_ACCOUNT_INDEX);
                    i = Integer.parseInt(val);
                } catch (java.lang.ClassCastException cce) {
                    i = (Integer) wallet.get(KEY_HD_WALLET__DEFAULT_ACCOUNT_INDEX);
                }
                hdw.setDefaultIndex(i);
            }

            if (((JSONObject) wallets.get(0)).has(KEY_PAYLOAD__ACCOUNTS)) {

                JSONArray accounts = (JSONArray) ((JSONObject) wallets.get(0)).get(KEY_PAYLOAD__ACCOUNTS);
                if (accounts != null && accounts.length() > 0) {
                    List<Account> walletAccounts = new ArrayList<Account>();
                    for (int i = 0; i < accounts.length(); i++) {

                        JSONObject accountJsonObj = accounts.getJSONObject(i);

                        Account account = null;
                        try {
                            account = new Account(accountJsonObj, i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        walletAccounts.add(account);

                        xpubToAccountIndexMap.put(account.getXpub(), i);
                        accountIndexToXpubMap.put(i, account.getXpub());
                    }

                    hdw.setAccounts(walletAccounts);

                }
            }

            hdWalletList.add(hdw);
        } else {
            isUpgraded = false;
        }

        if (payloadJson.has(KEY_PAYLOAD__LEGACY_KEYS)) {

            List<String> seenAddrs = new ArrayList<String>();
            JSONArray keys = payloadJson.getJSONArray(KEY_PAYLOAD__LEGACY_KEYS);

            if (keys != null && keys.length() > 0) {

                for (int i = 0; i < keys.length(); i++) {

                    LegacyAddress legacyAddress = new LegacyAddress(keys.getJSONObject(i));

                    if (!seenAddrs.contains(legacyAddress.getAddress())) {
                        legacyAddressList.add(legacyAddress);
                        seenAddrs.add(legacyAddress.getAddress());
                    }
                }
            }
        }

        if (payloadJson.has(KEY_PAYLOAD__ADDRESS_BOOK)) {

            JSONArray address_book = payloadJson.getJSONArray(KEY_PAYLOAD__ADDRESS_BOOK);

            if (address_book != null && address_book.length() > 0) {

                for (int i = 0; i < address_book.length(); i++) {

                    addressBookEntryList.add(new AddressBookEntry(address_book.getJSONObject(i)));
                }
            }
        }

    }

    public Map<String, Integer> getXpubToAccountIndexMap() {
        return xpubToAccountIndexMap;
    }

    public Map<Integer, String> getAccountIndexToXpubMap() {
        return accountIndexToXpubMap;
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    public void setUpgraded(boolean upgraded) {
        this.isUpgraded = upgraded;
    }

    /**
     * Returns this instance of payload and all dependant payload object instances in a single
     * JSONObject. The returned JSON object can be serialized and written to server.
     *
     * @return JSONObject
     */
    public JSONObject dumpJSON() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put(KEY_PAYLOAD__GUID, getGuid());
        obj.put(KEY_PAYLOAD__SHAREDKEY, getSharedKey());
        obj.put(KEY_PAYLOAD__PBKDF2_ITERATIONS, this.getDoubleEncryptionPbkdf2Iterations());

        if (isDoubleEncrypted) {
            obj.put(KEY_PAYLOAD__DOUBLE_ENCRYPTION, true);
            obj.put(KEY_PAYLOAD__DPASSWORDHASH, secondPasswordHash);
        }

        if (isUpgraded) {
            JSONArray wallets = new JSONArray();
            for (HDWallet wallet : hdWalletList) {
                wallets.put(wallet.dumpJSON());
            }
            obj.put(KEY_PAYLOAD__HD_WALLET, wallets);
        }

        JSONArray keys = new JSONArray();
        for (LegacyAddress addr : legacyAddressList) {
            JSONObject key = addr.dumpJSON();
            keys.put(key);
        }
        obj.put(KEY_PAYLOAD__LEGACY_KEYS, keys);

        JSONObject optionsObj = options.dumpJSON();
        obj.put(KEY_PAYLOAD__OPTION, optionsObj);

        JSONArray address_book = new JSONArray();
        for (AddressBookEntry addr : addressBookEntryList) {
            address_book.put(addr.dumpJSON());
        }
        obj.put(KEY_PAYLOAD__ADDRESS_BOOK, address_book);

        JSONObject notesObj = new JSONObject();
        Set<String> nkeys = transactionNotesMap.keySet();
        for (String key : nkeys) {
            notesObj.put(key, transactionNotesMap.get(key));
        }
        obj.put(KEY_PAYLOAD__TX_NOTES, notesObj);

        JSONObject tagsObj = new JSONObject();
        Set<String> tkeys = transactionTagsMap.keySet();
        for (String key : tkeys) {
            List<Integer> ints = transactionTagsMap.get(key);
            JSONArray tints = new JSONArray();
            for (Integer i : ints) {
                tints.put(i);
            }
            tagsObj.put(key, tints);
        }
        obj.put(KEY_PAYLOAD__TX_TAGS, tagsObj);

        JSONArray tnames = new JSONArray();
        Set<Integer> skeys = tagNamesMap.keySet();
        for (Integer key : skeys) {
            tnames.put(key, tagNamesMap.get(key));
        }
        obj.put(KEY_PAYLOAD__TAG_NAMES, tnames);

        JSONObject paidToObj = new JSONObject();
        Set<String> pkeys = paidToMap.keySet();
        for (String key : pkeys) {
            PaidTo pto = paidToMap.get(key);
            paidToObj.put(key, pto.dumpJSON());
        }
        obj.put(KEY_PAYLOAD__PAIDTO, paidToObj);

        return obj;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getDecryptedPayload() {
        return decryptedPayload;
    }

    public void setDecryptedPayload(String decryptedPayload) {
        this.decryptedPayload = decryptedPayload;
    }

    public Pair encryptPayload(String payloadCleartext, CharSequenceX password, int iterations, double version) throws Exception {

        String payloadEncrypted = AESUtil.encrypt(payloadCleartext, password, iterations);
        JSONObject rootObj = new JSONObject();
        rootObj.put(KEY_WALLET_VERSION, version);
        rootObj.put(KEY_WALLET_PBKDF2_ITERATIONS, iterations);
        rootObj.put(KEY_WALLET_PAYLOAD, payloadEncrypted);

        String checkSum = new String(Hex.encode(MessageDigest.getInstance("SHA-256").digest(rootObj.toString().getBytes("UTF-8"))));

        return Pair.of(checkSum, rootObj);
    }
}