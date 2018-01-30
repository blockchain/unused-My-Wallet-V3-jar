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
import java.util.List;

/**
 * <p> Generic coin data that can be stored in blockchain.info KV store. </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        creatorVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE)
public class GenericMetadataWallet {

    @JsonProperty("default_account_idx")
    private int defaultAcccountIdx;

    @JsonProperty("has_seen")
    private boolean hasSeen;

    @JsonProperty("accounts")
    private List<GenericMetadataAccount> accounts;

    public GenericMetadataWallet() {
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

    public List<GenericMetadataAccount> getAccounts() {
        return accounts;
    }

    public void setDefaultAcccountIdx(int defaultAcccountIdx) {
        this.defaultAcccountIdx = defaultAcccountIdx;
    }

    public void setHasSeen(boolean hasSeen) {
        this.hasSeen = hasSeen;
    }

    public void addAccount(GenericMetadataAccount account) {
        accounts.add(account);
    }

    public void setAccounts(List<GenericMetadataAccount> accounts) {
        this.accounts = accounts;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(returnSafeClone());
    }

    public static GenericMetadataWallet fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, GenericMetadataWallet.class);
    }

    /**
     * Returns a deep clone of the current object, but strips out any xPubs from the {@link
     * GenericMetadataAccount} objects, as we're not currently storing them in metadata but may be
     * serialising them in-app.
     *
     * @return A {@link GenericMetadataWallet} with xPubs removed
     */
    private GenericMetadataWallet returnSafeClone() {
        List<GenericMetadataAccount> safeAccounts = new ArrayList<>();
        for (GenericMetadataAccount account : getAccounts()) {
            GenericMetadataAccount safeClone =
                    new GenericMetadataAccount(account.getLabel(), account.isArchived());
            safeAccounts.add(safeClone);
        }

        final GenericMetadataWallet wallet = new GenericMetadataWallet();
        wallet.setDefaultAcccountIdx(getDefaultAcccountIdx());
        wallet.setHasSeen(isHasSeen());
        wallet.setAccounts(safeAccounts);
        return wallet;
    }
}
