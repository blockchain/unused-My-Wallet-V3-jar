package info.blockchain.wallet.multiaddress;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.Address;
import info.blockchain.api.data.Input;
import info.blockchain.api.data.MultiAddress;
import info.blockchain.api.data.Output;
import info.blockchain.api.data.Transaction;
import info.blockchain.api.data.Xpub;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDAccount;
import info.blockchain.wallet.bip44.HDChain;
import info.blockchain.wallet.multiaddress.TransactionSummary.Direction;
import info.blockchain.wallet.payload.data.AddressLabels;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

        return changeIndex;
    }

    public static int getNextReceiveAddress(MultiAddress body, String addressOrXpub, List<AddressLabels> reservedAddresses) {

        int receiveIndex = 0;
        for (Address address : body.getAddresses()) {
            if(address.getAddress().equals(addressOrXpub)) {
                receiveIndex = address.getAccountIndex();
            }
        }

        //Skip reserved addresses
        for(AddressLabels reservedAddress : reservedAddresses) {
            if(reservedAddress.getIndex() == receiveIndex) {
                receiveIndex++;
            }
        }

        return receiveIndex;
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

    public static List<TransactionSummary> summarize(LinkedHashSet<String> ownAddressesAndXpubs
        , List<String> watchOnlyAddresses
        , MultiAddress multiAddress
        , List<String> legacy) {

        List<TransactionSummary> summaryList = new ArrayList<>();

        List<Transaction> txs = multiAddress.getTxs();
        if(txs == null) {
            //Address might not contain transactions
            return summaryList;
        }

        for(Transaction tx : txs) {

            boolean isLegacy = false;

            TransactionSummary txSummary = new TransactionSummary();
            txSummary.inputsMap = new HashMap<>();
            txSummary.outputsMap = new HashMap<>();

            if(tx.getResult().signum() > 0) {
                txSummary.setDirection(Direction.RECEIVED);
            } else {
                txSummary.setDirection(Direction.SENT);
                //Or potential Direction.TRANSFERRED
            }

            //Inputs
            String inputAddr;
            BigInteger inputValue;
            for(Input input : tx.getInputs()) {

                Output prevOut = input.getPrevOut();
                if(prevOut != null) {

                    inputAddr = prevOut.getAddr();
                    inputValue = prevOut.getValue();
                    if(inputAddr != null) {

                        //Transaction from HD account
                        Xpub xpubBody = prevOut.getXpub();
                        if (xpubBody != null) {
                            //xpubBody will only show if it belongs to our account
                            //inputAddr belongs to our own account - add it, it's a transfer/send
                            ownAddressesAndXpubs.add(inputAddr);
                        }

                        //Flag as watch only
                        if (watchOnlyAddresses.contains(inputAddr)) {
                            txSummary.setWatchOnly(true);
                        }

                        //Flag as imported legacy address
                        if (legacy != null && legacy.contains(inputAddr)) {
                            isLegacy = true;
                        }

                        //Keep track of inputs
                        txSummary.inputsMap.put(inputAddr, inputValue);
                    } else {
                        //This will never happen unless server has issues
                    }

                } else {
                    //Newly generated coin
                }
            }

            //Outputs
            HashMap<String, BigInteger> allOutputs = new HashMap<>();
            String outputAddr;
            BigInteger outputValue;
            for(Output output :tx.getOut()) {

                outputAddr = output.getAddr();
                outputValue = output.getValue();
                if(outputAddr != null) {

                    Xpub xpubBody = output.getXpub();
                    if (xpubBody != null) {

                        //inputAddr belongs to our own account - add it
                        ownAddressesAndXpubs.add(outputAddr);
                        if(xpubBody.getPath().startsWith("M/"+HDChain.RECEIVE_CHAIN+"/")) {

                            if(txSummary.getDirection() == Direction.SENT) {
                                txSummary.setDirection(Direction.TRANSFERRED);
                            }

                            txSummary.outputsMap.put(outputAddr, outputValue);
                        } else {
                            //Change - ignore
                        }

                    } else {
                        //If we own this address, it's a transfer
                        if (ownAddressesAndXpubs.contains(outputAddr)) {

                            if(txSummary.getDirection() == Direction.SENT) {
                                txSummary.setDirection(Direction.TRANSFERRED);
                            }

                            //Don't add change coming back
                            if (!txSummary.inputsMap.containsKey(outputAddr)) {
                                txSummary.outputsMap.put(outputAddr, outputValue);
                            }

                        } else {
                            //Addres does not belong to us
                            txSummary.outputsMap.put(outputAddr, outputValue);
                        }
                    }

                    //Flag as watch only
                    if (watchOnlyAddresses.contains(outputAddr)) {
                        txSummary.setWatchOnly(true);
                    }

                    //Flag as imported legacy address
                    if (legacy != null && legacy.contains(outputAddr)) {
                        isLegacy = true;
                    }

                    //Keep track of outputs
                    allOutputs.put(outputAddr, outputValue);
                } else {
                    //This will never happen unless server has issues
                }
            }

            //If we are filtering for legacy and nothing found
            if (legacy != null && !isLegacy) {
                continue;
            }

            BigInteger fee = calculateFee(txSummary.inputsMap, allOutputs, txSummary.getDirection());
            BigInteger total = calculateTotal(tx.getHash(), txSummary.outputsMap, fee, txSummary.getDirection());
            txSummary.setHash(tx.getHash());
            txSummary.setTotal(total);
            txSummary.setFee(fee);
            txSummary.setTime(tx.getTime());
            txSummary.setDoubleSpend(tx.isDoubleSpend());

            //Set confirmations
            long latestBlock = multiAddress.getInfo().getLatestBlock().getHeight();
            long txBlockHeight = tx.getBlockHeight();
            if(latestBlock > 0 && txBlockHeight > 0) {
                txSummary.setConfirmations((int) ((latestBlock - txBlockHeight) + 1));
            } else {
                txSummary.setConfirmations(0);
            }

            summaryList.add(txSummary);
        }

        return summaryList;
    }

    private static BigInteger calculateTotal(String hash, HashMap<String, BigInteger> nonChange, BigInteger fee, Direction direction) {

        BigInteger total = BigInteger.ZERO;

        for(BigInteger amount : nonChange.values()) {
            total = total.add(amount);
        }

        if(direction == Direction.SENT) {
            total = total.add(fee);
        }

        return total;
    }

    private static BigInteger calculateFee(HashMap<String, BigInteger> inputs, HashMap<String, BigInteger> outputs, Direction direction) {

        if(direction == Direction.RECEIVED) {
            //Receive doesn't have fee. We don't carry cost of tx
            return BigInteger.ZERO;
        }

        BigInteger inputTotal = BigInteger.ZERO;
        BigInteger outputTotal = BigInteger.ZERO;

        for(BigInteger inputAmount : inputs.values()) {
            inputTotal = inputTotal.add(inputAmount);
        }

        for(BigInteger outputAmount : outputs.values()) {
            outputTotal = outputTotal.add(outputAmount);
        }

        return inputTotal.subtract(outputTotal);
    }
}