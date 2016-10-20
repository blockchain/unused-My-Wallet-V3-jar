package info.blockchain.wallet.payload;

import info.blockchain.wallet.util.CharSequenceX;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BlockchainWalletTest {

    //Wallet json/string as returned from server using guid and sharedkey
    JSONArray v1Wallets;
    JSONArray v2Wallets;
    JSONArray v3Wallets;
    JSONArray v4Wallets;

    //Wallet Credentials
    JSONArray v1Wallets_credentials;
    JSONArray v2Wallets_credentials;
    JSONArray v3Wallets_credentials;
    JSONArray v4Wallets_credentials;

    //Actual payload
    JSONArray v1Wallets_payload;
    JSONArray v2Wallets_payload;
    JSONArray v3Wallets_payload;
    JSONArray v4Wallets_payload;

    @Before
    public void setUp() throws Exception {

        URI uri = PayloadTest.class.getClassLoader().getResource("wallet-data.json").toURI();
        String string = new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));

        JSONObject jsonArray = new JSONObject(string);
        v1Wallets = jsonArray.getJSONArray("v1");
        v2Wallets = jsonArray.getJSONArray("v2");
        v3Wallets = jsonArray.getJSONArray("v3");

        v1Wallets_payload = jsonArray.getJSONArray("v1_payload");
        v2Wallets_payload = jsonArray.getJSONArray("v2_payload");
        v3Wallets_payload = jsonArray.getJSONArray("v3_payload");

        v1Wallets_credentials = jsonArray.getJSONArray("v1_credentials");
        v2Wallets_credentials = jsonArray.getJSONArray("v2_credentials");
        v3Wallets_credentials = jsonArray.getJSONArray("v3_credentials");
    }

    @Test
    public void decryptAndParsePayload_v1() throws Exception {
        System.out.println("-----------v1------------");
        decryptAndParsePayload(v1Wallets, v1Wallets_credentials, v1Wallets_payload);
    }

    @Test
    public void decryptAndParsePayload_v2() throws Exception {
        System.out.println("-----------v2------------");
        decryptAndParsePayload(v2Wallets, v2Wallets_credentials, v2Wallets_payload);
    }

    @Test
    public void decryptAndParsePayload_v3() throws Exception {
        System.out.println("-----------v3------------");
        decryptAndParsePayload(v3Wallets, v3Wallets_credentials, v3Wallets_payload);
    }


    private void decryptAndParsePayload(JSONArray wallet, JSONArray walletCredentials, JSONArray walletPayload) throws Exception {

        for (int i = 0; i < wallet.length(); i++) {

            String walletString = wallet.getJSONObject(i).toString();

            JSONObject credentialsJson = walletCredentials.getJSONObject(i);
            JSONObject payloadJson = walletPayload.getJSONObject(i);

            String password = credentialsJson.getString("password");

            BlockchainWallet bciWallet = new BlockchainWallet(walletString, new CharSequenceX(password));


            assertThat(bciWallet.getVersion(), is(credentialsJson.getDouble("version")));
            assertThat(bciWallet.getPbkdf2Iterations(), is(credentialsJson.getInt("iterations")));
            assertThat(bciWallet.getVersion(), is(credentialsJson.getDouble("version")));

            assertThat(bciWallet.getPayload().getGuid(), is(credentialsJson.getString("guid")));
            assertThat(bciWallet.getPayload().getSharedKey(), is(credentialsJson.getString("sharedKey")));

            JSONArray keys = payloadJson.getJSONArray("keys");

            for (int j = 0; j < keys.length(); j++) {
                JSONObject json = keys.getJSONObject(j);

                assertThat(bciWallet.getPayload().getLegacyAddressList().get(j).getEncryptedKey(), is(json.getString("priv")));
                assertThat(bciWallet.getPayload().getLegacyAddressList().get(j).getAddress(), is(json.getString("addr")));
            }
        }
    }
}