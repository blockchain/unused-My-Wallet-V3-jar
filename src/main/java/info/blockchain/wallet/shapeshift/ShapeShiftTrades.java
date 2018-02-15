package info.blockchain.wallet.shapeshift;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.metadata.Metadata;
import info.blockchain.wallet.shapeshift.data.State;
import info.blockchain.wallet.shapeshift.data.Trade;
import info.blockchain.wallet.util.MetadataUtil;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.bitcoinj.crypto.DeterministicKey;
import org.spongycastle.crypto.InvalidCipherTextException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShapeShiftTrades {

    private static final int METADATA_TYPE_EXTERNAL = 6;

    @JsonProperty("trades")
    private List<Trade> trades;
    @JsonProperty("USAState")
    private State state;

    private Metadata metadata;

    public ShapeShiftTrades() {
    }

    /**
     * Creates new shapeshift trade metadata.
     *
     * @param walletMasterKey DeterministicKey of root node
     */
    public ShapeShiftTrades(DeterministicKey walletMasterKey)
            throws IOException, MetadataException, NoSuchAlgorithmException {

        DeterministicKey metaDataHDNode = MetadataUtil.deriveMetadataNode(walletMasterKey);

        this.trades = new ArrayList<>();
        this.metadata = getShapeShiftMetadataNode(metaDataHDNode);
    }

    /**
     * Loads existing trades from derived trades metadata node.
     *
     * @return Existing shapeshift trades or Null if no existing trades found.
     * @throws InvalidCipherTextException MetadataHdNode encryption/decryption error
     */
    public static ShapeShiftTrades load(DeterministicKey metaDataHDNode) throws
            MetadataException,
            IOException,
            InvalidCipherTextException {

        Metadata metadata = getShapeShiftMetadataNode(metaDataHDNode);
        String walletJson = metadata.getMetadata();

        if (walletJson != null) {
            ShapeShiftTrades tradeData = fromJson(walletJson);
            tradeData.metadata = metadata;
            return tradeData;
        } else {
            return null;
        }
    }

    private static Metadata getShapeShiftMetadataNode(DeterministicKey metaDataHDNode)
            throws IOException, MetadataException {
        return new Metadata.Builder(metaDataHDNode, METADATA_TYPE_EXTERNAL).build();
    }

    public synchronized void save() throws
            IOException,
            MetadataException,
            InvalidCipherTextException {
        metadata.putMetadata(toJson());
    }

    public static ShapeShiftTrades fromJson(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        return mapper.readValue(json, ShapeShiftTrades.class);
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public List<Trade> getTrades() {
        return trades;
    }

    @JsonProperty("USAState")
    public State getUsState() {
        return state;
    }

    public synchronized void setTrades(List<Trade> trades) {
        this.trades = trades;
    }

    @JsonProperty("USAState")
    public void setUsState(State state) {
        this.state = state;
    }

}
