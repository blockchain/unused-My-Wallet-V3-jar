package info.blockchain.wallet.exceptions;

public class AccountLockedException extends Exception {

    public AccountLockedException() {
        // Empty constructor
    }

    public AccountLockedException(String message) {
        super(message);
    }

    public AccountLockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountLockedException(Throwable cause) {
        super(cause);
    }
}
