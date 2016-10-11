package info.blockchain.bip44;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Account.java : an account in a BIP44 wallet
 */
public class Account {

    private DeterministicKey aKey = null;
    private int aID;
    private List<Chain> chains = null;

    private String strXPUB = null;
    private String strPath = null;

    private NetworkParameters params = null;

    private Account() {
    }

    /**
     * Constructor for account.
     *
     * @param params Example MainNetParams, RegTestParams, TestNet2Params
     * @param wKey   deterministic key for this account
     * @param child  id within the wallet for this account
     */
    public Account(NetworkParameters params, DeterministicKey wKey, int child) {

        this.params = params;
        aID = child;

        // L0PRV & STDVx: private derivation.
        int childnum = child;
        childnum |= ChildNumber.HARDENED_BIT;
        aKey = HDKeyDerivation.deriveChildKey(wKey, childnum);

        strXPUB = aKey.serializePubB58(params);

        chains = new ArrayList<Chain>();
        chains.add(new Chain(params, aKey, true));
        chains.add(new Chain(params, aKey, false));

        strPath = aKey.getPathAsString();

    }

    /**
     * Constructor for watch-only account.
     *
     * @param params NetworkParameters
     * @param xpub   XPUB for this account
     * @param child  id within the wallet for this account
     */
    public Account(NetworkParameters params, String xpub, int child) throws AddressFormatException {

        this.params = params;
        aID = child;

        // assign master key to account key
        aKey = createMasterPubKeyFromXPub(xpub);

        strXPUB = xpub;

        chains = new ArrayList<Chain>();
        chains.add(new Chain(params, aKey, true));
        chains.add(new Chain(params, aKey, false));

    }

    /**
     * Constructor for watch-only account.
     *
     * @param params NetworkParameters
     * @param xpub   XPUB for this account
     */
    public Account(NetworkParameters params, String xpub) throws AddressFormatException {

        this.params = params;

        // assign master key to account key
        aKey = createMasterPubKeyFromXPub(xpub);

        strXPUB = xpub;

        chains = new ArrayList<Chain>();
        chains.add(new Chain(params, aKey, true));
        chains.add(new Chain(params, aKey, false));

    }

    /**
     * Restore watch-only account deterministic public key from XPUB.
     *
     * @return DeterministicKey
     */
    private DeterministicKey createMasterPubKeyFromXPub(String xpubstr) throws AddressFormatException {

        byte[] xpubBytes = Base58.decodeChecked(xpubstr);

        ByteBuffer bb = ByteBuffer.wrap(xpubBytes);
        if (bb.getInt() != 0x0488B21E) {
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
    public String xpubstr() {

        return strXPUB;

    }

    /**
     * Return xprv string for this account.
     *
     * @return String
     */
    public String xprvstr() {

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
    public Chain getReceive() {
        return chains.get(0);
    }

    /**
     * Return change chain this account.
     *
     * @return HD_Chain
     */
    public Chain getChange() {
        return chains.get(1);
    }

    /**
     * Return chain for this account as indicated by index: 0 = receive, 1 = change.
     *
     * @return HD_Chain
     */
    public Chain getChain(int idx) {

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
    private String getPath() {
        return strPath;
    }

    /**
     * Write account to JSONObject. For debugging only.
     *
     * @return JSONObject
     */
    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();

            obj.put("xpub", xpubstr());
            if (aKey.hasPrivKey()) {
                obj.put("xprv", xprvstr());
            }

            JSONArray _chains = new JSONArray();
            for (Chain chain : chains) {
                _chains.put(chain.toJSON());
            }
            obj.put("chains", _chains);

            obj.put("path", getPath());

            return obj;
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
}