package org.bitcoinj.core.bip44;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Chain {

    private DeterministicKey cKey = null;
    private boolean isReceive;

    private String strPath = null;

    static private final int DESIRED_MARGIN = 32;
    static private final int ADDRESS_GAP_MAX = 20;

    private NetworkParameters params = null;

    private Chain() { ; }

    public Chain(NetworkParameters params, DeterministicKey aKey, boolean isReceive) {

        this.params = params;
        this.isReceive = isReceive;
        int chain = isReceive ? 0 : 1;
        cKey = HDKeyDerivation.deriveChildKey(aKey, chain);

        strPath = cKey.getPathAsString();
    }

    public static int maxSafeExtend() {
        return DESIRED_MARGIN - ADDRESS_GAP_MAX;
    }

    public boolean isReceive() {
        return isReceive;
    }

    public Address getAddressAt(int addrIdx) {
        return new Address(params, cKey, addrIdx);
    }

    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();

            obj.put("path", strPath);

            JSONArray addresses = new JSONArray();
            for(int i = 0; i < 2; i++) {
                Address addr = new Address(params, cKey, i);
                addresses.put(addr.toJSON());
            }
            obj.put("addresses", addresses);

            return obj;
        }
        catch(JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

}
