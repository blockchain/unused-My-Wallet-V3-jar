package info.blockchain.wallet.exceptions;

public class EncryptionException extends Exception {
    //Parameterless Constructor
    public EncryptionException() {
    }

    //Constructor that accepts a message
    public EncryptionException(String message) {
        super(message);
    }
}