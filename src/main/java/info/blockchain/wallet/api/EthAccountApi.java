package info.blockchain.wallet.api;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.data.EthAccount;

import io.reactivex.Single;

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

    private EthEndpoints getApiInstance() {
        if (endpoints == null) {
            endpoints = BlockchainFramework.getRetrofitApiInstance().
                    create(EthEndpoints.class);
        }
        return endpoints;
    }

}
