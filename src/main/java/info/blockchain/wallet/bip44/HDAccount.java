package info.blockchain.wallet.bip44;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;

/**
 * HDAccount.java : an account in a BIP44 wallet
 */
public class HDAccount {

    private DeterministicKey aKey = null;
    private int aID;
    private List<HDChain> chains = null;

    private String strXPUB = null;
    private String strPath = null;

    private NetworkParameters params = null;

    /**
     * Constructor for account.
     *
     * @param params Example MainNetParams, RegTestParams, TestNet2Params
     * @param wKey   deterministic key for this account
     * @param child  id within the wallet for this account
     */
    public HDAccount(NetworkParameters params, DeterministicKey wKey, int child) {

        this.params = params;
        aID = child;

        // L0PRV & STDVx: private derivation.
        int childnum = child;
        childnum |= ChildNumber.HARDENED_BIT;
        aKey = HDKeyDerivation.deriveChildKey(wKey, childnum);

        strXPUB = aKey.serializePubB58(params);

        chains = new ArrayList<HDChain>();
        chains.add(new HDChain(params, aKey, true));
        chains.add(new HDChain(params, aKey, false));

        strPath = aKey.getPathAsString();

    }

    /**
     * Constructor for watch-only account.
     *
     * @param params NetworkParameters
     * @param xpub   XPUB for this account
     * @param child  id within the wallet for this account
     */
    public HDAccount(NetworkParameters params, String xpub, int child) throws AddressFormatException {

        this.params = params;
        aID = child;

        // assign master key to account key
        aKey = createMasterPubKeyFromXPub(xpub);

        strXPUB = xpub;

        chains = new ArrayList<HDChain>();
        chains.add(new HDChain(params, aKey, true));
        chains.add(new HDChain(params, aKey, false));

    }

    /**
     * Constructor for watch-only account.
     *
     * @param params NetworkParameters
     * @param xpub   XPUB for this account
     */
    public HDAccount(NetworkParameters params, String xpub) throws AddressFormatException {

        this.params = params;

        // assign master key to account key
        aKey = createMasterPubKeyFromXPub(xpub);

        strXPUB = xpub;

        chains = new ArrayList<HDChain>();
        chains.add(new HDChain(params, aKey, true));
        chains.add(new HDChain(params, aKey, false));

    }

    /**
     * Restore watch-only account deterministic public key from XPUB.
     *
     * @return DeterministicKey
     */
    private DeterministicKey createMasterPubKeyFromXPub(String xpubstr) throws AddressFormatException {

        boolean isTestnet = !(this.params instanceof MainNetParams);

        byte[] xpubBytes = Base58.decodeChecked(xpubstr);

        ByteBuffer bb = ByteBuffer.wrap(xpubBytes);

        int prefix = bb.getInt();

        if (!isTestnet && prefix != 0x0488B21E) {
            throw new AddressFormatException("invalid xpub version");
        }
        if (isTestnet && prefix != 0x043587CF) {
            throw new AddressFormatException("invalid xpub version");
        }

        byte[] chain = new byte[32];
        byte[] pub = new byte[33];
        // depth:
        bb.get();
        // parent fingerprint:
        bb.getInt();
        // child no.
        bb.getInt();
        bb.get(chain);
        bb.get(pub);

        return HDKeyDerivation.createMasterPubKeyFromBytes(pub, chain);
    }

    /**
     * Return XPUB string for this account.
     *
     * @return String
     */
    public String getXpub() {

        return strXPUB;

    }

    /**
     * Return xprv string for this account.
     *
     * @return String
     */
    public String getXPriv() {

        if (aKey.hasPrivKey()) {
            return aKey.serializePrivB58(params);
        } else {
            return null;
        }

    }

    /**
     * Return id for this account.
     *
     * @return int
     */
    public int getId() {
        return aID;
    }

    /**
     * Return receive chain this account.
     *
     * @return HD_Chain
     */
    public HDChain getReceive() {
        return chains.get(0);
    }

    /**
     * Return change chain this account.
     *
     * @return HD_Chain
     */
    public HDChain getChange() {
        return chains.get(1);
    }

    /**
     * Return chain for this account as indicated by index: 0 = receive, 1 = change.
     *
     * @return HD_Chain
     */
    public HDChain getChain(int idx) {

        if (idx < 0 || idx > 1) {
            return null;
        }

        return chains.get(idx);
    }

    /**
     * Return BIP44 path for this account (m / purpose' / coin_type' / account').
     *
     * @return String
     */
    public String getPath() {
        return strPath;
    }

}