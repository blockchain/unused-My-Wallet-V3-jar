package info.blockchain.wallet.payment.data;

import java.math.BigInteger;

/**
 * Created by riaanvos on 03/08/16.
 */
public class SweepBundle {

    private BigInteger sweepAmount;
    private BigInteger sweepFee;
    private BigInteger consumedAmount;

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

    public BigInteger getConsumedAmount() {
        return consumedAmount;
    }

    public void setConsumedAmount(BigInteger consumedAmount) {
        this.consumedAmount = consumedAmount;
    }

    @Override
    public String toString() {
        return "SweepBundle{" +
                "sweepAmount=" + sweepAmount +
                ", sweepFee=" + sweepFee +
                ", consumedAmount=" + consumedAmount +
                '}';
    }
}
