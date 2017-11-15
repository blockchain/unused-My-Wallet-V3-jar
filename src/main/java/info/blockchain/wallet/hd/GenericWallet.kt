package info.blockchain.wallet.hd

import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.util.ArrayList
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.MnemonicCode
import java.io.File

abstract class GenericWallet : Node {

    var masterSeed: ByteArray? = null
        private set
    var mnemonic: List<String>? = null
        private set

    var node: DeterministicKey? = null

    private var coinHDKey: DeterministicKey? = null

    private var accounts: ArrayList<GenericAccount>? = null

    private val wordlist: MnemonicCode
        get() {
            File("wordlist/en_US.txt").usel
            val inputStream: InputStream = File("wordlist/en_US.txt").inputStream()

            val wis = this::class.java!!
                    .getClassLoader()
                    .getResourceAsStream("wordlist/en_US.txt")

            if (wis != null) {
                val mc = MnemonicCode(wis!!, BIP39_ENGLISH_SHA256)
                wis!!.close()
                return mc
            } else {
                throw IOException("Cannot read BIP39 word list")
            }
        }

    constructor(coinType: Int, passphrase: String) {

        // len == 16 (12 words), len == 24 (18 words), len == 32 (24 words)
        val len = MNEMONIC_LENGTH / 3 * 4

        val random = SecureRandom()
        val seed = ByteArray(len)
        random.nextBytes(seed)

        fromSeed(coinType, seed, null, passphrase, 1)
    }

    constructor(coinType: Int, seed: ByteArray, passphrase: String, accountCount: Int) {
        fromSeed(coinType, seed, null, passphrase, accountCount)
    }

    constructor(coinType: Int, mnemonic: List<String>, passphrase: String, accountCount: Int) {
        fromSeed(coinType, masterSeed, mnemonic, passphrase, accountCount)
    }

    private fun fromSeed(coinType: Int, seed: ByteArray?, mnemonic: List<String>?, passphrase: String, accountCount: Int) {

        val mc = wordlist

        this.masterSeed = seed
        this.mnemonic = mc.toMnemonic(seed!!)
        this.accounts = ArrayList()

        val hdSeed: ByteArray

        if (mnemonic != null) {
            hdSeed = mc.toEntropy(mnemonic)
        } else {
            hdSeed = MnemonicCode.toSeed(mnemonic!!, passphrase)
        }

        this.node = HDKeyDerivation.createMasterPrivateKey(hdSeed)
        val purposeDKey = HDKeyDerivation
                .deriveChildKey(node!!, 44 or ChildNumber.HARDENED_BIT)
        this.coinHDKey = HDKeyDerivation
                .deriveChildKey(purposeDKey, coinType or ChildNumber.HARDENED_BIT)

        for (i in 0 until accountCount) {
            addAccount()
        }
    }

    override val getPath: String = coinHDKey!!.pathAsString

    override val getNode: DeterministicKey = coinHDKey!!

    fun addAccount() {
        val account = GenericAccount(coinHDKey!!, accounts!!.size)
        accounts!!.add(account)
    }

    fun getAccountAt(accountIndex: Int): GenericAccount {
        return accounts!![accountIndex]
    }

    fun getChangeAddressAt(accountIndex: Int, addressIndex: Int): GenericAddress {
        return accounts!![accountIndex].getChainList()[GenericChain.CHANGE_CHAIN]
                .getAddressAt(addressIndex)
    }

    fun getReceiveAddressAt(accountIndex: Int, addressIndex: Int): GenericAddress {
        return accounts!![accountIndex].getChainList()[GenericChain.RECEIVE_CHAIN]
                .getAddressAt(addressIndex)
    }

    companion object {

        private val MNEMONIC_LENGTH = 12
        val BIP39_ENGLISH_SHA256 = "ad90bf3beb7b0eb7e5acd74727dc0da96e0a280a258354e7293fb7e211ac03db"
    }
}
