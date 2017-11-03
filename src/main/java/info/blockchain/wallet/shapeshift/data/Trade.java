package info.blockchain.wallet.shapeshift.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.ParseException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Trade {

    @JsonProperty("status")
    private String status;

    @JsonProperty("hashIn")
    private String hashIn;

    @JsonProperty("hashOut")
    private String hashOut;

    //Ignored - web-wallet js time string
//    @JsonProperty("time")
//    private String time;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("quote")
    private Quote quote;

    public STATUS getStatus() {

        // TODO: 11/10/2017 There must be an easier way to do this.
        if (status.equals(STATUS.NO_DEPOSITS.toString())) {
            return STATUS.NO_DEPOSITS;
        } else if (status.equals(STATUS.RECEIVED.toString())) {
            return STATUS.RECEIVED;
        } else if (status.equals(STATUS.COMPLETE.toString())) {
            return STATUS.COMPLETE;
        } else if (status.equals(STATUS.RESOLVED.toString())) {
            return STATUS.RESOLVED;
        } else {
            return STATUS.FAILED;
        }
    }

    public void setStatus(STATUS status) {
        this.status = status.toString();
    }

    public String getHashIn() {
        return hashIn;
    }

    public void setHashIn(String hashIn) {
        this.hashIn = hashIn;
    }

    public String getHashOut() {
        return hashOut;
    }

    public void setHashOut(String hashOut) {
        this.hashOut = hashOut;
    }

    public long getTimestamp() throws ParseException {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public static Trade fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, Trade.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public enum STATUS {
        NO_DEPOSITS("no_deposits"),
        RECEIVED("received"),
        COMPLETE("complete"),
        FAILED("failed"),
        RESOLVED("resolved");

        private final String text;

        private STATUS(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
