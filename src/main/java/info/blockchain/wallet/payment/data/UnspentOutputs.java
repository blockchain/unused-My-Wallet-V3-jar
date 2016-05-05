package info.blockchain.wallet.payment.data;

import info.blockchain.wallet.send.MyTransactionOutPoint;

import java.math.BigInteger;
import java.util.List;

public class UnspentOutputs {

    private List<MyTransactionOutPoint> outputs = null;
    private String notice = null;
    private BigInteger balance = BigInteger.ZERO;

    public UnspentOutputs(List<MyTransactionOutPoint> outputs, BigInteger balance, String notice) {
        this.outputs = outputs;
        this.notice = notice;
        this.balance = balance;
    }

    public List<MyTransactionOutPoint> getOutputs() {
        return outputs;
    }

    public String getNotice() {
        return notice;
    }

    public BigInteger getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "UnspentOutputsBundle{" +
                "outputs=" + outputs +
                ", notice='" + notice + '\'' +
                ", balance=" + balance +
                '}';
    }
}
