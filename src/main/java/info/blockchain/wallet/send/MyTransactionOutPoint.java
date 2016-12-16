package info.blockchain.wallet.send;

import info.blockchain.api.PersistentUrls;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;

import java.math.BigInteger;

public class MyTransactionOutPoint extends TransactionOutPoint {

    private static final long serialVersionUID = 1L;
    private final byte[] scriptBytes;
    private final int txOutputN;
    private final Sha256Hash txHash;
    private final BigInteger value;
    private int confirmations;
    private String path;

    public MyTransactionOutPoint(Sha256Hash txHash, int txOutputN, BigInteger value, byte[] scriptBytes) throws ProtocolException {
        super(PersistentUrls.getInstance().getCurrentNetworkParams(), txOutputN, Sha256Hash.wrap(txHash.getBytes()));
        this.scriptBytes = scriptBytes;
        this.value = value;
        this.txOutputN = txOutputN;
        this.txHash = txHash;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public byte[] getScriptBytes() {
        return scriptBytes;
    }

    public int getTxOutputN() {
        return txOutputN;
    }

    public Sha256Hash getTxHash() {
        return txHash;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    @Override
    public TransactionOutput getConnectedOutput() {
        return new TransactionOutput(params, null, Coin.valueOf(value.longValue()), scriptBytes);
    }

    //@Override
    public byte[] getConnectedPubKeyScript() {
        return scriptBytes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}