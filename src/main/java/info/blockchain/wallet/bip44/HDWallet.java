package info.blockchain.wallet.bip44;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

/**
 * HDWallet.java : BIP44 wallet
 */
public class HDWallet {

    private byte[] seed = null;
    private String strPassphrase = null;
    private List<String> wordList = null;

    private DeterministicKey dkKey = null;
    private DeterministicKey dkRoot = null;

    private ArrayList<HDAccount> accounts = null;

    private String strPath = null;

    private NetworkParameters params = null;

    /**
     * Constructor for wallet.
     *
     * @param mc         mnemonic code object
     * @param seed       seed for this wallet
     * @param passphrase optional BIP39 passphrase
     * @param nbAccounts number of accounts to create
     */
    public HDWallet(MnemonicCode mc, NetworkParameters params, byte[] seed, String passphrase, int nbAccounts) throws MnemonicException.MnemonicLengthException {

        this.params = params;
        this.seed = seed;
        strPassphrase = passphrase;

        wordList = mc.toMnemonic(seed);
        byte[] hd_seed = MnemonicCode.toSeed(wordList, strPassphrase);
        dkKey = HDKeyDerivation.createMasterPrivateKey(hd_seed);
        DeterministicKey dKey = HDKeyDerivation.deriveChildKey(dkKey, 44 | ChildNumber.HARDENED_BIT);
        dkRoot = HDKeyDerivation.deriveChildKey(dKey, ChildNumber.HARDENED_BIT);

        accounts = new ArrayList<HDAccount>();
        for (int i = 0; i < nbAccounts; i++) {
            accounts.add(new HDAccount(params, dkRoot, i));
        }

        strPath = dKey.getPathAsString();
    }

    /**
     * Constructor for watch-only wallet initialized from submitted XPUB(s).
     *
     * @param xpubs arrayList of XPUB strings
     */
    public HDWallet(NetworkParameters params, ArrayList<String> xpubs) throws AddressFormatException {

        this.params = params;
        accounts = new ArrayList<>();

        int i = 0;
        for(String xpub : xpubs) {
            accounts.add(new HDAccount(params, xpub, i));
            i++;
        }
    }

    /**
     * Return wallet seed as byte array.
     *
     * @return byte[]
     */
    public byte[] getSeed() {
        return seed;
    }

    /**
     * Return wallet seed as hex string.
     *
     * @return String
     */
    public String getSeedHex() {
        return new String(Hex.encodeHex(seed));
    }

    /**
     * Return wallet BIP39 mnemonic as string containing space separated words.
     *
     * @return String
     */
    @Deprecated
    public String getMnemonicOld() {
        return Joiner.on(" ").join(wordList);
    }

    public List<String> getMnemonic() {
        return wordList;
    }

    /**
     * Return wallet BIP39 passphrase.
     *
     * @return String
     */
    public String getPassphrase() {
        return strPassphrase;
    }

    /**
     * Return accounts for this wallet.
     *
     * @return List<HDAccount>
     */
    public List<HDAccount> getAccounts() {
        return accounts;
    }

    /**
     * Return account for submitted account id.
     *
     * @return HDAccount
     */
    public HDAccount getAccount(int accountId) {
        return accounts.get(accountId);
    }

    /**
     * Add new account.
     */
    public HDAccount addAccount() {
        HDAccount account = new HDAccount(params, dkRoot, accounts.size());
        accounts.add(account);

        return account;
    }

    /**
     * Return BIP44 path for this wallet (m / purpose').
     *
     * @return String
     */
    public String getPath() {
        return strPath;
    }


    public DeterministicKey getMasterKey() {
        return dkKey;
    }

}