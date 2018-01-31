package info.blockchain.wallet.api;

import info.blockchain.wallet.BaseIntegTest;
import info.blockchain.wallet.api.data.FeeList;
import info.blockchain.wallet.api.data.Status;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;

import io.reactivex.observers.TestObserver;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Simple integration test.
 * Ensures endpoints are reachable even if responses return an error body.
 */
public class WalletApiIntegTest extends BaseIntegTest {

    private String guid = "cfd055ed-1a7f-4a92-8584-2f4d01365034";
    private String sharedKey = "b4ff6bf5-17a9-4905-b54b-a526816aa100";
    private WalletApi walletApi = new WalletApi();

    @Test
    public void getRandomBytesCall() throws Exception {
        Response<ResponseBody> call = walletApi.getRandomBytesCall().execute();

        assertNotNull(call.body());
        assertNotNull(call.body().string());
    }

    @Test
    public void getRandomBytesObservable() throws Exception {
        final TestObserver<ResponseBody> testObserver = walletApi.getRandomBytes().test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertNotNull(testObserver.values().get(0));
        assertNotNull(testObserver.values().get(0).toString());
    }

    @Test
    public void updateFirebaseNotificationToken() throws Exception {
        final TestObserver<ResponseBody> testObserver =
                walletApi.updateFirebaseNotificationToken("", "", "").test();

        testObserver.assertTerminated();
        testObserver.assertNotComplete();
        testObserver.assertError(HttpException.class);
    }

    @Test
    public void registerMdid() throws Exception {
        final TestObserver<ResponseBody> testObserver =
                walletApi.registerMdid("", "", "").test();

        testObserver.assertTerminated();
        testObserver.assertNotComplete();
        testObserver.assertError(HttpException.class);
    }

    @Test
    public void unregisterMdid() throws Exception {
        final TestObserver<ResponseBody> testObserver =
                walletApi.unregisterMdid("", "", "").test();

        testObserver.assertTerminated();
        testObserver.assertNotComplete();
        testObserver.assertError(HttpException.class);
    }

    @Test
    public void setAccess() throws Exception {

        byte[] bytes = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        String key = new String(Hex.encode(bytes), "UTF-8");
        random.nextBytes(bytes);
        String value = new String(Hex.encode(bytes), "UTF-8");

        final TestObserver<Response<Status>> testObserver =
                walletApi.setAccess(key, value, "1234").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEquals(200, testObserver.values().get(0).code());
        assertEquals("Key Saved", testObserver.values().get(0).body().getSuccess());
    }

    @Test
    public void validateAccess() throws Exception {
        String key = "db2f4184429bf05c1a962384befb8873";

        final TestObserver<Response<Status>> testObserver =
                walletApi.validateAccess(key, "1234").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEquals("3236346436663830663565363434383130393262343739613437333763333739",
            testObserver.values().get(0).body().getSuccess());
        assertEquals(200, testObserver.values().get(0).code());
    }

    @Test
    public void fetchWalletData() throws Exception {
        Response<ResponseBody> call = walletApi.fetchWalletData(guid, sharedKey).execute();

        assertNotNull(call.body());
        assertNotNull(call.body().string());
    }

    @Test
    public void fetchEncryptedPayload() throws Exception {
        final TestObserver<Response<ResponseBody>> testObserver =
                walletApi.fetchEncryptedPayload(guid, "").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertNotNull(testObserver.values().get(0));
        assertNotNull(testObserver.values().get(0).toString());
    }

    @Test
    public void fetchPairingEncryptionPasswordCall() throws Exception {
        Response<ResponseBody> call =
                walletApi.fetchPairingEncryptionPasswordCall("").execute();

        assertNotNull(call.errorBody());
        assertNotNull(call.errorBody().string());
    }

    @Test
    public void fetchPairingEncryptionPasswordObservable() throws Exception {
        final TestObserver<ResponseBody> testObserver =
                walletApi.fetchPairingEncryptionPassword("").test();

        testObserver.assertTerminated();
        testObserver.assertError(HttpException.class);
    }

//    "message":"503 Service Unavailable"
//    @Test
//    public void getAllMerchants() throws Exception {
//        final TestObserver<List<Merchant>> testObserver = walletApi.getAllMerchants().test();
//
//        testObserver.assertComplete();
//        testObserver.assertNoErrors();
//        assertNotNull(testObserver.values().get(0));
//        assertNotNull(testObserver.values().get(0).toString());
//    }

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
