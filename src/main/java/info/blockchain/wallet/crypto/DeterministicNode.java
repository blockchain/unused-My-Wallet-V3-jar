package info.blockchain.wallet.crypto;

import org.bitcoinj.crypto.DeterministicKey;

public interface DeterministicNode {

    DeterministicKey getNode();

    String getPath();
}
