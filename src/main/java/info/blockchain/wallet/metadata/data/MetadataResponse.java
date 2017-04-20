package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataResponse {

    private int version;
    private String payload;
    private String signature;
    private String prevMagicHash;
    private int typeId;
    private long createdAt;
    private long updatedAt;
    private String address;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @JsonProperty("prev_magic_hash")
    public String getPrevMagicHash() {
        return prevMagicHash;
    }

    @JsonProperty("prev_magic_hash")
    public void setPrevMagicHash(String prevMagicHash) {
        this.prevMagicHash = prevMagicHash;
    }

    @JsonProperty("type_id")
    public int getTypeId() {
        return typeId;
    }

    @JsonProperty("type_id")
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    @JsonProperty("created_at")
    public long getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("updated_at")
    public long getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
