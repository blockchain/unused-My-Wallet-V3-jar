package info.blockchain.wallet.crypto;

import com.google.common.collect.ImmutableList;

import info.blockchain.wallet.exceptions.DeterministicWalletException;
import info.blockchain.wallet.util.HexUtils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.crypto.LinuxSecureRandom;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A DeterministicWallet calculates and keeps a whole tree (hierarchy) of keys originating from a
 * single root key. This implements part of the BIP 32 specification.</p>
 *
 * <p>The hierarchy is started from a single root key {@link #node} which can be calculated from a
 * {@link #masterSeed}</p>
 */
public abstract class DeterministicWallet implements DeterministicNode {

    private static final Logger log = LoggerFactory.getLogger(DeterministicWallet.class);

    static {
        if (Utils.isAndroidRuntime()) {
            log.info(
                "Android runtime detected - Init proper random number generator, "
                    + "as some old Android installations have bugs that make it unsecure.");
            new LinuxSecureRandom();
        }
    }

    protected NetworkParameters params;

    protected byte[] masterSeed;
    protected byte[] entropy;
    protected List<String> mnemonic;
    protected String passphrase;

    protected DeterministicKey node;
    protected DeterministicKey deterministicWalletKey;
    protected List<DeterministicAccount> accounts;

    /**
     * Generates new wallet for given coin path and passphrase
     *
     * @param coinPath
     * @param passphrase
     * @throws MnemonicLengthException
     * @throws MnemonicWordException
     * @throws MnemonicChecksumException
     * @throws IOException
     */
    public DeterministicWallet(NetworkParameters params, String coinPath, int mnemonicLength, String passphrase) {

        this.entropy = generateSecureRandomNumber(mnemonicLength);
        init(params, coinPath, passphrase);
    }

    /**
     * Restores wallet from given coin type, entropy and passphrase
     *
     * @param coinPath
     * @param entropyHex
     * @param passphrase
     * @throws MnemonicLengthException
     * @throws MnemonicWordException
     * @throws MnemonicChecksumException
     * @throws IOException
     * @throws DecoderException
     */
    public DeterministicWallet(NetworkParameters params, String coinPath, String entropyHex, String passphrase) {

        try {
            this.entropy = Hex.decodeHex(entropyHex.toCharArray());
        }catch (DecoderException e){
            throw new DeterministicWalletException("Illegal entropyHex supplied", e);
        }
        init(params, coinPath, passphrase);
    }

    /**
     * Restores wallet from given coin type, mnemonic and passphrase
     *
     * @param coinPath
     * @param mnemonic
     * @param passphrase
     * @throws MnemonicLengthException
     * @throws MnemonicWordException
     * @throws MnemonicChecksumException
     * @throws IOException
     */
    public DeterministicWallet(NetworkParameters params, String coinPath, List<String> mnemonic, String passphrase) {

        try {
            MnemonicCode mc = new MnemonicCode();

            this.masterSeed = MnemonicCode.toSeed(mnemonic, passphrase);
            this.entropy = mc.toEntropy(mnemonic);
        } catch (IOException | MnemonicException e) {
            throw new DeterministicWalletException("Unrecoverable mnemonic exception", e);
        }

        init(params, coinPath, passphrase);
    }

    /**
     * Creates empty watch only wallet
     */
    public DeterministicWallet(NetworkParameters params) {

        this.params = params;
        this.masterSeed = null;
        this.entropy = null;
        this.mnemonic = null;
        this.passphrase = null;
        this.deterministicWalletKey = null;

        this.accounts = new ArrayList();
    }

    private ImmutableList<ChildNumber> getRootPath(String coinPath) {
        return ImmutableList.<ChildNumber>builder().addAll(HDUtils.parsePath(coinPath)).build();
    }

    private byte[] generateSecureRandomNumber(int mnemonicLength) {
        // len == 16 (12 words), len == 24 (18 words), len == 32 (24 words)
        int len = mnemonicLength / 3 * 4;

        SecureRandom random = new SecureRandom();
        byte[] seed = new byte[len];
        random.nextBytes(seed);

        return seed;
    }

    private void init(NetworkParameters params, String coinPath, String passphrase) {

        this.params = params;

        try {
            MnemonicCode mc = new MnemonicCode();
            this.mnemonic = mc.toMnemonic(entropy);
            this.masterSeed = MnemonicCode.toSeed(mnemonic, passphrase);
            this.passphrase = passphrase;

            mc.check(this.mnemonic);
        }catch (IOException|MnemonicException e) {
            throw new DeterministicWalletException("Unrecoverable mnemonic exception", e);
        }

        this.accounts = new ArrayList();
        this.node = HDKeyDerivation.createMasterPrivateKey(masterSeed);

        ImmutableList<ChildNumber> rootPath = getRootPath(coinPath);

        this.deterministicWalletKey = node;
        for (ChildNumber child : rootPath) {
            this.deterministicWalletKey = HDKeyDerivation
                .deriveChildKey(this.deterministicWalletKey, child.getI());
        }
    }

    public String getUriScheme() {
        return params.getUriScheme();
    }

    public String getAccountPubB58(int accountIndex) {
        return getAccountAt(accountIndex).getNode().serializePubB58(params);
    }

    public String getAccountPrivB58(int accountIndex) {

        DeterministicKey key = getAccountAt(accountIndex).getNode();

        if (key.hasPrivKey()) {
            return key.serializePrivB58(params);
        } else {
            return null;
        }
    }

    public List<String> getMnemonic() {
        return mnemonic;
    }

    public void addAccount() {
        int accountIndex = accounts.size();
        log.info("Adding account at index " + accountIndex);
        DeterministicAccount account = new DeterministicAccount(deterministicWalletKey,
            accountIndex);
        accounts.add(account);
    }

    public void addWatchOnlyAccount(String xpub) {
        int accountIndex = accounts.size();
        log.info("Adding account at index " + accountIndex);
        DeterministicAccount account = new DeterministicAccount(params, xpub);
        accounts.add(account);
    }

    public boolean isWatchOnly() {
        return deterministicWalletKey == null;
    }

    public String getPath() {
        return deterministicWalletKey.getPathAsString();
    }

    public DeterministicKey getNode() {
        return deterministicWalletKey;
    }

    public String getSeedHex() {
        return HexUtils.encodeHexString(masterSeed);
    }

    public String getEntropyHex() {
        if (entropy != null) {
            return HexUtils.encodeHexString(entropy);
        } else {
            return null;
        }
    }

    public String getPassphrase() {
        return passphrase;
    }

    public DeterministicAccount getAccountAt(int accountIndex) {
        return accounts.get(accountIndex);
    }

    public List<DeterministicAccount> getAccounts() {
        return accounts;
    }

    public int getAccountTotal() {
        return accounts.size();
    }

    /**
     * DeterministicAddress corresponding to given account index and address index.
     *
     * @param accountIndex
     * @param addressIndex
     * @return
     */
    public ECKey getChangeECKeyAt(int accountIndex, int addressIndex) {
        return accounts.get(accountIndex).getChains().get(DeterministicChain.CHANGE_CHAIN)
            .getAddressAt(addressIndex).getEcKey();
    }

    public ECKey getReceiveECKeyAt(int accountIndex, int addressIndex) {
        return accounts.get(accountIndex).getChains().get(DeterministicChain.RECEIVE_CHAIN)
            .getAddressAt(addressIndex).getEcKey();
    }

    // TODO: 01/02/2018 The below receive/change address strings are coin specific - Consider abstracting
    protected String getReceiveBase58AddressAt(int accountIndex, int addressIndex) {
        ECKey key = getReceiveECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toBase58();
    }

    protected String getChangeBase58AddressAt(int accountIndex, int addressIndex) {
        ECKey key = getChangeECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toBase58();
    }

    public String getReceiveCashAddressAt(int accountIndex, int addressIndex) {
        ECKey key = getReceiveECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toCashAddress();
    }

    public String getChangeCashAddressAt(int accountIndex, int addressIndex) {
        ECKey key = getChangeECKeyAt(accountIndex, addressIndex);
        return key.toAddress(params).toCashAddress();
    }
}
