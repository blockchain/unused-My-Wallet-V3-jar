package info.blockchain.wallet.api.trade.sfox;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.trade.sfox.data.SFOXTransaction;
import io.reactivex.Observable;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class SFOXApi {

    private final String partner = "blockchain";

    private static SFOXEndpoints sfoxEndpoints;

    private SFOXEndpoints getSfoxApiInstance() {
        if (sfoxEndpoints == null) {
            sfoxEndpoints = BlockchainFramework.getRetrofitSFOXInstance().
                create(SFOXEndpoints.class);
        }
        return sfoxEndpoints;
    }

    public Observable<List<SFOXTransaction>> getTransactions(String accountToken) {

        HashMap<String,String> headerMap = new HashMap<>();
        headerMap.put("X-SFOX-PARTNER-ID",partner);
        headerMap.put("Authorization","Bearer "+accountToken);
        headerMap.put("Content-type","application/json");

        return getSfoxApiInstance().getTransactions(headerMap, partner);
    }
}
