package info.blockchain.api.metadata.data;

public class Auth {

    String nonce;
    String token;

    public String getNonce() {
        return nonce;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "nonce='" + nonce + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
