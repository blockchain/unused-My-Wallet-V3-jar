package info.blockchain.wallet.payload;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.exceptions.DecryptionException;
import info.blockchain.wallet.exceptions.PayloadException;
import info.blockchain.wallet.util.CharSequenceX;
import info.blockchain.wallet.util.FormatsUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

public class BlockchainWallet {

    public static final int DEFAULT_PBKDF2_ITERATIONS = 5000;

    private String extraSeed;
    private String payloadChecksum;
    private String warChecksum;
    private String language;
    private String storageToken;
    private boolean syncPubkeys;

    private Payload payload;

    private final String KEY_EXTRA_SEED = "extra_seed";
    private final String KEY_PAYLOAD_CHECKSUM = "payload_checksum";
    private final String KEY_PAYLOAD = "payload";
    private final String KEY_WAR_CHECKSUM = "war_checksum";
    private final String KEY_LANGUAGE = "language";
    private final String KEY_STORAGE_TOKEN = "storage_token";
    private final String KEY_SYNC_PUBKEYS = "sync_pubkeys";

    //Payload wrapper
    private int pdfdf2Iterations;
    private double version;

    private final String KEY_VERSION = "version";
    private final String KEY_PBKDF2_ITERATIONS = "pbkdf2_iterations";

    public BlockchainWallet(Payload payload) {

        this.pdfdf2Iterations = DEFAULT_PBKDF2_ITERATIONS;
        this.version = 3.0;
        this.syncPubkeys = false;
        this.payloadChecksum = "";//TODO - set checksum
    }

    public BlockchainWallet(String walletData, CharSequenceX password) throws PayloadException, DecryptionException {

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
        pdfdf2Iterations = (Integer) pair.getRight();
        payload = new Payload(decyptedPayload, pdfdf2Iterations);
    }

    private void parseWallet(JSONObject walletJson, CharSequenceX password) throws PayloadException, DecryptionException {

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

        if (walletJson.has(KEY_PAYLOAD)) {

            //Payload wrapper contains version, iterations and encrypted payload
            JSONObject payloadWrapper = new JSONObject(walletJson.getString(KEY_PAYLOAD));

            version = payloadWrapper.getDouble(KEY_VERSION);

            if (payloadWrapper.has(KEY_PBKDF2_ITERATIONS)) {
                pdfdf2Iterations = payloadWrapper.getInt(KEY_PBKDF2_ITERATIONS);
            } else {
                pdfdf2Iterations = DEFAULT_PBKDF2_ITERATIONS;
            }

            if (payloadWrapper.has(KEY_PAYLOAD)) {
                String decryptedPayload = decryptWallet(payloadWrapper.getString(KEY_PAYLOAD), password, pdfdf2Iterations);

                if (decryptedPayload != null) {
                    payload = new Payload(decryptedPayload, pdfdf2Iterations);
                } else {
                    throw new DecryptionException("Payload null after decrypt.");
                }

            } else {
                throw new PayloadException("No payload in payload wrapper.");
            }

        } else {
            throw new PayloadException("No payload wrapper in json.");
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

    public int getPdfdf2Iterations() {
        return pdfdf2Iterations;
    }

    public void setPdfdf2Iterations(int pdfdf2Iterations) {
        this.pdfdf2Iterations = pdfdf2Iterations;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public Pair decryptV1Wallet(String encryptedPayload, CharSequenceX password) {

        final int defaultIterations = 10;

        String decrypted = null;
        int succeededIterations = 1;

        int iterations = 1;
        boolean isBCB = true;
        while (decrypted == null && iterations <= defaultIterations) {
            try {
                if (isBCB) {
                    decrypted = AESUtil.decrypt(encryptedPayload, password, iterations);
                } else {
                    decrypted = AESUtil.decrypt_OFB(encryptedPayload, password, iterations);
                }

                //Ensure it's parsable
                new JSONObject(decrypted);

                succeededIterations = iterations;

            } catch (Exception e) {
                decrypted = null;
            } finally {
                iterations++;
                if (iterations > 10 && isBCB) {
                    iterations = 0;
                    isBCB = false;
                }
            }
        }
        return Pair.of(decrypted, succeededIterations);
    }

    public String decryptWallet(String encryptedPayload, CharSequenceX password, int pdfdf2Iterations) {

        return AESUtil.decrypt(encryptedPayload, password, pdfdf2Iterations);
    }
}
