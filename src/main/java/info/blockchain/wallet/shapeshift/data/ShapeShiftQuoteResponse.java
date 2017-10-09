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
public class ShapeShiftQuoteResponse {

    //what coins are being exchanged in the form [input coin]_[output coin]  ie eth_btc
    @JsonProperty("pair")
    private String pair;

    //Amount of the output coin you will receive
    @JsonProperty("withdrawalAmount")
    private Double withdrawalAmount;

    //Exact amount of input coin to send in
    @JsonProperty("depositAmount")
    private Double depositAmount;

    //Timestamp of when trade will expire
    @JsonProperty("expiration")
    private long expiration;

    //Excahnge rate to be honored
    @JsonProperty("quotedRate")
    private double quotedRate;

    //Public API attached to this shift
    @JsonProperty("minerFee")
    private double minerFee;

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public Double getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(Double withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    public Double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(Double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public double getQuotedRate() {
        return quotedRate;
    }

    public void setQuotedRate(double quotedRate) {
        this.quotedRate = quotedRate;
    }

    public double getMinerFee() {
        return minerFee;
    }

    public void setMinerFee(double minerFee) {
        this.minerFee = minerFee;
    }

    public static ShapeShiftQuoteResponse fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, ShapeShiftQuoteResponse.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
