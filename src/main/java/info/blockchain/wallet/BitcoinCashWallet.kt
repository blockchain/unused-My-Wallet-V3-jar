package info.blockchain.wallet

import info.blockchain.api.blockexplorer.BlockExplorer
import info.blockchain.wallet.crypto.DeterministicChain
import info.blockchain.wallet.crypto.DeterministicWallet
import info.blockchain.wallet.multiaddress.MultiAddressFactoryBch
import info.blockchain.wallet.multiaddress.TransactionSummary
import info.blockchain.wallet.payload.BalanceManagerBch
import info.blockchain.wallet.payload.data.LegacyAddress
import io.reactivex.Completable
import org.bitcoinj.core.NetworkParameters
import org.slf4j.LoggerFactory
import java.math.BigInteger

@Suppress("unused")
open class BitcoinCashWallet : DeterministicWallet {

    private lateinit var balanceManager: BalanceManagerBch
    private lateinit var multiAddressFactory: MultiAddressFactoryBch

    private constructor(
            blockExplorer: BlockExplorer,
            params: NetworkParameters,
            coinPath: String,
            passphrase: String
    ) : super(params, coinPath, MNEMONIC_LENGTH, passphrase) {
        setupApi(blockExplorer)
    }

    private constructor(
            blockExplorer: BlockExplorer,
            params: NetworkParameters,
            coinPath: String,
            entropyHex: String,
            passphrase: String
    ) : super(params, coinPath, entropyHex, passphrase) {
        setupApi(blockExplorer)
    }

    private constructor(
            blockExplorer: BlockExplorer,
            params: NetworkParameters,
            coinPath: String,
            mnemonic: List<String>,
            passphrase: String
    ) : super(params, coinPath, mnemonic, passphrase) {
        setupApi(blockExplorer)
    }

    private constructor(blockExplorer: BlockExplorer, params: NetworkParameters) : super(params) {
        setupApi(blockExplorer)
    }

    private fun setupApi(blockExplorer: BlockExplorer) {
        this.balanceManager = BalanceManagerBch(blockExplorer)
        this.multiAddressFactory = MultiAddressFactoryBch(blockExplorer)
    }

    @Deprecated(
            message = "since 14 January 2017",
            replaceWith = ReplaceWith(
                    "bitcoinCashWallet.getReceiveCashAddressAt(int, int)",
                    "info.blockchain.wallet.BitcoinCashWallet"
            )
    )
    fun getReceiveAddressAt(accountIndex: Int, addressIndex: Int): String {
        return getReceiveBase58AddressAt(accountIndex, addressIndex)
    }

    @Deprecated(
            message = "since 14 January 2017",
            replaceWith = ReplaceWith(
                    "bitcoinCashWallet.getChangeCashAddressAt(int, int)",
                    "info.blockchain.wallet.BitcoinCashWallet"
            )
    )
    fun getChangeAddressAt(accountIndex: Int, addressIndex: Int): String {
        return getChangeBase58AddressAt(accountIndex, addressIndex)
    }

    /**
     * Updates the state of the [BalanceManagerBch], which ingests the balances for each address or
     * xPub.
     *
     * @param legacyAddressList A list of [LegacyAddress] addresses
     * @param allAccountsAndAddresses A list of both xPubs from HD accounts and [LegacyAddress]
     * addresses
     */
    fun updateAllBalances(legacyAddressList: List<String>, allAccountsAndAddresses: List<String>): Completable =
            Completable.fromCallable {
                balanceManager.updateAllBalances(legacyAddressList, allAccountsAndAddresses)
            }

    fun getAddressBalance(address: String): BigInteger =
            balanceManager.getAddressBalance(address) ?: BigInteger.ZERO

    fun getWalletBalance(): BigInteger = balanceManager.walletBalance ?: BigInteger.ZERO

    /**
     * Returns the balance of all imported addresses in BCH, excluding those belonging to
     * archived addresses.
     */
    fun getImportedAddressBalance(): BigInteger =
            balanceManager.importedAddressesBalance ?: BigInteger.ZERO

    /**
     *
     * @param legacyAddressList A list of all xpubs and legacy addresses
     * @param watchOnly A list of watch-only legacy addresses
     * @param activeXpubs A list of active xPubs addresses.
     * @param context Xpub or legacy address. Used to fetch transaction only relating to this address.
     * @param limit Maximum amount of transactions fetched
     * @param offset Page offset
     * @return All wallet transactions, all legacy transactions, or transaction relating to a single context/address
     */
    fun getTransactions(
            legacyAddressList: List<String>?,
            watchOnly: List<String>,
            activeXpubs: List<String>,
            context: String?,
            limit: Int,
            offset: Int
    ): MutableList<TransactionSummary> = multiAddressFactory.getAccountTransactions(
            activeXpubs,
            watchOnly,
            legacyAddressList,
            context,
            limit,
            offset,
            BCH_FORK_HEIGHT
    )

