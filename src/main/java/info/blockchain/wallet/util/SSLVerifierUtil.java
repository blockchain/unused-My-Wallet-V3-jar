package info.blockchain.wallet.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

// openssl s_client -showcerts -connect blockchain.info:443

public class SSLVerifierUtil {

    private static SSLVerifierUtil instance = null;

    // DER encoded public key
    private final String PUB_KEY = "30820122300d06092a864886f70d01010105000382010f003082010a0282010100bff56f562096307165320b0f04ff30e3f7d7e7a2813a35c16bfbe549c23f2a5d0388818fc0f9326a9679322fd7a6d4a1f2c4d45129c8641f6a3e7d9175938f050352a1cf09440399a36a358a846e4b5ef43baafbcb6af9f3615a7a49aae497cfeaaeb943e0175bab546abacc60b29c9bb7f588c62ac81e21038e760f044c07fe6d8a1cba4f8b5e9835bb8eddec79d506dc47fd73030630bf1af7bd70352ced281efae1675e70a6918d98645ebc389d2169ff72a82c7ff7a6328f0cd337197d87e208d2bc8cdd21182157fcb12a6db697dbd62b76800debef8feea2da2a5e074feea56af52f4300c17892018f7584eb5d4946c10156a85746ae8eacc5ebe112df0203010001";

    private SSLVerifierUtil() { ; }

    public static SSLVerifierUtil getInstance() {

        if(instance == null) {
            instance = new SSLVerifierUtil();
        }

        return instance;
    }

    public boolean isValidHostname() {

        int responseCode = -1;
        boolean ret = false;

        DefaultHttpClient client = new DefaultHttpClient();
        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        HostnameVerifier verifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
        socketFactory.setHostnameVerifier((X509HostnameVerifier)verifier);
        registry.register(new Scheme("https", socketFactory, 443));
        SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
        DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
        HttpsURLConnection.setDefaultHostnameVerifier(verifier);
        HttpPost httpPost = new HttpPost(WebUtil.VALIDATE_SSL_URL);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            responseCode = response.getStatusLine().getStatusCode();
            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean certificateIsPinned() {

        int responseCode = -1;
        TrustManager tm[] = null;

        try {

            byte[] secret = null;

            tm = new TrustManager[]{ new PubKeyManager() };
            assert (null != tm);

            SSLContext context = SSLContext.getInstance("TLS");
            assert (null != context);
            context.init(null, tm, null);

            URL url = new URL(WebUtil.VALIDATE_SSL_URL);
            assert (null != url);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            assert (null != connection);

            connection.setSSLSocketFactory(context.getSocketFactory());
            InputStreamReader instream = new InputStreamReader(connection.getInputStream());
            assert (null != instream);

            responseCode = connection.getResponseCode();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if(responseCode != 200) {
            return true;
        }
        else {
            return ((PubKeyManager)(tm[0])).isPinned();
        }

    }

    private class PubKeyManager implements X509TrustManager {

        private boolean pinned = false;

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            if (chain == null) {
                // X509Certificate array is null
                pinned = false;
                return;
            }

            if (!(chain.length > 0)) {
                // X509Certificate is empty
                pinned = false;
                return;
            }

            if (!(null != authType && authType.equalsIgnoreCase("ECDHE_RSA"))) {
                // AuthType is not ECDHE_RSA
                pinned = false;
                return;
            }

            // Perform customary SSL/TLS checks
            TrustManagerFactory tmf;
            try {
                tmf = TrustManagerFactory.getInstance("X509");
                tmf.init((KeyStore) null);

                for (TrustManager trustManager : tmf.getTrustManagers()) {
                    ((X509TrustManager) trustManager).checkServerTrusted(chain, authType);
                }

            } catch (Exception e) {
                throw new CertificateException(e);
            }

            RSAPublicKey pubkey = (RSAPublicKey)chain[0].getPublicKey();
            String encoded = new BigInteger(1, pubkey.getEncoded()).toString(16);

            if (PUB_KEY.equalsIgnoreCase(encoded)) {
                pinned = true;
            }
            else {
                pinned = false;
            }

        }

        public void checkClientTrusted(X509Certificate[] xcs, String string) { ; }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isPinned() {
            return pinned;
        }

    }

}
