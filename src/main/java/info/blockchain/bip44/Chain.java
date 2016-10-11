package info.blockchain.bip44;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Chain.java : a chain in a BIP44 wallet account
 */
public class Chain {

    private DeterministicKey cKey = null;
    private boolean isReceive;

    private String strPath = null;

    static private final int DESIRED_MARGIN = 32;
    static private final int ADDRESS_GAP_MAX = 20;

    private NetworkParameters params = null;

    public static final int RECEIVE_CHAIN = 0;
    public static final int CHANGE_CHAIN = 1;

    private Chain() {
    }

    /**
     * Constructor for a chain.
     *
     * @param params    NetworkParameters
     * @param aKey      deterministic key for this chain
     * @param isReceive this is the receive chain
     */
    public Chain(NetworkParameters params, DeterministicKey aKey, boolean isReceive) {

        this.params = params;
        this.isReceive = isReceive;
        int chain = isReceive ? RECEIVE_CHAIN : CHANGE_CHAIN;
        cKey = HDKeyDerivation.deriveChildKey(aKey, chain);

        strPath = cKey.getPathAsString();
    }

    /**
     * Test if this is the receive chain.
     *
     * @return boolean
     */
    public boolean isReceive() {
        return isReceive;
    }

    /**
     * Return Address at provided index into chain.
     *
     * @return Address
     */
    public Address getAddressAt(int addrIdx) {
        return new Address(params, cKey, addrIdx);
    }

    /**
     * Return BIP44 path for this chain (m / purpose' / coin_type' / account' / chain).
     *
     * @return String
     */
    public String getPath() {
        return strPath;
    }

    /**
     * Write chain to JSONObject. For debugging only.
     *
     * @return JSONObject
     */
    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();

            obj.put("path", getPath());

            JSONArray addresses = new JSONArray();
            for (int i = 0; i < 2; i++) {
                Address addr = new Address(params, cKey, i);
                addresses.put(addr.toJSON());
            }
            obj.put("addresses", addresses);

            return obj;
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

}