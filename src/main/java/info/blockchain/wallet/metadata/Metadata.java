package info.blockchain.wallet.metadata;

import info.blockchain.BlockchainFramework;
import info.blockchain.api.MetadataEndpoints;
import info.blockchain.api.PersistentUrls;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.metadata.data.MetadataRequest;
import info.blockchain.wallet.metadata.data.MetadataResponse;
import info.blockchain.wallet.util.FormatsUtil;
import info.blockchain.wallet.util.MetadataUtil;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class Metadata {

    public static final int METADATA_VERSION = 1;

    private boolean isEncrypted = true;

    private MetadataEndpoints endpoints;
    private int type;
    private String address;
    private ECKey node;
    private byte[] encryptionKey;
    private byte[] magicHash;

    public Metadata() {
        // Empty constructor
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setNode(ECKey node) {
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

    public ECKey getNode() {
        return this.node;
    }

    private MetadataEndpoints getApiInstance() {
        if (endpoints == null) {
            endpoints = BlockchainFramework
                    .getRetrofitApiInstance()
                    .create(MetadataEndpoints.class);
        }
        return endpoints;
    }

    public void fetchMagic() throws IOException, MetadataException {

        Call<MetadataResponse> response = getApiInstance().getMetadata(address);

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
                throw new MetadataException(exe.code() + " " + exe.message());
            }
        }
    }

    /**
     * Put new metadata entry
     * @param payload JSON Stringified object
     * @throws Exception
     */
    public void putMetadata(String payload) throws IOException, InvalidCipherTextException, MetadataException {

        //Ensure json syntax is correct
        if(!FormatsUtil.getInstance().isValidJson(payload))
            throw new JSONException("Payload is not a valid json object.");

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

        Call<Void> response = getApiInstance().putMetadata(address, body);

        Response<Void> exe = response.execute();

        if (!exe.isSuccessful()) {
            throw new MetadataException(exe.code() + " " + exe.message());
        } else {
            magicHash = nextMagicHash;
        }
    }

    public String getMetadata() throws MetadataException, IOException, InvalidCipherTextException {
        return getMetadataEntry(address, isEncrypted);
    }

    public String getMetadata(String address, boolean isEncrypted) throws MetadataException, IOException, InvalidCipherTextException {
        return getMetadataEntry(address, isEncrypted);
    }

    /**
     * Get metadata entry
     */
    private String getMetadataEntry(String address, boolean isEncrypted) throws IOException,
            InvalidCipherTextException, MetadataException {

        Call<MetadataResponse> response = getApiInstance().getMetadata(address);

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
                throw new MetadataException(exe.code() + " " + exe.message());
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

        Call<Void> response = getApiInstance().deleteMetadata(address, signature);

        Response<Void> exe = response.execute();

        if (!exe.isSuccessful()) {
            throw new Exception(exe.code() + " " + exe.message());
        } else {
            magicHash = null;
        }
    }

    public static class Builder{

        //Required
        private int type;
        private DeterministicKey metaDataHDNode;

        //Optional Override
        private boolean isEncrypted = true;//default
        private byte[] encryptionKey;


        public Builder(DeterministicKey metaDataHDNode, int type){
            this.metaDataHDNode = metaDataHDNode;
            this.type = type;
        }

        public Builder setEncrypted(boolean isEncrypted){
            this.isEncrypted = isEncrypted;
            return this;
        }

        public Builder setEncryptionKey(byte[] encryptionKey){
            this.encryptionKey = encryptionKey;
            return this;
        }

        /**
         * purpose' / type' / 0' : https://meta.blockchain.info/{address} - signature used to authorize
         * purpose' / type' / 1' : sha256(private key) used as 256 bit AES key
         */
        public Metadata build() throws IOException, MetadataException {

            DeterministicKey payloadTypeNode = MetadataUtil.deriveHardened(metaDataHDNode, type);
            DeterministicKey node = MetadataUtil.deriveHardened(payloadTypeNode, 0);

            if(encryptionKey == null){
                byte[] privateKeyBuffer = MetadataUtil.deriveHardened(payloadTypeNode, 1).getPrivKeyBytes();
                encryptionKey = Sha256Hash.hash(privateKeyBuffer);
            }

            Metadata metadata = new Metadata();
            metadata.setEncrypted(isEncrypted);
            metadata.setAddress(node.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString());
            metadata.setNode(node);
            metadata.setEncryptionKey(encryptionKey);
            metadata.setType(type);
            metadata.fetchMagic();

            return metadata;
        }
    }
}