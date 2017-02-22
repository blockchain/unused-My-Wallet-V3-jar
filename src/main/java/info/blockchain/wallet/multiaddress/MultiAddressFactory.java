package info.blockchain.wallet.multiaddress;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.Address;
import info.blockchain.api.data.Input;
import info.blockchain.api.data.MultiAddress;
import info.blockchain.api.data.Output;
import info.blockchain.api.data.Transaction;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDAccount;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import retrofit2.Call;

// TODO: 17/02/2017  Order txs - TxMostRecentDateComparator
// TODO: 22/02/2017  Not 100% yet
public class MultiAddressFactory {

    public static Call<MultiAddress> getMultiAddress(List<String> addressList, String context,
        int filter, int limit,
        int offset) throws IOException {

        BlockExplorer blockExplorer = new BlockExplorer(
            BlockchainFramework.getRetrofitServerInstance(), BlockchainFramework.getApiCode());
        return blockExplorer
            .getMultiAddress(addressList, context, filter, limit, offset);
    }

    public static boolean isOwnHDAddress(MultiAddress body, String address) {

        for (Transaction tx : body.getTxs()) {
            for (Input input : tx.getInputs()) {
                Output prevOut = input.getPrevOut();
                if (prevOut.getXpub() != null && address.equals(prevOut.getAddr())) {
                    return true;
                }
            }

            for (Output out : tx.getOut()) {
                if (out.getXpub() != null && address.equals(out.getAddr())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Pair<Integer, Integer> getHighestIndexes(MultiAddress body, String xpub) {

        int receiveIndex = 0;
        int changeIndex = 0;

        for (Address address : body.getAddresses()) {

            if (address.getAddress().equals(xpub)) {
                receiveIndex = address.getAccountIndex();
                changeIndex = address.getChangeIndex();
            }
        }

        return Pair.of(receiveIndex, changeIndex);
    }

    public static String getXpubFromAddress(MultiAddress body, String address) {

        final int lookAhead = 10;

        for (Address addr : body.getAddresses()) {

            String xpubOrAddress = addr.getAddress();

            for (int i = 0; i <= addr.getAccountIndex() + lookAhead; i++) {
                try {
                    HDAccount account = new HDAccount(
                        PersistentUrls.getInstance().getCurrentNetworkParams(), xpubOrAddress);
                    if (address.equals(account.getReceive().getAddressAt(i).getAddressString())) {
                        return xpubOrAddress;
                    }
                } catch (Exception e) {
                    //might be address
                }
            }

            for (int i = 0; i <= addr.getChangeIndex() + lookAhead; i++) {
                try {
                    HDAccount account = new HDAccount(
                        PersistentUrls.getInstance().getCurrentNetworkParams(), xpubOrAddress);
                    if (address.equals(account.getChange().getAddressAt(i).getAddressString())) {
                        return xpubOrAddress;
                    }
                } catch (Exception e) {
                    //might be address
                }
            }
        }

        return null;
    }

    public static int getNextChangeAddress(MultiAddress body, String addressOrXpub) {

        int changeIndex = 0;
        for (Address address : body.getAddresses()) {
            if(address.getAddress().equals(addressOrXpub)) {
                changeIndex = address.getChangeIndex();
            }
        }
        return changeIndex + 1;
    }

    public static int getNextReceiveAddress(MultiAddress body, String addressOrXpub) {

        int receiveIndex = 0;
        for (Address address : body.getAddresses()) {
            if(address.getAddress().equals(addressOrXpub)) {
                receiveIndex = address.getAccountIndex();
            }
        }
        return receiveIndex + 1;
    }
}