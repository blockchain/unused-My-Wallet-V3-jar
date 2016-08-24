package info.blockchain.wallet.exceptions;

public class UnsupportedVersionException extends Exception {
    //Parameterless Constructor
    public UnsupportedVersionException() {
    }

    //Constructor that accepts a message
    public UnsupportedVersionException(String message) {
        super(message);
    }
}