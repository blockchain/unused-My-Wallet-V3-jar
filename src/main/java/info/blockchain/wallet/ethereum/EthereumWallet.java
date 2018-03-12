package info.blockchain.wallet.ethereum;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.crypto.DeterministicKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class EthereumWallet {

    public static final int METADATA_TYPE_EXTERNAL = 5;
    private static final int ACCOUNT_INDEX = 0;

    @JsonProperty("ethereum")
    private EthereumWalletData walletData;

    public EthereumWallet() {
        //default constructor for Jackson
    }

    /**
     * Creates new Ethereum wallet and derives account from provided wallet seed.
     *
     * @param walletMasterKey DeterministicKey of root node
     * @param defaultAccountName The desired default account name
     */
    public EthereumWallet(DeterministicKey walletMasterKey, String defaultAccountName) {

        ArrayList<EthereumAccount> accounts = new ArrayList<>();
        accounts.add(EthereumAccount.deriveAccount(walletMasterKey, ACCOUNT_INDEX, defaultAccountName));

        this.walletData = new EthereumWalletData();
        this.walletData.setHasSeen(false);
        this.walletData.setDefaultAccountIdx(0);
        this.walletData.setTxNotes(new HashMap<String, String>());
        this.walletData.setAccounts(accounts);
    }

    /**
     * Loads existing Ethereum wallet from derived Ethereum metadata node.
     *
     * @return Existing Ethereum wallet or Null if no existing Ethereum wallet found.
     * @throws IOException
     */
    public static EthereumWallet load(String walletJson) throws IOException {

        if (walletJson != null) {
            EthereumWallet ethereumWallet = fromJson(walletJson);

            // Web can store an empty EthereumWalletData object
            if (ethereumWallet.walletData == null || ethereumWallet.walletData.getAccounts().isEmpty()) {
                return null;
            } else {
                return ethereumWallet;
            }
        } else {
            return null;
        }
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public static EthereumWallet fromJson(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        return mapper.readValue(json, EthereumWallet.class);
    }

    public boolean hasSeen() {
        return walletData.hasSeen();
    }

    /**
     * Set flag to indicate that user has acknowledged their ether wallet.
     * @param hasSeen
     */
    public void setHasSeen(boolean hasSeen) {
        walletData.setHasSeen(hasSeen);
    }

    /**
     * @return Single Ethereum account
     */
    public EthereumAccount getAccount() {

        if(walletData.getAccounts().isEmpty()){
            return null;
        }

        return walletData.getAccounts().get(ACCOUNT_INDEX);
    }

    public HashMap<String, String> getTxNotes() {
        return walletData.getTxNotes();
    }

    public void putTxNotes(String txHash, String txNote) {
        HashMap<String, String> notes = walletData.getTxNotes();
        notes.put(txHash, txNote);
    }

    public void removeTxNotes(String txHash) {
        HashMap<String, String> notes = walletData.getTxNotes();
        notes.remove(txHash);
    }

    public String getLastTransactionHash() {
        return walletData.getLastTx();
    }

    public void setLastTransactionHash(String txHash) {
        walletData.setLastTx(txHash);
    }

    public void setLastTransactionTimestamp(long timestamp) {
        walletData.setLastTxTimestamp(timestamp);
    }

    public long getLastTransactionTimestamp() {
        return walletData.getLastTxTimestamp();
    }
}
