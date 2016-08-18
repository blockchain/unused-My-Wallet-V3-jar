package info.blockchain.wallet.exceptions;

public class PayloadException extends Exception {
    //Parameterless Constructor
    public PayloadException() {
    }

    //Constructor that accepts a message
    public PayloadException(String message) {
        super(message);
    }
}