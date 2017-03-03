package info.blockchain.wallet.multiaddress;

import com.fasterxml.jackson.core.JsonProcessingException;
import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.Address;
import info.blockchain.api.data.Input;
import info.blockchain.api.data.MultiAddress;
import info.blockchain.api.data.Output;
import info.blockchain.api.data.Transaction;
import info.blockchain.api.data.Transaction.Direction;
import info.blockchain.api.data.Xpub;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDAccount;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import retrofit2.Call;

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

    public static void sort(ArrayList<Transaction> txs) {
        if(txs == null) return;
        Collections.sort(txs, new TxMostRecentDateComparator());
    }

    public static class TxMostRecentDateComparator implements Comparator<Transaction> {

        public int compare(Transaction t1, Transaction t2) {

            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            int ret;

            if (t1.getTime() > t2.getTime()) {
                ret = BEFORE;
            } else if (t1.getTime() < t2.getTime()) {
                ret = AFTER;
            } else {
                ret = EQUAL;
            }

            return ret;
        }
    }

    public static void flagTransactionDirection(LinkedHashSet<String> ownAddressesAndXpubs
        , List<String> watchOnlyAddresses
        , MultiAddress multiAddress) {

        List<Transaction> txs = multiAddress.getTxs();
        if(txs == null) return;

        List<String> own_hd_addresses = new ArrayList<>();
        List<String> moveToAddrArray = new ArrayList<>();

        for(Transaction tx : txs) {

            //Set confirmations
            long latestBlock = multiAddress.getInfo().getLatestBlock().getHeight();
            long txBlockHeight = tx.getBlockHeight();
            if(latestBlock > 0 && txBlockHeight > 0) {
                tx.setConfirmations((int) ((latestBlock - txBlockHeight) + 1));
            } else {
                tx.setConfirmations(0);
            }

            //Set direction
            if(tx.getResult().signum() > 0) {
                tx.setDirection(Direction.RECEIVED);
            } else {
                tx.setDirection(Direction.SENT);
            }

            BigInteger moveAmount = BigInteger.ZERO;
            Direction direction = Direction.SENT;

            ArrayList<String> ownInput = new ArrayList<>();
            ArrayList<String> ownOutput = new ArrayList<>();

            ArrayList<BigInteger> amountListOut = new ArrayList<>();
            ArrayList<BigInteger> amountListIn = new ArrayList<>();

            String inputAddr = null;
            long inputs_amount = 0L;

            String outputAddr = null;
            long outputs_amount = 0L;

            for(Input input : tx.getInputs()) {

                Output prevOut = input.getPrevOut();

                if(prevOut != null) {

                    inputAddr = prevOut.getAddr();

                    if(watchOnlyAddresses.contains(inputAddr)) {
                        tx.setWatchOnly(true);
                    }

                    if (prevOut.getXpub() != null) {
//                        Xpub xpubBody = prevOut.getXpub();

                        if (prevOut.getAddr() != null && !own_hd_addresses
                            .contains(prevOut.getAddr())) {
                            own_hd_addresses.add(prevOut.getAddr());
                            direction = Direction.TRANSFERRED;
                        }
                    } else {
                        //(Legacy to HD transfer check)
                        //If contained in our own legacy addresses - it is a move
                        //We still need to calculate the move amount below
                        if (ownAddressesAndXpubs.contains(prevOut.getAddr())) {
                            direction = Direction.TRANSFERRED;

                            BigInteger amountInput = prevOut.getValue();
                            if (ownInput.contains(inputAddr)) {
                                int index = ownInput.indexOf(inputAddr);
                                amountListIn.set(index, amountListIn.get(index).add(amountInput));
                            } else {
                                ownInput.add(inputAddr);
                                amountListIn.add(amountInput);
                            }
                        }
                    }

                    inputs_amount += prevOut.getValue().longValue();
                }
            }

            for(Output output :tx.getOut()) {

                outputAddr = output.getAddr();

                if(watchOnlyAddresses.contains(outputAddr)) {
                    tx.setWatchOnly(true);
                }

                if(output.getXpub() != null) {

                    Xpub xpubBody = output.getXpub();

                    if(xpubBody.getPath().startsWith("M/0/")) {
                        moveAmount = moveAmount.add(output.getValue());
                        moveToAddrArray.add(xpubBody.getM());
                    }
                    if (output.getAddr() != null
                        && !own_hd_addresses.contains(output.getAddr())) {
                        own_hd_addresses.add(output.getAddr());
                    }

                } else {

                    //If output is own legacy or hd address = move
                    if(output.getAddr() != null
                        && ownAddressesAndXpubs.contains(output.getAddr())
                        && own_hd_addresses.contains(output.getAddr())) {

                        BigInteger amountOutput = output.getValue();

                        //Don't add change coming back
                        if (inputAddr != null && !inputAddr.equals(outputAddr)) {
                            moveAmount = moveAmount.add(output.getValue());

                            if (ownOutput.contains(outputAddr)) {
                                int index = ownOutput.indexOf(outputAddr);
                                amountListOut.set(index, amountListOut.get(index).add(amountOutput));
                            } else {
                                ownOutput.add(outputAddr);
                                amountListOut.add(amountOutput);
                            }

                            moveToAddrArray.add(output.getAddr());
                            direction = Direction.TRANSFERRED;
                        }

                    } else {
                        //one foreign address is enough to not call it move anymore
                        direction = Direction.SENT;
                    }

                }

                outputs_amount += output.getValue().longValue();
            }

            // TODO: 03/03/2017  Some transferred cases might not have been covered yet
//            for (String address : ownOutput) {
//                int index = ownOutput.indexOf(address);
//                BigInteger outputAmount = amountListOut.get(index);
//
//                //Check if this is just the change we get back
//                if (ownInput.contains(address)) {
//
//                    BigInteger inputAmount = amountListIn.get(ownInput.indexOf(address));
//                    if (inputAmount.longValue() < outputAmount.longValue()) {
//                        direction = Direction.RECEIVED;
//                        outputAmount = inputAmount.subtract(outputAmount).abs();
//                    } else {
//                        direction = Direction.SENT;
//                        outputAmount = amountListIn.get(ownInput.indexOf(address))
//                            .subtract(outputAmount).negate();
//                    }
//
//                    tx.setResult(outputAmount);
//                }
//            }
//
//            for (String address : ownInput) {
//
//                if (ownOutput.contains(address))
//                    continue;
//
//                int index = ownInput.indexOf(address);
//                BigInteger inputAmount = amountListIn.get(index).negate();
//
//                if (direction != Direction.TRANSFERRED)
//                    direction = Direction.SENT;
//
//                tx.setResult(inputAmount);
//            }

            if (Math.abs(inputs_amount - outputs_amount) == Math.abs(tx.getResult().longValue())) {
                direction = Direction.TRANSFERRED;
            }

            if(direction == Direction.TRANSFERRED) {
                tx.setDirection(Direction.TRANSFERRED);
                tx.setResult(moveAmount);
            }
        }
    }
}