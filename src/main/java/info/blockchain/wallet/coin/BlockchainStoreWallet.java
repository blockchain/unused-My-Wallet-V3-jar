package info.blockchain.wallet.coin;

import info.blockchain.wallet.crypto.DeterministicWallet;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.exceptions.WalletMetadataException;
import info.blockchain.wallet.metadata.Metadata;
import info.blockchain.wallet.util.MetadataUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.annotation.Nonnull;
import org.bitcoinj.crypto.DeterministicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;

/**
 * <p>A BlockchainStoreWallet is a storable wallet used in blockchain.info's key-value store service with a generic
 * wallet data format.</p>
 *
 * <p>The hierarchy is started from a single root key {@link #node} which can be calculated from a
 * {@link #masterSeed}</p>
 */
public abstract class BlockchainStoreWallet extends DeterministicWallet{

    private static final Logger log = LoggerFactory.getLogger(BlockchainStoreWallet.class);

    protected Metadata metadata;
    protected AbstractCoinData data;

    public BlockchainStoreWallet(int metadataType, String coinPath, int mnemonicLength, String passphrase)
        throws WalletMetadataException {
        super(coinPath, mnemonicLength, passphrase);
        setMetadataNode(node,metadataType);
    }

    public BlockchainStoreWallet(int metadataType, String coinPath, String entropyHex, String passphrase)
        throws WalletMetadataException {
        super(coinPath, entropyHex, passphrase);
        setMetadataNode(node,metadataType);
    }

    public BlockchainStoreWallet(int metadataType, String coinPath, List<String> mnemonic, String passphrase)
        throws WalletMetadataException {
        super(coinPath, mnemonic, passphrase);
        setMetadataNode(node,metadataType);
    }

    protected void addAccount(String label) {
        data.addAccount(new AbstractCoinAccount(label, false));
    }

    /**
     * Creates new {@link #data} with initial 0 accounts, and unseen status.
     *
     * Note: This does not save any data to key-value store
     */
    public void createNewWalletMetadata() {
        data = new AbstractCoinData();
    }

    /**
     * If wallet data exists, save to key-value store service.
     *
     * @throws WalletMetadataException Failed to push data to key-value store service.
     */
    public void saveWalletMetadata() throws WalletMetadataException {
        if (data != null) {
            try {
                metadata.putMetadata(data.toJson());
            } catch (IOException|InvalidCipherTextException|MetadataException e) {
                throw new WalletMetadataException(e);
            }
        } else {
            log.info("No wallet data to save.");
        }
    }

    /**
     * Loads existing wallet data from derived metadata node.
     *
     * @return Success status
     * @throws WalletMetadataException Failed to fetch data from key-value store service.
     * @throws IOException Wallet data parsing error. Possible corrupt or non json format.
     */
    public boolean loadWalletMetadata() throws WalletMetadataException, IOException {

        String walletJson = null;
        try {
            walletJson = metadata.getMetadata();
        } catch (MetadataException|IOException|InvalidCipherTextException e) {
            throw new WalletMetadataException(e);
        }

        if (walletJson != null) {
            data = AbstractCoinData.fromJson(walletJson);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param node
     * @param metadataType
     * @throws WalletMetadataException Key-value store issue. Most likely that service is offline.
     */
    private void setMetadataNode(@Nonnull DeterministicKey node, int metadataType)
        throws WalletMetadataException {
        DeterministicKey metaDataHDNode = null;

        try {
            metaDataHDNode = MetadataUtil.deriveMetadataNode(node);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            log.error("This shouldn't happen",e);
        }

        try {
            metadata = new Metadata.Builder(metaDataHDNode, metadataType).build();
        } catch (IOException | MetadataException e) {
            //Key-value store service is most likely unreachable
            throw new WalletMetadataException(e);
        }
    }
}
