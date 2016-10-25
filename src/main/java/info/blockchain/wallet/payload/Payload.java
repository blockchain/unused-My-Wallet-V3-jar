package info.blockchain.wallet.payload;

import info.blockchain.wallet.exceptions.PayloadException;
import info.blockchain.wallet.util.FormatsUtil;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
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
public class Payload implements Serializable {

    private final String KEY_GUID = "guid";
    private final String KEY_SHAREDKEY = "sharedKey";
    private final String KEY_DOUBLE_ENCRYPTION = "double_encryption";
    private final String KEY_DPASSWORDHASH = "dpasswordhash";
    private final String KEY_PBKDF2_ITERATIONS = "pbkdf2_iterations";
    private final String KEY_TX_NOTES = "tx_notes";
    private final String KEY_TX_TAGS = "tx_tags";
    private final String KEY_TAG_NAMES = "tag_names";
    private final String KEY_OPTION = "options";
    private final String KEY_WALLET_OPTIONS = "wallet_options";//some wallets might have this key in stead of 'options'
    private final String KEY_HD_WALLET = "hd_wallets";
    private final String KEY_LEGACY_KEYS = "keys";
    private final String KEY_ADDRESS_BOOK = "address_book";

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

    public Payload() {
        legacyAddressList = new ArrayList<LegacyAddress>();
        addressBookEntryList = new ArrayList<AddressBookEntry>();
        hdWalletList = new ArrayList<HDWallet>();
        transactionNotesMap = new HashMap<String, String>();
        transactionTagsMap = new HashMap<String, List<Integer>>();
        tagNamesMap = new HashMap<Integer, String>();
        options = new Options();
    }

    public Payload(String decryptedPayload, int pdfdf2Iterations) throws PayloadException {
        legacyAddressList = new ArrayList<LegacyAddress>();
        addressBookEntryList = new ArrayList<AddressBookEntry>();
        hdWalletList = new ArrayList<HDWallet>();
        transactionNotesMap = new HashMap<String, String>();
        transactionTagsMap = new HashMap<String, List<Integer>>();
        tagNamesMap = new HashMap<Integer, String>();
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

    public List<LegacyAddress> getActiveLegacyAddressList() {
        List<LegacyAddress> addrs = new ArrayList<LegacyAddress>();

        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.getTag() == LegacyAddress.NORMAL_ADDRESS &&
                    !legacyAddress.isWatchOnly()) {
                addrs.add(legacyAddress);
            }
        }

