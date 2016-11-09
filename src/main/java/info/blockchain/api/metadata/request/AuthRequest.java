package info.blockchain.api.metadata.request;

public class AuthRequest {

    String nonce;
    String signature;
    String mdid;

    public AuthRequest(String nonce, String signature, String mdid) {
        this.nonce = nonce;
        this.signature = signature;
        this.mdid = mdid;
    }

}
