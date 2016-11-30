package info.blockchain.wallet.metadata;

import info.blockchain.api.MetadataEndpoints;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.metadata.data.MetadataRequest;
import info.blockchain.wallet.metadata.data.MetadataResponse;
import info.blockchain.wallet.util.MetadataUtil;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Metadata {

    public final static int PAYLOAD_TYPE_GUID = 0;
    public final static int PAYLOAD_TYPE_RESERVED = 1;
    public final static int PAYLOAD_TYPE_WHATS_NEW = 2;
    public final static int PAYLOAD_TYPE_BUY_SELL = 3;
    public final static int PAYLOAD_TYPE_CONTACT = 4;

    final static int METADATA_VERSION = 1;

    boolean isEncrypted;

    private MetadataEndpoints endpoints;
    private int type;
    private String address;
    private DeterministicKey node;
    private byte[] encryptionKey;
    private byte[] magicHash;

    public Metadata() {
        //no op
    }

    /**
     * Constructor for metadata service
     */
    public Metadata(DeterministicKey masterHDNode, int type, boolean isEncrypted) throws Exception{

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MetadataEndpoints.API_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        endpoints = retrofit.create(MetadataEndpoints.class);

        this.isEncrypted = isEncrypted;
        setMetadataNode(type, masterHDNode);
        fetch();
    }

    /**
     * Set original metadata node and address
     * Set encryption key:
     * purpose' / type' / 0' : https://meta.blockchain.info/{address} - signature used to authenticate
     * purpose' / type' / 1' : sha256(private key) used as 256 bit AES key
     * @param type
     * @param masterHDNode
     * @throws Exception
     */
    private void setMetadataNode(int type, DeterministicKey masterHDNode) throws Exception{

        int purpose = MetadataUtil.getPurpose();

        DeterministicKey metaDataHDNode = MetadataUtil.deriveHardened(masterHDNode, purpose);
        DeterministicKey payloadTypeNode = MetadataUtil.deriveHardened(metaDataHDNode, type);
        DeterministicKey node = MetadataUtil.deriveHardened(payloadTypeNode, 0);

        this.type = type;
        this.address = node.toAddress(MainNetParams.get()).toString();
        this.node = node;
        byte[] privateKeyBuffer = MetadataUtil.deriveHardened(payloadTypeNode, 1).getPrivKeyBytes();
        this.encryptionKey = Sha256Hash.hash(privateKeyBuffer);
    }

    private void fetch() throws Exception{

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
     * @return address
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * @return Either metadataNode or masterNode - depending on constructor used
     */
    public DeterministicKey getNode() {
        return this.node;
    }

    /**
     * Put new metadata entry
     */
    public void putMetadata(String payload) throws Exception {

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
        body.setPayload(new String(Base64.encode(encryptedPayloadBytes)));//working
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

    /**
     * Get metadata entry
     */
    public String getMetadata() throws Exception {

        Call<MetadataResponse> response = endpoints.getMetadata(address);

        Response<MetadataResponse> exe = response.execute();

        if (exe.isSuccessful()) {

            if(isEncrypted){
                return AESUtil.decryptWithKey(encryptionKey, exe.body().getPayload());
            } else {
                return new String(Base64.decode(exe.body().getPayload()));
            }
        } else {
            throw new Exception(exe.code() + " " + exe.message());
        }
    }

    /**
     * Delete metadata entry
     */
    public void deleteMetadata(String payload) throws Exception {

//        String encryptedPayload = AESUtil.encryptWithKey(encryptionKey, payload);
//        byte[] encryptedPayloadBytes = encryptedPayload.getBytes("utf-8");
//
//        byte[] message = MetadataUtil.message(encryptedPayloadBytes, magicHash);
//
//        String signature = node.signMessage(new String(Base64.encode(message)));
//
//        Call<Void> response = endpoints.deleteMetadata(address, signature);
//
//        Response<Void> exe = response.execute();
//
//        if (!exe.isSuccessful()) {
//            throw new Exception(exe.code() + " " + exe.message());
//        } else {
//            magicHash = null;
//        }
    }
}