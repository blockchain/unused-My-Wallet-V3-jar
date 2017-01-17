package info.blockchain.wallet.exceptions;

public class SharedMetadataException extends Exception {
    //Parameterless Constructor
    public SharedMetadataException() {
    }

    //Constructor that accepts a message
    public SharedMetadataException(String message) {
        super(message);
    }

    public SharedMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public SharedMetadataException(Throwable cause) {
        super(cause);
    }
}