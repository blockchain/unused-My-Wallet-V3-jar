package info.blockchain.wallet.exceptions;

public class MetadataException extends Exception {
    //Parameterless Constructor
    public MetadataException() {
    }

    //Constructor that accepts a message
    public MetadataException(String message) {
        super(message);
    }

    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataException(Throwable cause) {
        super(cause);
    }
}