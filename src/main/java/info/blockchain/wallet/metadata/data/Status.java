package info.blockchain.wallet.metadata.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {

    String status;

    @Override
    public String toString() {
        return "Status{" +
                "status='" + status + '\'' +
                '}';
    }
}
