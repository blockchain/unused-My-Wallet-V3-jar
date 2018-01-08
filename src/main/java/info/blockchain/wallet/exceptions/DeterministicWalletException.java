package info.blockchain.wallet.exceptions;

/**
 * Unchecked exception.
 * Thrown to indicate any unrecoverable deterministic wallet state.
 */
public class DeterministicWalletException extends RuntimeException {
    public DeterministicWalletException() {
        super();
    }

    public DeterministicWalletException(String message) {
        super(message);
    }

    public DeterministicWalletException(String message, Throwable cause) {
        super(message, cause);
    }
}
