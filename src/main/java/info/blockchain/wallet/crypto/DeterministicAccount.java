package info.blockchain.wallet.crypto;

import java.util.ArrayList;
import java.util.List;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

public class DeterministicAccount implements DeterministicNode {

    private DeterministicKey deterministicAccountKey;
    private List<DeterministicChain> chains = null;

    public DeterministicAccount(DeterministicKey deterministicWalletKey, int accountIndex) {

        // L0PRV & STDVx: private derivation.
        int childnum = accountIndex;
        childnum |= ChildNumber.HARDENED_BIT;
        deterministicAccountKey = HDKeyDerivation.deriveChildKey(deterministicWalletKey, childnum);

        chains = new ArrayList();
        chains.add(new DeterministicChain(deterministicAccountKey, DeterministicChain.RECEIVE_CHAIN));
        chains.add(new DeterministicChain(deterministicAccountKey, DeterministicChain.CHANGE_CHAIN));
    }

    @Override
    public DeterministicKey getNode() {
        return deterministicAccountKey;
    }

    @Override
    public String getPath() {
        return deterministicAccountKey.getPathAsString();
    }

    public List<DeterministicChain> getChains() {
        return chains;
    }
}
