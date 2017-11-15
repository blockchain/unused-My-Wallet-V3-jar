package info.blockchain.wallet.hd

import java.math.BigInteger
import org.apache.commons.lang3.ArrayUtils
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Utils
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation

class GenericAddress(chainHdKey: DeterministicKey, val addressIndex: Int) : Node {

    internal var addressHdKey: DeterministicKey
    private var addressKey: ECKey

    val pubKey: ByteArray
        get() = addressKey.pubKey

    val pubKeyHash: ByteArray
        get() = addressKey.pubKeyHash

    init {

        addressHdKey = HDKeyDerivation.deriveChildKey(chainHdKey, ChildNumber(
                addressIndex, false))

        // compressed WIF private key format
        if (addressHdKey.hasPrivKey()) {
            val prepended0Byte = ArrayUtils.addAll(ByteArray(1), * addressHdKey.privKeyBytes)
            addressKey = ECKey.fromPrivate(BigInteger(prepended0Byte), true)
        } else {
            addressKey = ECKey.fromPublicOnly(addressHdKey.pubKey)
        }

        // use Unix time (in seconds)
        val now = Utils.now().time / 1000
        addressKey.creationTimeSeconds = now
    }

    override fun getNode(): DeterministicKey {
        return addressHdKey
    }

    override fun getPath(): String {
        return addressHdKey.pathAsString
    }
}
