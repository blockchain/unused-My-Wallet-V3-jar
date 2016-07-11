package info.blockchain.wallet.payload;

import info.blockchain.bip44.Address;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.multiaddr.MultiAddrFactory;
import info.blockchain.wallet.util.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * PayloadManager.java : singleton class for reading/writing/parsing Blockchain HD JSON payload
 *
 */
public class PayloadManager {

    public static final double SUPPORTED_ENCRYPTION_VERSION = 3.0;
    public static final long NORMAL_ADDRESS = 0L;
    public static final long ARCHIVED_ADDRESS = 2L;
    public static final int RECEIVE_CHAIN = 0;
    public static final int CHANGE_CHAIN = 0;

    private static PayloadManager instance = null;
    // active payload:
    private static Payload payload = null;
    // cached payload, compare to this payload to determine if changes have been made. Used to avoid needless remote saves to server
    private static String cached_payload = null;

    private static final int WalletDefaultPbkdf2Iterations = 5000;
    public static int WalletPbkdf2Iterations = WalletDefaultPbkdf2Iterations;

    private static CharSequenceX strTempPassword = null;
    private static CharSequenceX strTempDoubleEncryptPassword = null;
    private static String strCheckSum = null;
    private static boolean isNew = false;
    private static boolean syncPubKeys = true;
    private static String email = null;

    private static double version = 2.0;

    private static WalletFactory bip44WalletFactory;
    private static int defaultMnemonicLength = 12;//Default for bci wallets
    private static int defaultAccountSize = 1;//Default for bci wallets
    private static Wallet wallet;
    private static Wallet watchOnlyWallet;

    private PayloadManager() {
        ;
    }

    /**
     * Return instance for a payload factory.
     *
     * @return PayloadManager
     */
    public static PayloadManager getInstance() {

        if (instance == null) {
            instance = new PayloadManager();
            payload = new Payload();
            cached_payload = "";
            bip44WalletFactory = new WalletFactory();
        }

        return instance;
    }

    /**
     * Reset PayloadManager to null instance.
     */
    public void wipe() {
        instance = null;
    }

    public interface InitiatePayloadListener {
        void onInitSuccess();

        void onInitPairFail();

        void onInitCreateFail(String error);
    }

    /*
    Initiate payload after pairing or after PIN entered
     */
    public void initiatePayload(String sharedKey, String guid, CharSequenceX password, InitiatePayloadListener listener) throws MnemonicException.MnemonicWordException, DecoderException, IOException, AddressFormatException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicChecksumException {

        payload = getPayloadFromServer(guid, sharedKey, password);

        if (payload == null || payload.stepNumber != 9) {
            String error = "";
            if (payload != null) {
                error = error + " Failed at step: " + payload.stepNumber;
                if (payload.lastErrorMessage != null) {
                    error = error + " with message: " + payload.lastErrorMessage;
                }
            }
            if(listener != null)listener.onInitCreateFail(error);
        }

        if (payload.getJSON() == null && listener != null) {
            listener.onInitPairFail();
        }

        //bip44 wallet need to be kept in sync
        if (payload.getHdWallet() != null && payload.getHdWallet().getSeedHex() != null) {

            if (!payload.isDoubleEncrypted()) {

                wallet = bip44WalletFactory.restoreWallet(payload.getHdWallet().getSeedHex(),
                        payload.getHdWallet().getPassphrase(),
                        payload.getHdWallet().getAccounts().size());
            }else{
                watchOnlyWallet = new Wallet(MainNetParams.get(), getXPUBs(true));
            }
        }

        if(listener != null){
            listener.onInitSuccess();
        }
    }

    /**
     * Get temporary password for user. Read password from here rather than reprompting user.
     *
     * @return CharSequenceX
     */
    public CharSequenceX getTempPassword() {
        return strTempPassword;
    }

    /**
     * Set temporary password for user once it has been validated. Read password from here rather than reprompting user.
     *
     * @param temp_password Validated user password
     */
    public void setTempPassword(CharSequenceX temp_password) {
        strTempPassword = temp_password;
        clearCachedPayload();
    }

