package info.blockchain.wallet.metadata;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
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

    public static final short FETCH_MAGIC_HASH_ATTEMPT_LIMIT = 1;
    private short attempt;

    public Metadata() {
        attempt = FETCH_MAGIC_HASH_ATTEMPT_LIMIT;
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

    public byte[] getMagicHash() {
        return magicHash;
    }

    @VisibleForTesting
    void setMagicHash(byte[] magicHash) {
        this.magicHash = magicHash;
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

            if (body.getPrevMagicHash() != null) {
                byte[] prevMagicBytes = Hex.decode(body.getPrevMagicHash());
                magicHash = MetadataUtil.magic(encryptedPayloadBytes, prevMagicBytes);
            } else {
                magicHash = MetadataUtil.magic(encryptedPayloadBytes, null);
            }

        } else {
            if (exe.code() == 404) {
                magicHash = null;
            } else {
                throw new MetadataException(exe.code() + " " + exe.message());
            }
        }
    }

    /**
     * Put new metadata entry
     *
     * @param payload JSON Stringified object
     */
    public void putMetadata(String payload) throws IOException, InvalidCipherTextException,
            MetadataException {

        //Ensure json syntax is correct
        if (!FormatsUtil.isValidJson(payload))
            throw new JSONException("Payload is not a valid json object.");

        byte[] encryptedPayloadBytes;

        if (isEncrypted) {
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
        body.setPrevMagicHash(magicHash != null ? Hex.toHexString(magicHash) : null);
        body.setTypeId(type);

        Call<Void> response = getApiInstance().putMetadata(address, body);

        Response<Void> exe = response.execute();

        if (!exe.isSuccessful()) {
            if (exe.code() == 401 && attempt > 0) {
                // Unauthorized - Possible cross platform clash
                // Fetch magic hash and retry
                fetchMagic();
                attempt--;
                putMetadata(payload);
            } else {
                throw new MetadataException(exe.code() + " " + exe.message());
            }
        } else {
            attempt = FETCH_MAGIC_HASH_ATTEMPT_LIMIT;
            magicHash = nextMagicHash;
        }
    }

    public String getMetadata() throws MetadataException, IOException, InvalidCipherTextException {
        return getMetadataEntry(address, isEncrypted).orNull();
    }

    public String getMetadata(String address, boolean isEncrypted) throws MetadataException,
            IOException,
            InvalidCipherTextException {
        return getMetadataEntry(address, isEncrypted).orNull();
    }

    // Handling null in RxJava 2.0
    public Optional<String> getMetadataOptional()  throws MetadataException, IOException, InvalidCipherTextException  {
        return getMetadataEntry(address, isEncrypted);
    }

    /**
     * Get metadata entry
     */
    private Optional<String> getMetadataEntry(String address, boolean isEncrypted) throws MetadataException,
            IOException,
            InvalidCipherTextException {

        Call<MetadataResponse> response = getApiInstance().getMetadata(address);

        Response<MetadataResponse> exe = response.execute();

        if (exe.isSuccessful()) {

            if (isEncrypted) {
                return Optional.of(AESUtil.decryptWithKey(encryptionKey, exe.body().getPayload()));
            } else {
                return Optional.of(new String(Base64.decode(exe.body().getPayload())));
            }
        } else {

            if (exe.code() == 404) {
                return Optional.absent();
            } else {
                throw new MetadataException(exe.code() + " " + exe.message());
            }
        }
    }

    /**
     * Delete metadata entry
     */
    public void deleteMetadata(String payload) throws IOException, InvalidCipherTextException,
            MetadataException {

        byte[] encryptedPayloadBytes;

        if (isEncrypted) {
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
            throw new MetadataException(exe.code() + " " + exe.message());
        } else {
            magicHash = null;
        }
    }

    public static class Builder {

        //Required
        private int type;
        private DeterministicKey metaDataHDNode;

        //Optional Override
        private boolean isEncrypted = true;//default
        private byte[] encryptionKey;


        public Builder(DeterministicKey metaDataHDNode, int type) {
            this.metaDataHDNode = metaDataHDNode;
            this.type = type;
        }

        public Builder setEncrypted(boolean isEncrypted) {
            this.isEncrypted = isEncrypted;
            return this;
        }

        public Builder setEncryptionKey(byte[] encryptionKey) {
            this.encryptionKey = encryptionKey;
            return this;
        }

        /**
         * purpose' / type' / 0' : https://meta.blockchain.info/{address} - signature used to
         * authorize purpose' / type' / 1' : sha256(private key) used as 256 bit AES key
         */
        public Metadata build() throws IOException, MetadataException {

            DeterministicKey payloadTypeNode = MetadataUtil.deriveHardened(metaDataHDNode, type);
            DeterministicKey node = MetadataUtil.deriveHardened(payloadTypeNode, 0);

            if (encryptionKey == null) {
                byte[] privateKeyBuffer = MetadataUtil.deriveHardened(payloadTypeNode, 1).getPrivKeyBytes();
                encryptionKey = Sha256Hash.hash(privateKeyBuffer);
            }

            Metadata metadata = new Metadata();
            metadata.setEncrypted(isEncrypted);
            metadata.setAddress(node.toAddress(PersistentUrls.getInstance().getBitcoinParams()).toString());
            metadata.setNode(node);
            metadata.setEncryptionKey(encryptionKey);
            metadata.setType(type);
            metadata.fetchMagic();

            return metadata;
        }
    }
}