package info.blockchain.wallet.bip44;

import java.math.BigInteger;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

/**
 * HDAddress.java : an address in a BIP44 wallet account chain
 */
public class HDAddress {

    private int childNum;
    private String strPath = null;
    private ECKey ecKey = null;
    private byte[] pubKey = null;
    private byte[] pubKeyHash = null;

    private NetworkParameters params = null;

    /**
     * Constructor an HD address.
     *
     * @param params NetworkParameters
     * @param cKey   deterministic key for this address
     * @param child  index of this address in its chain
     */
    public HDAddress(NetworkParameters params, DeterministicKey cKey, int child) {

        this.params = params;
        childNum = child;

        DeterministicKey dk = HDKeyDerivation.deriveChildKey(cKey, new ChildNumber(childNum, false));
        // compressed WIF private key format
        if (dk.hasPrivKey()) {
            byte[] prepended0Byte = ArrayUtils.addAll(new byte[1], dk.getPrivKeyBytes());
            ecKey = ECKey.fromPrivate(new BigInteger(prepended0Byte), true);
        } else {
            ecKey = ECKey.fromPublicOnly(dk.getPubKey());
        }

        long now = Utils.now().getTime() / 1000;    // use Unix time (in seconds)
        ecKey.setCreationTimeSeconds(now);

        pubKey = ecKey.getPubKey();
        pubKeyHash = ecKey.getPubKeyHash();

        strPath = dk.getPathAsString();
    }

    /**
     * Get pubKey as byte array.
     *
     * @return byte[]
     */
    public byte[] getPubKey() {
        return pubKey;
    }

    /**
     * Get pubKeyHash as byte array.
     *
     * @return byte[]
     */
    public byte[] getPubKeyHash() {
        return pubKeyHash;
    }

    /**
     * Return public address for this instance.
     *
     * @return String
     */
    public String getAddressString() {
        return ecKey.toAddress(params).toString();
    }

    /**
     * Return private key for this address (compressed WIF format).
     *
     * @return String
     */
    public String getPrivateKeyString() {

        if (ecKey.hasPrivKey()) {
            return ecKey.getPrivateKeyEncoded(params).toString();
        } else {
            return null;
        }

    }

    /**
     * Return Bitcoinj address instance for this HDAddress.
     *
     * @return org.bitcoinj.core.HDAddress
     */
    public org.bitcoinj.core.Address getAddress() {
        return ecKey.toAddress(params);
    }

    /**
     * Return BIP44 path for this address (m / purpose' / coin_type' / account' / chain /
     * address_index).
     *
     * @return String
     */
    public String getPath() {
        return strPath;
    }

    public int getChildNum() {
        return childNum;
    }

}