    /**
     * Get temporary double encrypt password for user. Read double encrypt password from here rather than reprompting user.
     *
     * @return CharSequenceX
     */
    public CharSequenceX getTempDoubleEncryptPassword() {
        return strTempDoubleEncryptPassword;
    }

    /**
     * Set temporary double encrypt password for user once it has been validated. Read double encrypt password from here rather than reprompting user.
     *
     * @param temp_password2 Validated user double encrypt password
     */
    public void setTempDoubleEncryptPassword(CharSequenceX temp_password2) {
        strTempDoubleEncryptPassword = temp_password2;
    }

    /**
     * Get checksum for this payload.
     *
     * @return String
     */
    public String getCheckSum() {
        return strCheckSum;
    }

    /**
     * Set checksum for this payload.
     *
     * @param checksum Checksum to be set for this payload
     */
    public void setCheckSum(String checksum) {
        this.strCheckSum = checksum;
    }

    /**
     * Check if this payload is for a new Blockchain account.
     *
     * @return boolean
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * Set if this payload is for a new Blockchain account.
     *
     * @param isNew
     */
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Remote get(). Get refreshed payload from server.
     *
     * @param guid      User's wallet 'guid'
     * @param sharedKey User's sharedKey value
     * @param password  User password
     * @return Payload
     */
    private Payload getPayloadFromServer(String guid, String sharedKey, CharSequenceX password) {

        Payload payload = null;
        String checksum = null;

        try {
            String response = WebUtil.getInstance().postURL(WebUtil.PAYLOAD_URL, "method=wallet.aes.json&guid=" + guid + "&sharedKey=" + sharedKey + "&format=json" + "&api_code=" + WebUtil.API_CODE);
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("payload_checksum")) {
                checksum = jsonObject.get("payload_checksum").toString();
            }

            if (jsonObject.has("payload")) {
                String encrypted_payload = null;
                JSONObject _jsonObject = null;
                try {
                    _jsonObject = new JSONObject((String) jsonObject.get("payload"));
                } catch (Exception e) {
                    _jsonObject = null;
                }
                if (_jsonObject != null && _jsonObject.has("payload")) {
                    if (_jsonObject.has("pbkdf2_iterations")) {
                        WalletPbkdf2Iterations = Integer.valueOf(_jsonObject.get("pbkdf2_iterations").toString());
                    }
                    if (_jsonObject.has("version")) {
                        version = Double.valueOf(_jsonObject.get("version").toString());
                    }
                    encrypted_payload = (String) _jsonObject.get("payload");
                } else {
                    if (jsonObject.has("pbkdf2_iterations")) {
                        WalletPbkdf2Iterations = Integer.valueOf(jsonObject.get("pbkdf2_iterations").toString());
                    }
                    if (jsonObject.has("version")) {
                        version = Double.valueOf(jsonObject.get("version").toString());
                    }
                    encrypted_payload = (String) jsonObject.get("payload");
                }

                String decrypted = null;
                try {
                    decrypted = AESUtil.decrypt(encrypted_payload, password, WalletPbkdf2Iterations);
                } catch (Exception e) {
                    payload.lastErrorMessage = e.getMessage();
                    e.printStackTrace();
                    return null;
                }
                if (decrypted == null) {
                    try {
                        // v1 wallet fixed PBKDF2 iterations at 10
                        decrypted = AESUtil.decrypt(encrypted_payload, password, 10);
                    } catch (Exception e) {
                        payload.lastErrorMessage = e.getMessage();
                        e.printStackTrace();
                        return null;
                    }
                    if (decrypted == null) {
                        payload.lastErrorMessage = "Empty after decrypt";
                        return null;
                    }
                }
                payload = new Payload(decrypted);
                if (payload.getJSON() == null) {
                    payload.lastErrorMessage = "Can't parse JSON";
                    return null;
                }

                // Default to wallet pbkdf2 iterations in case the double encryption pbkdf2 iterations is not set in wallet.json > options
                payload.setDoubleEncryptionPbkdf2Iterations(WalletPbkdf2Iterations);

                try {
                    payload.parseJSON();
                } catch (JSONException je) {
                    payload.lastErrorMessage = je.getMessage();
                    je.printStackTrace();
                    return null;
                }
            } else {
//                Log.i("PayloadManager", "jsonObject has no payload");
                return null;
            }
        } catch (JSONException e) {
            payload.lastErrorMessage = e.getMessage();
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            payload.lastErrorMessage = e.getMessage();
            e.printStackTrace();
            return null;
        }

