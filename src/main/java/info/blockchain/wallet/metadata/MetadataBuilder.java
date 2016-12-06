package info.blockchain.wallet.metadata;

import info.blockchain.api.MetadataEndpoints;
import info.blockchain.wallet.util.MetadataUtil;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;

public class MetadataBuilder{

    public static final String PURPOSE_BASIC = "metadata";
    public static final String PURPOSE_SHARED = "mdid";

    private MetadataEndpoints endpoints;
    private int type;
    private DeterministicKey rootNode;
    private boolean isEncrypted = true;//default
    private String purpose;

    public MetadataBuilder(MetadataEndpoints endpoints){
        this.endpoints = endpoints;
    }

    public MetadataBuilder setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public MetadataBuilder setType(int type) {
        this.type = type;
        return this;
    }

    public MetadataBuilder setRootNode(DeterministicKey rootNode){
        this.rootNode = rootNode;
        return this;
    }

    public MetadataBuilder setEncrypted(boolean isEncrypted){
        this.isEncrypted = isEncrypted;
        return this;
    }

    /**
     * purpose' / type' / 0' : https://meta.blockchain.info/{address} - signature used to authorize
     * purpose' / type' / 1' : sha256(private key) used as 256 bit AES key
     */
    public <T> T build() throws Exception {

        int purposeI = MetadataUtil.getPurpose(purpose);

        if (purpose.equals(PURPOSE_BASIC)) {

            DeterministicKey metaDataHDNode = MetadataUtil.deriveHardened(rootNode, purposeI);
            DeterministicKey payloadTypeNode = MetadataUtil.deriveHardened(metaDataHDNode, type);
            DeterministicKey node = MetadataUtil.deriveHardened(payloadTypeNode, 0);

            byte[] privateKeyBuffer = MetadataUtil.deriveHardened(payloadTypeNode, 1).getPrivKeyBytes();

            Metadata metadata = new Metadata();
            metadata.setEndpoints(endpoints);
            metadata.setEncrypted(isEncrypted);
            metadata.setAddress(node.toAddress(MainNetParams.get()).toString());
            metadata.setNode(node);
            metadata.setEncryptionKey(Sha256Hash.hash(privateKeyBuffer));
            metadata.setType(type);// TODO: 05/12/2016 Not sure if this type relates to PUT body type
            metadata.fetchMagic();

            return (T) metadata;

        } else if (purpose.equals(PURPOSE_SHARED)){

            DeterministicKey node = MetadataUtil.deriveHardened(rootNode, purposeI);

            MetadataShared metadata = new MetadataShared();
            metadata.setEndpoints(endpoints);
            metadata.setAddress(node.toAddress(MainNetParams.get()).toString());
            metadata.setNode(node);
            metadata.setXpub(node.serializePubB58(MainNetParams.get()));
            metadata.authorize();
            metadata.publishXpub();

            return (T) metadata;
        }

        return null;
    }
}
