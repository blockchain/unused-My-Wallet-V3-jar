package info.blockchain.api.metadata.response;

public class AuthNonceResponse {

    String nonce;

    public String getNonce() {
        return nonce;
    }

    @Override
    public String toString() {
        return "AuthNonceResponse{" +
                "nonce='" + nonce + '\'' +
                '}';
    }
}
