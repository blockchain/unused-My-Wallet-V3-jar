package info.blockchain.wallet.api;

import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.api.data.FeesListBody;
import info.blockchain.wallet.api.data.MerchantBody;
import info.blockchain.wallet.api.data.SettingsBody;
import info.blockchain.wallet.settings.Settings;
import java.util.ArrayList;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
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
        Response<FeesListBody> call = WalletApi.getDynamicFee().execute();
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
        Response<Void> call = WalletApi.setAccess("some_key","1234").execute();
        Assert.assertEquals(200, call.code());
    }

    @Test
    public void validateAccess() throws Exception {
        Response<Void> call = WalletApi.validateAccess("some_key","1234").execute();
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
        Response<ArrayList<MerchantBody>> call = WalletApi.getAllMerchants().execute();
        Assert.assertNotNull(call.body());
        Assert.assertTrue(call.body().size() > 0);
    }

    @Test
    public void fetchSettings() throws Exception {
        Response<SettingsBody> call = WalletApi.fetchSettings(Settings.METHOD_GET_INFO, guid, sharedKey).execute();
        Assert.assertNotNull(call.body());
        Assert.assertNotNull(call.body().toJson());
    }

    @Test
    public void updateSettings() throws Exception {
        Response<ResponseBody> call = WalletApi.updateSettings(Settings.METHOD_UPDATE_EMAIL, guid, sharedKey, "a@a.com").execute();
        Assert.assertNotNull(call.body());
        Assert.assertNotNull(call.body().string());
    }
}
