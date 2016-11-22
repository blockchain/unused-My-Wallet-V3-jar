package info.blockchain.wallet.metadata.data;

public class Invitation {

    String id;//one-time UUID
    String mdid;//mdid of sender
    String contact;//mdid of recipient

    public String getId() {
        return id;
    }

    public String getMdid() {
        return mdid;
    }

    public String getContact() {
        return contact;
    }

    @Override
    public String toString() {
        return "Invitation{" +
                "id='" + id + '\'' +
                ", mdid='" + mdid + '\'' +
                ", contact='" + contact + '\'' +
                '}';
    }
}
