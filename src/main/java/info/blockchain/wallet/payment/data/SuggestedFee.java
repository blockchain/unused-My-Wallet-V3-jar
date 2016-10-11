package info.blockchain.wallet.payment.data;

import java.math.BigInteger;
import java.util.ArrayList;

public class SuggestedFee {

    public BigInteger defaultFeePerKb;
    public ArrayList<Estimates> estimateList;
    public boolean isSurge;

    @Override
    public String toString() {
        return "SuggestedFee{" +
                "defaultFeePerKb=" + defaultFeePerKb +
                ", estimateList=" + estimateList +
                '}';
    }

    public static class Estimates {

        public Estimates(BigInteger fee, boolean surge, boolean ok) {
            this.fee = fee;
            this.surge = surge;
            this.ok = ok;
        }

        public final BigInteger fee;
        public final boolean surge;
        public final boolean ok;
    }
}
