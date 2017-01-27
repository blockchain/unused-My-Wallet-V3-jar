package info.blockchain.wallet.bip44;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

/**
 * Wallet.java : BIP44 wallet
 */
public class Wallet {

    private byte[] seed = null;
    private String strPassphrase = null;
    private List<String> wordList = null;

    private DeterministicKey dkKey = null;
    private DeterministicKey dkRoot = null;

    private ArrayList<Account> accounts = null;

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
    public Wallet(MnemonicCode mc, NetworkParameters params, byte[] seed, String passphrase, int nbAccounts) throws MnemonicException.MnemonicLengthException {

        this.params = params;
        this.seed = seed;
        strPassphrase = passphrase;

        wordList = mc.toMnemonic(seed);
        byte[] hd_seed = MnemonicCode.toSeed(wordList, strPassphrase);
        dkKey = HDKeyDerivation.createMasterPrivateKey(hd_seed);
        DeterministicKey dKey = HDKeyDerivation.deriveChildKey(dkKey, 44 | ChildNumber.HARDENED_BIT);
        dkRoot = HDKeyDerivation.deriveChildKey(dKey, ChildNumber.HARDENED_BIT);

        accounts = new ArrayList<Account>();
        for (int i = 0; i < nbAccounts; i++) {
            accounts.add(new Account(params, dkRoot, i));
        }

        strPath = dKey.getPathAsString();
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
    public String getMnemonic() {
        return Joiner.on(" ").join(wordList);
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
     * @return List<Account>
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Return account for submitted account id.
     *
     * @return Account
     */
    public Account getAccount(int accountId) {
        return accounts.get(accountId);
    }

    /**
     * Add new account.
     */
    public void addAccount() {
        accounts.add(new Account(params, dkRoot, accounts.size()));
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