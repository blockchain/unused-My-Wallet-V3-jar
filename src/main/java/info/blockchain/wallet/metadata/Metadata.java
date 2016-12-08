package info.blockchain.wallet.metadata;

import info.blockchain.api.MetadataEndpoints;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.metadata.data.MetadataRequest;
import info.blockchain.wallet.metadata.data.MetadataResponse;
import info.blockchain.wallet.util.FormatsUtil;
import info.blockchain.wallet.util.MetadataUtil;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import retrofit2.Call;
import retrofit2.Response;

public class Metadata {

    public final static int PAYLOAD_TYPE_GUID = 0;
    public final static int PAYLOAD_TYPE_RESERVED = 1;
    public final static int PAYLOAD_TYPE_WHATS_NEW = 2;
    public final static int PAYLOAD_TYPE_BUY_SELL = 3;
    public final static int PAYLOAD_TYPE_CONTACT = 4;

    final static int METADATA_VERSION = 1;

    boolean isEncrypted = true;

    MetadataEndpoints endpoints;
    int type;
    String address;
    DeterministicKey node;
    byte[] encryptionKey;
    byte[] magicHash;

    public Metadata() {
        //no op
    }

    public void setEndpoints(MetadataEndpoints endpoints) {
        this.endpoints = endpoints;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setNode(DeterministicKey node) {
        this.node = node;
    }

    public void setEncryptionKey(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setEncrypted(boolean encrypted) {
        this.isEncrypted = encrypted;
    }

    public String getAddress() {
        return this.address;
    }

    public DeterministicKey getNode() {
        return this.node;
    }

    public void fetchMagic() throws Exception{

        Call<MetadataResponse> response = endpoints.getMetadata(address);

        Response<MetadataResponse> exe = response.execute();

        if (exe.isSuccessful()) {
            MetadataResponse body = exe.body();

            byte[] encryptedPayloadBytes = Base64.decode(exe.body().getPayload().getBytes("utf-8"));

            if(body.getPrev_magic_hash() != null){
                byte[] prevMagicBytes = Hex.decode(body.getPrev_magic_hash());
                magicHash = MetadataUtil.magic(encryptedPayloadBytes, prevMagicBytes);
            } else {
                magicHash = MetadataUtil.magic(encryptedPayloadBytes, null);
            }

        } else {
            if(exe.code() == 404) {
                magicHash = null;
            } else {
                throw new Exception(exe.code() + " " + exe.message());
            }
        }
    }

    /**
     * Put new metadata entry
     * @param payload JSON Stringified object
     * @throws Exception
     */
    public void putMetadata(String payload) throws Exception {

        //Ensure json syntax is correct
        FormatsUtil.getInstance().isValidJson(payload);

        byte[] encryptedPayloadBytes;

        if(isEncrypted){
            //base64 to buffer
            encryptedPayloadBytes = Base64.decode(AESUtil.encryptWithKey(encryptionKey, payload));
        } else {
            encryptedPayloadBytes = payload.getBytes("utf-8");
        }

        byte[] nextMagicHash = MetadataUtil.magic(encryptedPayloadBytes, magicHash);

        byte[] message = MetadataUtil.message(encryptedPayloadBytes, magicHash);

        String signature = node.signMessage(new String(Base64.encode(message)));

        MetadataRequest body = new MetadataRequest();
        body.setVersion(METADATA_VERSION);
        body.setPayload(new String(Base64.encode(encryptedPayloadBytes)));
        body.setSignature(signature);
        body.setPrev_magic_hash(magicHash != null ? Hex.toHexString(magicHash) : null);
        body.setType_id(type);

        Call<Void> response = endpoints.putMetadata(address, body);

        Response<Void> exe = response.execute();

        if (!exe.isSuccessful()) {
            throw new Exception(exe.code() + " " + exe.message());
        } else {
            magicHash = nextMagicHash;
        }
    }

    public String getMetadata() throws Exception {
        return getMetadataEntry(address);
    }

    public String getMetadata(String address) throws Exception {
        return getMetadataEntry(address);
    }

    /**
     * Get metadata entry
     */
    private String getMetadataEntry(String address) throws Exception {

        Call<MetadataResponse> response = endpoints.getMetadata(address);

        Response<MetadataResponse> exe = response.execute();

        if (exe.isSuccessful()) {

            if(isEncrypted){
                return AESUtil.decryptWithKey(encryptionKey, exe.body().getPayload());
            } else {
                return new String(Base64.decode(exe.body().getPayload()));
            }
        } else {

            if (exe.code() == 404) {
                return null;
            } else {
                throw new Exception(exe.code() + " " + exe.message());
            }
        }
    }

    /**
     * Delete metadata entry
     */
    public void deleteMetadata(String payload) throws Exception {

        byte[] encryptedPayloadBytes;

        if(isEncrypted){
            //base64 to buffer
            encryptedPayloadBytes = Base64.decode(AESUtil.encryptWithKey(encryptionKey, payload));
        } else {
            encryptedPayloadBytes = payload.getBytes("utf-8");
        }

        byte[] message = MetadataUtil.message(encryptedPayloadBytes, magicHash);

        String signature = node.signMessage(new String(Base64.encode(message)));

        Call<Void> response = endpoints.deleteMetadata(address, signature);

        Response<Void> exe = response.execute();

        if (!exe.isSuccessful()) {
            throw new Exception(exe.code() + " " + exe.message());
        } else {
            magicHash = null;
        }
    }

    static class Builder{

        //Required
        private MetadataEndpoints endpoints;
        private int type;
        private DeterministicKey rootNode;

        //Optional
        private boolean isEncrypted = true;//default

        public Builder(MetadataEndpoints endpoints, DeterministicKey rootNode, int type){
            this.endpoints = endpoints;
            this.rootNode = rootNode;
            this.type = type;
        }

        public Builder setEncrypted(boolean isEncrypted){
            this.isEncrypted = isEncrypted;
            return this;
        }

        /**
         * purpose' / type' / 0' : https://meta.blockchain.info/{address} - signature used to authorize
         * purpose' / type' / 1' : sha256(private key) used as 256 bit AES key
         */
        public Metadata build() throws Exception {

            int purposeI = MetadataUtil.getPurposeMetadata();

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

            return metadata;
        }
    }
}