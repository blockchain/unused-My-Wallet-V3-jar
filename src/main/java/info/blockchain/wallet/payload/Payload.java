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

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 *
 * Payload.java : java class for encapsulating Blockchain HD wallet payload
 *
 * <p>The Blockchain HD wallet payload is read from/written to the server in an encryptedPairingCode JSON file
 *
 * <p>The Blockchain HD wallet payload format was previously fully documented on Basecamp but the latest
 * documentation there is out-of-date. This java class encapsulates the JSON format as most recently
 * updated by @Sjors and is subject to change.
 *
 * <p>Unused portions of the payload (either previously defined and unused, or previously used and now deprecated)
 * have been deliberately left in this code pending decisions concerning their use in future versions of the
 * Blockchain HD wallet as well as pending a full documentation of the Blockchain HD wallet payload format.
 * Such portions might be commented out in the other payload member classes of this package.
 *
 */
public class Payload implements Serializable{

    private String strGuid = null;
    private String strSharedKey = null;
    private Options options = null;
    private boolean doubleEncryption = false;
    private String strDoublePWHash = null;
    private List<LegacyAddress> legacyAddresses = null;
    private List<AddressBookEntry> addressBookEntries = null;
    private List<HDWallet> hdWallets = null;
    private Map<String,String> notes = null;
    private Map<String,List<Integer>> tags = null;
    private Map<Integer,String> tag_names = null;
    private Map<String,PaidTo> paidTo = null;
    private Map<String,Integer> xpub2Account = null;
    private Map<Integer,String> account2Xpub = null;

    private String decryptedPayload = null;

    private boolean isUpgraded = false;

    public Payload() {
        legacyAddresses = new ArrayList<LegacyAddress>();
        addressBookEntries = new ArrayList<AddressBookEntry>();
        hdWallets = new ArrayList<HDWallet>();
        notes = new HashMap<String,String>();
        tags = new HashMap<String,List<Integer>>();
        tag_names = new HashMap<Integer,String>();
        paidTo = new HashMap<String,PaidTo>();
        options = new Options();
        xpub2Account = new HashMap<String,Integer>();
        account2Xpub = new HashMap<Integer,String>();
    }

    public Payload(String decryptedPayload, int pdfdf2Iterations) throws PayloadException{
        legacyAddresses = new ArrayList<LegacyAddress>();
        addressBookEntries = new ArrayList<AddressBookEntry>();
        hdWallets = new ArrayList<HDWallet>();
        notes = new HashMap<String,String>();
        tags = new HashMap<String,List<Integer>>();
        tag_names = new HashMap<Integer,String>();
        paidTo = new HashMap<String,PaidTo>();
        xpub2Account = new HashMap<String,Integer>();
        account2Xpub = new HashMap<Integer,String>();

        options = new Options();
        options.setIterations(pdfdf2Iterations);

        this.decryptedPayload = decryptedPayload;

        parseWalletData(decryptedPayload, pdfdf2Iterations);
    }

    private void parseWalletData(final String decryptedPayload, int pdfdf2Iterations) throws PayloadException {

        if(FormatsUtil.getInstance().isValidJson(decryptedPayload)){

            parsePayload(new JSONObject(decryptedPayload));

            // Default to wallet pbkdf2 iterations in case the double encryption pbkdf2 iterations is not set in wallet.json > options
            setDoubleEncryptionPbkdf2Iterations(pdfdf2Iterations);

        }else{
            //Iterations might be incorrect
            throw new PayloadException("Payload not valid json");
        }
    }

    public String getGuid() {
        return strGuid;
    }

    public void setGuid(String guid) {
        this.strGuid = guid;
    }

    public String getSharedKey() {
        return strSharedKey;
    }

