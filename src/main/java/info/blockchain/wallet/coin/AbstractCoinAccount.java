package info.blockchain.wallet.coin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * <p>
 *     Generic coin account data that can be stored in blockchain.info KV store.
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class AbstractCoinAccount {

    @JsonProperty("label")
    private String label;

    @JsonProperty("archived")
    private boolean archived;

    public AbstractCoinAccount() {
    }

    public AbstractCoinAccount(String label, boolean archived) {
        this.label = label;
        this.archived = archived;
    }

    public String getLabel() {
        return label;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public static AbstractCoinAccount fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, AbstractCoinAccount.class);
    }
}
