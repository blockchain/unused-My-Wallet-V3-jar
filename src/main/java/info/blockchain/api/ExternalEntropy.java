package info.blockchain.api;

import info.blockchain.wallet.util.WebUtil;

import org.spongycastle.util.encoders.Hex;

public class ExternalEntropy extends BaseApi {

    private static final String RANDOM_BYTES = "v2/randombytes?bytes=32&format=hex";

    @Override
    String getRoute() {
        return PersistentUrls.getInstance().getDefaultBaseApiUrl() + RANDOM_BYTES;
    }

    public byte[] getRandomBytes() throws Exception {

        String result = WebUtil.getInstance().getURL(getRoute());
        if (result == null || !result.matches("^[A-Fa-f0-9]{64}$")) {
            throw new Exception("Failed to get random bytes");
        }

        return Hex.decode(result);
    }
}
