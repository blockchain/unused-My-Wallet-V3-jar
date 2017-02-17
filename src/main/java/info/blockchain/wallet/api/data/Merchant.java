package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class Merchant {

    @JsonIgnore
    public static final int HEADING_CAFE = 1;
    @JsonIgnore
    public static final int HEADING_BAR = 2;
    @JsonIgnore
    public static final int HEADING_RESTAURANT = 3;
    @JsonIgnore
    public static final int HEADING_SPEND = 4;
    @JsonIgnore
    public static final int HEADING_ATM = 5;

    @JsonProperty("id")
    public int id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("address")
    public String address;

    @JsonProperty("city")
    public String city;

    @JsonProperty("postal_code")
    public String postalCode;

    @JsonProperty("phone")
    public String phone;

    @JsonProperty("website")
    public String website;

    @JsonProperty("latitude")
    public double latitude;

    @JsonProperty("longitude")
    public double longitude;

    @JsonProperty("approved")
    public boolean approved;

    @JsonProperty("blockchain_merchant")
    public boolean blockchainMerchant;

    @JsonProperty("featured_merchant")
    public boolean featuredMerchant;

    @JsonProperty("description")
    public String description;

    @JsonProperty("category_id")
    public int categoryId;

    public static int getHeadingCafe() {
        return HEADING_CAFE;
    }

    public static int getHeadingBar() {
        return HEADING_BAR;
    }

    public static int getHeadingRestaurant() {
        return HEADING_RESTAURANT;
    }

    public static int getHeadingSpend() {
        return HEADING_SPEND;
    }

    public static int getHeadingAtm() {
        return HEADING_ATM;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isBlockchainMerchant() {
        return blockchainMerchant;
    }

    public boolean isFeaturedMerchant() {
        return featuredMerchant;
    }

    public String getDescription() {
        return description;
    }

    public int getCategoryId() {
        return categoryId;
    }

    @JsonIgnore
    public static Merchant fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, Merchant.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

}
