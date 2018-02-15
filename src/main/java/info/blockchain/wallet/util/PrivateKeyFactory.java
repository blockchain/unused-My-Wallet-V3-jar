package info.blockchain.wallet.util;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.blockexplorer.FilterType;
import info.blockchain.api.data.Balance;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.exceptions.ApiException;
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
import org.bitcoinj.params.BitcoinMainNetParams;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Call;
import retrofit2.Response;

@SuppressWarnings("WeakerAccess")
public class PrivateKeyFactory {

    public final static String BASE58 = "base58";
    public final static String BASE64 = "base64";
    public final static String BIP38 = "bip38";
    public final static String HEX = "hex";
    public final static String MINI = "mini";
    public final static String WIF_COMPRESSED = "wif_c";
    public final static String WIF_UNCOMPRESSED = "wif_u";

    public String getFormat(String key) {

        // TODO: 04/01/2018 Pass params in. What about other coin params?
        boolean isTestnet = !(PersistentUrls.getInstance().getBitcoinParams() instanceof BitcoinMainNetParams);

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
        else if (key.matches("^[A-Fa-f0-9]{64}$")) {
            return HEX;
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

    // TODO: 04/01/2018 Pass params in. What about other coin params?
    public ECKey getKey(String format, String data) throws Exception {
        switch (format) {
            case WIF_UNCOMPRESSED:
            case WIF_COMPRESSED:
                DumpedPrivateKey pk = DumpedPrivateKey.fromBase58(PersistentUrls.getInstance().getBitcoinParams(), data);
                return pk.getKey();
            case BASE58:
                return decodeBase58PK(data);
            case BASE64:
                return decodeBase64PK(data);
            case HEX:
                return determineKey(data);
            case MINI:
                return decodeMiniKey(data);
            default:
                throw new Exception("Unknown key format: " + format);
        }
    }

    private ECKey decodeMiniKey(String mini) throws Exception {
        Hash hash = new Hash(MessageDigest.getInstance("SHA-256").digest(mini.getBytes("UTF-8")));
        return determineKey(hash.toString());
    }

    // TODO: 04/01/2018 Pass params in. What about other coin params?
    private ECKey determineKey(String hash) throws Exception {

        ECKey uncompressedKey = decodeHexPK(hash, false);
        ECKey compressedKey = decodeHexPK(hash, true);

        try {
            String uncompressedAddress = uncompressedKey.toAddress(PersistentUrls.getInstance().getBitcoinParams()).toString();
            String compressedAddress = compressedKey.toAddress(PersistentUrls.getInstance().getBitcoinParams()).toString();

            ArrayList<String> list = new ArrayList<>();
            list.add(uncompressedAddress);
            list.add(compressedAddress);

            BlockExplorer blockExplorer = new BlockExplorer(BlockchainFramework.getRetrofitExplorerInstance(),
                    BlockchainFramework.getRetrofitApiInstance(), BlockchainFramework.getApiCode());
            Call<HashMap<String, Balance>> call = blockExplorer.getBalance(list, FilterType.RemoveUnspendable);

            Response<HashMap<String, Balance>> exe = call.execute();

            if (!exe.isSuccessful()) {
                throw new ApiException("Failed to connect to server.");
            }

            HashMap<String, Balance> body = exe.body();

            BigInteger uncompressedBalance = body.get(uncompressedAddress).getFinalBalance();
            BigInteger compressedBalance = body.get(compressedAddress).getFinalBalance();

            if (compressedBalance != null && compressedBalance.compareTo(BigInteger.ZERO) == 0
                && uncompressedBalance != null && uncompressedBalance.compareTo(BigInteger.ZERO) == 1) {
                return uncompressedKey;
            } else {
                return compressedKey;
            }
        } catch (Exception e) {
            // TODO: 08/03/2017 Is this safe? Could this not return an uninitialized ECKey?
            e.printStackTrace();
            return compressedKey;
        }
    }

    private ECKey decodeBase58PK(String base58Priv) {
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

    private ECKey decodePK(String base58Priv) {
        return decodeBase58PK(base58Priv);
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