    public void setSharedKey(String key) {
        this.strSharedKey = key;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public List<LegacyAddress> getLegacyAddresses() {
        return legacyAddresses;
    }

    public List<LegacyAddress> getLegacyAddresses(long tag) {

      List<LegacyAddress> addrs = new ArrayList<LegacyAddress>();
      for(LegacyAddress legacyAddress : legacyAddresses) {
          if(legacyAddress.getTag() == tag)  {
            addrs.add(legacyAddress);
          }
      }

        return addrs;
    }

    public List<LegacyAddress> getActiveLegacyAddresses() {
        List<LegacyAddress> addrs = new ArrayList<LegacyAddress>();

        for(LegacyAddress legacyAddress : legacyAddresses) {
            if(legacyAddress.getTag() == LegacyAddress.NORMAL_ADDRESS &&
               !legacyAddress.isWatchOnly()) {
                addrs.add(legacyAddress);
            }
        }

        return addrs;
    }

    public List<String> getLegacyAddressStrings() {

        List<String> addrs = new ArrayList<String>();
        for(LegacyAddress legacyAddress : legacyAddresses) {
            addrs.add(legacyAddress.getAddress());
        }

        return addrs;
    }

    public List<String> getWatchOnlyAddressStrings() {

        List<String> addrs = new ArrayList<String>();
        for(LegacyAddress legacyAddress : legacyAddresses) {
            if (legacyAddress.isWatchOnly()) {
                addrs.add(legacyAddress.getAddress());
            }
        }

        return addrs;
    }

    public List<String> getLegacyAddressStrings(long tag) {

        List<String> addrs = new ArrayList<String>();
        for(LegacyAddress legacyAddress : legacyAddresses) {
            if(legacyAddress.getTag() == tag)  {
              addrs.add(legacyAddress.getAddress());
            }
        }

        return addrs;
    }

    public List<String> getActiveLegacyAddressStrings() {
        List<String> addrs = new ArrayList<String>();

        for(LegacyAddress legacyAddress : legacyAddresses) {
            if(legacyAddress.getTag() == LegacyAddress.NORMAL_ADDRESS) {
              addrs.add(legacyAddress.getAddress());
            }
        }

        return addrs;
    }

    public void setLegacyAddresses(List<LegacyAddress> legacyAddresses) {
        this.legacyAddresses = legacyAddresses;
    }

    public boolean containsLegacyAddress(String addr) {

        for(LegacyAddress legacyAddress : legacyAddresses) {
            if(legacyAddress.getAddress().equals(addr)) {
                return true;
            }
        }

        return false;
    }

    public List<AddressBookEntry> getAddressBookEntries() { return addressBookEntries; }

    public void setAddressBookEntries(List<AddressBookEntry> addressBookEntries) { this.addressBookEntries = addressBookEntries; }

    public Map<String, String> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, String> notes) {
        this.notes = notes;
    }

    public Map<String, List<Integer>> getTags() {
        return tags;
    }

    public void setTags(Map<String, List<Integer>> tags) {
        this.tags = tags;
    }

    public List<HDWallet> getHdWallets() {
        return hdWallets;
    }

    public void setHdWallets(List<HDWallet> hdWallets) {
        this.hdWallets = hdWallets;
    }

    public HDWallet getHdWallet() {
        return hdWallets.size() > 0 ? hdWallets.get(0) : null;
    }

    public void setHdWallets(HDWallet hdWallet) { this.hdWallets.clear(); this.hdWallets.add(hdWallet); }

    public Map<Integer, String> getTagNames() {
        return tag_names;
    }

    public void setTagNames(Map<Integer, String> tag_names) {
        this.tag_names = tag_names;
    }

    public Map<String, PaidTo> getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(Map<String, PaidTo> paidTo) {
        this.paidTo = paidTo;
    }

    public int getDoubleEncryptionPbkdf2Iterations() {
        return options.getIterations();
    }

    public void setDoubleEncryptionPbkdf2Iterations(int doubleEncryptionPbkdf2Iterations) {
        options.setIterations(doubleEncryptionPbkdf2Iterations);
    }

    public boolean isDoubleEncrypted() {
        return doubleEncryption;
    }

    public void setDoubleEncrypted(boolean encrypted2) {
        this.doubleEncryption = encrypted2;
    }

    public String getDoublePasswordHash() {
        return strDoublePWHash;
    }

    public void setDoublePasswordHash(String hash2) {
        this.strDoublePWHash = hash2;
    }

