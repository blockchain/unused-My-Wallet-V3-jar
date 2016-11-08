package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.spongycastle.util.encoders.Hex;

public class ExternalEntropy extends BaseApi {

    private static final String RANDOM_BYTES = "v2/randombytes?bytes=32&format=hex";
    public static final String PROD_EXTERNAL_ENTROPY_URL = PROTOCOL + API_SUBDOMAIN + SERVER_ADDRESS + RANDOM_BYTES;

    public byte[] getRandomBytes() throws Exception {

        String result = WebUtil.getInstance().getURL(PROD_EXTERNAL_ENTROPY_URL);
        if (result == null || !result.matches("^[A-Fa-f0-9]{64}$")) {
            throw new Exception("Failed to get random bytes");
        }

        return Hex.decode(result);
    }
}