        if (StringUtils.isNotEmpty(checksum)) {
            strCheckSum = checksum;
        }
        return payload;
    }

    /**
     * Local get(). Returns current payload from the client.
     *
     * @return Payload
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     * Local set(). Sets current payload on the client.
     *
     * @param p Payload to be assigned
     */
    public void setPayload(Payload p) {
        payload = p;
    }

    /**
     * Remote save of current client payload to server. Will not save if no change as compared to cached payload.
     *
     * @return boolean
     */
    public boolean savePayloadToServer() {

        if (payload == null) return false;

        String strOldCheckSum = strCheckSum;
        String payloadCleartext = null;

        StringBuilder args = new StringBuilder();
        try {

            if (cached_payload != null && cached_payload.equals(payload.dumpJSON().toString())) {
                return true;
            }

            payloadCleartext = payload.dumpJSON().toString();
            String payloadEncrypted = AESUtil.encrypt(payloadCleartext, new CharSequenceX(strTempPassword), WalletPbkdf2Iterations);
            JSONObject rootObj = new JSONObject();
            rootObj.put("version", payload.isUpgraded() ? 3.0 : 2.0);
            rootObj.put("pbkdf2_iterations", WalletPbkdf2Iterations);
            rootObj.put("payload", payloadEncrypted);

            strCheckSum = new String(Hex.encode(MessageDigest.getInstance("SHA-256").digest(rootObj.toString().getBytes("UTF-8"))));

            String method = isNew ? "insert" : "update";

            String urlEncodedPayload = URLEncoder.encode(rootObj.toString());

            args.append("guid=");
            args.append(URLEncoder.encode(payload.getGuid(), "utf-8"));
            args.append("&sharedKey=");
            args.append(URLEncoder.encode(payload.getSharedKey(), "utf-8"));
            args.append("&payload=");
            args.append(urlEncodedPayload);
            args.append("&method=");
            args.append(method);
            args.append("&length=");
            args.append(rootObj.toString().length());
            args.append("&checksum=");
            args.append(URLEncoder.encode(strCheckSum, "utf-8"));

        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return false;
        } catch (JSONException je) {
            je.printStackTrace();
            return false;
        }

        if (syncPubKeys) {
            args.append("&active=");

            String[] legacyAddrs = null;
            List<LegacyAddress> legacyAddresses = payload.getLegacyAddresses();
            List<String> addrs = new ArrayList<String>();
            for (LegacyAddress addr : legacyAddresses) {
                if (addr.getTag() == 0L) {
                    addrs.add(addr.getAddress());
                }
            }

            args.append(StringUtils.join(addrs.toArray(new String[addrs.size()]), "|"));
        }

        if (email != null && email.length() > 0) {
            try {
                args.append("&email=");
                args.append(URLEncoder.encode(email, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        args.append("&device=");
        args.append("android");

        if (strOldCheckSum != null && strOldCheckSum.length() > 0) {
            args.append("&old_checksum=");
            args.append(strOldCheckSum);
        }

        args.append("&api_code=" + WebUtil.API_CODE);

        try {
            String response = WebUtil.getInstance().postURL(WebUtil.PAYLOAD_URL, args.toString());
            isNew = false;
            if (response.contains("Wallet successfully synced")) {
                cachePayload();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Write to current client payload to cache.
     */
    public void cachePayload() {
        try {
            cached_payload = payload.dumpJSON().toString();
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    private void clearCachedPayload() {
        cached_payload = null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        PayloadManager.email = email;
    }

    public double getVersion() {
        return version;
    }


    public boolean setDoubleEncryptPassword(String password, boolean isHD) {

        if (DoubleEncryptionFactory.getInstance().validateSecondPassword(payload.getDoublePasswordHash(),
                payload.getSharedKey(),
                new CharSequenceX(password),
                payload.getDoubleEncryptionPbkdf2Iterations())) {

            if (isHD) {
                String encrypted_hex = payload.getHdWallet().getSeedHex();
                String decrypted_hex = DoubleEncryptionFactory.getInstance().decrypt(
                        encrypted_hex,
                        payload.getSharedKey(),
                        password,
                        payload.getDoubleEncryptionPbkdf2Iterations());

                try {
                    watchOnlyWallet = bip44WalletFactory.restoreWallet(decrypted_hex, "", payload.getHdWallet().getAccounts().size());
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                setTempDoubleEncryptPassword(new CharSequenceX(password));
            }

            return true;

        } else {
            return false;
        }
    }

    //TODO - inspect this
    public String[] getMnemonicForDoubleEncryptedWallet() {

        if (getTempDoubleEncryptPassword().toString().length() == 0) {
            return null;
        }

        // Decrypt seedHex (which is double encrypted in this case)
        String decrypted_hex = DoubleEncryptionFactory.getInstance().decrypt(
                payload.getHdWallet().getSeedHex(),
                payload.getSharedKey(),
                getTempDoubleEncryptPassword().toString(),
                payload.getDoubleEncryptionPbkdf2Iterations());

        String mnemonic = null;

        // Try to create a using the decrypted seed hex
        try {
            watchOnlyWallet = bip44WalletFactory.restoreWallet(decrypted_hex, "", 1);
            mnemonic = watchOnlyWallet.getMnemonic();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mnemonic != null && mnemonic.length() > 0) {

                return mnemonic.split("\\s+");

            } else {
                return null;
            }
        }
    }

    public Payload createHDWallet(String passphrase, String defaultAccountName) throws Exception {

        setTempPassword(new CharSequenceX(passphrase));
        wallet = bip44WalletFactory.newWallet(defaultMnemonicLength, passphrase, defaultAccountSize);

        payload = createBlockchainWallet(defaultAccountName, wallet);
        if(!savePayloadToServer()){
            //if save failed don't return payload
            payload = null;
        }

        return payload;
    }

    public Payload restoreHDWallet(String seed, String passphrase, String defaultAccountName) throws Exception {

        setTempPassword(new CharSequenceX(passphrase));
        wallet = bip44WalletFactory.restoreWallet(seed, passphrase, defaultAccountSize);

        payload = createBlockchainWallet(defaultAccountName, wallet);
        if(!savePayloadToServer()){
            //if save failed don't return payload
            payload = null;
        }

        return payload;
    }


    public interface UpgradePayloadListener{
        void onDoubleEncryptionPasswordError();
        void onUpgradeSuccess();
        void onUpgradeFail();
    }
    /*
    When called from Android - First apply PRNGFixes
     */
    public void upgradeV2PayloadToV3(CharSequenceX password, CharSequenceX secondPassword, boolean isNewlyCreated, String defaultAccountName, UpgradePayloadListener listener) throws Exception {

        if (payload.isDoubleEncrypted()) {
            if (StringUtils.isEmpty(secondPassword) || !DoubleEncryptionFactory.getInstance().validateSecondPassword(
                    payload.getDoublePasswordHash(),
                    payload.getSharedKey(),
                    new CharSequenceX(secondPassword),
                    payload.getOptions().getIterations())) {

                listener.onDoubleEncryptionPasswordError();
            }
        }
        //
        // create HD wallet and sync w/ payload
        //

        if (payload.getHdWallets() == null ||
                payload.getHdWallets().size() == 0) {

            String xpub = null;
            int attempts = 0;
            boolean no_tx = false;

            do {

                attempts++;

                wallet = bip44WalletFactory.newWallet(defaultMnemonicLength, "", defaultAccountSize);
                HDWallet hdw = new HDWallet();
                String seedHex = wallet.getSeedHex();
                if (!StringUtils.isEmpty(secondPassword)) {
                    seedHex = DoubleEncryptionFactory.getInstance().encrypt(
                            seedHex,
                            payload.getSharedKey(),
                            secondPassword.toString(),
                            payload.getDoubleEncryptionPbkdf2Iterations());
                }

                hdw.setSeedHex(seedHex);
                List<Account> accounts = new ArrayList<Account>();
                xpub = wallet.getAccount(0).xpubstr();
                if (isNewlyCreated) {
                    accounts.add(new Account());
                    accounts.get(0).setXpub(xpub);
                    String xpriv = wallet.getAccount(0).xprvstr();
                    if (!StringUtils.isEmpty(secondPassword)) {
                        xpriv = DoubleEncryptionFactory.getInstance().encrypt(
                                xpriv,
                                payload.getSharedKey(),
                                secondPassword.toString(),
                                payload.getDoubleEncryptionPbkdf2Iterations());
                    }
                    accounts.get(0).setXpriv(xpriv);
                }
                hdw.setAccounts(accounts);
                payload.setHdWallets(hdw);
                payload.setUpgraded(true);

                payload.getHdWallet().getAccounts().get(0).setLabel(defaultAccountName);

                try {
                    no_tx = (MultiAddrFactory.getInstance().getXpubTransactionCount(xpub) == 0L);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (!no_tx && attempts < 3);

            if (!no_tx && isNewlyCreated) {
                listener.onUpgradeFail();
            } else {
                if(!savePayloadToServer())
                    listener.onUpgradeFail();
            }
        }

        try {
            updateBalancesAndTransactions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Account> accounts = payload.getHdWallet().getAccounts();
        payload.getHdWallet().setAccounts(accounts);
        cachePayload();

        listener.onUpgradeSuccess();
    }

    public String getChangeAddress(int accountIndex) throws Exception {
        int changeIdx = payload.getHdWallet().getAccounts().get(accountIndex).getIdxChangeAddresses();

        if (!payload.isDoubleEncrypted()) {
            return wallet.getAccount(accountIndex).getChange().getAddressAt(changeIdx).getAddressString();
        } else {
            return watchOnlyWallet.getAccount(accountIndex).getChange().getAddressAt(changeIdx).getAddressString();
        }
    }

    public String getReceiveAddress(int accountIndex) {

        try {
            Address addr = null;
            int idx = payload.getHdWallet().getAccounts().get(accountIndex).getIdxReceiveAddresses();
            if (!payload.isDoubleEncrypted()) {
                addr = wallet.getAccount(accountIndex).getChain(RECEIVE_CHAIN).getAddressAt(idx);
            } else {
                addr = watchOnlyWallet.getAccount(accountIndex).getChain(RECEIVE_CHAIN).getAddressAt(idx);
            }

            ReceiveAddress receiveAddress = new ReceiveAddress(addr.getAddressString(), idx);
            return receiveAddress.getAddress();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ECKey getECKey(int accountIndex, String path) throws Exception {

        String[] s = path.split("/");
        Address hd_address = null;
        if (!payload.isDoubleEncrypted()) {
            hd_address = wallet.getAccount(accountIndex).getChain(Integer.parseInt(s[1])).getAddressAt(Integer.parseInt(s[2]));
        } else {
            hd_address = watchOnlyWallet.getAccount(accountIndex).getChain(Integer.parseInt(s[1])).getAddressAt(Integer.parseInt(s[2]));
        }
        return PrivateKeyFactory.getInstance().getKey(PrivateKeyFactory.WIF_COMPRESSED, hd_address.getPrivateKeyString());
    }

    public String getXpubFromAccountIndex(int accountIdx) {
        return payload.getHdWallet().getAccounts().get(accountIdx).getXpub();
    }

    public void updateBalancesAndTransactions() throws Exception {
        // TODO unify legacy and HD call to one API call
        // TODO getXpub must be called before getLegacy (unify should fix this)

        boolean isNotUpgraded = isNotUpgraded();

        // xPub balance
        if (!isNotUpgraded) {
            String[] xpubs = getXPUBs(false);
            if (xpubs.length > 0) {
                MultiAddrFactory.getInstance().refreshXPUBData(xpubs);
            }
            List<Account> accounts = payload.getHdWallet().getAccounts();
            for (Account a : accounts) {
                a.setIdxReceiveAddresses(MultiAddrFactory.getInstance().getHighestTxReceiveIdx(a.getXpub()) > a.getIdxReceiveAddresses() ?
                        MultiAddrFactory.getInstance().getHighestTxReceiveIdx(a.getXpub()) : a.getIdxReceiveAddresses());
                a.setIdxChangeAddresses(MultiAddrFactory.getInstance().getHighestTxChangeIdx(a.getXpub()) > a.getIdxChangeAddresses() ?
                        MultiAddrFactory.getInstance().getHighestTxChangeIdx(a.getXpub()) : a.getIdxChangeAddresses());
            }
        }

        // Balance for legacy addresses
        if (payload.getLegacyAddresses().size() > 0) {
            List<String> legacyAddresses = payload.getLegacyAddressStrings();
            String[] addresses = legacyAddresses.toArray(new String[legacyAddresses.size()]);
            MultiAddrFactory.getInstance().refreshLegacyAddressData(addresses, false);
        }
    }

    public boolean isNotUpgraded() {
        return payload != null && !payload.isUpgraded();
    }

    public String[] getXPUBs(boolean includeArchives) throws IOException, DecoderException, AddressFormatException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicChecksumException, MnemonicException.MnemonicWordException {

        ArrayList<String> xpubs = new ArrayList<String>();

        if (payload.getHdWallet() != null) {
            int nb_accounts = payload.getHdWallet().getAccounts().size();
            for (int i = 0; i < nb_accounts; i++) {
                boolean isArchived = payload.getHdWallet().getAccounts().get(i).isArchived();
                if (isArchived && !includeArchives) {
                    ;
                } else {
                    String s = payload.getHdWallet().getAccounts().get(i).getXpub();
                    if (s != null && s.length() > 0) {
                        xpubs.add(s);
                    }
                }
            }
        }

        return xpubs.toArray(new String[xpubs.size()]);
    }

    public interface AccountAddListener {
        void onAccountAddSuccess(Account account);

        void onAccountAddFail();

        void onPayloadSaveFail();
    }

    public void addAccount(String accountLabel, AccountAddListener listener) throws Exception {

        //Add account
        String xpub = null;
        String xpriv = null;

        if(!payload.isDoubleEncrypted()) {

            wallet.addAccount();

            xpub = wallet.getAccounts().get(wallet.getAccounts().size() - 1).xpubstr();
            xpriv = wallet.getAccounts().get(wallet.getAccounts().size() - 1).xprvstr();
        }
        else {

            CharSequenceX tempPassword = getTempDoubleEncryptPassword();
            String tempPasswordS = "";
            if (tempPassword != null)
                tempPasswordS = tempPassword.toString();

            String decrypted_hex = DoubleEncryptionFactory.getInstance().decrypt(
                    payload.getHdWallet().getSeedHex(),
                    payload.getSharedKey(),
                    tempPasswordS,
                    payload.getDoubleEncryptionPbkdf2Iterations());

            //Need to decrypt watch-only wallet before adding new xpub
            watchOnlyWallet = bip44WalletFactory.restoreWallet(decrypted_hex, "", payload.getHdWallet().getAccounts().size());
            watchOnlyWallet.addAccount();

            xpub = watchOnlyWallet.getAccounts().get(watchOnlyWallet.getAccounts().size() - 1).xpubstr();
            xpriv = watchOnlyWallet.getAccounts().get(watchOnlyWallet.getAccounts().size() - 1).xprvstr();
        }

        //Initialize newly created xpub's tx list and balance
        List<Tx> txs = new ArrayList<Tx>();
        MultiAddrFactory.getInstance().getXpubTxs().put(xpub, txs);
        MultiAddrFactory.getInstance().getXpubAmounts().put(xpub, 0L);

        //Get account list from payload (not in sync with wallet from WalletFactory)
        List<Account> accounts = payload.getHdWallet().getAccounts();

        //Create new account (label, xpub, xpriv)
        Account account = new Account(accountLabel);
        account.setXpub(xpub);
        if(!payload.isDoubleEncrypted()) {
            account.setXpriv(xpriv);
        }
        else {
            String encrypted_xpriv = DoubleEncryptionFactory.getInstance().encrypt(
                    xpriv,
                    payload.getSharedKey(),
                    getTempDoubleEncryptPassword().toString(),
                    payload.getDoubleEncryptionPbkdf2Iterations());
            account.setXpriv(encrypted_xpriv);
        }

        //Add new account to payload
        if(accounts.get(accounts.size() - 1) instanceof ImportedAccount) {
            accounts.add(accounts.size() - 1, account);
        }
        else {
            accounts.add(account);
        }
        payload.getHdWallet().setAccounts(accounts);

        //Save payload
        if (savePayloadToServer()) {
            if(listener != null)listener.onAccountAddSuccess(account);
        } else {
            if(listener != null)listener.onPayloadSaveFail();
        }

        //Reset 2nd pwd
        setTempDoubleEncryptPassword(new CharSequenceX(""));
    }

    /*
    Generate V2 legacy address
    When called from Android - First apply PRNGFixes
     */
    public ECKey newLegacyAddress() {

        String result = null;
        byte[] data = null;
        try {
            result = WebUtil.getInstance().getURL(WebUtil.EXTERNAL_ENTROPY_URL);
            if (!result.matches("^[A-Fa-f0-9]{64}$")) {
                return null;
            }
            data = Hex.decode(result);
        } catch (Exception e) {
            return null;
        }

        ECKey ecKey = null;
        if (data != null) {
            byte[] rdata = new byte[32];
            SecureRandom random = new SecureRandom();
            random.nextBytes(rdata);
            byte[] privbytes = Util.getInstance().xor(data, rdata);
            if (privbytes == null) {
                return null;
            }
            ecKey = ECKey.fromPrivate(privbytes, true);
            // erase all byte arrays:
            random.nextBytes(privbytes);
            random.nextBytes(rdata);
            random.nextBytes(data);
        } else {
            return null;
        }

        return ecKey;
    }

    private Payload createBlockchainWallet(String defaultAccountName, Wallet hdw) throws IOException, MnemonicException.MnemonicLengthException {

        String guid = UUID.randomUUID().toString();
        String sharedKey = UUID.randomUUID().toString();

        Payload payload = new Payload();
        payload.setGuid(guid);
        payload.setSharedKey(sharedKey);

        HDWallet payloadHDWallet = new HDWallet();
        payloadHDWallet.setSeedHex(hdw.getSeedHex());

        List<info.blockchain.bip44.Account> hdAccounts = hdw.getAccounts();
        List<info.blockchain.wallet.payload.Account> payloadAccounts = new ArrayList<Account>();
        for (int i = 0; i < hdAccounts.size(); i++) {
            info.blockchain.wallet.payload.Account account = new info.blockchain.wallet.payload.Account(defaultAccountName);

            String xpub = hdw.getAccounts().get(i).xpubstr();
            account.setXpub(xpub);
            String xpriv = hdw.getAccounts().get(i).xprvstr();
            account.setXpriv(xpriv);

            payloadAccounts.add(account);
        }
        payloadHDWallet.setAccounts(payloadAccounts);

        payload.setHdWallets(payloadHDWallet);

        payload.setUpgraded(true);
        setPayload(payload);
        setNew(true);

        return payload;
    }

    public String getHDSeed() throws IOException, MnemonicException.MnemonicLengthException {
        return wallet.getSeedHex();
    }

    public String getHDMnemonic() throws IOException, MnemonicException.MnemonicLengthException {
        return wallet.getMnemonic();
    }

    public String getHDPassphrase() throws IOException, MnemonicException.MnemonicLengthException {
        return wallet.getPassphrase();
    }
}
