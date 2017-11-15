package info.blockchain.wallet.coin

import info.blockchain.wallet.hd.GenericWallet
import org.bitcoinj.params.AbstractBitcoinNetParams

class BitcoinWallet : GenericWallet {

    private var params: AbstractBitcoinNetParams? = null

    constructor(params: AbstractBitcoinNetParams, passphrase: String) : super(COIN_TYPE, passphrase) {
        this.params = params
    }

    constructor(coinType: Int, passphrase: String,
                params: AbstractBitcoinNetParams) : super(coinType, passphrase) {
        this.params = params
    }

    constructor(coinType: Int, seed: ByteArray, passphrase: String, accountCount: Int,
                params: AbstractBitcoinNetParams) : super(coinType, seed, passphrase, accountCount) {
        this.params = params
    }

    constructor(coinType: Int, mnemonic: List<String>, passphrase: String,
                accountCount: Int, params: AbstractBitcoinNetParams) : super(coinType, mnemonic, passphrase, accountCount) {
        this.params = params
    }

    fun getPubB58(accountIndex: Int): String {
        return getAccountAt(accountIndex).node.serializePubB58(params!!)
    }

    fun getPrivB58(accountIndex: Int): String? {
        return if (getAccountAt(accountIndex).node.hasPrivKey()) {
            getAccountAt(accountIndex).node.serializePrivB58(params!!)
        } else {
            null
        }
    }

    companion object {

        private val COIN_TYPE = 0
    }
}
