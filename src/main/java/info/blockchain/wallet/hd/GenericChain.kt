package info.blockchain.wallet.hd

import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation

class GenericChain(accountHdKey: DeterministicKey, chainIndex: Int) : Node {

    private var chainHdKey: DeterministicKey

    init {
        chainHdKey = HDKeyDerivation.deriveChildKey(accountHdKey, chainIndex)
    }

    override fun getNode(): DeterministicKey? {
        return chainHdKey
    }

    override fun getPath(): String {
        return chainHdKey.pathAsString
    }

    fun getAddressAt(addrIdx: Int): GenericAddress {
        return GenericAddress(chainHdKey, addrIdx)
    }

    companion object {

        val CHANGE_CHAIN = 0
        val RECEIVE_CHAIN = 1
    }
}
