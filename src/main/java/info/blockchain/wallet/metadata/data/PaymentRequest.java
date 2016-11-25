package info.blockchain.wallet.metadata.data;

public class PaymentRequest {

    long amount;
    String note;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long satoshis) {
        this.amount = satoshis;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
