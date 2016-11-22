package info.blockchain.wallet.metadata.data;

import java.util.Arrays;

public class Trusted {

    String mdid;
    String[] contacts;
    String contact;

    public String getMdid() {
        return mdid;
    }

    public String[] getContacts() {
        return contacts;
    }

    public String getContact() {
        return contact;
    }

    @Override
    public String toString() {
        return "Trusted{" +
                "mdid='" + mdid + '\'' +
                ", contacts=" + Arrays.toString(contacts) +
                ", contact='" + contact + '\'' +
                '}';
    }
}
