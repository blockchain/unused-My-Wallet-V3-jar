package info.blockchain.wallet.api.trade.coinify.data;

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
public class CoinifyTrade {

    @JsonProperty("id")
    private long id;

    @JsonProperty("traderId")
    private long traderId;

    @JsonProperty("state")
    private String state;

    @JsonProperty("inCurrency")
    private String inCurrency;

    @JsonProperty("outCurrency")
    private String outCurrency;

    @JsonProperty("inAmount")
    private double inAmount;

    @JsonProperty("outAmountExpected")
    private double outAmountExpected;

    @JsonProperty("updateTime")
    private String updateTime;

    @JsonProperty("createTime")
    private String createTime;

    @JsonProperty("transferIn")
    private CoinifyTransferIn transferIn;

    @JsonProperty("transferOut")
    private CoinifyTransferOut transferOut;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTraderId() {
        return traderId;
    }

    public void setTraderId(long traderId) {
        this.traderId = traderId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInCurrency() {
        return inCurrency;
    }

    public void setInCurrency(String inCurrency) {
        this.inCurrency = inCurrency;
    }

    public String getOutCurrency() {
        return outCurrency;
    }

    public void setOutCurrency(String outCurrency) {
        this.outCurrency = outCurrency;
    }

    public double getInAmount() {
        return inAmount;
    }

    public void setInAmount(double inAmount) {
        this.inAmount = inAmount;
    }

    public double getOutAmountExpected() {
        return outAmountExpected;
    }

    public void setOutAmountExpected(double outAmountExpected) {
        this.outAmountExpected = outAmountExpected;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public CoinifyTransferIn getTransferIn() {
        return transferIn;
    }

    public void setTransferIn(CoinifyTransferIn transferIn) {
        this.transferIn = transferIn;
    }

    public CoinifyTransferOut getTransferOut() {
        return transferOut;
    }

    public void setTransferOut(CoinifyTransferOut transferOut) {
        this.transferOut = transferOut;
    }

    public static CoinifyTrade fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, CoinifyTrade.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
