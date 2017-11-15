package info.blockchain.wallet.hd

import java.util.ArrayList
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation

class GenericAccount(coinHDKey: DeterministicKey, val accountIndex: Int) : Node {

    private val accountHdKey: DeterministicKey
    private val chainList: MutableList<GenericChain>

    init {

        // L0PRV & STDVx: private derivation.
        var childnum = accountIndex
        childnum = childnum or ChildNumber.HARDENED_BIT
        accountHdKey = HDKeyDerivation.deriveChildKey(coinHDKey, childnum)

        chainList = ArrayList()
        chainList.add(GenericChain(accountHdKey, GenericChain.CHANGE_CHAIN))
        chainList.add(GenericChain(accountHdKey, GenericChain.RECEIVE_CHAIN))
    }

    override fun getNode(): DeterministicKey {
        return accountHdKey
    }

    override fun getPath(): String {
        return accountHdKey.pathAsString
    }

    fun getChainList(): List<GenericChain> {
        return chainList
    }
}
