package info.blockchain.wallet.crypto;

import com.google.common.collect.ImmutableList;
import info.blockchain.wallet.exceptions.DeterministicWalletException;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.ECKey;
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
    public DeterministicWallet(String coinPath, int mnemonicLength, String passphrase) {

        this.entropy = generateSecureRandomNumber(mnemonicLength);
        init(coinPath, passphrase);
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
    public DeterministicWallet(String coinPath, String entropyHex, String passphrase) {

        try {
            this.entropy = Hex.decodeHex(entropyHex.toCharArray());
        }catch (DecoderException e){
            throw new DeterministicWalletException("Illegal entropyHex supplied", e);
        }
        init(coinPath, passphrase);
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
    public DeterministicWallet(String coinPath, List<String> mnemonic, String passphrase) {

        try {
            MnemonicCode mc = new MnemonicCode();

            this.masterSeed = MnemonicCode.toSeed(mnemonic, passphrase);
            this.entropy = mc.toEntropy(mnemonic);
        } catch (IOException | MnemonicException e) {
            throw new DeterministicWalletException("Unrecoverable mnemonic exception", e);
        }

        init(coinPath, passphrase);
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

    private void init(String coinPath, String passphrase) {

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

    public String getPath() {
        return deterministicWalletKey.getPathAsString();
    }

    public DeterministicKey getNode() {
        return deterministicWalletKey;
    }

    public String getSeedHex() {
        return Hex.encodeHexString(masterSeed);
    }

    public String getEntropyHex() {
        if (entropy != null) {
            return Hex.encodeHexString(entropy);
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
}
