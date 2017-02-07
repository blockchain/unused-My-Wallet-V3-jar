package info.blockchain.wallet.api;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.data.FeesItem;
import info.blockchain.wallet.api.data.FeesResponse;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class Wallet2 {

    private static WalletEndpoints api;

    private static WalletEndpoints getApiInstance() {
        if (api == null) {
            api = BlockchainFramework.getRetrofitApiInstance().create(WalletEndpoints.class);
        }
        return api;
    }


    public static Call<FeesResponse> getDynamicFee() {
        return getApiInstance().getFees();
    }

    public static FeesItem getDefaultFee() throws IOException {
        return FeesItem.fromJson(""
            + "{\n"
            + "     \"fee\": 35000,\n"
            + "     \"surge\": false,\n"
            + "     \"ok\": true\n"
            + "}");
    }

    public static Call<ResponseBody> getRandomBytes() {
        return getApiInstance().getRandomBytes(32, "hex");
    }
}
