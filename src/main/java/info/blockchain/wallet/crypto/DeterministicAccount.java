package info.blockchain.wallet.crypto;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

public class DeterministicAccount implements DeterministicNode {

    private DeterministicKey deterministicAccountKey;
    private List<DeterministicChain> chains = null;

    public DeterministicAccount(DeterministicKey deterministicWalletKey, int accountIndex) {

        // L0PRV & STDVx: private derivation.
        int childnum = accountIndex;
        childnum |= ChildNumber.HARDENED_BIT;
        deterministicAccountKey = HDKeyDerivation.deriveChildKey(deterministicWalletKey, childnum);

        chains = new ArrayList();
        chains.add(new DeterministicChain(deterministicAccountKey, DeterministicChain.RECEIVE_CHAIN));
        chains.add(new DeterministicChain(deterministicAccountKey, DeterministicChain.CHANGE_CHAIN));
    }

    public DeterministicAccount(NetworkParameters params, String xpub) {

        deterministicAccountKey = createMasterPubKeyFromXPub(params, xpub);

        chains = new ArrayList();
        chains.add(new DeterministicChain(deterministicAccountKey, DeterministicChain.RECEIVE_CHAIN));
        chains.add(new DeterministicChain(deterministicAccountKey, DeterministicChain.CHANGE_CHAIN));
    }

    private DeterministicKey createMasterPubKeyFromXPub(NetworkParameters params, String xpub) throws AddressFormatException {

        byte[] xpubBytes = Base58.decodeChecked(xpub);

        ByteBuffer bb = ByteBuffer.wrap(xpubBytes);

        int version = bb.getInt();
        if(version != params.getBip32HeaderPub())   {
            throw new AddressFormatException("invalid xpub version");
        }

        byte[] chain = new byte[32];
        byte[] pub = new byte[33];
        // depth:
        bb.get();
        // parent fingerprint:
        bb.getInt();
        // child no.
        bb.getInt();
        bb.get(chain);
        bb.get(pub);

        return HDKeyDerivation.createMasterPubKeyFromBytes(pub, chain);
    }

    @Override
    public DeterministicKey getNode() {
        return deterministicAccountKey;
    }

    @Override
    public String getPath() {
        return deterministicAccountKey.getPathAsString();
    }

    public List<DeterministicChain> getChains() {
        return chains;
    }
}
