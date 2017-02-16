package info.blockchain.wallet.payload.data2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.Balance;
import info.blockchain.api.data.UnspentOutput;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.Address;
import info.blockchain.wallet.bip44.HDAccount;
import info.blockchain.wallet.bip44.HDWallet;
import info.blockchain.wallet.bip44.HDWalletFactory;
import info.blockchain.wallet.bip44.HDWalletFactory.Language;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.EncryptionException;
import info.blockchain.wallet.exceptions.NoSuchAddressException;
import info.blockchain.wallet.exceptions.PayloadException;
import info.blockchain.wallet.payment.PaymentBundle;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import info.blockchain.wallet.util.FormatsUtil;
import info.blockchain.wallet.util.PrivateKeyFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.spongycastle.crypto.InvalidCipherTextException;
import retrofit2.Response;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class WalletBody {

    private static final int DEFAULT_MNEMONIC_LENGTH = 12;
    private static final int DEFAULT_NEW_WALLET_SIZE = 1;
    private static final String DEFAULT_PASSPHRASE = "";

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("sharedKey")
    private String sharedKey;

    @JsonProperty("double_encryption")
    private boolean doubleEncryption;

    @JsonProperty("dpasswordhash")
    private String dpasswordhash;

    @JsonProperty("tx_notes")
    private Map<String, String> txNotes;

    @JsonProperty("tx_tags")
    private Map<String, List<Integer>> txTags;

    @JsonProperty("tag_names")
    private Map<Integer, String> tagNames;

    @JsonProperty("options")
    private OptionsBody options;

    @JsonProperty("wallet_options")
    private OptionsBody walletOptions;

    @JsonProperty("hd_wallets")
    private List<HDWalletBody> hdWallets;

    @JsonProperty("keys")
    private List<LegacyAddressBody> keys;

    @JsonProperty("address_book")
    private List<AddressBookBody> addressBook;

    //Have to handle HD here.
    //WalletBody contains doubleEncryption hash to to handle encrypt/decrypt
    //for HD and legacy.
    private HDWallet HD;

    public WalletBody() {
        //Empty constructor needed for Jackson
    }

    public WalletBody(String defaultAccountName) throws IOException, MnemonicLengthException {

        guid = UUID.randomUUID().toString();
        sharedKey = UUID.randomUUID().toString();
        txNotes = new HashMap<>();
        keys = new ArrayList<>();
        options = OptionsBody.getDefaultOptions();

        //Bip44
        this.HD = HDWalletFactory
            .createWallet(PersistentUrls.getInstance().getCurrentNetworkParams(), Language.US,
                DEFAULT_MNEMONIC_LENGTH, DEFAULT_PASSPHRASE, DEFAULT_NEW_WALLET_SIZE);

        HDWalletBody hdWalletBody = new HDWalletBody();

        List<HDAccount> hdAccounts = this.HD.getAccounts();
        List<AccountBody> accountBodyList = new ArrayList<>();
        int accountNumber = 1;
        for (int i = 0; i < hdAccounts.size(); i++) {

            String label = defaultAccountName;
            if (accountNumber > 1) {
                label = defaultAccountName + " " + accountNumber;
            }

            AccountBody accountBody = new AccountBody();
            accountBody.setLabel(label);
            accountBody.setXpriv(this.HD.getAccount(0).getXPriv());
            accountBody.setXpub(this.HD.getAccount(0).getXpub());
            accountBodyList.add(accountBody);

            accountNumber++;
        }

        hdWalletBody.setSeedHex(this.HD.getSeedHex());
        hdWalletBody.setDefaultAccountIdx(0);
        hdWalletBody.setMnemonicVerified(false);
        hdWalletBody.setPassphrase(DEFAULT_PASSPHRASE);
        hdWalletBody.setAccounts(accountBodyList);

        hdWallets = new ArrayList<>();
        hdWallets.add(hdWalletBody);
    }

    public String getGuid() {
        return guid;
    }

    public String getSharedKey() {
        return sharedKey;
    }

    public boolean isDoubleEncryption() {
        return doubleEncryption;
    }

    public String getDpasswordhash() {
        return dpasswordhash;
    }

    public Map<String, String> getTxNotes() {
        return txNotes;
    }

    public Map<String, List<Integer>> getTxTags() {
        return txTags;
    }

    public Map<Integer, String> getTagNames() {
        return tagNames;
    }

    private int fixPbkdf2Iterations() {

        //Use default initially
        int iterations = WalletWrapperBody.DEFAULT_PBKDF2_ITERATIONS_V2;

        //Old wallets may contain 'wallet_options' key - we'll use this now
        if (walletOptions != null && walletOptions.getPbkdf2Iterations() > 0) {
            iterations = walletOptions.getPbkdf2Iterations();
        }

        //'options' key override wallet_options key - we'll use this now
        if (options != null && options.getPbkdf2Iterations() > 0) {
            iterations = options.getPbkdf2Iterations();
        }

        //If wallet doesn't contain 'option' - use default
        if(options == null) {
            options = OptionsBody.getDefaultOptions();
        }

        //Set iterations
        options.setPbkdf2Iterations(iterations);

        return iterations;
    }

    public OptionsBody getOptions() {
        fixPbkdf2Iterations();
        return options;
    }

    public OptionsBody getWalletOptions() {
        return walletOptions;
    }

    /*
    Currently Bci wallet only handles 1 wallet in payload.
     */
    public HDWalletBody getHdWallet() {
        return hdWallets.get(0);
    }

    public List<HDWalletBody> getHdWallets() {
        return hdWallets;
    }

    public List<LegacyAddressBody> getLegacyAddressList() {
        return keys;
    }

    public List<String> getLegacyAddressStringList() {

        List<String> addrs = new ArrayList<>();
        for (LegacyAddressBody legacyAddress : keys) {
            addrs.add(legacyAddress.getAddressString());
        }

        return addrs;
    }

    public List<String> getWatchOnlyAddressStringList() {

        List<String> addrs = new ArrayList<>();
        for (LegacyAddressBody legacyAddress : keys) {
            if (legacyAddress.isWatchOnly()) {
                addrs.add(legacyAddress.getAddressString());
            }
        }

        return addrs;
    }

    public List<String> getLegacyAddressStringList(long tag) {

        List<String> addrs = new ArrayList<>();
        for (LegacyAddressBody legacyAddress : keys) {
            if (legacyAddress.getTag() == tag) {
                addrs.add(legacyAddress.getAddressString());
            }
        }

        return addrs;
    }

    public List<LegacyAddressBody> getLegacyAddressList(long tag) {

        List<LegacyAddressBody> addrs = new ArrayList<>();
        for (LegacyAddressBody legacyAddress : keys) {
            if (legacyAddress.getTag() == tag) {
                addrs.add(legacyAddress);
            }
        }

        return addrs;
    }

    public boolean containsLegacyAddress(String addr) {

        for (LegacyAddressBody legacyAddress : keys) {
            if (legacyAddress.getAddressString().equals(addr)) {
                return true;
            }
        }

        return false;
    }

    public List<AddressBookBody> getAddressBook() {
        return addressBook;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public void setDoubleEncryption(boolean doubleEncryption) {
        this.doubleEncryption = doubleEncryption;
    }

    public void setDpasswordhash(String dpasswordhash) {
        this.dpasswordhash = dpasswordhash;
    }

    public void setTxNotes(Map<String, String> txNotes) {
        this.txNotes = txNotes;
    }

    public void setTxTags(Map<String, List<Integer>> txTags) {
        this.txTags = txTags;
    }

    public void setTagNames(Map<Integer, String> tagNames) {
        this.tagNames = tagNames;
    }

    public void setOptions(OptionsBody options) {
        this.options = options;
    }

    public void setWalletOptions(OptionsBody walletOptions) {
        this.walletOptions = walletOptions;
    }

    public void setHdWallets(List<HDWalletBody> hdWallets) {
        this.hdWallets = hdWallets;
    }

    public void setLegacyAddressList(List<LegacyAddressBody> keys) {
        this.keys = keys;
    }

    public void setAddressBook(List<AddressBookBody> addressBook) {
        this.addressBook = addressBook;
    }

    public boolean isUpgraded() {
        return (hdWallets != null);
    }

    public static WalletBody fromJson(String json)
        throws IOException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException,
        DecoderException, InvalidCipherTextException, DecryptionException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        WalletBody walletBody = mapper.readValue(json, WalletBody.class);
        walletBody.initHD();
        return walletBody;
    }

    private void initHD()
        throws DecryptionException, DecoderException, MnemonicWordException, MnemonicChecksumException,
        MnemonicLengthException, InvalidCipherTextException, IOException {

        //V1 won't have hdWallets
        if(hdWallets != null){
            if(isDoubleEncryption()) {
                ArrayList<String> xpubList = new ArrayList<>();
                for(AccountBody account : getHdWallet().getAccounts()) {
                    xpubList.add(account.getXpub());
                }

                //Wallet still double encrypted at this point
                //pass xpubs to give watch only wallet
                HD = HDWalletFactory
                    .restoreWatchOnlyWallet(PersistentUrls.getInstance().getCurrentNetworkParams(),
                        xpubList);
            } else {
                HD = HDWalletFactory
                    .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(), Language.US,
                        getHdWallet().getSeedHex(), getHdWallet().getPassphrase(), DEFAULT_NEW_WALLET_SIZE);
            }
        }
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public static WalletBody recoverFromMnemonic(String mnemonic, String defaultAccountName)
        throws Exception {
        return recoverFromMnemonic(mnemonic, "", defaultAccountName, 0);
    }

    public static WalletBody recoverFromMnemonic(String mnemonic, String defaultAccountName,
        int accountSize) throws Exception {
        return recoverFromMnemonic(mnemonic, "", defaultAccountName, accountSize);
    }

    public static WalletBody recoverFromMnemonic(String mnemonic, String passphrase,
        String defaultAccountName) throws Exception {
        return recoverFromMnemonic(mnemonic, passphrase, defaultAccountName, 0);
    }

    public static WalletBody recoverFromMnemonic(String mnemonic, String passphrase,
        String defaultAccountName, int accountSize) throws Exception {

//        HDWalletBody hdWallet = HDWalletFactory2
//            .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(), Language.US,
//                mnemonic, passphrase, DEFAULT_NEW_WALLET_SIZE, defaultAccountName);
//
//        BlockExplorer blockExplorer = new BlockExplorer(
//            BlockchainFramework.getRetrofitServerInstance(),
//            BlockchainFramework.getApiCode());
//
//        int walletSize = 1;
//        if (accountSize <= 0) {
//            int index = 0;
//
//            final int lookAheadTotal = 10;
//            int lookAhead = lookAheadTotal;
//
//            while (lookAhead > 0) {
//
//                String xpub = hdWallet.getAccount(index).getXpub();
//                if (hasTransactions(blockExplorer, xpub)) {
//                    lookAhead = lookAheadTotal;
//                    walletSize++;
//                }
//
//                hdWallet.addAccountDoubleEncrypt(defaultAccountName+" "+(index+1));
//                index++;
//                lookAhead--;
//            }
//        } else {
//            walletSize = accountSize;
//        }
//
//        hdWallet = HDWalletFactory2
//            .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(), Language.US,
//                mnemonic, passphrase, walletSize, defaultAccountName);

        WalletBody walletBody = new WalletBody(defaultAccountName);
//        walletBody.setHdWallets(Arrays.asList(hdWallet));

        return walletBody;
    }

    public static boolean hasTransactions(BlockExplorer blockExplorer, String xpub)
        throws Exception {

        Response<HashMap<String, Balance>> exe = blockExplorer
            .getBalance(Arrays.asList(xpub), BlockExplorer.TX_FILTER_ALL).execute();

        if (!exe.isSuccessful()) {
            throw new Exception(exe.code() + " " + exe.errorBody().string());
        }

        HashMap<String, Balance> body = exe.body();

        return body.get(xpub).getNTx() > 0L;
    }

    /**
     * Checks imported address and hd keys for possible double encryption corruption
     */
    public boolean isEncryptionConsistent() {
        ArrayList<String> keyList = new ArrayList<>();

        if (getLegacyAddressList() != null) {
            List<LegacyAddressBody> legacyAddresses = getLegacyAddressList();
            for (LegacyAddressBody legacyAddress : legacyAddresses) {
                if (!legacyAddress.isWatchOnly()) {
                    keyList.add(legacyAddress.getPrivateKey());
                }
            }
        }

        if (getHdWallets() != null && getHdWallets().size() > 0) {

            for (HDWalletBody hdWallet : getHdWallets()) {
                List<AccountBody> accounts = hdWallet.getAccounts();
                for (AccountBody account : accounts) {
                    keyList.add(account.getXpriv());
                }
            }
        }

        return isEncryptionConsistent(isDoubleEncryption(), keyList);
    }

    public boolean isEncryptionConsistent(boolean isDoubleEncrypted, List<String> keyList) {

        FormatsUtil formatsUtil = FormatsUtil.getInstance();

        boolean consistent = true;

        for (String key : keyList) {

            if (isDoubleEncrypted) {
                consistent = formatsUtil.isKeyEncrypted(key);
            } else {
                consistent = formatsUtil.isKeyUnencrypted(key);
            }

            if (!consistent) {
                break;
            }
        }

        return consistent;
    }

    public void validateSecondPassword(@Nullable String secondPassword) throws DecryptionException {

        if(isDoubleEncryption()) {
            DoubleEncryptionFactory.getInstance().validateSecondPassword(
                getDpasswordhash(),
                getSharedKey(),
                secondPassword,
                getOptions().getPbkdf2Iterations(),
                true);
        } else if(!isDoubleEncryption() && secondPassword != null) {
            throw new DecryptionException("Double encryption password specified on non double encrypted wallet.");
        }
    }

    public void upgradeV2PayloadToV3(@Nullable String secondPassword, String defaultAccountName) throws Exception {

        //Check if payload has 2nd password
        validateSecondPassword(secondPassword);

        if (getHdWallets() == null || getHdWallets().size() == 0) {

            int attempts = 0;
            boolean isEmpty;

            BlockExplorer blockExplorer = new BlockExplorer(
                BlockchainFramework.getRetrofitServerInstance(),
                BlockchainFramework.getApiCode());

            do {

                attempts++;

                //Create new hd wallet
                WalletBody wallet = new WalletBody(defaultAccountName);
                HDWalletBody hdWalletBody = wallet.getHdWallet();

                //Double encrypt if need
                if (!StringUtils.isEmpty(secondPassword)) {

                    //Double encrypt seedHex
                    String doubleEncryptedSeedHex = DoubleEncryptionFactory.getInstance().encrypt(
                        hdWalletBody.getSeedHex(),
                        getSharedKey(),
                        secondPassword,
                        getOptions().getPbkdf2Iterations());
                    hdWalletBody.setSeedHex(doubleEncryptedSeedHex);

                    //Double encrypt private key
                    for(AccountBody account : hdWalletBody.getAccounts()) {

                        String encryptedXPriv = DoubleEncryptionFactory.getInstance().encrypt(
                            account.getXpriv(),
                            getSharedKey(),
                            secondPassword,
                            getOptions().getPbkdf2Iterations());

                        account.setXpriv(encryptedXPriv);

                    }
                }

                setHdWallets(Arrays.asList(hdWalletBody));

                hdWalletBody.getAccounts().get(0).setLabel(defaultAccountName);

                isEmpty = !hasTransactions(blockExplorer, hdWalletBody.getAccounts().get(0).getXpub());

            } while (!isEmpty && attempts < 3);

            if(!isEmpty)
                throw new PayloadException("Failed to upgrade to V3!");
        }
    }

    public LegacyAddressBody addLegacyAddress(String label, @Nullable String secondPassword)
        throws Exception {
        validateSecondPassword(secondPassword);
        LegacyAddressBody addressBody = LegacyAddressBody.generateNewLegacy();
        addressBody.setLabel(label);

        if(secondPassword != null) {
            //Double encryption
            String unencryptedKey = addressBody.getPrivateKey();

            String encryptedKey = DoubleEncryptionFactory.getInstance().encrypt(unencryptedKey,
                getSharedKey(),
                secondPassword,
                getOptions().getPbkdf2Iterations());

            addressBody.setPrivateKey(encryptedKey);

        }

        keys.add(addressBody);

        return addressBody;
    }

    public AccountBody addAccount(String label, @Nullable String secondPassword)
        throws Exception {

        validateSecondPassword(secondPassword);
        decryptHDWallet(secondPassword);

        HD.addAccount();

        HDAccount newlyDerived = HD.getAccount(HD.getAccounts().size() - 1);
        String xpriv = newlyDerived.getXPriv();
        String xpub = newlyDerived.getXpub();

        //Double encryption
        if(secondPassword != null) {
            String encrypted = DoubleEncryptionFactory.getInstance().encrypt(
                xpriv,
                sharedKey,
                secondPassword,
                getOptions().getPbkdf2Iterations());
            xpriv = encrypted;
        }

        return getHdWallet().addAccount(label, xpriv, xpub);
    }

    private void decryptHDWallet(@Nullable String secondPassword)
        throws IOException, DecryptionException, InvalidCipherTextException, DecoderException,
        MnemonicLengthException, MnemonicWordException, MnemonicChecksumException {

        if(getHdWallets() == null || getHdWallets().size() == 0) {
            throw new MnemonicLengthException("No HDWallet to decrypt!");
        }

        if(secondPassword != null) {

            String encryptedSeedHex = getHdWallet().getSeedHex();

            String decryptedSeedHex = DoubleEncryptionFactory.getInstance().decrypt(
                encryptedSeedHex, sharedKey, secondPassword,
                getOptions().getPbkdf2Iterations());

            HD = HDWalletFactory
                .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(),
                    Language.US,
                    decryptedSeedHex,
                    getHdWallet().getPassphrase(),
                    getHdWallet().getAccounts().size());
        }
    }

    public LegacyAddressBody setKeyForLegacyAddress(ECKey key, @Nullable String secondPassword)
        throws DecryptionException, UnsupportedEncodingException, EncryptionException,
        NoSuchAddressException {

        validateSecondPassword(secondPassword);

        List<LegacyAddressBody> addressList = getLegacyAddressList();

        String address = key.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString();

        LegacyAddressBody matchingAddressBody = null;

        for(LegacyAddressBody addressBody : addressList) {
            if(addressBody.getAddressString().equals(address)) {
                matchingAddressBody = addressBody;
            }
        }

        if(matchingAddressBody == null) {
            throw new NoSuchAddressException("No matching address found for key");
        }

        if(secondPassword != null) {
            //Double encryption
            String encryptedKey = Base58.encode(key.getPrivKeyBytes());
            String encrypted2 = DoubleEncryptionFactory.getInstance().encrypt(encryptedKey,
                getSharedKey(),
                secondPassword != null ? secondPassword : null,
                getOptions().getPbkdf2Iterations());

            matchingAddressBody.setPrivateKey(encrypted2);

        } else {
            matchingAddressBody.setPrivateKeyFromBytes(key.getPrivKeyBytes());
        }

        return matchingAddressBody;
    }

    public DeterministicKey getMasterKey(@Nullable String secondPassword)
        throws DecryptionException, MnemonicWordException, DecoderException, IOException,
        MnemonicChecksumException, MnemonicLengthException, InvalidCipherTextException {

        validateSecondPassword(secondPassword);
        decryptHDWallet(secondPassword);
        return HD.getMasterKey();
    }

    public List<String> getMnemonic(@Nullable String secondPassword)
        throws DecryptionException, MnemonicWordException, DecoderException, IOException,
        MnemonicChecksumException, MnemonicLengthException, InvalidCipherTextException {

        validateSecondPassword(secondPassword);
        decryptHDWallet(secondPassword);
        return HD.getMnemonic();
    }

    public List<ECKey> getHDKeysForSigning(@Nullable String secondPassword, AccountBody account, PaymentBundle unspentOutputBundle)
        throws Exception {

        validateSecondPassword(secondPassword);
        decryptHDWallet(secondPassword);


        List<ECKey> keys = new ArrayList<>();

        HDAccount hdAccount = getHDAccountFromAccountBody(account);
        if (hdAccount != null) {
            for (UnspentOutput unspent : unspentOutputBundle.getSpendableOutputs()) {
                String[] split = unspent.getXpub().getPath().split("/");
                int chain = Integer.parseInt(split[1]);
                int addressIndex = Integer.parseInt(split[2]);

                Address hdAddress = hdAccount.getChain(chain).getAddressAt(addressIndex);
                ECKey walletKey = PrivateKeyFactory
                    .getKey(PrivateKeyFactory.WIF_COMPRESSED, hdAddress.getPrivateKeyString());
                keys.add(walletKey);
            }
        }

        return keys;
    }

    private HDAccount getHDAccountFromAccountBody(AccountBody accountBody) {
        for(HDAccount account : HD.getAccounts()) {
            if(account.getXpub().equals(accountBody.getXpub())) {
                return account;
            }
        }
        return null;
    }

    //no need for second pw. only using HD xpubs
    // TODO: 16/02/2017 this is so ugly to use. investigate other option
    public BiMap<String, Integer> getXpubToAccountIndexMap() {

        BiMap<String, Integer> xpubToAccountIndexMap = HashBiMap.create();

        List<HDAccount> accountList = HD.getAccounts();

        for (HDAccount account : accountList) {
            xpubToAccountIndexMap.put(account.getXpub(), account.getId());
        }

        return xpubToAccountIndexMap;
    }

    // TODO: 16/02/2017 this is so ugly to use. investigate other option
    public Map<Integer, String> getAccountIndexToXpubMap() {
        return getXpubToAccountIndexMap().inverse();
    }
}

// TODO: 16/02/2017 secondPassword could be extracted from methods if handled wallet side