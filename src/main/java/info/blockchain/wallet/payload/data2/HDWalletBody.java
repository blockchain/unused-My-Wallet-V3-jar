package info.blockchain.wallet.payload.data2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;

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
}
