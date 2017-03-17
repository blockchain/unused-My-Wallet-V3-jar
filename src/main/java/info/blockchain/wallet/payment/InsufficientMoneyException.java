package info.blockchain.wallet.payment;

import java.math.BigInteger;

public class InsufficientMoneyException extends Exception {

    public InsufficientMoneyException(BigInteger missing) {
        super("Insufficient money,  missing "+missing);
    }
}
