package info.blockchain.wallet;

import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.coin.BlockchainStoreWallet;
import info.blockchain.wallet.exceptions.WalletMetadataException;
import java.util.List;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;

public class BitcoinCashWallet extends BlockchainStoreWallet {

    /**
     * Coin parameters
     */
    public int BCH_FORK_HEIGHT = 478558;
    public static final String BITCOIN_COIN_PATH = "M/44H/0H";
    public static final String BITCOINCASH_COIN_PATH = "M/44H/145H";
    private static final int MNEMONIC_LENGTH = 12;
    NetworkParameters params;

    /**
     * Coin metadata store
     */
    private static final int METADATA_TYPE_EXTERNAL = 7;

    private void init() {
        params = PersistentUrls.getInstance().getBitcoinCashParams();
    }

    private BitcoinCashWallet(String coinPath, String passphrase)
        throws WalletMetadataException {
        super(METADATA_TYPE_EXTERNAL, coinPath, MNEMONIC_LENGTH, passphrase);
        init();
    }

    private BitcoinCashWallet(String coinPath, String entropyHex, String passphrase)
        throws WalletMetadataException {
        super(METADATA_TYPE_EXTERNAL, coinPath, entropyHex, passphrase);
        init();
    }

    private BitcoinCashWallet(String coinPath, List<String> mnemonic, String passphrase)
        throws WalletMetadataException {
        super(METADATA_TYPE_EXTERNAL, coinPath, mnemonic, passphrase);
        init();
    }

    public static synchronized BitcoinCashWallet create(String coinPath)
        throws WalletMetadataException {
        return new BitcoinCashWallet(coinPath, "");
    }

    public static synchronized BitcoinCashWallet create(String coinPath, String passphrase)
        throws WalletMetadataException {
        return new BitcoinCashWallet(coinPath, passphrase);
    }

    public static synchronized BitcoinCashWallet restore(String coinPath, String entropyHex, String passphrase)
        throws WalletMetadataException {
        return new BitcoinCashWallet(coinPath, entropyHex, passphrase);
    }

    public static synchronized BitcoinCashWallet restore(String coinPath, List<String> mnemonic, String passphrase)
        throws WalletMetadataException {
        return new BitcoinCashWallet(coinPath, mnemonic, passphrase);
    }

    public String getUriScheme() {
        return params.getUriScheme();
    }

    public String getPubB58(int accountIndex) {
        return getAccountAt(accountIndex).getNode().serializePubB58(params);
    }

    public String getPrivB58(int accountIndex) {

        DeterministicKey key = getAccountAt(accountIndex).getNode();

        if (key.hasPrivKey()) {
            return key.serializePrivB58(params);
        } else {
            return null;
        }
    }

    /**
     * @deprecated since 14 January 2017, replaced by {@link #getReceiveAddressAt(int, int)}
     */
    @Deprecated
    public String getReceiveLegacyAddressAt(int accountIndex, int addressIndex) {
        ECKey key = getReceiveECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toBase58();
    }

    /**
     * @deprecated since 14 January 2017, replaced by {@link #getChangeAddressAt(int, int)}
     */
    @Deprecated
    public String getChangeLegacyAddressAt(int accountIndex, int addressIndex) {
        ECKey key = getChangeECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toBase58();
    }

    public String getReceiveAddressAt(int accountIndex, int addressIndex) throws Exception {
        ECKey key = getReceiveECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toBech32();
    }

    public String getChangeAddressAt(int accountIndex, int addressIndex) throws Exception {
        ECKey key = getChangeECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toBech32();
    }
}
