package info.blockchain.wallet.api.trade.coinify;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.trade.coinify.data.CoinifyTrade;
import io.reactivex.Observable;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class CoinifyApi {

    private static CoinifyEndpoints coinifyEndpoints;

    private CoinifyEndpoints getCoinifyApiInstance() {
        if (coinifyEndpoints == null) {
            coinifyEndpoints = BlockchainFramework.getRetrofitCoinifyInstance().
                create(CoinifyEndpoints.class);
        }
        return coinifyEndpoints;
    }

    public Observable<List<CoinifyTrade>> getTrades(String accessToken) {

        HashMap<String,String> headerMap = new HashMap<>();
        headerMap.put("Authorization","Bearer "+accessToken);
        headerMap.put("Content-type","application/json");

        return getCoinifyApiInstance().getTrades(headerMap);
    }

    public Observable<CoinifyTrade> getTradeInformation(String accessToken, long tradeId) {

        HashMap<String,String> headerMap = new HashMap<>();
        headerMap.put("Authorization","Bearer "+accessToken);
        headerMap.put("Content-type","application/json");

        return getCoinifyApiInstance().getTradeInformation(headerMap, tradeId);
    }
}
