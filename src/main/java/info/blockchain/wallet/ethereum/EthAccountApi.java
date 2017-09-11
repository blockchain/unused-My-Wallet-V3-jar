package info.blockchain.wallet.ethereum;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.ethereum.data.EthAddressResponse;

import info.blockchain.wallet.ethereum.data.EthAddressResponseMap;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

@SuppressWarnings("WeakerAccess")
public class EthAccountApi {

    private EthEndpoints endpoints;

    /**
     * Returns an {@link EthAddressResponse} object for a given ETH address as an {@link
     * Observable}. An {@link EthAddressResponse} contains a list of transactions associated with
     * the account, as well as a final balance.
     *
     * @param address The ETH address to be queried
     * @return An {@link Observable} wrapping an {@link EthAddressResponse}
     */
    public Observable<EthAddressResponseMap> getEthAddress(String address) {
        return getApiInstance().getEthAccount(address);
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
                    public Boolean apply(HashMap<String, Boolean> map) throws Exception {
                        return map.get("contract");
                    }
                });
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