    /**
     * Parser for Blockchain HD JSON object.
     *
     * <p>Parses the JSONObject passed as an argument and populates the payload instance
     * with all payload data for legacy and HD parts of the wallet.
     *
     * @param jsonObject JSON object to be parsed
     *
     */
    public void parsePayload(@Nonnull JSONObject jsonObject) throws PayloadException {

        strGuid = jsonObject.getString("guid");

        if (!jsonObject.has("sharedKey")) {
            throw new PayloadException("Payload contains no shared key!");
        }

        strSharedKey = jsonObject.getString("sharedKey");
        doubleEncryption = jsonObject.has("double_encryption") ? (Boolean)jsonObject.get("double_encryption") : false;
        strDoublePWHash = jsonObject.has("dpasswordhash") ? (String)jsonObject.get("dpasswordhash") : "";

        //
        // "options" or "wallet_options" ?
        //
        JSONObject optionsObj = null;
        options = new Options();
        if(jsonObject.has("options"))  {
            optionsObj = (JSONObject)jsonObject.get("options");
        }
        if(optionsObj == null && jsonObject.has("wallet_options"))  {
            optionsObj = (JSONObject)jsonObject.get("wallet_options");
        }
        if(optionsObj != null)  {
            if(optionsObj.has("pbkdf2_iterations"))  {
                int val = (Integer)optionsObj.get("pbkdf2_iterations");
                options.setIterations(val);
            }
            if(optionsObj.has("fee_per_kb"))  {
                long val = optionsObj.getLong("fee_per_kb");
                options.setFeePerKB(val);
            }
            if(optionsObj.has("logout_time"))  {
                long val = optionsObj.getLong("logout_time");
                options.setLogoutTime(val);
            }
            if(optionsObj.has("html5_notifications"))  {
                boolean val = optionsObj.getBoolean("html5_notifications");
                options.setHtml5Notifications(val);
            }
            if(optionsObj.has("additional_seeds"))  {
                JSONArray seeds = (JSONArray)optionsObj.get("additional_seeds");
                List<String> additionalSeeds = new ArrayList<String>();
                for(int i = 0; i < seeds.length(); i++)  {
                    additionalSeeds.add((String)seeds.get(i));
                }
                options.setAdditionalSeeds(additionalSeeds);
            }
        }

        if(jsonObject.has("tx_notes"))  {
            JSONObject tx_notes = (JSONObject)jsonObject.get("tx_notes");
            Map<String,String> notes = new HashMap<String,String>();
            for(Iterator<String> keys = tx_notes.keys(); keys.hasNext();)  {
                String key = keys.next();
                String note = (String)tx_notes.get(key);
                notes.put(key, note);
            }
            setNotes(notes);
        }

        if(jsonObject.has("tx_tags"))  {
            JSONObject tx_tags = (JSONObject)jsonObject.get("tx_tags");
            Map<String,List<Integer>> _tags = new HashMap<String,List<Integer>>();
            for(Iterator<String> keys = tx_tags.keys(); keys.hasNext();)  {
                String key = keys.next();
                JSONArray tagsObj = (JSONArray)tx_tags.get(key);
                List<Integer> tags = new ArrayList<Integer>();
                for(int i = 0; i < tagsObj.length(); i++)  {
                    long val = (Long)tagsObj.get(i);
                    tags.add((int)val);
                }
                _tags.put(key, tags);
            }
            setTags(_tags);
        }

        if(jsonObject.has("tag_names"))  {
            JSONArray tnames = (JSONArray)jsonObject.get("tag_names");
            Map<Integer,String> _tnames = new HashMap<Integer,String>();
            for(int i = 0; i < tnames.length(); i++)  {
                _tnames.put(i, (String)tnames.get(i));
            }
            setTagNames(_tnames);
        }

        if(jsonObject.has("paidTo"))  {
            JSONObject paid2 = (JSONObject)jsonObject.get("paidTo");
            Map<String,PaidTo> pto = new HashMap<String,PaidTo>();
            for(Iterator<String> keys = paid2.keys(); keys.hasNext();)  {
                String key = keys.next();
                PaidTo p = new PaidTo();
                JSONObject t = (JSONObject)paid2.get(key);
                p.setEmail(t.isNull("email") ? null : t.optString("email", null));
                p.setMobile(t.isNull("mobile") ? null : t.optString("mobile", null));
                p.setRedeemedAt(t.isNull("redeemedAt") ? null : (Integer)t.get("redeemedAt"));
                p.setAddress(t.isNull("address") ? null : t.optString("address", null));
                pto.put(key, p);
            }
            setPaidTo(pto);
        }

        if(jsonObject.has("hd_wallets"))  {
            isUpgraded = true;

            JSONArray wallets = (JSONArray)jsonObject.get("hd_wallets");
            JSONObject wallet = (JSONObject)wallets.get(0);
            HDWallet hdw = new HDWallet();

            if(wallet.has("seed_hex"))  {
                hdw.setSeedHex((String)wallet.get("seed_hex"));
            }
            if(wallet.has("passphrase"))  {
                hdw.setPassphrase((String)wallet.get("passphrase"));
            }
            if(wallet.has("mnemonic_verified"))  {
                hdw.mnemonic_verified(wallet.getBoolean("mnemonic_verified"));
            }
            if(wallet.has("default_account_idx"))  {
                int i = 0;
                try  {
                    String val = (String)wallet.get("default_account_idx");
                    i = Integer.parseInt(val);
                }
                catch(java.lang.ClassCastException cce)  {
                    i = (Integer)wallet.get("default_account_idx");
                }
                hdw.setDefaultIndex(i);
            }

            if(((JSONObject)wallets.get(0)).has("accounts"))  {

                JSONArray accounts = (JSONArray)((JSONObject)wallets.get(0)).get("accounts");
                if(accounts != null && accounts.length() > 0)  {
                    List<Account> walletAccounts = new ArrayList<Account>();
                    for(int i = 0; i < accounts.length(); i++)  {

                        JSONObject accountObj = (JSONObject)accounts.get(i);
                        Account account = new Account();
                        account.setRealIdx(i);
                        account.setArchived(accountObj.has("archived") ? (Boolean)accountObj.get("archived") : false);
                        if(accountObj.has("archived") && (Boolean)accountObj.get("archived"))  {
                            account.setArchived(true);
                        }
                        else  {
                            account.setArchived(false);
                        }
                        account.setLabel(accountObj.has("label") ? (String)accountObj.get("label") : "");
                        if(accountObj.has("xpub") && ((String)accountObj.get("xpub")) != null && ((String)accountObj.get("xpub")).length() > 0)  {
                            account.setXpub((String)accountObj.get("xpub"));
                            xpub2Account.put((String)accountObj.get("xpub"), i);
                            account2Xpub.put(i, (String)accountObj.get("xpub"));
                        }
                        else  {
                            continue;
                        }
                        if(accountObj.has("xpriv") && ((String)accountObj.get("xpriv")) != null && ((String)accountObj.get("xpriv")).length() > 0)  {
                            account.setXpriv((String)accountObj.get("xpriv"));
                        }
                        else  {
                            continue;
                        }

                        if(accountObj.has("receive_addresses"))  {
                            JSONArray receives = (JSONArray)accountObj.get("receive_addresses");
                            List<ReceiveAddress> receiveAddresses = new ArrayList<ReceiveAddress>();
                            for(int j = 0; j < receives.length(); j++)  {
                                JSONObject receiveObj = (JSONObject)receives.get(j);
                                ReceiveAddress receiveAddress = new ReceiveAddress();
                                if(receiveObj.has("index"))  {
                                    int val = (Integer)receiveObj.get("index");
                                    receiveAddress.setIndex(val);
                                }
                                receiveAddress.setLabel(receiveObj.has("label") ? (String)receiveObj.get("label") : "");
                                receiveAddress.setAmount(receiveObj.has("amount") ? (Long)receiveObj.getLong("amount") : 0L);
                                receiveAddress.setPaid(receiveObj.has("paid") ? (Long)receiveObj.getLong("paid") : 0L);
//                                    receiveAddress.setCancelled(receiveObj.has("cancelled") ? (Boolean)receiveObj.get("cancelled") : false);
//                                    receiveAddress.setComplete(receiveAddress.getPaid() >= receiveAddress.getAmount());
                                receiveAddresses.add(receiveAddress);
                            }
                            account.setReceiveAddresses(receiveAddresses);
                        }

                        if(accountObj.has("tags"))  {
                            JSONArray tags = (JSONArray)accountObj.get("tags");
                            if(tags != null && tags.length() > 0)  {
                                List<String> accountTags = new ArrayList<String>();
                                for(int j = 0; j < tags.length(); j++)  {
                                    accountTags.add((String)tags.get(j));
                                }
                                account.setTags(accountTags);
                            }
                        }

                        if(accountObj.has("address_labels"))  {
                            JSONArray labels = (JSONArray)accountObj.get("address_labels");
                            if(labels != null && labels.length() > 0)  {
                                TreeMap<Integer,String> addressLabels = new TreeMap<Integer,String>();
                                for(int j = 0; j < labels.length(); j++)  {
                                    JSONObject obj = labels.getJSONObject(j);
                                    addressLabels.put(obj.getInt("index"), obj.getString("label"));
                                }
                                account.setAddressLabels(addressLabels);
                            }
                        }

                        if(accountObj.has("cache"))  {

                            JSONObject cacheObj = (JSONObject)accountObj.get("cache");

                            Cache cache = new Cache();

                            if(cacheObj.has("receiveAccount"))  {
                                cache.setReceiveAccount((String)cacheObj.get("receiveAccount"));
                            }

                            if(cacheObj.has("changeAccount"))  {
                                cache.setChangeAccount((String)cacheObj.get("changeAccount"));
                            }

                            account.setCache(cache);

                        }

                        walletAccounts.add(account);
                    }

                    hdw.setAccounts(walletAccounts);

                }
            }

            hdWallets.add(hdw);
        }
        else    {
            isUpgraded = false;
        }

        if(jsonObject.has("keys"))  {
            JSONArray keys = (JSONArray)jsonObject.get("keys");
            if(keys != null && keys.length() > 0)  {
                List<String> seenAddrs = new ArrayList<String>();
                String addr = null;
                JSONObject key = null;
                LegacyAddress legacyAddress = null;
                for(int i = 0; i < keys.length(); i++)  {
                    key = (JSONObject)keys.get(i);

                    addr = (String)key.get("addr");

                    if(addr != null && !addr.equals("null") && !seenAddrs.contains(addr))  {

                        String priv = null;
                        long created_time = 0L;
                        String label = null;
                        long tag = 0L;
                        String created_device_name = null;
                        String created_device_version = null;
                        boolean watchOnly = false;

                        try {
                          if(key.has("priv"))  {
                            priv = key.getString("priv");
                          }
                          if(priv == null || priv.equals("null"))  {
                            priv = "";
                          }
                        }
                        catch(Exception e) {
                          priv = "";
                        }

                        if(priv.length() == 0)  {
                            watchOnly = true;
                        }

                        if(key.has("created_time"))  {
                            try {
                              created_time = key.getLong("created_time");
                            }
                            catch(Exception e) {
                              created_time = 0L;
                            }
                        }
                        else  {
                            created_time = 0L;
                        }

                        try {
                          if(key.has("label"))  {
                            label = key.getString("label");
                          }
                          if(label == null || label.equals("null"))  {
                            label = "";
                          }
                        }
                        catch(Exception e) {
                          label = "";
                        }

                        if(key.has("tag"))  {
                          try {
                            tag = key.getLong("tag");
                          }
                          catch(Exception e) {
                            tag = LegacyAddress.NORMAL_ADDRESS;
                          }
                        }
                        else  {
                            tag = LegacyAddress.NORMAL_ADDRESS;
                        }

                        if (watchOnly) {
                            //Watch-only addresses, although possible, should never be archived.
                            tag = LegacyAddress.NORMAL_ADDRESS;
                        }

                        try {
                          if(key.has("created_device_name"))  {
                            created_device_name = key.getString("created_device_name");
                          }
                          if(created_device_name == null || created_device_name.equals("null"))  {
                            created_device_name = "";
                          }
                        }
                        catch(Exception e) {
                          created_device_name = "";
                        }

                        try {
                          if(key.has("created_device_version"))  {
                            created_device_version = key.getString("created_device_version");
                          }
                          if(created_device_version == null || created_device_version.equals("null"))  {
                            created_device_version = "";
                          }
                        }
                        catch(Exception e) {
                          created_device_version = "";
                        }

                        legacyAddress = new LegacyAddress(priv, created_time, addr, label, tag, created_device_name, created_device_version, watchOnly);
                        legacyAddresses.add(legacyAddress);
                        seenAddrs.add(addr);

                    }
                }
            }
        }

        if(jsonObject.has("address_book"))  {

            JSONArray address_book = (JSONArray)jsonObject.get("address_book");

            if(address_book != null && address_book.length() > 0)  {
                JSONObject addr = null;
                AddressBookEntry addr_entry = null;
                for(int i = 0; i < address_book.length(); i++)  {
                    addr = (JSONObject)address_book.get(i);

                    addr_entry = new AddressBookEntry(
                            addr.has("addr") ? (String)addr.get("addr") : null,
                            addr.has("label") ? (String)addr.get("label") : null
                    );

                    addressBookEntries.add(addr_entry);
                }
            }
        }

    }

