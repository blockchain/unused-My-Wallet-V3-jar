package info.blockchain.wallet.payload;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.PayloadException;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.FormatsUtil;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.paddings.BlockCipherPadding;
import org.spongycastle.crypto.paddings.ISO10126d2Padding;
import org.spongycastle.crypto.paddings.ISO7816d4Padding;
import org.spongycastle.crypto.paddings.ZeroBytePadding;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("FieldCanBeLocal")
public class BlockchainWallet {

    public static final int DEFAULT_PBKDF2_ITERATIONS_V2 = 5000;
    private static final int DEFAULT_PBKDF2_ITERATIONS_V1_A = 1;
    private static final int DEFAULT_PBKDF2_ITERATIONS_V1_B = 10;

    private final String KEY_VERSION = "version";
    private final String KEY_PAYLOAD = "payload";//encrypted payload
    private final String KEY_PBKDF2_ITERATIONS = "pbkdf2_iterations";

    private final String KEY_EXTRA_SEED = "extra_seed";
    private final String KEY_PAYLOAD_CHECKSUM = "payload_checksum";
    private final String KEY_WAR_CHECKSUM = "war_checksum";
    private final String KEY_LANGUAGE = "language";
    private final String KEY_STORAGE_TOKEN = "storage_token";
    private final String KEY_SYNC_PUBKEYS = "sync_pubkeys";

    private String extraSeed;
    private String payloadChecksum;
    private String warChecksum;
    private String language;
    private String storageToken;
    private boolean syncPubkeys;

    private String unparsedWalletData;//Un-parsed wallet data - Debugging purposes
    private Payload payload;

    //Payload wrapper
    private int pbkdf2Iterations;
    private double version;

