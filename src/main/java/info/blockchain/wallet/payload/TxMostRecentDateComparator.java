package info.blockchain.wallet.payload;

import java.util.Comparator;

import info.blockchain.wallet.payload.Tx;

public class TxMostRecentDateComparator implements Comparator<Tx> {

    public int compare(Tx t1, Tx t2) {

        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        int ret = 0;

        if(t1.getTS() > t2.getTS()) {
            ret = BEFORE;
        }
        else if(t1.getTS() < t2.getTS()) {
            ret = AFTER;
        }
        else    {
            ret = EQUAL;
        }

        return ret;
    }

}
