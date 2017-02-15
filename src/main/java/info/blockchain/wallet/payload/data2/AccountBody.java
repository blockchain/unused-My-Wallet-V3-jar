package info.blockchain.wallet.payload.data2;

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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class AccountBody {

    @JsonProperty("label")
    private String label;

    @JsonProperty("archived")
    private boolean archived;

    @JsonProperty("xpriv")
    private String xpriv;

    @JsonProperty("xpub")
    private String xpub;

    @JsonProperty("cache")
    private CacheBody cache;

    @JsonProperty("address_labels")
    private List<AddressLabelsBody> addressLabels;

    /////////////////////////////////////REMOVE BELOW///////////////////////////////////////////////
    // TODO: 15/02/2017 These vars shouldn't be here wtf
    protected int realIdx = -1;
    protected long amount = 0L;
    protected int idxChangeAddresses = 0;
    protected int idxReceiveAddresses = 0;

    public int getIdxChangeAddresses() {
        return idxChangeAddresses;
    }

    public void setIdxChangeAddresses(int nbChangeAddresses) {
        this.idxChangeAddresses = nbChangeAddresses;
    }

    public int getIdxReceiveAddresses() {
        return idxReceiveAddresses;
    }

    public void setIdxReceiveAddresses(int nbReceiveAddresses) {
        this.idxReceiveAddresses = nbReceiveAddresses;
    }

    public int getRealIdx() {
        return realIdx;
    }

    public void setRealIdx(int realIdx) {
        this.realIdx = realIdx;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    /////////////////////////////////////REMOVE ABOVE///////////////////////////////////////////////

    public String getLabel() {
        return label;
    }

    public AccountBody() {
        addressLabels = new ArrayList<>();
    }

    public void setLabel(String label) {
        // TODO: 15/02/2017  client side ellipse after 32 chars
        this.label = label;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getXpriv() {
        return xpriv;
    }

    public void setXpriv(String xpriv) {
        this.xpriv = xpriv;
    }

    public String getXpub() {
        return xpub;
    }

    public void setXpub(String xpub) {
        this.xpub = xpub;
    }

    public CacheBody getCache() {
        return cache;
    }

    public void setCache(CacheBody cache) {
        this.cache = cache;
    }

    public List<AddressLabelsBody> getAddressLabels() {
        return addressLabels;
    }

    public void setAddressLabels(
        List<AddressLabelsBody> addressLabels) {
        this.addressLabels = addressLabels;
    }


    public static AccountBody fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, AccountBody.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
