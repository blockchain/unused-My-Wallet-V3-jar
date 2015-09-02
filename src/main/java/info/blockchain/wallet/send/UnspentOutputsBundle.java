package info.blockchain.wallet.send;

import java.math.BigInteger;
import java.util.List;

public class UnspentOutputsBundle	{

    private List<MyTransactionOutPoint> outputs = null;
    private BigInteger totalAmount = BigInteger.ZERO;
    private BigInteger recommendedFee = BigInteger.valueOf(-1L);

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
}
