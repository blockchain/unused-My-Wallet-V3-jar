package info.blockchain.wallet.send;

import java.math.BigInteger;
import java.util.List;

public class UnspentOutputsBundle	{

    private List<MyTransactionOutPoint> outputs = null;
    private BigInteger totalAmount = BigInteger.ZERO;//Selected coin total
    private BigInteger recommendedFee = BigInteger.valueOf(-1L);
    private String notice = null;
    private BigInteger sweepAmount = BigInteger.ZERO;//Selected coin total

    public UnspentOutputsBundle() {
        outputs = null;
        totalAmount = BigInteger.ZERO;
    }

    public List<MyTransactionOutPoint> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<MyTransactionOutPoint> outputs) {
        this.outputs = outputs;
    }

    public BigInteger getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigInteger totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigInteger getRecommendedFee() {
        return recommendedFee;
    }

    public void setRecommendedFee(BigInteger recommendedFee) {
        this.recommendedFee = recommendedFee;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public BigInteger getSweepAmount() {
        return sweepAmount;
    }

    public void setSweepAmount(BigInteger sweepAmount) {
        this.sweepAmount = sweepAmount;
    }

    @Override
    public String toString() {
        return "UnspentOutputsBundle{" +
                "outputs=" + outputs +
                ", totalAmount=" + totalAmount +
                ", recommendedFee=" + recommendedFee +
                ", notice='" + notice + '\'' +
                ", sweepAmount=" + sweepAmount +
                '}';
    }
}
