package info.blockchain.wallet.ethereum;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.ethereum.data.EthAccount;

import java.util.HashMap;

import io.reactivex.Single;
import io.reactivex.functions.Function;

@SuppressWarnings("WeakerAccess")
public class EthAccountApi {

    private EthEndpoints endpoints;

    /**
     * Returns an {@link EthAccount} object for a given ETH address as a {@link Single}, which is to
     * say that either the object is returned or an error. An {@link EthAccount} contains a list of
     * transactions associated with the account, as well as a final balance.
     *
     * @param address The ETH address to be queried
     * @return A {@link Single} wrapping an {@link EthAccount}
     */
    public Single<EthAccount> getEthAccount(String address) {
        return getApiInstance().getEthAccount(address);
    }

    /**
     * Returns true if a given ETH address is associated with an Ethereum contract, which is
     * currently unsupported. This should be used to validate any proposed destination address for
     * funds. The returned object is a {@link Single}, which is to say that either a boolean is
     * returned or an error.
     *
     * @param address The ETH address to be queried
     * @return A {@link Single} returning true or false based on the address's contract status
     */
    public Single<Boolean> getIfContract(String address) {
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
