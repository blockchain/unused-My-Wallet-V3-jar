package info.blockchain.api.metadata.data;

public class Share {

    String id;//one-time UUID
    String mdid;//mdid of sender
//    String contact;//this is returned but not sure what for yet

    public String getId() {
        return id;
    }

    public String getMdid() {
        return mdid;
    }

    @Override
    public String toString() {
        return "Share{" +
                "id='" + id + '\'' +
                ", mdid='" + mdid + '\'' +
                '}';
    }
}
