package info.blockchain.wallet.ethereum;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE)
public class EthereumAccount {

    private static final String DERIVATION_PATH = "m/44'/60'/0'/0";
    private static final int DERIVATION_PATH_PURPOSE = 44;
    private static final int DERIVATION_PATH_COIN = 60;
    private static final int CHANGE_INDEX = 0;
    private static final int ADDRESS_INDEX = 0;

    @JsonProperty("archived")
    private boolean archived;

    @JsonProperty("label")
    private String label;

    @JsonProperty("addr")
    private String address;

    ECKey accountKey;

    public EthereumAccount() {
        //default constructor for Jackson
    }

    public EthereumAccount(ECKey addressKey, String label) {
        this.accountKey = addressKey;
        assert Utils.isValidAddress(accountKey.getAddress()) : "Invalid Ethereum address";
        this.address = "0x" + Hex.toHexString(accountKey.getAddress());
        this.label = label;
    }

    public static EthereumAccount derive(DeterministicKey masterKey, int accountIndex, String label) {

        DeterministicKey purposeKey = HDKeyDerivation.deriveChildKey(masterKey, DERIVATION_PATH_PURPOSE | ChildNumber.HARDENED_BIT);
        DeterministicKey rootKey = HDKeyDerivation.deriveChildKey(purposeKey, DERIVATION_PATH_COIN | ChildNumber.HARDENED_BIT);
        DeterministicKey accountKey = HDKeyDerivation.deriveChildKey(rootKey, accountIndex | ChildNumber.HARDENED_BIT);
        DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(accountKey, CHANGE_INDEX);
        DeterministicKey addressKey = HDKeyDerivation.deriveChildKey(changeKey, ADDRESS_INDEX);

        return new EthereumAccount(ECKey.fromPrivate(addressKey.getPrivKeyBytes()), label);
    }

    public String getPrivateKey() {
        return Hex.toHexString(accountKey.getPrivKeyBytes());
    }

    //TODO: 24/08/2017 Case sensitivity is used for optional checksum
    //TODO: 24/08/2017 https://forum.ethereum.org/discussion/9220/eth-address-upper-and-lower-characters-does-not-matter
    public String getAddress() {
        return address;
    }
}
