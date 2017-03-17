package info.blockchain.wallet.api.trade;

import info.blockchain.wallet.BlockchainFramework;
import io.reactivex.Observable;
import java.util.HashMap;

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

    public Observable<SFOXResponse> getTransactions(String accountToken) {

        HashMap<String,String> headerMap = new HashMap<>();
        headerMap.put("X-SFOX-PARTNER-ID",partner);
        headerMap.put("Authorization","Bearer "+accountToken);
        headerMap.put("Content-type","application/json");

        return getSfoxApiInstance().getTransactions(headerMap, partner);
    }
}
