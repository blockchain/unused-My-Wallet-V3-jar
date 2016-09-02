package info.blockchain.wallet.exceptions;

public class ServerConnectionException extends Exception {
    //Parameterless Constructor
    public ServerConnectionException() {
    }

    //Constructor that accepts a message
    public ServerConnectionException(String message) {
        super(message);
    }
}