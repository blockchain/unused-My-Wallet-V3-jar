package info.blockchain.wallet.exceptions;

public class SharedMetadataConnectionException extends Exception {
    //Parameterless Constructor
    public SharedMetadataConnectionException() {
    }

    //Constructor that accepts a message
    public SharedMetadataConnectionException(String message) {
        super(message);
    }

    public SharedMetadataConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SharedMetadataConnectionException(Throwable cause) {
        super(cause);
    }
}