    /**
     * Allows you to generate a BCH receive address at an arbitrary number of positions on the chain
     * from the next valid unused address. For example, the passing 5 as the position will generate
     * an address which correlates with the next available address + 5 positions.
     *
     * @param accountIndex  The index of the [Account] you wish to generate an address from
     * @param addressIndex Represents how many positions on the chain beyond what is already used that
     * you wish to generate
     * @return A Bitcoin Cash address
     */
    fun getReceiveAddressAtPositionBch(accountIndex: Int, addressIndex: Int): String? {
        val xpub = getAccountPubB58(accountIndex)
        val nextIndex = multiAddressFactory.getNextReceiveAddressIndex(xpub, listOf())
        return getAccountAt(accountIndex).chains[DeterministicChain.RECEIVE_CHAIN]
                .getAddressAt(nextIndex + addressIndex).ecKey.toAddress(params)
                .toCashAddress()
    }

    fun getNextReceiveCashAddress(accountIndex: Int): String {
        val xpub = getAccountPubB58(accountIndex)
        val addressIndex = multiAddressFactory.getNextReceiveAddressIndex(xpub, listOf())
        return getReceiveCashAddressAt(accountIndex, addressIndex)
    }

    fun getNextChangeCashAddress(accountIndex: Int): String {
        val xpub = getAccountPubB58(accountIndex)
        val addressIndex = multiAddressFactory.getNextChangeAddressIndex(xpub)
        return getChangeCashAddressAt(accountIndex, addressIndex)
    }

    fun incrementNextReceiveAddressBch(xpub: String) {
        multiAddressFactory.incrementNextReceiveAddress(xpub, listOf())
    }

    fun incrementNextChangeAddressBch(xpub: String) {
        multiAddressFactory.incrementNextChangeAddress(xpub)
    }

    fun isOwnAddress(address: String) =
            multiAddressFactory.isOwnHDAddress(address)

    fun getXpubFromAddress(address: String): String? {
        return multiAddressFactory.getXpubFromAddress(address)
    }

    /**
     * Returns an xPub from a Bitcoin Cash address if the address belongs to this wallet.
     * @param address The Bitcoin Cash address you want to query
     * @return  An xPub as a String
     */
    fun getXpubFromBchAddress(address: String): String? {
        return multiAddressFactory.getXpubFromAddress(address)
    }

    /**
     * Updates address balance as well as wallet balance in BCH. This is used to immediately update
     * balances after a successful transaction which speeds up the balance the UI reflects without
     * the need to wait for incoming websocket notification.
     *
     * @param amount  The amount to be subtracted from the address's BCH balance
     * @param address A valid Bitcoin or Bitcoin cash address
     */
    @Throws(Exception::class)
    fun subtractAmountFromAddressBalance(address: String, amount: BigInteger) {
        balanceManager.subtractAmountFromAddressBalance(address, amount)
    }

    companion object {

        /**
         * Coin parameters
         */
        private val log = LoggerFactory.getLogger(BitcoinCashWallet::class.java)
        const val BITCOIN_COIN_PATH = "M/44H/0H"
        const val BITCOINCASH_COIN_PATH = "M/44H/145H"
        const val MNEMONIC_LENGTH = 12
        const val BCH_FORK_HEIGHT = 478558

        /**
         * Coin metadata store
         */
        const val METADATA_TYPE_EXTERNAL = 7

        @Synchronized
        fun create(blockExplorer: BlockExplorer, params: NetworkParameters, coinPath: String): BitcoinCashWallet {
            return BitcoinCashWallet(blockExplorer, params, coinPath, "")
        }

        @Synchronized
        fun create(blockExplorer: BlockExplorer, params: NetworkParameters, coinPath: String, passphrase: String): BitcoinCashWallet {
            return BitcoinCashWallet(blockExplorer, params, coinPath, passphrase)
        }

        @Synchronized
        fun restore(blockExplorer: BlockExplorer, params: NetworkParameters, coinPath: String, entropyHex: String, passphrase: String): BitcoinCashWallet {
            return BitcoinCashWallet(blockExplorer, params, coinPath, entropyHex, passphrase)
        }

        @Synchronized
        fun restore(blockExplorer: BlockExplorer, params: NetworkParameters, coinPath: String, mnemonic: List<String>, passphrase: String): BitcoinCashWallet {
            return BitcoinCashWallet(blockExplorer, params, coinPath, mnemonic, passphrase)
        }

        @Synchronized
        fun createWatchOnly(blockExplorer: BlockExplorer, params: NetworkParameters): BitcoinCashWallet {
            return BitcoinCashWallet(blockExplorer, params)
        }
    }
}
