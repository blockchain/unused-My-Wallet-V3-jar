package info.blockchain.wallet;

import info.blockchain.wallet.crypto.DeterministicWallet;
import java.util.List;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitcoinCashWallet extends DeterministicWallet {

    private static Logger log = LoggerFactory.getLogger(BitcoinCashWallet.class);

    /**
     * Coin parameters
     */
    public int BCH_FORK_HEIGHT = 478558;
    public static final String BITCOIN_COIN_PATH = "M/44H/0H";
    public static final String BITCOINCASH_COIN_PATH = "M/44H/145H";
    private static final int MNEMONIC_LENGTH = 12;

    /**
     * Coin metadata store
     */
    public static final int METADATA_TYPE_EXTERNAL = 7;


    private BitcoinCashWallet(NetworkParameters params, String coinPath, String passphrase) {
        super(params, coinPath, MNEMONIC_LENGTH, passphrase);
    }

    private BitcoinCashWallet(NetworkParameters params, String coinPath, String entropyHex, String passphrase) {
        super(params, coinPath, entropyHex, passphrase);
    }

    private BitcoinCashWallet(NetworkParameters params, String coinPath, List<String> mnemonic, String passphrase) {
        super(params, coinPath, mnemonic, passphrase);
    }

    private BitcoinCashWallet(NetworkParameters params) {
        super(params);
    }

    public static synchronized BitcoinCashWallet create(NetworkParameters params, String coinPath) {
        return new BitcoinCashWallet(params, coinPath, "");
    }

    public static synchronized BitcoinCashWallet create(NetworkParameters params, String coinPath, String passphrase) {
        return new BitcoinCashWallet(params, coinPath, passphrase);
    }

    public static synchronized BitcoinCashWallet restore(NetworkParameters params, String coinPath, String entropyHex, String passphrase) {
        return new BitcoinCashWallet(params, coinPath, entropyHex, passphrase);
    }

    public static synchronized BitcoinCashWallet restore(NetworkParameters params, String coinPath, List<String> mnemonic, String passphrase) {
        return new BitcoinCashWallet(params, coinPath, mnemonic, passphrase);
    }

    public static synchronized BitcoinCashWallet createWatchOnly(NetworkParameters params) {
        return new BitcoinCashWallet(params);
    }

    /**
     * @deprecated since 14 January 2017, replaced by {@link #getReceiveCashAddressAt(int, int)}
     */
    @Deprecated
    public String getReceiveAddressAt(int accountIndex, int addressIndex) {
        return getReceiveBase58AddressAt(accountIndex, addressIndex);
    }

    /**
     * @deprecated since 14 January 2017, replaced by {@link #getChangeCashAddressAt(int, int)}
     */
    @Deprecated
    public String getChangeAddressAt(int accountIndex, int addressIndex) {
        return getChangeBase58AddressAt(accountIndex, addressIndex);
    }

    public String getReceiveCashAddressAt(int accountIndex, int addressIndex) throws Exception {
        return getReceiveBech32AddressAt(accountIndex, addressIndex);
    }

    public String getChangeCashAddressAt(int accountIndex, int addressIndex) throws Exception {
        return getChangeBech32AddressAt(accountIndex, addressIndex);
    }
}
