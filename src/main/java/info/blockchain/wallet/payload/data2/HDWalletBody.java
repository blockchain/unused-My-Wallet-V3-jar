package info.blockchain.wallet.payload.data2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDAccount;
import info.blockchain.wallet.bip44.HDWallet;
import info.blockchain.wallet.bip44.HDWalletFactory;
import info.blockchain.wallet.bip44.HDWalletFactory.Language;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.EncryptionException;
import info.blockchain.wallet.util.DoubleEncryptionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.codec.DecoderException;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.spongycastle.crypto.InvalidCipherTextException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class HDWalletBody {

    private static final int DEFAULT_MNEMONIC_LENGTH = 12;
    private static final int DEFAULT_NEW_WALLET_SIZE = 1;
    private static final String DEFAULT_PASSPHRASE = "";

    @JsonProperty("accounts")
    private List<AccountBody> accounts;

    @JsonProperty("seed_hex")
    private String seedHex;

    @JsonProperty("passphrase")
    private String passphrase;

    @JsonProperty("mnemonic_verified")
    private boolean mnemonicVerified;

    @JsonProperty("default_account_idx")
    private int defaultAccountIdx;

    // TODO: 15/02/2017 Refactor HDWallet and HDWalletBody to be one
    private HDWallet HD;

    public HDWalletBody(){
        //Empty constructor needed for Jackson

    }

    public HDWalletBody(String defaultAccountName) throws IOException, MnemonicLengthException {

        //Bip44
        this.HD = HDWalletFactory
            .createWallet(PersistentUrls.getInstance().getCurrentNetworkParams(), Language.US,
                DEFAULT_MNEMONIC_LENGTH, DEFAULT_PASSPHRASE, DEFAULT_NEW_WALLET_SIZE);

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

        this.seedHex = this.HD.getSeedHex();
        this.defaultAccountIdx = 0;
        this.mnemonicVerified = false;
        this.passphrase = DEFAULT_PASSPHRASE;
        this.accounts = accountBodyList;
    }

    public List<AccountBody> getAccounts() {
        return accounts;
    }

    public AccountBody getAccount(int accountId) {
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

    public void setAccounts(List<AccountBody> accounts) {
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

    public static HDWalletBody fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, HDWalletBody.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public ArrayList<String> getActive() {

        ArrayList<String> xpubs = new ArrayList<>();

        if(getAccounts() == null) {
            return xpubs;
        }

        int nb_accounts = getAccounts().size();
        for (int i = 0; i < nb_accounts; i++) {

            AccountBody account = getAccounts().get(i);
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

    // TODO: 16/02/2017  Refactor HDWallet and HDWalletBody to be one
    public DeterministicKey getMasterKey() {
        return HD.getMasterKey();
    }

    public void addAccount(String label)
        throws IOException, DecryptionException, InvalidCipherTextException, DecoderException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, EncryptionException {

        HD.addAccount();
        HDAccount newlyDerived = HD
            .getAccount(HD.getAccounts().size() - 1);

        AccountBody accountBody = new AccountBody();
        accountBody.setLabel(label);

        String xpriv = newlyDerived.getXPriv();

        accountBody.setXpub(newlyDerived.getXpub());
        accountBody.setXpriv(xpriv);

        getAccounts().add(accountBody);

    }

    public void addAccountDoubleEncrypt(String label, String secondPassword, String sharedKey, int iterations)
        throws IOException, DecryptionException, InvalidCipherTextException, DecoderException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, EncryptionException {

        if(secondPassword != null) {

            String encryptedSeedHex = getSeedHex();

            String decryptedSeedHex = DoubleEncryptionFactory.getInstance().decrypt(
                encryptedSeedHex, sharedKey, secondPassword,
                iterations);

            HD = HDWalletFactory
                .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(),
                    Language.US,
                    decryptedSeedHex,
                    getPassphrase(),
                    getAccounts().size());
        } else {
            HD = HDWalletFactory
                .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(),
                    Language.US,
                    getSeedHex(),
                    getPassphrase(),
                    getAccounts().size());
        }

        HD.addAccount();
        HDAccount newlyDerived = HD
            .getAccount(HD.getAccounts().size() - 1);

        AccountBody accountBody = new AccountBody();
        accountBody.setLabel(label);

        String xpriv = newlyDerived.getXPriv();

        //Double encryption
        if(secondPassword != null) {
            String encrypted = DoubleEncryptionFactory.getInstance().encrypt(
                xpriv,
                sharedKey,
                secondPassword,
                iterations);
            xpriv = encrypted;
            System.out.println(encrypted);
        }

        accountBody.setXpub(newlyDerived.getXpub());
        accountBody.setXpriv(xpriv);

        getAccounts().add(accountBody);

    }

    // TODO: 16/02/2017 Refactor HDWallet and HDWalletBody to be one
    public void initHD()
        throws DecoderException, MnemonicLengthException, MnemonicWordException,
        MnemonicChecksumException, IOException, InvalidCipherTextException, DecryptionException {

        HD = HDWalletFactory
            .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(), Language.US,
                seedHex, passphrase, DEFAULT_NEW_WALLET_SIZE);
    }

    // TODO: 16/02/2017 Refactor HDWallet and HDWalletBody to be one
    public void initHDNoPrivateKeys()
        throws DecoderException, MnemonicLengthException, MnemonicWordException,
        MnemonicChecksumException, IOException, InvalidCipherTextException, DecryptionException {

        ArrayList<String> xpubList = new ArrayList<>();
        for(AccountBody account : accounts) {
            xpubList.add(account.getXpub());
        }

        //pass xpubs to give watch only wallet
        HD = HDWalletFactory
            .restoreWatchOnlyWallet(PersistentUrls.getInstance().getCurrentNetworkParams(),
                xpubList);
    }

//    public ArrayList<String> getMnemonic(@Nullable String secondPassword) {
//
//    }
}
