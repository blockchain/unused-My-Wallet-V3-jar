package info.blockchain.wallet.crypto;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

public class DeterministicChain implements DeterministicNode {

    public static final int RECEIVE_CHAIN = 0;
    public static final int CHANGE_CHAIN = 1;

    private DeterministicKey deterministicChainKey;

    public DeterministicChain(DeterministicKey deterministicAccountKey, int chainIndex) {
        this.deterministicChainKey = HDKeyDerivation.deriveChildKey(deterministicAccountKey, chainIndex);
    }

    @Override
    public DeterministicKey getNode() {
        return deterministicChainKey;
    }

    @Override
    public String getPath() {
        return deterministicChainKey.getPathAsString();
    }

    public DeterministicAddress getAddressAt(int addressIndex) {
        return new DeterministicAddress(deterministicChainKey, addressIndex);
    }
}
