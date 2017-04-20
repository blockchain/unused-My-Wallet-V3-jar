package info.blockchain.wallet.payment;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;

public class TransactionOutPointConnected extends TransactionOutPoint {

    private TransactionOutput connectedOutput;

    public TransactionOutPointConnected(NetworkParameters params, long index, Sha256Hash hash) {
        super(params, index, hash);
    }

    public void setConnectedOutput(TransactionOutput connectedOutput){
        this.connectedOutput = connectedOutput;
    }

    /**
     * Crucial for signing transactions.
     * ConnectedOutput is populated inside bitcoinj and can't be set.
     * @return TransactionOutput
     */
    @Override
    public TransactionOutput getConnectedOutput() {
        return connectedOutput;
    }
}
