package info.blockchain.wallet.metadata;

import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.metadata.data.RemoteMetadataNodes;
import info.blockchain.wallet.util.MetadataUtil;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Restores derived metadata nodes from a metadata node derived from user credentials.
 * This is to avoid repeatedly asking user for second password.
 */
public class MetadataNodeFactory {

    private DeterministicKey sharedMetadataNode;
    private DeterministicKey metadataNode;

    private Metadata secondPwNode;
    private Metadata secondPwNodeLegacy;

    public MetadataNodeFactory(String guid, String sharedKey, String walletPassword) throws Exception {
        this.secondPwNode = deriveSecondPasswordNode(guid, sharedKey, walletPassword);

        this.secondPwNodeLegacy = deriveSecondPasswordNodeLegacy(guid, sharedKey, walletPassword);
        deleteMetadataFromNode(secondPwNodeLegacy);
    }

    public boolean isMetadataUsable() {
        try {
            String nodesJson = secondPwNode.getMetadata();
            if (nodesJson == null) {
                //no record
                //prompt for second pw if need then saveMetadataHdNodes()
                return false;
            } else {
                //Yes load nodes from stored metadata.
                return loadNodes(RemoteMetadataNodes.fromJson(nodesJson));
            }

        } catch (Exception e) {
            //Metadata decryption will fail if user changes wallet password.
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadNodes(RemoteMetadataNodes remoteMetadataNodes) {
        //If not all nodes available fail.
        if (!remoteMetadataNodes.isAllNodesAvailable()) {
            return false;
        }

        sharedMetadataNode = DeterministicKey.deserializeB58(
                remoteMetadataNodes.getMdid(),
                PersistentUrls.getInstance().getBitcoinParams());
        metadataNode = DeterministicKey.deserializeB58(
                remoteMetadataNodes.getMetadata(),
                PersistentUrls.getInstance().getBitcoinParams());

        return true;
    }

    public boolean saveMetadataHdNodes(DeterministicKey masterKey) throws Exception {
        //Derive nodes
        DeterministicKey md = MetadataUtil.deriveMetadataNode(masterKey);
        DeterministicKey smd = MetadataUtil.deriveSharedMetadataNode(masterKey);

        //Save nodes hex on 2nd pw metadata
        RemoteMetadataNodes remoteMetadataNodes = new RemoteMetadataNodes();
        remoteMetadataNodes.setMdid(smd.serializePrivB58(PersistentUrls.getInstance().getBitcoinParams()));
        remoteMetadataNodes.setMetadata(md.serializePrivB58(PersistentUrls.getInstance().getBitcoinParams()));
        secondPwNode.putMetadata(remoteMetadataNodes.toJson());

        return loadNodes(remoteMetadataNodes);
    }

    private Metadata deriveSecondPasswordNodeLegacy(String guid, String sharedkey, String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String text = guid + sharedkey;
        md.update(text.getBytes("UTF-8"));
        byte[] entropy = md.digest();
        BigInteger bi = new BigInteger(1, entropy);

        ECKey key = ECKey.fromPrivate(bi);

        byte[] enc = AESUtil.stringToKey(password + sharedkey, 5000);

        Metadata metadata = new Metadata();
        metadata.setEncrypted(true);
        metadata.setAddress(key.toAddress(PersistentUrls.getInstance().getBitcoinParams()).toString());
        metadata.setNode(key);
        metadata.setEncryptionKey(enc);
        metadata.setType(-1);
        metadata.fetchMagic();

        return metadata;
    }

    private Metadata deriveSecondPasswordNode(String guid, String sharedkey, String password) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String input = guid + sharedkey + password;
        md.update(input.getBytes("UTF-8"));
        byte[] entropy = md.digest();
        BigInteger bi = new BigInteger(1, entropy);

        ECKey key = ECKey.fromPrivate(bi);

        Metadata metadata = new Metadata();
        metadata.setEncrypted(true);
        metadata.setAddress(key.toAddress(PersistentUrls.getInstance().getBitcoinParams()).toString());
        metadata.setNode(key);
        metadata.setEncryptionKey(key.getPrivKeyBytes());
        metadata.setType(-1);
        metadata.fetchMagic();

        return metadata;
    }

    public boolean isLegacySecondPwNodeAvailable() {
        try {
            return  secondPwNodeLegacy.getMetadata() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void deleteMetadataFromNode(Metadata node) {
        try {
            String nodesJson = node.getMetadata();
            if (nodesJson == null) {
                //no record
            } else {
                RemoteMetadataNodes remoteMetadataNodes = RemoteMetadataNodes.fromJson(nodesJson);
                node.deleteMetadata(remoteMetadataNodes.toJson());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DeterministicKey getSharedMetadataNode() {
        return sharedMetadataNode;
    }

    public DeterministicKey getMetadataNode() {
        return metadataNode;
    }
}
