package info.blockchain.wallet.metadata.data;

import org.bitcoinj.core.Coin;
import org.bitcoinj.uri.BitcoinURI;

public class PaymentRequestResponse {

    long amount;
    String label = null;//unused
    String note;
    String address;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toBitcoinURI() {
        return BitcoinURI.convertToBitcoinURI(address, Coin.valueOf(amount), label, note);
    }
}
