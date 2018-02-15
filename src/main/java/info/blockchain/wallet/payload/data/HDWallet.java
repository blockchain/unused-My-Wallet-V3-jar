package info.blockchain.wallet.payload.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.blockexplorer.FilterType;
import info.blockchain.api.data.Balance;
import info.blockchain.api.data.UnspentOutput;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDAccount;
import info.blockchain.wallet.bip44.HDAddress;
import info.blockchain.wallet.bip44.HDWalletFactory;
import info.blockchain.wallet.bip44.HDWalletFactory.Language;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.HDWalletException;
import info.blockchain.wallet.payment.SpendableUnspentOutputs;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import info.blockchain.wallet.util.PrivateKeyFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Response;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class HDWallet {

    private static final int DEFAULT_MNEMONIC_LENGTH = 12;
    private static final int DEFAULT_NEW_WALLET_SIZE = 1;
    private static final String DEFAULT_PASSPHRASE = "";

    @JsonProperty("accounts")
    private List<Account> accounts;

    @JsonProperty("seed_hex")
    private String seedHex;

    @JsonProperty("passphrase")
    private String passphrase;

    @JsonProperty("mnemonic_verified")
    private boolean mnemonicVerified;

    @JsonProperty("default_account_idx")
    private int defaultAccountIdx;

    //bip44 Wallet needed for address derivation
    private info.blockchain.wallet.bip44.HDWallet HD;

    public void decryptHDWallet(@Nullable String validatedSecondPassword, String sharedKey, int iterations)
        throws IOException, DecryptionException, InvalidCipherTextException, DecoderException,
        MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, HDWalletException {

        if(HD == null) {
            instantiateBip44Wallet();
        }

        if(validatedSecondPassword != null && !isBip44AlreadyDecrypted()) {

            String encryptedSeedHex = getSeedHex();

            String decryptedSeedHex = DoubleEncryptionFactory.decrypt(
                encryptedSeedHex, sharedKey, validatedSecondPassword,
                iterations);

            HD = HDWalletFactory
                .restoreWallet(PersistentUrls.getInstance().getBitcoinParams(),
                    Language.US,
                    decryptedSeedHex,
                    getPassphrase(),
                    accounts.size());
        }
    }

    public void instantiateBip44Wallet()
        throws DecoderException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException,
        IOException, HDWalletException {

        try{
            int walletSize = DEFAULT_NEW_WALLET_SIZE;
            if(accounts != null) walletSize = accounts.size();
            HD = HDWalletFactory
                .restoreWallet(PersistentUrls.getInstance().getBitcoinParams(), Language.US,
                    getSeedHex(), getPassphrase(), walletSize);
        } catch (Exception e) {

            ArrayList<String> xpubList = new ArrayList<>();
            for(Account account : getAccounts()) {
                xpubList.add(account.getXpub());
            }

            HD = HDWalletFactory
                .restoreWatchOnlyWallet(PersistentUrls.getInstance().getBitcoinParams(),
                    xpubList);
        }

        if(HD == null) {
            throw new HDWalletException("HD instantiation failed");
        }
    }

    private boolean isBip44AlreadyDecrypted() {

        return
            HD != null
            && HD.getAccount(0).getXPriv() != null
            && HD.getAccounts().size() == accounts.size();
    }

    private void validateHD() throws HDWalletException {
        if(HD == null) {
            throw new HDWalletException("HD wallet not instantiated");
        } else if (HD.getAccount(0).getXPriv() == null) {
            throw new HDWalletException("Wallet private key unavailable. First decrypt with second password.");
        }
    }

    public HDWallet() {
        //parameterless constructor needed for jackson
    }

    public HDWallet(String defaultAccountName) throws Exception {

        this.HD = HDWalletFactory
            .createWallet(PersistentUrls.getInstance().getBitcoinParams(), Language.US,
                DEFAULT_MNEMONIC_LENGTH, DEFAULT_PASSPHRASE, DEFAULT_NEW_WALLET_SIZE);

        List<HDAccount> hdAccounts = this.HD.getAccounts();
        List<Account> accountBodyList = new ArrayList<>();
        int accountNumber = 1;
        for (int i = 0; i < hdAccounts.size(); i++) {

            String label = defaultAccountName;
            if (accountNumber > 1) {
                label = defaultAccountName + " " + accountNumber;
            }

            Account accountBody = new Account();
            accountBody.setLabel(label);
            accountBody.setXpriv(this.HD.getAccount(0).getXPriv());
            accountBody.setXpub(this.HD.getAccount(0).getXpub());
            accountBodyList.add(accountBody);

            accountNumber++;
        }

        setSeedHex(this.HD.getSeedHex());
        setDefaultAccountIdx(0);
        setMnemonicVerified(false);
        setPassphrase(DEFAULT_PASSPHRASE);
        setAccounts(accountBodyList);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Account getAccount(int accountId) {
        return accounts.get(accountId);
    }

    public String getSeedHex() {
        return seedHex;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public boolean isMnemonicVerified() {
        return mnemonicVerified;
    }

    public int getDefaultAccountIdx() {
        return defaultAccountIdx;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void setSeedHex(String seedHex) {
        this.seedHex = seedHex;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public void setMnemonicVerified(boolean mnemonicVerified) {
        this.mnemonicVerified = mnemonicVerified;
    }

    public void setDefaultAccountIdx(int defaultAccountIdx) {
        this.defaultAccountIdx = defaultAccountIdx;
    }

    public static HDWallet fromJson(String json)
        throws IOException, MnemonicWordException, DecoderException,
        MnemonicChecksumException, MnemonicLengthException, HDWalletException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        HDWallet hdWallet = mapper.readValue(json, HDWallet.class);
        hdWallet.instantiateBip44Wallet();

        return hdWallet;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    /**
     * @return Non-archived account xpubs
     */
    public List<String> getActiveXpubs() {

        ArrayList<String> xpubs = new ArrayList<>();

        if(getAccounts() == null) {
            return xpubs;
        }

        int nb_accounts = getAccounts().size();
        for (int i = 0; i < nb_accounts; i++) {

            Account account = getAccounts().get(i);
            boolean isArchived = account.isArchived();
            if (!isArchived) {
                String xpub = account.getXpub();
                if (xpub != null && xpub.length() > 0) {
                    xpubs.add(xpub);
                }
            }
        }
        return xpubs;
    }

    public Account addAccount(String label)
        throws
            HDWalletException {

        if(HD == null) {
            throw new HDWalletException("HD wallet not instantiated");
        }

        info.blockchain.wallet.bip44.HDWallet bip44Wallet = HD;
        HDAccount hdAccount = bip44Wallet.addAccount();

        if(HD.getAccounts().get(0).getXPriv() == null) {
            throw new HDWalletException("HD wallet not decrypted");
        }

        Account accountBody = new Account();
        accountBody.setLabel(label);
        accountBody.setXpub(hdAccount.getXpub());
        accountBody.setXpriv(hdAccount.getXPriv());

        getAccounts().add(accountBody);

        return accountBody;
    }

    public Account addAccount(String label, String xpriv, String xpub) {

        Account accountBody = new Account();
        accountBody.setLabel(label);
        accountBody.setXpub(xpub);
        accountBody.setXpriv(xpriv);

        getAccounts().add(accountBody);

        return accountBody;
    }

    public static HDWallet recoverFromMnemonic(String mnemonic, String defaultAccountName)
        throws Exception {
        return recoverFromMnemonic(mnemonic, "", defaultAccountName, 0);
    }

    public static HDWallet recoverFromMnemonic(String mnemonic, String defaultAccountName,
        int accountSize) throws Exception {
        return recoverFromMnemonic(mnemonic, "", defaultAccountName, accountSize);
    }

    public static HDWallet recoverFromMnemonic(String mnemonic, String passphrase,
        String defaultAccountName) throws Exception {
        return recoverFromMnemonic(mnemonic, passphrase, defaultAccountName, 0);
    }

    public static HDWallet recoverFromMnemonic(String mnemonic, String passphrase,
        String defaultAccountName, int walletSize) throws Exception {

        //Start with initial wallet size of 1.
        //After wallet is recovered we'll check how many accounts to restore
        info.blockchain.wallet.bip44.HDWallet bip44Wallet = HDWalletFactory
            .restoreWallet(PersistentUrls.getInstance().getBitcoinParams(), Language.US,
                mnemonic, passphrase, DEFAULT_NEW_WALLET_SIZE);

        BlockExplorer blockExplorer = new BlockExplorer(
            BlockchainFramework.getRetrofitExplorerInstance(),
            BlockchainFramework.getRetrofitApiInstance(),
            BlockchainFramework.getApiCode());

        HDWallet hdWalletBody = new HDWallet();
        hdWalletBody.setAccounts(new ArrayList<Account>());

        if(walletSize <= 0) {
            walletSize = getDeterminedSize(1, 5, 0, blockExplorer, bip44Wallet);
        }

        bip44Wallet = HDWalletFactory
            .restoreWallet(PersistentUrls.getInstance().getBitcoinParams(), Language.US,
                mnemonic, passphrase, walletSize);

        //Set accounts
        int accountNumber = 1;
        for(HDAccount account : bip44Wallet.getAccounts()) {
            String xpub = account.getXpub();
            String xpriv = account.getXPriv();
            String label = defaultAccountName;
            if (accountNumber > 1) {
                label = defaultAccountName + " " + accountNumber;
            }

            hdWalletBody.addAccount(label, xpriv, xpub);
            accountNumber++;
        }

        hdWalletBody.setSeedHex(Hex.toHexString(bip44Wallet.getSeed()));
        hdWalletBody.setPassphrase(bip44Wallet.getPassphrase());
        hdWalletBody.setMnemonicVerified(false);
        hdWalletBody.setDefaultAccountIdx(0);

        return hdWalletBody;
    }

    private static int getDeterminedSize(int walletSize, int trySize, int currentGap, BlockExplorer blockExplorer, info.blockchain.wallet.bip44.HDWallet bip44Wallet) throws Exception {

        LinkedList<String> xpubs = new LinkedList<>();

        for(int i = 0; i < trySize; i++) {
            HDAccount account = bip44Wallet.addAccount();
            xpubs.add(account.getXpub());
        }

        Response<HashMap<String, Balance>> exe = blockExplorer
            .getBalance(xpubs, FilterType.RemoveUnspendable).execute();

        if(!exe.isSuccessful()) {
            throw new Exception(exe.code() + " " + exe.errorBody().string());
        }

        HashMap<String, Balance> map = exe.body();

        final int lookAheadTotal = 10;
        for (String xpub : xpubs) {

            //If account has txs
            if(map.get(xpub).getTxCount() > 0L) {
                walletSize++;
                currentGap = 0;
            } else {
                currentGap++;
            }

            if(currentGap >= lookAheadTotal) {
                return walletSize;
            }
        }

        return getDeterminedSize(walletSize, trySize*2, currentGap, blockExplorer, bip44Wallet);
    }

    public static boolean hasTransactions(BlockExplorer blockExplorer, String xpub)
        throws Exception {

        Response<HashMap<String, Balance>> exe = blockExplorer
            .getBalance(Arrays.asList(xpub), FilterType.RemoveUnspendable).execute();

        if (!exe.isSuccessful()) {
            throw new Exception(exe.code() + " " + exe.errorBody().string());
        }

        HashMap<String, Balance> body = exe.body();

        return body.get(xpub).getTxCount() > 0L;
    }

    public List<ECKey> getHDKeysForSigning(Account account, SpendableUnspentOutputs unspentOutputBundle)
        throws Exception {

        validateHD();

        List<ECKey> keys = new ArrayList<>();

        HDAccount hdAccount = getHDAccountFromAccountBody(account);
        if (hdAccount != null) {
            for (UnspentOutput unspent : unspentOutputBundle.getSpendableOutputs()) {
                if(unspent.getXpub() != null) {
                    String[] split = unspent.getXpub().getPath().split("/");
                    int chain = Integer.parseInt(split[1]);
                    int addressIndex = Integer.parseInt(split[2]);

                    HDAddress hdAddress = hdAccount.getChain(chain).getAddressAt(addressIndex);
                    ECKey walletKey = new PrivateKeyFactory()
                        .getKey(PrivateKeyFactory.WIF_COMPRESSED, hdAddress.getPrivateKeyString());
                    keys.add(walletKey);
                }
            }
        }

        return keys;
    }

    public HDAccount getHDAccountFromAccountBody(Account accountBody) throws HDWalletException {

        if(HD == null) {
            throw new HDWalletException("HD wallet not instantiated");
        }

        for(HDAccount account : HD.getAccounts()) {
            if(account.getXpub().equals(accountBody.getXpub())) {
                return account;
            }
        }
        return null;
    }

    //no need for second pw. only using HD xpubs
    // TODO: 16/02/2017 Old. Investigate better way to do this
    public BiMap<String, Integer> getXpubToAccountIndexMap() throws HDWalletException {

        if(HD == null) {
            throw new HDWalletException("HD wallet not instantiated");
        }

        BiMap<String, Integer> xpubToAccountIndexMap = HashBiMap.create();

        List<HDAccount> accountList = HD.getAccounts();

        for (HDAccount account : accountList) {
            xpubToAccountIndexMap.put(account.getXpub(), account.getId());
        }

        return xpubToAccountIndexMap;
    }

    // TODO: 16/02/2017 Old. Investigate better way to do this
    public Map<Integer, String> getAccountIndexToXpubMap() throws HDWalletException {
        return getXpubToAccountIndexMap().inverse();
    }

    /**
     * Bip44 master private key. Not to be confused with bci HDWallet seed
     * @return
     */
    public DeterministicKey getMasterKey() throws HDWalletException {

        validateHD();
        return HD.getMasterKey();
    }

    public List<String> getMnemonic() throws HDWalletException {

        validateHD();
        return HD.getMnemonic();
    }

    @Nullable
    public String getLabelFromXpub(String xpub) {
        List<Account> accounts = getAccounts();

        for (Account account : accounts) {
            if (account.getXpub().equals(xpub)) {
                return account.getLabel();
            }
        }

        return null;
    }
}
