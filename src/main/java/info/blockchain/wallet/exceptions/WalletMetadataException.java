package info.blockchain.wallet.exceptions;

public class WalletMetadataException extends Exception {

    //Parameterless Constructor
    public WalletMetadataException() {
    }

    //Constructor that accepts a message
    public WalletMetadataException(String message) {
        super(message);
    }

    public WalletMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public WalletMetadataException(Throwable cause) {
        super(cause);
    }

    protected WalletMetadataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}