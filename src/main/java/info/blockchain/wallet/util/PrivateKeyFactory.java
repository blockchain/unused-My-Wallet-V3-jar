package info.blockchain.wallet.util;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.Balance;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Call;
import retrofit2.Response;

public class PrivateKeyFactory {

    public final static String BASE58 = "base58";
    public final static String BASE64 = "base64";
    public final static String BIP38 = "bip38";
    public final static String HEX_UNCOMPRESSED = "hex_u";
    public final static String HEX_COMPRESSED = "hex_c";
    public final static String MINI = "mini";
    public final static String WIF_COMPRESSED = "wif_c";
    public final static String WIF_UNCOMPRESSED = "wif_u";

    public static String getFormat(String key) {

        boolean isTestnet = !(PersistentUrls.getInstance().getCurrentNetworkParams() instanceof MainNetParams);

        // 51 characters base58, always starts with a '5'  (or '9', for testnet)
        if (!isTestnet && key.matches("^5[1-9A-HJ-NP-Za-km-z]{50}$") ||
            isTestnet && key.matches("^9[1-9A-HJ-NP-Za-km-z]{50}$")) {
            return WIF_UNCOMPRESSED;
        }
        // 52 characters, always starts with 'K' or 'L' (or 'c' for testnet)
        else if (!isTestnet && key.matches("^[LK][1-9A-HJ-NP-Za-km-z]{51}$") ||
            isTestnet && key.matches("^[c][1-9A-HJ-NP-Za-km-z]{51}$")) {
            return WIF_COMPRESSED;

        } else if (key.matches("^[1-9A-HJ-NP-Za-km-z]{44}$") || key
            .matches("^[1-9A-HJ-NP-Za-km-z]{43}$")) {
            return BASE58;
        }
        //Assume compressed
        else if (key.matches("^[A-Fa-f0-9]{64}$")) {
            return HEX_COMPRESSED;
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
                Hash hash = new Hash(
                    MessageDigest.getInstance("SHA-256").digest(data.getBytes("UTF-8")));
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

    public static ECKey getKey(String format, String data) throws Exception {
        if (format.equals(WIF_UNCOMPRESSED) || format.equals(WIF_COMPRESSED)) {
            DumpedPrivateKey pk = DumpedPrivateKey.fromBase58(PersistentUrls.getInstance().getCurrentNetworkParams(), data);
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

    private static ECKey decodeMiniKey(String hex) throws Exception {

        Hash hash = new Hash(MessageDigest.getInstance("SHA-256").digest(hex.getBytes("UTF-8")));
        ECKey uncompressedKey = decodeHexPK(hash.toString(), false);
        ECKey compressedKey = decodeHexPK(hash.toString(), true);

        try {
            String uncompressedAddress = uncompressedKey.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString();
            String compressedAddress = compressedKey.toAddress(PersistentUrls.getInstance().getCurrentNetworkParams()).toString();

            ArrayList<String> list = new ArrayList<String>();
            list.add(uncompressedAddress);
            list.add(compressedAddress);

            BlockExplorer blockExplorer = new BlockExplorer(BlockchainFramework.getRetrofitServerInstance(), BlockchainFramework.getApiCode());
            Call<HashMap<String, Balance>> call = blockExplorer.getBalance(list, BlockExplorer.TX_FILTER_ALL);

            Response<HashMap<String, Balance>> exe = call.execute();

            if(!exe.isSuccessful()) {
                throw new Exception("Failed to connect to server.");
            }

            HashMap<String, Balance> body = exe.body();

            long uncompressedBalance = body.get(uncompressedAddress).getFinalBalance().longValue();
            long compressedBalance = body.get(compressedAddress).getFinalBalance().longValue();

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

    private static ECKey decodeBase58PK(String base58Priv) throws Exception {
        byte[] privBytes = Base58.decode(base58Priv);
        // Prepend a zero byte to make the biginteger unsigned
        byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
        return ECKey.fromPrivate(new BigInteger(appendZeroByte), true);
    }

    private static ECKey decodeBase64PK(String base64Priv) {
        byte[] privBytes = Base64.decodeBase64(base64Priv.getBytes());
        // Prepend a zero byte to make the biginteger unsigned
        byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
        return ECKey.fromPrivate(new BigInteger(appendZeroByte), true);
    }

    private static ECKey decodeHexPK(String hex, boolean compressed) {
        byte[] privBytes = Hex.decode(hex);
        // Prepend a zero byte to make the biginteger unsigned
        byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
        return ECKey.fromPrivate(new BigInteger(appendZeroByte), compressed);
    }

    private static String decryptPK(String base58Priv) {

		/*
        if (this.isDoubleEncrypted()) {
			if (this.temporySecondPassword == null || !this.validateSecondPassword(temporySecondPassword))
				throw new Exception("You must provide a second password");

			base58Priv = decryptPK(base58Priv, getSharedKey(), this.temporySecondPassword, this.getDoubleEncryptionPbkdf2Iterations());
		}
		*/

        return base58Priv;
    }

    private static ECKey decodePK(String base58Priv) throws Exception {
        return decodeBase58PK(decryptPK(base58Priv));
    }

    private static byte[] hash(byte[] data, int offset, int len) {
        try {
            MessageDigest a = MessageDigest.getInstance("SHA-256");
            a.update(data, offset, len);
            return a.digest(a.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] hash(byte[] data) {
        return hash(data, 0, data.length);
    }

}