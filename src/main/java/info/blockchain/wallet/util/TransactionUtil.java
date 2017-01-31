package info.blockchain.wallet.util;

import info.blockchain.wallet.transaction.Transaction;

public final class TransactionUtil {

    /**
     * Returns the value of a {@link Transaction}, ie the actual amount sent to someone else's
     * wallet. It does so by checking the output values against the address passed to the function,
     * which should be the intended output address.
     *
     * @param address     The output address
     * @param transaction The Transaction to be evaluated
     * @return A long absolute value of the Transaction, in satoshis
     */
    public static long getTransactionValue(String address, Transaction transaction) {
        return Math.abs(transaction.getOutputValues().get(address));
    }

}
