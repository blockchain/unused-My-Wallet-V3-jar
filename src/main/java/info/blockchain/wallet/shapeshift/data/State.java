package info.blockchain.wallet.shapeshift.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class State {

    private String Name;
    private String Code;

    public State() {
        // Empty constructor for Jackson's reflection methods
    }

    public State(String name, String code) {
        Name = name;
        Code = code;
    }

    public String getName() {
        return Name;
    }

    public String getCode() {
        return Code;
    }

}