    /**
     *
     * @param payload Payload
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public BlockchainWallet(Payload payload) throws Exception {

        this.pbkdf2Iterations = DEFAULT_PBKDF2_ITERATIONS_V2;
        this.version = 3.0;
        this.syncPubkeys = false;
        this.payloadChecksum = new String(Hex.encode(MessageDigest.getInstance("SHA-256").digest(payload.toJson().toString().getBytes("UTF-8"))));
    }

    public BlockchainWallet(String walletData, CharSequenceX password) throws PayloadException, DecryptionException, UnsupportedEncodingException, InvalidCipherTextException {

        this.unparsedWalletData = walletData;

        if (FormatsUtil.getInstance().isValidJson(walletData)) {
            parseWallet(new JSONObject(walletData), password);
        } else {
            parseV1Wallet(walletData, password);
        }
    }

    private void parseV1Wallet(String walletData, CharSequenceX password) throws PayloadException, DecryptionException {

        setVersion(1.0);

        Pair pair = decryptV1Wallet(walletData, password);

        String decyptedPayload = (String) pair.getLeft();
        pbkdf2Iterations = (Integer) pair.getRight();
        payload = new Payload(decyptedPayload, pbkdf2Iterations);
    }

    private void parseWallet(JSONObject walletJson, CharSequenceX password) throws PayloadException, DecryptionException, UnsupportedEncodingException, InvalidCipherTextException {
        if (walletJson.has(KEY_EXTRA_SEED)) {
            extraSeed = walletJson.getString(KEY_EXTRA_SEED);
        }

        if (walletJson.has(KEY_PAYLOAD_CHECKSUM)) {
            payloadChecksum = walletJson.getString(KEY_PAYLOAD_CHECKSUM);
        }

        if (walletJson.has(KEY_WAR_CHECKSUM)) {
            warChecksum = walletJson.getString(KEY_WAR_CHECKSUM);
        }

        if (walletJson.has(KEY_LANGUAGE)) {
            language = walletJson.getString(KEY_LANGUAGE);
        }

        if (walletJson.has(KEY_STORAGE_TOKEN)) {
            storageToken = walletJson.getString(KEY_STORAGE_TOKEN);
        }

        if (walletJson.has(KEY_SYNC_PUBKEYS)) {
            syncPubkeys = walletJson.getBoolean(KEY_SYNC_PUBKEYS);
        }

        if (!walletJson.has(KEY_PAYLOAD)) {
            throw new PayloadException("No payload wrapper in json.");

        } else {

            //Payload wrapper contains version, iterations and encrypted payload
            String anyPayload = walletJson.getString(KEY_PAYLOAD);

            if (FormatsUtil.getInstance().isValidJson(anyPayload)) {
                //V2+
                JSONObject payloadWrapper = new JSONObject(anyPayload);

                version = payloadWrapper.getDouble(KEY_VERSION);
                setVersion(version);

                if (payloadWrapper.has(KEY_PBKDF2_ITERATIONS)) {
                    pbkdf2Iterations = payloadWrapper.getInt(KEY_PBKDF2_ITERATIONS);
                } else {
                    pbkdf2Iterations = DEFAULT_PBKDF2_ITERATIONS_V2;
                }

                if (!payloadWrapper.has(KEY_PAYLOAD)) {
                    throw new PayloadException("No payload in payload wrapper.");

                } else {
                    String decryptedPayload = decryptWallet(payloadWrapper.getString(KEY_PAYLOAD), password, pbkdf2Iterations);

                    if (decryptedPayload != null && FormatsUtil.getInstance().isValidJson(decryptedPayload)) {
                        payload = new Payload(decryptedPayload, pbkdf2Iterations);
                    } else {
                        throw new DecryptionException("Payload null after decrypt.");
                    }
                }
            } else {
                //V1
                setVersion(1.0);

                Pair pair = decryptV1Wallet(anyPayload, password);

                String decyptedPayload = (String) pair.getLeft();
                pbkdf2Iterations = (Integer) pair.getRight();
                payload = new Payload(decyptedPayload, pbkdf2Iterations);
            }
        }
    }

    public String getExtraSeed() {
        return extraSeed;
    }

    public void setExtraSeed(String extraSeed) {
        this.extraSeed = extraSeed;
    }

    public String getPayloadChecksum() {
        return payloadChecksum;
    }

    public void setPayloadChecksum(String payloadChecksum) {
        this.payloadChecksum = payloadChecksum;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public String getWarChecksum() {
        return warChecksum;
    }

    public void setWarChecksum(String warChecksum) {
        this.warChecksum = warChecksum;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStorageToken() {
        return storageToken;
    }

    public void setStorageToken(String storageToken) {
        this.storageToken = storageToken;
    }

    public boolean isSyncPubkeys() {
        return syncPubkeys;
    }

    public void setSyncPubkeys(boolean syncPubkeys) {
        this.syncPubkeys = syncPubkeys;
    }

    public int getPbkdf2Iterations() {
        return pbkdf2Iterations;
    }

    public void setPbkdf2Iterations(int pbkdf2Iterations) {
        this.pbkdf2Iterations = pbkdf2Iterations;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public Pair decryptV1Wallet(String encryptedPayload, CharSequenceX password) throws DecryptionException {

        String decrypted;
        int succeededIterations;

        int iterations[] = {DEFAULT_PBKDF2_ITERATIONS_V1_A, DEFAULT_PBKDF2_ITERATIONS_V1_B};
        int modes[] = {AESUtil.MODE_CBC, AESUtil.MODE_OFB};
        BlockCipherPadding[] paddings = {
                new ISO10126d2Padding(),
                new ISO7816d4Padding(),
                new ZeroBytePadding(),
                null};//NoPadding

        for (int iteration : iterations) {

            for (int mode : modes) {

                for (BlockCipherPadding padding : paddings) {

                    try {
                        decrypted = AESUtil.decryptWithSetMode(encryptedPayload, password, iteration, mode, padding);
                        //Ensure it's parsable
                        new JSONObject(decrypted);

                        succeededIterations = iteration;

                        return Pair.of(decrypted, succeededIterations);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        throw new DecryptionException("Failed to decrypt");
    }

    public String decryptWallet(String encryptedPayload, CharSequenceX password, int pdfdf2Iterations) throws UnsupportedEncodingException, DecryptionException, InvalidCipherTextException {

        return AESUtil.decrypt(encryptedPayload, password, pdfdf2Iterations);
    }

    public String getUnparsedWalletData() {
        return unparsedWalletData;
    }

    public Pair encryptPayload(String payloadCleartext, CharSequenceX password, int iterations, double version) throws Exception {

        String payloadEncrypted = AESUtil.encrypt(payloadCleartext, password, iterations);
        JSONObject rootObj = new JSONObject();
        rootObj.put(KEY_VERSION, version);
        rootObj.put(KEY_PBKDF2_ITERATIONS, iterations);
        rootObj.put(KEY_PAYLOAD, payloadEncrypted);

        String checkSum = new String(Hex.encode(MessageDigest.getInstance("SHA-256").digest(rootObj.toString().getBytes("UTF-8"))));

        return Pair.of(checkSum, rootObj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
