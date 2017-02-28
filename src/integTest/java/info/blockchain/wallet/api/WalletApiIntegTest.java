package info.blockchain.wallet.api;

import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.api.data.FeeList;
import info.blockchain.wallet.api.data.Merchant;
import info.blockchain.wallet.api.data.Status;
import java.security.SecureRandom;
import java.util.ArrayList;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Response;

/**
 * Simple integration test.
 * Ensures endpoints are reachable even if responses return an error body.
 */
public class WalletApiIntegTest extends BaseIntegTest{

    private String guid = "cfd055ed-1a7f-4a92-8584-2f4d01365034";
    private String sharedKey = "b4ff6bf5-17a9-4905-b54b-a526816aa100";

    @Test
    public void getDynamicFee() throws Exception {
        Response<FeeList> call = WalletApi.getDynamicFee().execute();
        Assert.assertNotNull(call.body());
        Assert.assertNotNull(call.body().toJson());
    }

    @Test
    public void getRandomBytes() throws Exception {
        Response<ResponseBody> call = WalletApi.getRandomBytes().execute();
        Assert.assertNotNull(call.body());
        Assert.assertNotNull(call.body().string());
    }

    @Test
    public void updateFirebaseNotificationToken() throws Exception {
        Response<ResponseBody> call = WalletApi.updateFirebaseNotificationToken("","","").execute();
        Assert.assertNotNull(call.errorBody());
        Assert.assertNotNull(call.errorBody().string());
    }

    @Test
    public void registerMdid() throws Exception {
        Response<ResponseBody> call = WalletApi.registerMdid("","","").execute();
        Assert.assertNotNull(call.errorBody());
        Assert.assertNotNull(call.errorBody().string());
    }

    @Test
    public void unregisterMdid() throws Exception {
        Response<ResponseBody> call = WalletApi.unregisterMdid("","","").execute();
        Assert.assertNotNull(call.errorBody());
        Assert.assertNotNull(call.errorBody().string());
    }

    @Test
    public void setAccess() throws Exception {

        byte[] bytes = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        String key = new String(Hex.encode(bytes), "UTF-8");
        random.nextBytes(bytes);
        String value = new String(Hex.encode(bytes), "UTF-8");

        Response<Status> call = WalletApi.setAccess(key, value,"1234").execute();
        Assert.assertEquals(200, call.code());
    }

    @Test
    public void validateAccess() throws Exception {

        String key = "db2f4184429bf05c1a962384befb8873";

        Response<Status> call = WalletApi.validateAccess(key,"1234").execute();

        Assert.assertEquals("3236346436663830663565363434383130393262343739613437333763333739",
            call.body().getSuccess());
        Assert.assertEquals(200, call.code());
    }

    @Test
    public void saveWallet() throws Exception {
        Response<Void> call = WalletApi.saveWallet(true,"","",
            null,new JSONObject("{}"),false,"",
            "","","").execute();
        Assert.assertNotNull(call.errorBody());
        Assert.assertNotNull(call.errorBody().string());
    }

    @Test
    public void fetchWalletData() throws Exception {
        Response<ResponseBody> call = WalletApi.fetchWalletData(guid, sharedKey).execute();
        Assert.assertNotNull(call.body());
        Assert.assertNotNull(call.body().string());
    }

    @Test
    public void fetchEncryptedPayload() throws Exception {
        Response<ResponseBody> call = WalletApi.fetchEncryptedPayload(guid).execute();
        Assert.assertNotNull(call.body());
        Assert.assertNotNull(call.body().string());
    }

    @Test
    public void fetchPairingEncryptionPassword() throws Exception {
        Response<ResponseBody> call = WalletApi.fetchPairingEncryptionPassword("").execute();
        Assert.assertNotNull(call.errorBody());
        Assert.assertNotNull(call.errorBody().string());
    }

    @Test
    public void getAllMerchants() throws Exception {
        Response<ArrayList<Merchant>> call = WalletApi.getAllMerchants().execute();
        Assert.assertNotNull(call.body());
        Assert.assertTrue(call.body().size() > 0);
    }

//    @Test
//    public void fetchSettings() throws Exception {
//        Response<Settings> call = WalletApi.fetchSettings(SettingsManager.METHOD_GET_INFO, guid, sharedKey).execute();
//        Assert.assertNotNull(call.body());
//        Assert.assertNotNull(call.body().toJson());
//    }
//
//    @Test
//    public void updateSettings() throws Exception {
//        Response<ResponseBody> call = WalletApi.updateSettings(
//            SettingsManager.METHOD_UPDATE_EMAIL, guid, sharedKey, "a@a.com").execute();
//        Assert.assertNotNull(call.body());
//        Assert.assertNotNull(call.body().string());
//    }
}
