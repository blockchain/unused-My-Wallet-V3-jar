package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

public class ExceptionLog implements BaseApi {

    public static final String PROD_EXCEPTION_LOG_URL = PROTOCOL + SERVER_ADDRESS + "exception_log?";

    public void logException(String message) throws Exception {

        StringBuilder builder = new StringBuilder();
        builder.append(PROD_EXCEPTION_LOG_URL)
                .append("device=").append("android")
                .append("&message=").append(message);

        WebUtil.getInstance().getURL(builder.toString());
    }
}
