package info.blockchain.wallet.payload.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class Options {

    private static long DEFAULT_FEE_PER_KB = 10000L;
    private static long DEFAULT_LOGOUT_TIME = 600000L;
    private static boolean DEFAULT_HTML5_NOTIFICATIONS = false;

    @JsonProperty("pbkdf2_iterations")
    private int pbkdf2Iterations;

    @JsonProperty("fee_per_kb")
    private long feePerKb;

    @JsonProperty("html5_notifications")
    private boolean html5Notifications;

    @JsonProperty("logout_time")
    private long logoutTime;

    public int getPbkdf2Iterations() {
        return pbkdf2Iterations;
    }

    public void setPbkdf2Iterations(int pbkdf2Iterations) {
        this.pbkdf2Iterations = pbkdf2Iterations;
    }

    public long getFeePerKb() {
        return feePerKb;
    }

    public void setFeePerKb(long feePerKb) {
        this.feePerKb = feePerKb;
    }

    public boolean isHtml5Notifications() {
        return html5Notifications;
    }

    public void setHtml5Notifications(boolean html5Notifications) {
        this.html5Notifications = html5Notifications;
    }

    public long getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(long logoutTime) {
        this.logoutTime = logoutTime;
    }

    public static Options fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, Options.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public static Options getDefaultOptions() {
        Options defaultOptions = new Options();
        defaultOptions.setPbkdf2Iterations(WalletWrapper.DEFAULT_PBKDF2_ITERATIONS_V2);
        defaultOptions.setHtml5Notifications(DEFAULT_HTML5_NOTIFICATIONS);
        defaultOptions.setLogoutTime(DEFAULT_LOGOUT_TIME);
        defaultOptions.setFeePerKb(DEFAULT_FEE_PER_KB);

        return defaultOptions;
    }
}
