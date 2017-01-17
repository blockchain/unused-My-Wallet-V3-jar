package info.blockchain.wallet.util;

import info.blockchain.api.Balance;
import info.blockchain.api.PersistentUrls;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class PrivateKeyFactory {

    public final static String BASE58 = "base58";
    public final static String BASE64 = "base64";
    public final static String BIP38 = "bip38";
    public final static String HEX_UNCOMPRESSED = "hex_u";
    public final static String HEX_COMPRESSED = "hex_c";
    public final static String MINI = "mini";
    public final static String WIF_COMPRESSED = "wif_c";
    public final static String WIF_UNCOMPRESSED = "wif_u";

    private Balance api;

    public PrivateKeyFactory(Balance api) {
        this.api = api;
    }

    public PrivateKeyFactory() {
        this.api = new Balance();
    }

    public String getFormat(String key) {
        // 51 characters base58, always starts with a '5'
        if (key.matches("^5[1-9A-HJ-NP-Za-km-z]{50}$")) {
            return WIF_UNCOMPRESSED;
        }
        // 52 characters, always starts with 'K' or 'L'
        else if (key.matches("^[LK][1-9A-HJ-NP-Za-km-z]{51}$")) {
            return WIF_COMPRESSED;
        } else if (key.matches("^[1-9A-HJ-NP-Za-km-z]{44}$") || key.matches("^[1-9A-HJ-NP-Za-km-z]{43}$")) {
            return BASE58;
        }
        // assume uncompressed for hex (secret exponent)
        else if (key.matches("^[A-Fa-f0-9]{64}$")) {
            return HEX_UNCOMPRESSED;
        } else if (key.matches("^[A-Za-z0-9/=+]{44}$")) {
            return BASE64;
        } else if (key.matches("^6P[1-9A-HJ-NP-Za-km-z]{56}$")) {
            return BIP38;
        } else if (key.matches("^S[1-9A-HJ-NP-Za-km-z]{21}$") ||
                key.matches("^S[1-9A-HJ-NP-Za-km-z]{25}$") ||
                key.matches("^S[1-9A-HJ-NP-Za-km-z]{29}$") ||
                key.matches("^S[1-9A-HJ-NP-Za-km-z]{30}$")) {

            byte[] testBytes;
            String data = key + "?";
            try {
                Hash hash = new Hash(MessageDigest.getInstance("SHA-256").digest(data.getBytes("UTF-8")));
                testBytes = hash.getBytes();

                if ((testBytes[0] == 0x00)) {
                    return MINI;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return null;
        } else {
            return null;
        }
    }

    public ECKey getKey(String format, String data) throws Exception {
        if (format.equals(WIF_UNCOMPRESSED) || format.equals(WIF_COMPRESSED)) {
            DumpedPrivateKey pk = new DumpedPrivateKey(PersistentUrls.getInstance().getCurrentNetworkParams(), data);
            return pk.getKey();
        } else if (format.equals(BASE58)) {
            return decodeBase58PK(data);
        } else if (format.equals(BASE64)) {
            return decodeBase64PK(data);
        } else if (format.equals(HEX_UNCOMPRESSED)) {
            return decodeHexPK(data, false);
        } else if (format.equals(HEX_COMPRESSED)) {
            return decodeHexPK(data, true);
        } else if (format.equals(MINI)) {
            return decodeMiniKey(data);
        } else {
            throw new Exception("Unknown key format: "+format);
        }
    }

    private ECKey decodeMiniKey(String hex) throws Exception {

        if (api == null) {
            throw new Exception("Balance API not set");
        }

        Hash hash = new Hash(MessageDigest.getInstance("SHA-256").digest(hex.getBytes("UTF-8")));
        ECKey uncompressedKey = decodeHexPK(hash.toString(), false);
        ECKey compressedKey = decodeHexPK(hash.toString(), true);

        try {
            String uncompressedAddress = uncompressedKey.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString();
            String compressedAddress = compressedKey.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString();

            ArrayList<String> list = new ArrayList<String>();
            list.add(uncompressedAddress);
            list.add(compressedAddress);

            JSONObject json = api.getBalance(list);
            long uncompressedBalance = json.getJSONObject(uncompressedAddress).getLong("final_balance");
            long compressedBalance = json.getJSONObject(compressedAddress).getLong("final_balance");

            if (compressedBalance == 0 && uncompressedBalance > 0) {
                return uncompressedKey;
            } else {
                return compressedKey;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return compressedKey;
        }
    }

    private ECKey decodeBase58PK(String base58Priv) throws Exception {
        byte[] privBytes = Base58.decode(base58Priv);
        // Prepend a zero byte to make the biginteger unsigned
        byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
        return ECKey.fromPrivate(new BigInteger(appendZeroByte), true);
    }

    private ECKey decodeBase64PK(String base64Priv) {
        byte[] privBytes = Base64.decodeBase64(base64Priv.getBytes());
        // Prepend a zero byte to make the biginteger unsigned
        byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
        return ECKey.fromPrivate(new BigInteger(appendZeroByte), true);
    }

    private ECKey decodeHexPK(String hex, boolean compressed) {
        byte[] privBytes = Hex.decode(hex);
        // Prepend a zero byte to make the biginteger unsigned
        byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
        return ECKey.fromPrivate(new BigInteger(appendZeroByte), compressed);
    }

    private String decryptPK(String base58Priv) {

		/*
        if (this.isDoubleEncrypted()) {
			if (this.temporySecondPassword == null || !this.validateSecondPassword(temporySecondPassword))
				throw new Exception("You must provide a second password");

			base58Priv = decryptPK(base58Priv, getSharedKey(), this.temporySecondPassword, this.getDoubleEncryptionPbkdf2Iterations());
		}
		*/

        return base58Priv;
    }

    private ECKey decodePK(String base58Priv) throws Exception {
        return decodeBase58PK(decryptPK(base58Priv));
    }

    private byte[] hash(byte[] data, int offset, int len) {
        try {
            MessageDigest a = MessageDigest.getInstance("SHA-256");
            a.update(data, offset, len);
            return a.digest(a.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] hash(byte[] data) {
        return hash(data, 0, data.length);
    }

}