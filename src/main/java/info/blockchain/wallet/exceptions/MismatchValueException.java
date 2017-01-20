package info.blockchain.wallet.exceptions;

public class MismatchValueException extends Exception {
    //Parameterless Constructor
    public MismatchValueException() {
    }

    //Constructor that accepts a message
    public MismatchValueException(String message) {
        super(message);
    }

    public MismatchValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public MismatchValueException(Throwable cause) {
        super(cause);
    }
}