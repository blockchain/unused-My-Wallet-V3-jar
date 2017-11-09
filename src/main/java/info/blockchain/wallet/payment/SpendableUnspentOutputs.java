package info.blockchain.wallet.payment;

import info.blockchain.api.data.UnspentOutput;
import java.math.BigInteger;
import java.util.List;

public class SpendableUnspentOutputs {

    private List<UnspentOutput> spendableOutputs = null;
    private BigInteger absoluteFee = BigInteger.ZERO;
    private BigInteger consumedAmount = BigInteger.ZERO;

    public SpendableUnspentOutputs() {
    }

    public void setSpendableOutputs(List<UnspentOutput> spendableOutputs) {
        this.spendableOutputs = spendableOutputs;
    }

    public List<UnspentOutput> getSpendableOutputs() {
        return spendableOutputs;
    }

    public BigInteger getAbsoluteFee() {
        return absoluteFee;
    }

    public void setAbsoluteFee(BigInteger absoluteFee) {
        this.absoluteFee = absoluteFee;
    }

    public BigInteger getConsumedAmount() {
        return consumedAmount;
    }

    public void setConsumedAmount(BigInteger consumedAmount) {
        this.consumedAmount = consumedAmount;
    }
}
