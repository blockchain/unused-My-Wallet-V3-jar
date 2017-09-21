package info.blockchain.wallet.ethereum;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.metadata.Metadata;
import info.blockchain.wallet.util.MetadataUtil;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.request.RawTransaction;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class EthereumWallet {

    private static final int METADATA_TYPE_EXTERNAL = 5;
    private static final int ACCOUNT_INDEX = 0;

    @JsonProperty("ethereum")
    private EthereumWalletData walletData;

    private Metadata metadata;

    public EthereumWallet() {
        //default constructor for Jackson
    }

    /**
     * Creates new Ethereum wallet and derives account from provided wallet seed.
     *
     * @param walletMasterKey DeterministicKey of root node
     * @throws IOException
     * @throws MetadataException
     */
    public EthereumWallet(DeterministicKey walletMasterKey, String defaultAccountName)
        throws IOException, MetadataException, NoSuchAlgorithmException {

        DeterministicKey metaDataHDNode = MetadataUtil.deriveMetadataNode(walletMasterKey);

        ArrayList<EthereumAccount> accounts = new ArrayList<>();
        accounts.add(EthereumAccount.deriveAccount(walletMasterKey, ACCOUNT_INDEX, defaultAccountName));

        this.walletData = new EthereumWalletData();
        this.walletData.setHasSeen(false);
        this.walletData.setDefaultAccountIdx(0);
        this.walletData.setTxNotes(new HashMap<String, String>());
        this.walletData.setAccounts(accounts);

        this.metadata = getEthereumMetadataNode(metaDataHDNode);
    }

    /**
     * Loads existing Ethereum wallet from derived Ethereum metadata node.
     *
     * @param metaDataHDNode
     * @return Existing Ethereum wallet or Null if no existing Ethereum wallet found.
     * @throws MetadataException
     * @throws IOException
     * @throws InvalidCipherTextException MetadataHdNode encryption/decryption error
     */
    public static EthereumWallet load(DeterministicKey metaDataHDNode) throws MetadataException, IOException, InvalidCipherTextException {

        Metadata metadata = getEthereumMetadataNode(metaDataHDNode);
        String walletJson = metadata.getMetadata();

        if(walletJson != null) {
            EthereumWallet ethereumWallet = fromJson(walletJson);
            ethereumWallet.metadata = metadata;
            return ethereumWallet;
        } else {
            return null;
        }
    }

    private static Metadata getEthereumMetadataNode(DeterministicKey metaDataHDNode)
        throws IOException, MetadataException {
        return new Metadata.Builder(metaDataHDNode, METADATA_TYPE_EXTERNAL).build();
    }

    public void save()
        throws IOException, MetadataException, InvalidCipherTextException {
        metadata.putMetadata(toJson());
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
}
