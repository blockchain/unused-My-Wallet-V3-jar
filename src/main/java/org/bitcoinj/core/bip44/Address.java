package org.bitcoinj.core.bip44;

import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

public class Address {

    private int childNum;
    private String strPath = null;
    private ECKey ecKey = null;
    private byte[] pubKey = null;
    private byte[] pubKeyHash = null;

    private NetworkParameters params = null;

    private Address() { ; }

    public Address(NetworkParameters params, DeterministicKey cKey, int child) {

        this.params = params;
        childNum = child;

        DeterministicKey dk = HDKeyDerivation.deriveChildKey(cKey, new ChildNumber(childNum, false));
        // compressed WIF private key format
        if(dk.hasPrivKey()) {
            byte[] prepended0Byte = ArrayUtils.addAll(new byte[1], dk.getPrivKeyBytes());
            ecKey = ECKey.fromPrivate(new BigInteger(prepended0Byte), true);
        }
        else {
            ecKey = ECKey.fromPublicOnly(dk.getPubKey());
        }

        long now = Utils.now().getTime() / 1000;    // use Unix time (in seconds)
        ecKey.setCreationTimeSeconds(now);

        pubKey = ecKey.getPubKey();
        pubKeyHash = ecKey.getPubKeyHash();

        strPath = dk.getPathAsString();
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public byte[] getPubKeyHash() {
        return pubKeyHash;
    }

    public String getPath() {
        return strPath;
    }

    public String getAddressString() {
        return ecKey.toAddress(params).toString();
    }

    public String getPrivateKeyString() {

        if(ecKey.hasPrivKey()) {
            return ecKey.getPrivateKeyEncoded(params).toString();
        }
        else    {
            return null;
        }

    }

    public org.bitcoinj.core.Address getAddress() {
        return ecKey.toAddress(params);
    }

    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();

            obj.put("address", getAddressString());
            if(ecKey.hasPrivKey()) {
                obj.put("key", getPrivateKeyString());
            }

            obj.put("path", strPath);

            return obj;
        }
        catch(JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
}
