package info.blockchain.wallet;

import com.google.common.annotations.Beta;
import info.blockchain.wallet.crypto.DeterministicWallet;
import java.io.IOException;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException;

/**
 * <p>
 *     Bitcoin wallet with new DeterministicWallet implementation.
 *     Not in use yet.
 * </p>
 */
public class BitcoinWallet extends DeterministicWallet{

    public static final String COIN_PATH = "M/44H/0H";
    private static final int MNEMONIC_LENGTH = 12;
    NetworkParameters params;

    /**
     * Generates random BitcoinWallet with one account.
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinWallet(NetworkParameters params)
        throws MnemonicException, IOException {
        super(COIN_PATH, MNEMONIC_LENGTH, "");
        this.params = params;
        addAccount();
    }

    /**
     * Generates random BitcoinWallet with one account and passphrase.
     * @param passphrase Passphrase to be used
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinWallet(String passphrase,
        NetworkParameters params)
        throws MnemonicException, IOException {
        super(COIN_PATH, MNEMONIC_LENGTH, passphrase);
        this.params = params;
        addAccount();
    }

    /**
     * Generates BitcoinWallet from supplied entropy and passphrase, with one account.
     * @param entropyHex Hex of entropy to be used to generate bitcoin wallet
     * @param passphrase Passphrase to be used
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinWallet(String entropyHex, String passphrase,
        NetworkParameters params)
        throws MnemonicException, IOException, DecoderException {
        super(COIN_PATH, entropyHex, passphrase);
        this.params = params;
        addAccount();
    }

    /**
     * Restores BitcoinWallet from supplied mnemonic and passphrase, with no accounts.
     *
     * @param mnemonic Mnemonic to be used to restore bitcoin wallet
     * @param passphrase Passphrase to be used
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinWallet(List<String> mnemonic, String passphrase, NetworkParameters params)
        throws MnemonicException, IOException {
        super(COIN_PATH, mnemonic, passphrase);
        this.params = params;
    }

    public String getUriScheme() {
        return params.getUriScheme();
    }

    public String getPubB58(int accountIndex) {
        return getAccountAt(accountIndex).getNode().serializePubB58(params);
    }

    public String getPrivB58(int accountIndex) {

        DeterministicKey key = getAccountAt(accountIndex).getNode();

        if(key.hasPrivKey()) {
            return key.serializePrivB58(params);
        } else {
            return null;
        }
    }

    public String getReceiveAddressAt(int accountIndex, int addressIndex) {
        ECKey key = getReceiveECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toString();
    }

    public String getChangeAddressAt(int accountIndex, int addressIndex) {
        ECKey key = getChangeECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toString();
    }

    /**
     * Experimental. Not ready for production yet.
     */
    @Beta
    public String getReceiveSegwitAddressAt(int accountIndex, int addressIndex) {
        ECKey key = getReceiveECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toBech32();
    }

    /**
     * Experimental. Not ready for production yet.
     */
    @Beta
    public String getChangeSegwitAddressAt(int accountIndex, int addressIndex) {
        ECKey key = getChangeECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toBech32();
    }
}
