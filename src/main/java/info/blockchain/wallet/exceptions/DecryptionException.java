package info.blockchain.wallet.exceptions;

public class DecryptionException extends Exception {

    //Parameterless Constructor
    public DecryptionException() {
    }

    //Constructor that accepts a message
    public DecryptionException(String message) {
        super(message);
    }

    public DecryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecryptionException(Throwable cause) {
        super(cause);
    }

    protected DecryptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}