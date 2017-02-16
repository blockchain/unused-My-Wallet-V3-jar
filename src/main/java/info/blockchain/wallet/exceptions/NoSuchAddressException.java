package info.blockchain.wallet.exceptions;

public class NoSuchAddressException extends Exception {
    //Parameterless Constructor
    public NoSuchAddressException() {
    }

    //Constructor that accepts a message
    public NoSuchAddressException(String message) {
        super(message);
    }
}