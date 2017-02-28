package info.blockchain.wallet.exceptions;

public class HDWalletException extends Exception {
    //Parameterless Constructor
    public HDWalletException() {
    }

    //Constructor that accepts a message
    public HDWalletException(String message) {
        super(message);
    }

    public HDWalletException(Throwable cause) {
        super(cause);
    }
}