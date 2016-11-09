package info.blockchain.api.metadata.response;

public class AuthTokenResponse {

    String token;

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "AuthTokenResponse{" +
                "token='" + token + '\'' +
                '}';
    }
}