    public Map<String,Integer> getXpub2Account()    {
        return xpub2Account;
    }

    public Map<Integer,String> getAccount2Xpub()    {
        return account2Xpub;
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    public void setUpgraded(boolean upgraded) {
        this.isUpgraded = upgraded;
    }

    /**
     * Returns this instance of payload and all dependant payload object instances
     * in a single JSONObject. The returned JSON object can be serialized and
     * written to server.
     *
     * @return JSONObject
     *
     */
    public JSONObject dumpJSON() throws JSONException{

        JSONObject obj = new JSONObject();

        obj.put("guid", getGuid());
        obj.put("sharedKey", getSharedKey());
        obj.put("pbkdf2_iterations", this.getDoubleEncryptionPbkdf2Iterations());

        if(doubleEncryption) {
            obj.put("double_encryption", true);
            obj.put("dpasswordhash", strDoublePWHash);
        }

        if(isUpgraded) {
            JSONArray wallets = new JSONArray();
            for(HDWallet wallet : hdWallets) {
                wallets.put(wallet.dumpJSON());
            }
            obj.put("hd_wallets", wallets);
        }

        JSONArray keys = new JSONArray();
        for(LegacyAddress addr : legacyAddresses) {
            JSONObject key = addr.dumpJSON();
            keys.put(key);
        }
        obj.put("keys", keys);

        JSONObject optionsObj = (JSONObject)options.dumpJSON();
        obj.put("options", optionsObj);

        JSONArray address_book = new JSONArray();
        for(AddressBookEntry addr : addressBookEntries) {
            address_book.put(addr.dumpJSON());
        }
        obj.put("address_book", address_book);

        JSONObject notesObj = new JSONObject();
        Set<String> nkeys = notes.keySet();
        for(String key : nkeys)  {
            notesObj.put(key, notes.get(key));
        }
        obj.put("tx_notes", notesObj);

        JSONObject tagsObj = new JSONObject();
        Set<String> tkeys = tags.keySet();
        for(String key : tkeys)  {
            List<Integer> ints = tags.get(key);
            JSONArray tints = new JSONArray();
            for(Integer i : ints)  {
                tints.put(i);
            }
            tagsObj.put(key, tints);
        }
        obj.put("tx_tags", tagsObj);

        JSONArray tnames = new JSONArray();
        Set<Integer> skeys = tag_names.keySet();
        for(Integer key : skeys)  {
            tnames.put(key, tag_names.get(key));
        }
        obj.put("tag_names", tnames);

        JSONObject paidToObj = new JSONObject();
        Set<String> pkeys = paidTo.keySet();
        for(String key : pkeys)  {
            PaidTo pto = paidTo.get(key);
            JSONObject pobj = new JSONObject();
            pobj.put("email", pto.getEmail());
            pobj.put("mobile", pto.getMobile());
            pobj.put("redeemedAt", pto.getRedeemedAt());
            pobj.put("address", pto.getAddress());
            paidToObj.put(key, pobj);
        }
        obj.put("paidTo", paidToObj);

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

    public Pair encryptPayload(String payloadCleartext, CharSequenceX password, int iterations, double version) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String payloadEncrypted = AESUtil.encrypt(payloadCleartext, password, iterations);
        JSONObject rootObj = new JSONObject();
        rootObj.put("version", version);
        rootObj.put("pbkdf2_iterations", iterations);
        rootObj.put("payload", payloadEncrypted);

        String checkSum = new String(Hex.encode(MessageDigest.getInstance("SHA-256").digest(rootObj.toString().getBytes("UTF-8"))));

        return Pair.of(checkSum, rootObj);
    }
}
