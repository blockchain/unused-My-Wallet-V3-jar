package info.blockchain.wallet.ethereum.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

public class EthAddressResponseMap {

    private Map<String, EthAddressResponse> map = new HashMap<>();

    @JsonAnyGetter
    public Map<String, EthAddressResponse> getEthAddressResponseMap() {
        return map;
    }

    @JsonAnySetter
    public void setEthAddressResponseMap(String name, EthAddressResponse value) {
        map.put(name, value);
    }
}