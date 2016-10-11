package info.blockchain.wallet.payment.data;

import java.math.BigInteger;

public class SweepBundle {

    private BigInteger sweepAmount;
    private BigInteger sweepFee;

    public BigInteger getSweepAmount() {
        return sweepAmount;
    }

    public void setSweepAmount(BigInteger sweepAmount) {
        this.sweepAmount = sweepAmount;
    }

    public BigInteger getSweepFee() {
        return sweepFee;
    }

    public void setSweepFee(BigInteger sweepFee) {
        this.sweepFee = sweepFee;
    }

    @Override
    public String toString() {
        return "SweepBundle{" +
                "sweepAmount=" + sweepAmount +
                ", sweepFee=" + sweepFee +
                '}';
    }
}
