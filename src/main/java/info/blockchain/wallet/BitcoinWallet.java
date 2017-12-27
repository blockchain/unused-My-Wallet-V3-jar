package info.blockchain.wallet;

import info.blockchain.wallet.crypto.DeterministicWallet;
import java.io.IOException;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.AbstractBitcoinNetParams;

/**
 * <p>
 *     Bitcoin wallet for future use.
 * </p>
 */
public class BitcoinWallet extends DeterministicWallet{

    private static final String COIN_PATH = "M/44H/0H";
    AbstractBitcoinNetParams params;

    /**
     * Generates random BitcoinWallet with one account no passphrase.
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinWallet(AbstractBitcoinNetParams params)
        throws MnemonicException, IOException {
        super(COIN_PATH, "");
        this.params = params;
        addAccount();
    }

    /**
     * Generates random BitcoinWallet with one account with passphrase.
     * @param passphrase Passphrase to be used
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinWallet(String passphrase,
        AbstractBitcoinNetParams params)
        throws MnemonicException, IOException {
        super(COIN_PATH, passphrase);
        this.params = params;
        addAccount();
    }

    /**
     * Generates BitcoinWallet from supplied entropy with one account with passphrase.
     * @param entropyHex Hex of entropy to be used to generate bitcoin wallet
     * @param passphrase Passphrase to be used
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinWallet(String entropyHex, String passphrase,
        AbstractBitcoinNetParams params)
        throws MnemonicException, IOException, DecoderException {
        super(COIN_PATH, entropyHex, passphrase);
        this.params = params;
        addAccount();
    }

    /**
     * Restores BitcoinWallet from supplied mnemonic with no accounts with passphrase.
     * // TODO: 22/12/2017 Check amount of accounts to be restored/added
     *
     * @param mnemonic Mnemonic to be used to restore bitcoin wallet
     * @param passphrase Passphrase to be used
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinWallet(List<String> mnemonic, String passphrase, AbstractBitcoinNetParams params)
        throws MnemonicException, IOException {
        super(COIN_PATH, mnemonic, passphrase);
        this.params = params;
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
}
