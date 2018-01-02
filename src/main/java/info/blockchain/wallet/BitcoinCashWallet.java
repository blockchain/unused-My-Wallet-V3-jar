package info.blockchain.wallet;

import info.blockchain.wallet.crypto.DeterministicWallet;
import java.io.IOException;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.AbstractBitcoinCashNetParams;
import org.spongycastle.util.encoders.Hex;

public class BitcoinCashWallet extends DeterministicWallet {

    public static final String COIN_PATH = "M/44H/145H";
    private static final int MNEMONIC_LENGTH = 12;
    AbstractBitcoinCashNetParams params;

    /**
     * Generates random BitcoinWallet with one account no passphrase.
     *
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinCashWallet(AbstractBitcoinCashNetParams params, String coinPath)
        throws MnemonicException, IOException {
        super(coinPath, MNEMONIC_LENGTH, "");
        this.params = params;
        addAccount();
    }

    /**
     * Generates random BitcoinWallet with one account with passphrase.
     *
     * @param passphrase Passphrase to be used
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinCashWallet(String passphrase,
        AbstractBitcoinCashNetParams params, String coinPath)
        throws MnemonicException, IOException {
        super(coinPath, MNEMONIC_LENGTH, passphrase);
        this.params = params;
        addAccount();
    }

    /**
     * Generates BitcoinWallet from supplied entropy with one account with passphrase.
     *
     * @param entropyHex Hex of entropy to be used to generate bitcoin wallet
     * @param passphrase Passphrase to be used
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinCashWallet(String entropyHex, String passphrase, String coinPath,
        AbstractBitcoinCashNetParams params)
        throws MnemonicException, IOException, DecoderException {
        super(coinPath, entropyHex, passphrase);
        this.params = params;
        addAccount();
    }

    /**
     * Restores BitcoinWallet from supplied mnemonic with no accounts with passphrase. // TODO:
     * 22/12/2017 Check amount of accounts to be restored/added
     *
     * @param mnemonic Mnemonic to be used to restore bitcoin wallet
     * @param passphrase Passphrase to be used
     * @param params BitcoinNetParams to be used
     * @throws MnemonicException
     * @throws IOException
     */
    public BitcoinCashWallet(List<String> mnemonic, String passphrase, String coinPath,
        AbstractBitcoinCashNetParams params)
        throws MnemonicException, IOException {
        super(coinPath, mnemonic, passphrase);
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

        if (key.hasPrivKey()) {
            return key.serializePrivB58(params);
        } else {
            return null;
        }
    }

    public String getReceiveLegacyAddressAt(int accountIndex, int addressIndex) {
        ECKey key = getReceiveECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toBase58();
    }

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
