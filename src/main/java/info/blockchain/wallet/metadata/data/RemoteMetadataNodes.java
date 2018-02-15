package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteMetadataNodes {

    /**
     * Private bytes as HEX
     */
    private String metadata;
    private String mdid;
    //Add any future metadata node derivations here

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getMdid() {
        return mdid;
    }

    public void setMdid(String mdid) {
        this.mdid = mdid;
    }

    @JsonIgnore
    public static RemoteMetadataNodes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, RemoteMetadataNodes.class);
    }

    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    @JsonIgnore
    public boolean isAllNodesAvailable() {
        return (metadata != null && mdid != null);
    }
}
