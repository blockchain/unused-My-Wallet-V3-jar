package info.blockchain.wallet.prices.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class PriceDatum {

    @JsonProperty("timestamp")
    private Long timestamp;
    @JsonProperty("price")
    private Double price;
    @JsonProperty("volume24h")
    private Double volume24h;

    public Long getTimestamp() {
        return timestamp;
    }

    @Nullable
    public Double getPrice() {
        return price;
    }

    @Nullable
    public Double getVolume24h() {
        return volume24h;
    }

}
