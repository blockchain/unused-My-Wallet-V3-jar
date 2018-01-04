package info.blockchain.wallet.coin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>
 *     Generic coin data that can be stored in blockchain.info KV store.
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class AbstractCoinData {

    @JsonProperty("default_account_idx")
    private int defaultAcccountIdx;

    @JsonProperty("has_seen")
    private boolean hasSeen;

    @JsonProperty("accounts")
    private ArrayList<AbstractCoinAccount> accounts;

    public AbstractCoinData() {
        accounts = new ArrayList<>();
        defaultAcccountIdx = 0;
        hasSeen = false;
    }

    public int getDefaultAcccountIdx() {
        return defaultAcccountIdx;
    }

    public boolean isHasSeen() {
        return hasSeen;
    }

    public ArrayList<AbstractCoinAccount> getAccounts() {
        return accounts;
    }

    public void setDefaultAcccountIdx(int defaultAcccountIdx) {
        this.defaultAcccountIdx = defaultAcccountIdx;
    }

    public void setHasSeen(boolean hasSeen) {
        this.hasSeen = hasSeen;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public static AbstractCoinData fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, AbstractCoinData.class);
    }
}
