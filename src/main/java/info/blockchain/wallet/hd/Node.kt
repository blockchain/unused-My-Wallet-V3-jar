package info.blockchain.wallet.hd

import org.bitcoinj.crypto.DeterministicKey

interface Node {

    val getNode: DeterministicKey

    val getPath: String
}
