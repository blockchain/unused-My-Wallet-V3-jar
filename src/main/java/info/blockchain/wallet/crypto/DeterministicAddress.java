package info.blockchain.wallet.crypto;

import java.math.BigInteger;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

public class DeterministicAddress implements DeterministicNode {

    private ECKey ecKey = null;
    private DeterministicKey deterministicAddressKey = null;

    public DeterministicAddress(DeterministicKey deterministicChainKey, int addressIndex) {

        deterministicAddressKey = HDKeyDerivation
            .deriveChildKey(deterministicChainKey, new ChildNumber(addressIndex, false));

        // compressed WIF private key format
        if (deterministicAddressKey.hasPrivKey()) {
            byte[] prepended0Byte = ArrayUtils
                .addAll(new byte[1], deterministicAddressKey.getPrivKeyBytes());
            ecKey = ECKey.fromPrivate(new BigInteger(prepended0Byte), true);
        } else {
            ecKey = ECKey.fromPublicOnly(deterministicAddressKey.getPubKey());
        }

        // use Unix time (in seconds)
        long now = Utils.now().getTime() / 1000;
        ecKey.setCreationTimeSeconds(now);
    }

    @Override
    public DeterministicKey getNode() {
        return deterministicAddressKey;
    }

    @Override
    public String getPath() {
        return deterministicAddressKey.getPathAsString();
    }

    public ECKey getEcKey() {
        return ecKey;
    }
}
