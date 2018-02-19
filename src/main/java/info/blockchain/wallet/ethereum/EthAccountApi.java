package info.blockchain.wallet.ethereum;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.ethereum.data.EthAddressResponse;
import info.blockchain.wallet.ethereum.data.EthAddressResponseMap;
import info.blockchain.wallet.ethereum.data.EthLatestBlock;
import info.blockchain.wallet.ethereum.data.EthPushTxRequest;
import info.blockchain.wallet.ethereum.data.EthTxDetails;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

@SuppressWarnings("WeakerAccess")
public class EthAccountApi {

    private EthEndpoints endpoints;

    /**
     * Returns an {@link EthAddressResponse} object for a list of given ETH addresses as an {@link
     * Observable}. An {@link EthAddressResponse} contains a list of transactions associated with
     * the accounts, as well as a final balance for each.
     *
     * @param addresses The ETH addresses to be queried
     * @return An {@link Observable} wrapping an {@link EthAddressResponse}
     */
    public Observable<EthAddressResponseMap> getEthAddress(List<String> addresses) {
        return getApiInstance().getEthAccount(StringUtils.join(addresses, ","));
    }

    /**
     * Returns true if a given ETH address is associated with an Ethereum contract, which is
     * currently unsupported. This should be used to validate any proposed destination address for
     * funds.
     *
     * @param address The ETH address to be queried
     * @return An {@link Observable} returning true or false based on the address's contract status
     */
    public Observable<Boolean> getIfContract(String address) {
        return getApiInstance().getIfContract(address)
                .map(new Function<HashMap<String, Boolean>, Boolean>() {
                    @Override
                    public Boolean apply(HashMap<String, Boolean> map) {
                        return map.get("contract");
                    }
                });
    }

    /**
     * Executes signed eth transaction and returns transaction hash.
     *
     * @param rawTx The ETH address to be queried
     * @return An {@link Observable} returning the transaction hash of a completed transaction.
     */
    public Observable<String> pushTx(String rawTx) {
        return getApiInstance().pushTx(new EthPushTxRequest(rawTx))
                .map(new Function<HashMap<String, String>, String>() {
                    @Override
                    public String apply(HashMap<String, String> map) {
                        return map.get("txHash");
                    }
                });
    }

    /**
     * Returns information about the latest block via a {@link EthLatestBlock} object.
     *
     * @return An {@link Observable} wrapping an {@link EthLatestBlock}
     */
    public Observable<EthLatestBlock> getLatestBlock() {
        return getApiInstance().getLatestBlock();
    }

    /**
     * Returns an {@link EthTxDetails} containing information about a specific ETH transaction. This
     * call will return a error 400 with the payload "message": "Transaction not found" if the hash
     * is incorrect or the transaction is still in the mempool.
     *
     * @param txHash The hash of the transaction you wish to check
     * @return An {@link Observable} wrapping an {@link EthTxDetails}
     */
    public Observable<EthTxDetails> getTransaction(String txHash) {
        return getApiInstance().getTransaction(txHash);
    }

    /**
     * Lazily evaluates an instance of {@link EthEndpoints}.
     */
    private EthEndpoints getApiInstance() {
        if (endpoints == null) {
            endpoints = BlockchainFramework.getRetrofitApiInstance().
                    create(EthEndpoints.class);
        }
        return endpoints;
    }

}
