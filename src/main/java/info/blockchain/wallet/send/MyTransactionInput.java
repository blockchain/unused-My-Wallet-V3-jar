package info.blockchain.wallet.send;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;

import java.math.BigInteger;

public class MyTransactionInput extends TransactionInput {

    private static final long serialVersionUID = 1L;

    public String address;
    public BigInteger value;
    public final NetworkParameters params;

    public String txHash;
    public int txPos;

    public MyTransactionInput(NetworkParameters params, Transaction parentTransaction, byte[] scriptBytes, TransactionOutPoint outpoint, String txHash, int txPos) {
        super(params, parentTransaction, scriptBytes, outpoint);
        this.params = params;
        this.txHash = txHash;
        this.txPos = txPos;
    }

    public MyTransactionInput(NetworkParameters params, Transaction parentTransaction, byte[] scriptBytes, TransactionOutPoint outpoint) {
        super(params, parentTransaction, scriptBytes, outpoint);
        this.params = params;
        this.txHash = "";
        this.txPos = -1;
    }

    public Coin getValue() {
        return Coin.valueOf(value.longValue());
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getTxPos() {
        return txPos;
    }

    public void setTxPos(int txPos) {
        this.txPos = txPos;
    }
}