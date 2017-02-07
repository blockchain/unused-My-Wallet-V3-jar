package info.blockchain.wallet.api;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.data.FeesItem;
import info.blockchain.wallet.api.data.FeesResponse;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class Wallet2 {

    private static WalletEndpoints walletApi;
    private static WalletEndpoints walletBase;

    private static WalletEndpoints getBaseApiInstance() {
        if (walletApi == null) {
            walletApi = BlockchainFramework.getRetrofitApiInstance().create(WalletEndpoints.class);
        }
        return walletApi;
    }

    private static WalletEndpoints getServerApiInstance() {
        if (walletBase == null) {
            walletBase = BlockchainFramework.getRetrofitServerInstance()
                .create(WalletEndpoints.class);
        }
        return walletBase;
    }

    public static Call<FeesResponse> getDynamicFee() {
        return getBaseApiInstance().getFees();
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
        return getBaseApiInstance().getRandomBytes(32, "hex");
    }

    public static Call<Void> updateFirebaseNotificationToken(String token, String guid, String sharedKey)
        throws Exception {

        return getServerApiInstance().postToWallet("update-firebase",
            guid,
            sharedKey,
            token,
            token.length(),
            BlockchainFramework.getApiCode());
    }

    public static Call<Void> registerMdid(String guid, String sharedKey,
        String signedGuid) {
        return getServerApiInstance().postToWallet("register-mdid",
            guid, sharedKey, signedGuid, signedGuid.length(),
            BlockchainFramework.getApiCode());
    }

    public static Call<Void> unregisterMdid(String guid, String sharedKey,
        String signedGuid) {
        return getServerApiInstance().postToWallet("unregister-mdid",
            guid, sharedKey, signedGuid, signedGuid.length(),
            BlockchainFramework.getApiCode());
    }
}
