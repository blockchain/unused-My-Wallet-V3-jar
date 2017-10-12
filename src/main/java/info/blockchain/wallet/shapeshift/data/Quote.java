package info.blockchain.wallet.shapeshift.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Quote {

    @JsonProperty("orderId")
    private String orderId;

    //what coins are being exchanged in the form [input coin]_[output coin]  ie eth_btc
    @JsonProperty("pair")
    private String pair;

    //Address submitted in post
    @JsonProperty("withdrawal")
    private String withdrawal;

    //Amount of the output coin you will receive
    @JsonProperty("withdrawalAmount")
    private String withdrawalAmount;//Implemented as string on web

    //Deposit Address
    @JsonProperty("deposit")
    private String deposit;

    //Exact amount of input coin to send in
    @JsonProperty("depositAmount")
    private String depositAmount;//Implemented as string on web

    //Timestamp of when trade will expire
    @JsonProperty("expiration")
    private long expiration;

    //Exchange rate to be honored
    @JsonProperty("quotedRate")
    private String quotedRate;//Implemented as string on web

    @JsonProperty("returnAddress")
    private String returnAddress;

    @JsonProperty("minerFee")
    private String minerFee;//Implemented as string on web

    //Public API attached to this shift
    @JsonProperty("apiPubKey")
    private String apiPubKey;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getWithdrawal() {
        return withdrawal;
    }

    public void setWithdrawal(String withdrawal) {
        this.withdrawal = withdrawal;
    }

    public Double getWithdrawalAmount() {
        return Double.parseDouble(withdrawalAmount);
    }

    public void setWithdrawalAmount(Double withdrawalAmount) {
        this.withdrawalAmount = Double.toString(withdrawalAmount);
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public Double getDepositAmount() {
        return Double.parseDouble(depositAmount);
    }

    public void setDepositAmount(Double depositAmount) {
        this.depositAmount = Double.toString(depositAmount);
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public double getQuotedRate() {
        return Double.parseDouble(quotedRate);
    }

    public void setQuotedRate(double quotedRate) {
        this.quotedRate = Double.toString(quotedRate);
    }

    public String getReturnAddress() {
        return returnAddress;
    }

    public void setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
    }

    public Double getMinerFee() {
        return Double.parseDouble(minerFee);
    }

    public void setMinerFee(Double minerFee) {
        this.minerFee = Double.toString(minerFee);
    }

    public String getApiPubKey() {
        return apiPubKey;
    }

    public void setApiPubKey(String apiPubKey) {
        this.apiPubKey = apiPubKey;
    }

    public static Quote fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, Quote.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
