package info.blockchain.wallet.transaction;

import info.blockchain.api.data.*;
import java.util.Map;

/*
Use info.blockchain.api.data.Transaction
 */
@Deprecated
public class Tx {

    private String strHash = null;
    private String strNote = null;//not used
    private String strDirection = null;
    private double amount = 0.0;
    private long ts = 0L;
    private Map<Integer, String> tags = null;//not used
    private boolean isMove = false;
    private long confirmations = 0L;
    private boolean isWatchOnly = false;
    private boolean isDoubleSpend = false;

    public Tx(String hash, String note, String direction, double amount, long date, Map<Integer, String> tags) {
        strHash = hash;
        strNote = note;
        strDirection = direction;
        this.amount = amount;
        ts = date;
        this.tags = tags;
    }

    public Tx(String note, String direction, double amount, long date, Map<Integer, String> tags) {
        strNote = note;
        strDirection = direction;
        this.amount = amount;
        ts = date;
        this.tags = tags;
    }

    public String getHash() {
        return strHash;
    }

    public void setHash(String hash) {
        strHash = hash;
    }

    /*
    Used in android
     */
    public String getDirection() {
        return strDirection;
    }

    /*
    Set from multiaddr
     */
    public void setDirection(String direction) {
        strDirection = direction;
    }

    public long getTS() {
        return ts;
    }

    public void setTS(long ts) {
        this.ts = ts;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    /*
    used in multiaddr
     */
    public boolean isMove() {
        return isMove;
    }

    public void setIsMove(boolean isMove) {
        this.isMove = isMove;
    }

    public boolean isDoubleSpend() {
        return isDoubleSpend;
    }

    public void setDoubleSpend(boolean doubleSpend) {
        isDoubleSpend = doubleSpend;
    }

//    public Map<Integer, String> getTags() {
//        return this.tags;
//    }

//    public void setTags(Map<Integer, String> tags) {
//        this.tags = tags;
//    }

    public long getConfirmations() {
        return confirmations;
    }

    /*
    calc with latest block and height
     */
    public void setConfirmations(long confirmations) {
        this.confirmations = confirmations;
    }

    //not used
    public void setIsWatchOnly(boolean isWatchOnly) {
        this.isWatchOnly = isWatchOnly;
    }

    public boolean isWatchOnly() {
        return isWatchOnly;
    }
}
