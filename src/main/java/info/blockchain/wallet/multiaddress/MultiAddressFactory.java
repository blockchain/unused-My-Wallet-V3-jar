package info.blockchain.wallet.multiaddress;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.*;
import info.blockchain.wallet.bip44.HDChain;
import info.blockchain.wallet.exceptions.ApiException;
import info.blockchain.wallet.multiaddress.TransactionSummary.Direction;
import info.blockchain.wallet.payload.data.Account;
import info.blockchain.wallet.payload.data.AddressLabels;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static info.blockchain.wallet.payload.PayloadManager.MULTI_ADDRESS_ALL;

public class MultiAddressFactory {

    private BlockExplorer blockExplorer;

    private HashMap<String, Integer> nextReceiveAddressMap;
    private HashMap<String, Integer> nextChangeAddressMap;

    //Field for testing if address belongs to us - Quicker than derivation
    private HashMap<String, String> addressToXpubMap;

    public MultiAddressFactory(BlockExplorer blockExplorer) {
        this.blockExplorer = blockExplorer;
        this.addressToXpubMap = new HashMap<>();
        this.nextReceiveAddressMap = new HashMap<>();
        this.nextChangeAddressMap = new HashMap<>();
    }

    public String getXpubFromAddress(String address) {
        return addressToXpubMap.get(address);
    }

    private MultiAddress getMultiAddress(List<String> allActive, String context, int limit, int offset) throws IOException, ApiException{

        if (context!=null && context.equals(MULTI_ADDRESS_ALL)) {

            Response<MultiAddress> call = blockExplorer.getMultiAddress(allActive, null, BlockExplorer.TX_FILTER_ALL, limit, offset).execute();

            if(call.isSuccessful()) {
                return call.body();
            } else {
                throw new ApiException(call.errorBody().string());
            }

        } else {
            Response<MultiAddress> call = blockExplorer.getMultiAddress(allActive, context, BlockExplorer.TX_FILTER_ALL, limit, offset).execute();

            if(call.isSuccessful()) {
                return call.body();
            } else {
                throw new ApiException(call.errorBody().string());
            }
        }
    }

    public List<TransactionSummary> getAccountTransactions(ArrayList<String> all, List<String> watchOnly, List<String> activeLegacy, String xpub, int limit, int offset)
        throws IOException, ApiException {

        MultiAddress multiAddress = getMultiAddress(all, xpub, limit, offset);
        if(multiAddress == null || multiAddress.getTxs() == null) {
            return new ArrayList<>();
        }

        List<TransactionSummary> summaryList = summarize(all, watchOnly, multiAddress, activeLegacy);
        return summaryList;
    }

    public int getNextChangeAddressIndex(String xpub) {

        if(!nextChangeAddressMap.containsKey(xpub)) {
            return 0;
        }

        return nextChangeAddressMap.get(xpub);
    }

    public int getNextReceiveAddressIndex(String xpub, List<AddressLabels> reservedAddresses) {

        if(!nextReceiveAddressMap.containsKey(xpub)) {
            return 0;
        }

        Integer receiveIndex = nextReceiveAddressMap.get(xpub);

        //Skip reserved addresses
        for(AddressLabels reservedAddress : reservedAddresses) {
            if(reservedAddress.getIndex() == receiveIndex) {
                receiveIndex++;
            }
        }

        return receiveIndex;
    }

    public void sort(ArrayList<Transaction> txs) {
        if(txs == null) return;
        Collections.sort(txs, new TxMostRecentDateComparator());
    }

    public boolean isOwnHDAddress(String address) {
        return addressToXpubMap.containsKey(address);
    }

    public int findNextUnreservedReceiveAddressIndex(Account account, int addressPosition) {
        return isReserved(account, addressPosition)
                ? findNextUnreservedReceiveAddressIndex(account, addressPosition + 1)
                : addressPosition;
    }

    private boolean isReserved(Account account, int position) {
        for (AddressLabels reservedAddress : account.getAddressLabels()) {
            if (reservedAddress.getIndex() == position) {
                return true;
            }
        }
        return false;
    }

    public void incrementNextReceiveAddress(String xpub, List<AddressLabels> reservedAddresses) {

        int receiveIndex = getNextReceiveAddressIndex(xpub, reservedAddresses);
        receiveIndex++;

        nextReceiveAddressMap.put(xpub, receiveIndex);
    }

    public void incrementNextChangeAddress(String xpub) {

        int index = getNextChangeAddressIndex(xpub);
        index++;

        nextChangeAddressMap.put(xpub, index);
    }

    public class TxMostRecentDateComparator implements Comparator<Transaction> {

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

    public List<TransactionSummary> summarize(ArrayList<String> ownAddressesAndXpubs,
                                              List<String> watchOnlyAddresses,
                                              MultiAddress multiAddress,
                                              List<String> legacy) {

        List<TransactionSummary> summaryList = new ArrayList<>();

        //Set next address indexes
        for(Address address : multiAddress.getAddresses()) {
            nextReceiveAddressMap.put(address.getAddress(),address.getAccountIndex());
            nextChangeAddressMap.put(address.getAddress(),address.getChangeIndex());
        }

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

            //Map which address belongs to which xpub.
            txSummary.inputsXpubMap = new HashMap<>();
            txSummary.outputsXpubMap = new HashMap<>();

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
                            txSummary.inputsXpubMap.put(inputAddr, xpubBody.getM());
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
                            txSummary.outputsXpubMap.put(outputAddr, xpubBody.getM());
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

            addressToXpubMap.putAll(txSummary.getInputsXpubMap());
            addressToXpubMap.putAll(txSummary.getOutputsXpubMap());

            summaryList.add(txSummary);
        }

        return summaryList;
    }

    private BigInteger calculateTotal(String hash, HashMap<String, BigInteger> nonChange, BigInteger fee, Direction direction) {

        BigInteger total = BigInteger.ZERO;

        for(BigInteger amount : nonChange.values()) {
            total = total.add(amount);
        }

        if(direction == Direction.SENT) {
            total = total.add(fee);
        }

        return total;
    }

    private BigInteger calculateFee(HashMap<String, BigInteger> inputs, HashMap<String, BigInteger> outputs, Direction direction) {

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