package info.blockchain.wallet.transaction;

import java.util.Comparator;

public class TxMostRecentDateComparator implements Comparator<Tx> {

    public int compare(Tx t1, Tx t2) {

        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        int ret;

        if (t1.getTS() > t2.getTS()) {
            ret = BEFORE;
        } else if (t1.getTS() < t2.getTS()) {
            ret = AFTER;
        } else {
            ret = EQUAL;
        }

        return ret;
    }

}
