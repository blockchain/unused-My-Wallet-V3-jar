package info.blockchain.wallet.exceptions;

public class ServerConnectionException extends Exception {
    //Parameterless Constructor
    public ServerConnectionException() {
    }

    //Constructor that accepts a message
    public ServerConnectionException(String message) {
        super(message);
    }

    public ServerConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerConnectionException(Throwable cause) {
        super(cause);
    }
}