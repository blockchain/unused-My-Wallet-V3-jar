package info.blockchain.wallet.ethereum;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class EthereumWalletData {

    @JsonProperty("has_seen")
    private boolean hasSeen;

    @JsonProperty("default_account_idx")
    private int defaultAccountIdx;

    @JsonProperty("accounts")
    private ArrayList<EthereumAccount> accounts;

    @JsonProperty("legacy_account")
    private EthereumAccount legacyAccount;

    @JsonProperty("tx_notes")
    private HashMap<String, String> txNotes;

    @JsonProperty("last_tx")
    private String lastTx;

    @JsonProperty("last_tx_timestamp")
    private long lastTxTimestamp;

    public boolean hasSeen() {
        return hasSeen;
    }

    public void setHasSeen(boolean hasSeen) {
        this.hasSeen = hasSeen;
    }

    public int getDefaultAccountIdx() {
        return defaultAccountIdx;
    }

    public void setDefaultAccountIdx(int defaultAccountIdx) {
        this.defaultAccountIdx = defaultAccountIdx;
    }

    public ArrayList<EthereumAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<EthereumAccount> accounts) {
        this.accounts = accounts;
    }

    public HashMap<String, String> getTxNotes() {
        return txNotes;
    }

    public void setTxNotes(HashMap<String, String> txNotes) {
        this.txNotes = txNotes;
    }

    public EthereumAccount getLegacyAccount() {
        return legacyAccount;
    }

    public void setLegacyAccount(EthereumAccount legacyAccount) {
        this.legacyAccount = legacyAccount;
    }

    public boolean isHasSeen() {
        return hasSeen;
    }

    public String getLastTx() {
        return lastTx;
    }

    public void setLastTx(String lastTx) {
        this.lastTx = lastTx;
    }

    public long getLastTxTimestamp() {
        return lastTxTimestamp;
    }

    public void setLastTxTimestamp(long lastTxTimestamp) {
        this.lastTxTimestamp = lastTxTimestamp;
    }
}
