package info.blockchain.wallet.metadata;

import info.blockchain.api.MetadataEndpoints;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.metadata.data.Auth;
import info.blockchain.wallet.metadata.data.Invitation;
import info.blockchain.wallet.metadata.data.Message;
import info.blockchain.wallet.metadata.data.MetadataRequest;
import info.blockchain.wallet.metadata.data.MetadataResponse;
import info.blockchain.wallet.metadata.data.Status;
import info.blockchain.wallet.metadata.data.Trusted;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.MetadataUtil;

import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.spongycastle.util.encoders.Hex;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Metadata {

    //Normal metadata types
//    public final static int PAYLOAD_TYPE_GUID = 0;
//    public final static int PAYLOAD_TYPE_RESERVED = 1;
//    public final static int PAYLOAD_TYPE_WHATS_NEW = 2;
//    public final static int PAYLOAD_TYPE_BUY_SELL = 3;
//    public final static int PAYLOAD_TYPE_CONTACT = 4;

    final static int METADATA_VERSION = 1;

    private MetadataEndpoints endpoints;
    private int type;
    private String address;
    private DeterministicKey node;
    private String token;
    private String encryptionKey;
    private byte[] magicHash;

    /**
     * Constructor for normal metadata service
     * @param masterHDNode
     * @param type
     * @throws Exception
     */
    public Metadata(DeterministicKey masterHDNode, int type) throws Exception{

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MetadataEndpoints.API_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        endpoints = retrofit.create(MetadataEndpoints.class);

        setMetadataNode(type, masterHDNode);
        this.token = getToken();
        this.type = type;
        this.magicHash = null;
    }

    /**
     * Constructor for shared metadata service
     * @param masterHDNode
     * @throws Exception
     */
    public Metadata(DeterministicKey masterHDNode) throws Exception{

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MetadataEndpoints.API_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        endpoints = retrofit.create(MetadataEndpoints.class);

        //Not sure if we should use masterHDNode here
        this.node = masterHDNode;
        this.address = masterHDNode.toAddress(MainNetParams.get()).toString();
        this.token = getToken();

        setMetadataNode(masterHDNode);
        this.token = getToken();
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

        int purpose = getPurpose();

        DeterministicKey metaDataHDNode = deriveHardened(masterHDNode, purpose);
        DeterministicKey payloadTypeNode = deriveHardened(metaDataHDNode, type);
        DeterministicKey node = deriveHardened(payloadTypeNode, 0);

        this.address = node.toAddress(MainNetParams.get()).toString();
        this.node = node;
        encryptionKey = deriveHardened(payloadTypeNode, 1).serializePrivB58(MainNetParams.get());
    }

    /**
     * Set shared metadata node and address
     * Set encryption key:
     * purpose' / type' / 0' : https://meta.blockchain.info/{address} - signature used to authenticate
     * purpose' / type' / 1' : sha256(private key) used as 256 bit AES key
     * @param masterHDNode
     * @throws Exception
     */
    private void setMetadataNode(DeterministicKey masterHDNode) throws Exception{

        int purpose = getPurpose();

        DeterministicKey metaDataHDNode = deriveHardened(masterHDNode, purpose);

        this.address = metaDataHDNode.toAddress(MainNetParams.get()).toString();
        this.node = metaDataHDNode;
        this.encryptionKey = null;//not going to encrypt for shared metadata
    }

    private DeterministicKey deriveHardened(DeterministicKey node, int type){
        return HDKeyDerivation.deriveChildKey(node, type | ChildNumber.HARDENED_BIT);
    }

    /**
     * BIP 43 purpose needs to be 31 bit or less. For lack of a BIP number
     * we take the first 31 bits of the SHA256 hash of a reverse domain.
     * @return
     * @throws Exception
     */
    private int getPurpose() throws Exception{

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String text = "info.blockchain.metadata";
        md.update(text.getBytes("UTF-8"));
        byte[] hash = md.digest();
        byte[] slice = Arrays.copyOfRange(hash, 0, 4);

        return (int) (Utils.readUint32BE(slice, 0) & 0x7FFFFFFF); // 510742
    }

    /**
     * @return Mdid
     */
    public String getAddress(){
        return this.address;
    }

    /**
     * @return Either metadataNode or masterNode - depending on constructor used
     */
    public DeterministicKey getNode(){
        return this.node;
    }

    /**
     * Get nonce generated by the server (auth challenge).
     * @return
     * @throws Exception
     */
    private String getNonce() throws Exception {

        Call<Auth> response = endpoints.getNonce();

        Response<Auth> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body().getNonce();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Get JSON Web Token if signed nonce is correct. Signed.
     * @return
     * @throws Exception
     */
    private String getToken() throws Exception {

        String nonce = getNonce();
        String sig = node.signMessage(nonce);

        HashMap<String, String> map = new HashMap<>();
        map.put("mdid", address);
        map.put("signature", sig);
        map.put("nonce", nonce);
        Call<Auth> response = endpoints.getToken(map);

        Response<Auth> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body().getToken();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Get list of all trusted MDIDs. Authenticated.
     * @return
     * @throws Exception
     */
    public Trusted getTrustedList() throws Exception {

        Call<Trusted> response = endpoints.getTrustedList("Bearer " + token);

        Response<Trusted> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Check if a contact is on trusted list of mdid. Authenticated.
     * @param mdid
     * @return
     * @throws Exception
     */
    public boolean getTrusted(String mdid) throws Exception {

        Call<Trusted> response = endpoints.getTrusted("Bearer " + token, mdid);

        Response<Trusted> exe = response.execute();

        if (exe.isSuccessful()) {
            return Arrays.asList(exe.body().getContacts()).contains(mdid);
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Add a contact to trusted list of mdid. Authenticated.
     * @param mdid
     * @return
     * @throws Exception
     */
    public boolean putTrusted(String mdid) throws Exception {

        Call<Trusted> response = endpoints.putTrusted("Bearer " + token, mdid);

        Response<Trusted> exe = response.execute();

        if (exe.isSuccessful()) {
            return mdid.equals(exe.body().getContact());
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Delete a contact from trusted list of mdid. Authenticated.
     * @param mdid
     * @return
     * @throws Exception
     */
    public boolean deleteTrusted(String mdid) throws Exception {

        Call<Status> response = endpoints.deleteTrusted("Bearer " + token, mdid);

        Response<Status> exe = response.execute();

        if (exe.isSuccessful()) {
            return true;
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Add new shared metadata entry. Signed. Authenticated.
     * @param mdid
     * @param message
     * @param type
     * @return
     * @throws Exception
     */
    public Message postMessage(String mdid, String message, int type) throws Exception {

        String encryptedMessage = encryptFor(message, mdid);

        String b64Msg = new String(Base64.encodeBase64String(encryptedMessage.getBytes()));

        String signature = node.signMessage(b64Msg);

        Message request = new Message();
        request.setRecipient(mdid);
        request.setType(type);
        request.setPayload(b64Msg);
        request.setSignature(signature);

        Call<Message> response = endpoints.postMessage("Bearer " + token, request);

        Response<Message> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }

    }

    private String encryptFor(String message, String mdid) throws Exception {

        return message;
    }

    /**
     * Get messages sent to my MDID. Authenticated.
     * @param onlyProcessed
     * @return
     * @throws Exception
     */
    public List<Message> getMessages(boolean onlyProcessed) throws Exception {

        Call<List<Message>> response = endpoints.getMessages("Bearer " + token, onlyProcessed);

        Response<List<Message>> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Get messages sent to my MDID. Authenticated.
     * @param lastMessageId
     * @return
     * @throws Exception
     */
    public List<Message> getMessages(String lastMessageId) throws Exception {

        Call<List<Message>> response = endpoints.getMessages("Bearer " + token, lastMessageId);

        Response<List<Message>> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Get message from message id. Authenticated.
     * @param messageId
     * @return
     * @throws Exception
     */
    public Message getMessage(String messageId) throws Exception {

        Call<Message> response = endpoints.getMessage("Bearer " + token, messageId);

        Response<Message> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Obtains a one-time UUID for key sharing
     * Gets MDID of sender from one-time UUID
     * @return
     * @throws Exception
     */
    public Invitation createInvitation() throws Exception {

        Call<Invitation> response = endpoints.postShare("Bearer " + token);

        Response<Invitation> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Sets the MDID of the recipient
     * @param uuid
     * @return
     * @throws Exception
     */
    public Invitation acceptInvitation(String uuid) throws Exception {

        Call<Invitation> response = endpoints.postToShare("Bearer " + token, uuid);

        Response<Invitation> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Gets MDID of sender from one-time UUID
     * @param uuid
     * @return
     * @throws Exception
     */
    public Invitation readInvitation(String uuid) throws Exception {

        Call<Invitation> response = endpoints.getShare("Bearer " + token, uuid);

        Response<Invitation> exe = response.execute();

        if (exe.isSuccessful()) {
            return exe.body();
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Deletes one-time UUID
     * @param uuid
     * @return
     * @throws Exception
     */
    public boolean deleteInvitation(String uuid) throws Exception {

        Call<Invitation> response = endpoints.deleteShare("Bearer " + token, uuid);

        Response<Invitation> exe = response.execute();

        if (exe.isSuccessful()) {
            return true;
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }


    /**
     * Put new metadata entry
     * @param payload
     * @throws Exception
     */
    public void putMetadata(String payload) throws Exception {

        String encryptedPayload = AESUtil.encrypt(payload, new CharSequenceX(encryptionKey), 65536);//todo iterations?
        byte[] encryptedPayloadBytes = encryptedPayload.getBytes("utf-8");
        byte[] nextMagicHash = MetadataUtil.magic(encryptedPayloadBytes, magicHash);

        byte[] message = MetadataUtil.message(encryptedPayloadBytes, magicHash);

        String signature = node.signMessage(Base64.encodeBase64String(message));

        MetadataRequest body = new MetadataRequest();
        body.setVersion(METADATA_VERSION);
        body.setPayload(Base64.encodeBase64String(encryptedPayloadBytes));
        body.setSignature(signature);
        body.setPrev_magic_hash(magicHash != null ? new String(Hex.encode(magicHash)) : null);
        body.setType_id(type);

        Call<Void> response = endpoints.putMetadata(address, body);

        Response<Void> exe = response.execute();

        if (!exe.isSuccessful()) {
            throw new Exception(exe.code()+" "+exe.message());
        } else {
            magicHash = nextMagicHash;
        }
    }

    /**
     * Get metadata entry
     * @return
     * @throws Exception
     */
    public String getMetadata() throws Exception {

        Call<MetadataResponse> response = endpoints.getMetadata(address);

        Response<MetadataResponse> exe = response.execute();

        if (exe.isSuccessful()) {
            String encrypted = new String(Base64.decodeBase64(exe.body().getPayload()));
            return AESUtil.decrypt(encrypted, new CharSequenceX(encryptionKey), 65536);
        } else {
            throw new Exception(exe.code()+" "+exe.message());
        }
    }

    /**
     * Delete metadata entry
     * @param payload
     * @throws Exception
     */
    public void deleteMetadata(String payload) throws Exception {

        String encryptedPayload = AESUtil.encrypt(payload, new CharSequenceX(encryptionKey), 65536);//todo iterations?
        byte[] encryptedPayloadBytes = encryptedPayload.getBytes("utf-8");

        byte[] message = MetadataUtil.message(encryptedPayloadBytes, magicHash);

        String signature = node.signMessage(Base64.encodeBase64String(message));

        Call<Void> response = endpoints.deleteMetadata(address, signature);

        Response<Void> exe = response.execute();

        if (!exe.isSuccessful()) {
            throw new Exception(exe.code()+" "+exe.message());
        } else {
            magicHash = null;
        }
    }
}