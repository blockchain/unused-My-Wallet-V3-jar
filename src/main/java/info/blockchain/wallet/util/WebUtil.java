package info.blockchain.wallet.util;

import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebUtil {

    private static final int DEFAULT_REQUEST_RETRY = 2;
    private static final int DEFAULT_REQUEST_TIMEOUT = 60000;

    private static WebUtil instance = null;

    private WebUtil() {
    }

    public static WebUtil getInstance() {

        if (instance == null) {
            instance = new WebUtil();
        }

        return instance;
    }

    public String postURL(String request, String urlParameters, int requestRetry) throws Exception {
        return postURLCall(request, urlParameters, requestRetry, "application/x-www-form-urlencoded");
    }

    public String postURL(String request, String urlParameters) throws Exception {
        return postURLCall(request, urlParameters, DEFAULT_REQUEST_RETRY, "application/x-www-form-urlencoded");
    }

    public String postURLJson(String request, String urlParameters) throws Exception {
        return this.postURLCall(request, urlParameters, 2, "application/json");
    }

    private String postURLCall(String request, String urlParameters, int requestRetry, String contentType) throws Exception {

        String error = null;

        for (int ii = 0; ii < requestRetry; ++ii) {
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", contentType);
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36");

                connection.setUseCaches(false);

                connection.setConnectTimeout(DEFAULT_REQUEST_TIMEOUT);
                connection.setReadTimeout(DEFAULT_REQUEST_TIMEOUT);

                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                connection.setInstanceFollowRedirects(false);

                if (connection.getResponseCode() == 200) {
//					Log.d("postURL", "return code 200");
                    return IOUtils.toString(connection.getInputStream(), "UTF-8");
                } else {
                    error = IOUtils.toString(connection.getErrorStream(), "UTF-8");
//					Log.d("postURL", "return code " + error);
                }

                // Sleep unless last request
                if (ii != requestRetry - 1) {
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                throw new Exception("Network error" + e.getMessage());
            } finally {
                connection.disconnect();
            }
        }

        throw new Exception("Invalid Response " + error);
    }

    @Deprecated
    public String getURL(String URL, String cookie) throws Exception {

        return getURLCall(URL, cookie);
    }

    @Deprecated
    public String getURL(String URL) throws Exception {

        return getURLCall(URL, null);
    }

    /**
     * This can return an error string instead of throwing an exception. Use {@link #getRequest(String, String)}
     * for proper error handling instead once the calling method handles errors appropriately and the endpoint
     * being called returns correctly formatted error codes.
     */
    @Deprecated
    private String getURLCall(String URL, String cookie) throws Exception {

        URL url = new URL(URL);

        String error = null;

        for (int ii = 0; ii < DEFAULT_REQUEST_RETRY; ++ii) {

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36");

                connection.setConnectTimeout(DEFAULT_REQUEST_TIMEOUT);
                connection.setReadTimeout(DEFAULT_REQUEST_TIMEOUT);
                if (cookie != null) connection.setRequestProperty("cookie", cookie);
                connection.setInstanceFollowRedirects(false);

                connection.connect();

                if (connection.getResponseCode() == 200) {
                    return IOUtils.toString(connection.getInputStream(), "UTF-8");
                } else {
                    error = IOUtils.toString(connection.getErrorStream(), "UTF-8");
                }

            } catch (Exception e) {
                throw new Exception("Network error" + e.getMessage());
            } finally {
                connection.disconnect();
            }
        }

        return error;
    }

    public String getRequest(String url) throws Exception {
        return getRequestCall(url, null);
    }

    public String getRequest(String url, String cookie) throws Exception {
        return getRequestCall(url, cookie);
    }

    private String getRequestCall(String URL, String cookie) throws Exception {

        URL url = new URL(URL);

        String error = null;

        for (int ii = 0; ii < DEFAULT_REQUEST_RETRY; ++ii) {

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36");

                connection.setConnectTimeout(DEFAULT_REQUEST_TIMEOUT);
                connection.setReadTimeout(DEFAULT_REQUEST_TIMEOUT);
                if (cookie != null) connection.setRequestProperty("cookie", cookie);
                connection.setInstanceFollowRedirects(false);

                connection.connect();

                if (connection.getResponseCode() == 200) {
                    return IOUtils.toString(connection.getInputStream(), "UTF-8");
                } else {
                    error = IOUtils.toString(connection.getErrorStream(), "UTF-8");
                }

            } catch (Exception e) {
                throw new Exception("Network error" + e.getMessage());
            } finally {
                connection.disconnect();
            }
        }

        throw new Exception(error);
    }

    public String getCookie(String url, String cname) throws Exception {

        String ret = null;

        URLConnection conn = new URL(url).openConnection();

        Map<String, List<String>> headerFields = conn.getHeaderFields();

        Set<String> headerFieldsSet = headerFields.keySet();

        for (String headerFieldKey : headerFieldsSet) {

            if ("Set-Cookie".equalsIgnoreCase(headerFieldKey)) {

                List<String> headerFieldValue = headerFields.get(headerFieldKey);

                for (String headerValue : headerFieldValue) {

                    String[] fields = headerValue.split(";\\s*");

                    String cookieValue = fields[0];

                    if (cookieValue.startsWith(cname + "=")) {
                        ret = cookieValue.substring(cname.length() + 1);
                    }
                }
            }
        }

        return ret;
    }

}