        return addrs;
    }

    public List<String> getLegacyAddressStringList() {

        List<String> addrs = new ArrayList<String>();
        for (LegacyAddress legacyAddress : legacyAddressList) {
            addrs.add(legacyAddress.getAddress());
        }

        return addrs;
    }

    public List<String> getWatchOnlyAddressStringList() {

        List<String> addrs = new ArrayList<String>();
        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.isWatchOnly()) {
                addrs.add(legacyAddress.getAddress());
            }
        }

        return addrs;
    }

    public List<String> getLegacyAddressStringList(long tag) {

        List<String> addrs = new ArrayList<String>();
        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.getTag() == tag) {
                addrs.add(legacyAddress.getAddress());
            }
        }

        return addrs;
    }

    public List<String> getActiveLegacyAddressStringList() {
        List<String> addrs = new ArrayList<String>();

        for (LegacyAddress legacyAddress : legacyAddressList) {
            if (legacyAddress.getTag() == LegacyAddress.NORMAL_ADDRESS &&
                    !legacyAddress.isWatchOnly()) {
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

    public Map<String, String> getTransactionNotesMap() {
        return transactionNotesMap;
    }

    public void setTransactionNotesMap(Map<String, String> notes) {
        this.transactionNotesMap = notes;
    }

    public Map<String, List<Integer>> getTransactionTagsMap() {
        return transactionTagsMap;
    }

    public void setTransactionTagsMap(Map<String, List<Integer>> tags) {
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

        guid = payloadJson.getString(KEY_GUID);

        if (!payloadJson.has(KEY_SHAREDKEY)) {
            throw new PayloadException("Payload contains no shared key!");
        }

        sharedKey = payloadJson.getString(KEY_SHAREDKEY);
        isDoubleEncrypted = payloadJson.has(KEY_DOUBLE_ENCRYPTION) ? payloadJson.getBoolean(KEY_DOUBLE_ENCRYPTION) : false;
        secondPasswordHash = payloadJson.has(KEY_DPASSWORDHASH) ? payloadJson.getString(KEY_DPASSWORDHASH) : "";

        //
        // "options" or "wallet_options" ?
        //
        JSONObject optionsJson = null;
        options = new Options();
        if (payloadJson.has(KEY_OPTION)) {
            optionsJson = payloadJson.getJSONObject(KEY_OPTION);
        }
        if (optionsJson == null && payloadJson.has(KEY_WALLET_OPTIONS)) {
            optionsJson = payloadJson.getJSONObject(KEY_WALLET_OPTIONS);
        }
        if (optionsJson != null) {
            options = Options.fromJson(optionsJson);
        }

        if (payloadJson.has(KEY_TX_NOTES)) {
            JSONObject txNotesJson = payloadJson.getJSONObject(KEY_TX_NOTES);
            transactionNotesMap = new HashMap<String, String>();

            for (Iterator keys = txNotesJson.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                String note = (String) txNotesJson.get(key);
                transactionNotesMap.put(key, note);
            }
        }

        if (payloadJson.has(KEY_TX_TAGS)) {
            JSONObject txTagsJson = payloadJson.getJSONObject(KEY_TX_TAGS);
            transactionTagsMap = new HashMap<String, List<Integer>>();
            for (Iterator keys = txTagsJson.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                JSONArray tagsJsonArray = txTagsJson.getJSONArray(key);
                List<Integer> tags = new ArrayList<Integer>();
                for (int i = 0; i < tagsJsonArray.length(); i++) {
                    long val = tagsJsonArray.getLong(i);
                    tags.add((int) val);
                }
                transactionTagsMap.put(key, tags);
            }
        }

        if (payloadJson.has(KEY_TAG_NAMES)) {
            JSONArray tagNamesJsonArray = payloadJson.getJSONArray(KEY_TAG_NAMES);
            tagNamesMap = new HashMap<Integer, String>();
            for (int i = 0; i < tagNamesJsonArray.length(); i++) {
                tagNamesMap.put(i, tagNamesJsonArray.getString(i));
            }
        }

        if (payloadJson.has(KEY_HD_WALLET)) {
            isUpgraded = true;

            //Json accommodates multiple wallets. Use single wallet untill further notice
            JSONArray walletJsonArray = payloadJson.getJSONArray(KEY_HD_WALLET);
            JSONObject walletJsonObject = walletJsonArray.getJSONObject(0);

            HDWallet hdw = HDWallet.fromJson(walletJsonObject);
            hdWalletList.add(hdw);

        } else {
            isUpgraded = false;
        }

        if (payloadJson.has(KEY_LEGACY_KEYS)) {

            List<String> seenAddrs = new ArrayList<String>();
            JSONArray keys = payloadJson.getJSONArray(KEY_LEGACY_KEYS);

            if (keys != null && keys.length() > 0) {

                for (int i = 0; i < keys.length(); i++) {

                    LegacyAddress legacyAddress = LegacyAddress.fromJson(keys.getJSONObject(i));

                    if (!seenAddrs.contains(legacyAddress.getAddress())) {
                        legacyAddressList.add(legacyAddress);
                        seenAddrs.add(legacyAddress.getAddress());
                    }
                }
            }
        }

        if (payloadJson.has(KEY_ADDRESS_BOOK)) {

            JSONArray address_book = payloadJson.getJSONArray(KEY_ADDRESS_BOOK);

            if (address_book != null && address_book.length() > 0) {

                for (int i = 0; i < address_book.length(); i++) {

                    addressBookEntryList.add(AddressBookEntry.fromJson(address_book.getJSONObject(i)));
                }
            }
        }

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
    public JSONObject toJson() throws Exception {

        JSONObject obj = new JSONObject();

        obj.put(KEY_GUID, getGuid());
        obj.put(KEY_SHAREDKEY, getSharedKey());
        obj.put(KEY_PBKDF2_ITERATIONS, this.getDoubleEncryptionPbkdf2Iterations());

        if (isDoubleEncrypted) {
            obj.put(KEY_DOUBLE_ENCRYPTION, true);
            obj.put(KEY_DPASSWORDHASH, secondPasswordHash);
        }

        if (isUpgraded) {
            JSONArray wallets = new JSONArray();
            for (HDWallet wallet : hdWalletList) {
                wallets.put(wallet.toJson());
            }
            obj.put(KEY_HD_WALLET, wallets);
        }

        JSONArray keys = new JSONArray();
        for (LegacyAddress addr : legacyAddressList) {
            JSONObject key = addr.toJson();
            keys.put(key);
        }
        obj.put(KEY_LEGACY_KEYS, keys);

        JSONObject optionsObj = options.toJson();
        obj.put(KEY_OPTION, optionsObj);

        JSONArray address_book = new JSONArray();
        for (AddressBookEntry addr : addressBookEntryList) {
            address_book.put(addr.toJson());
        }
        obj.put(KEY_ADDRESS_BOOK, address_book);

        JSONObject notesObj = new JSONObject();
        Set<String> nkeys = transactionNotesMap.keySet();
        for (String key : nkeys) {
            notesObj.put(key, transactionNotesMap.get(key));
        }
        obj.put(KEY_TX_NOTES, notesObj);

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
        obj.put(KEY_TX_TAGS, tagsObj);

        JSONArray tnames = new JSONArray();
        Set<Integer> skeys = tagNamesMap.keySet();
        for (Integer key : skeys) {
            tnames.put(key, tagNamesMap.get(key));
        }
        obj.put(KEY_TAG_NAMES, tnames);

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
}