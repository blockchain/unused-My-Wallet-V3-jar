package info.blockchain.wallet.payload.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.EncryptionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.DecoderException;
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

    public AccountBody addAccount(String label, String xpriv, String xpub)
        throws IOException, DecryptionException, InvalidCipherTextException, DecoderException, MnemonicLengthException, MnemonicWordException, MnemonicChecksumException, EncryptionException {

        AccountBody accountBody = new AccountBody();
        accountBody.setLabel(label);
        accountBody.setXpub(xpub);
        accountBody.setXpriv(xpriv);

        getAccounts().add(accountBody);

        return accountBody;
    }
}
