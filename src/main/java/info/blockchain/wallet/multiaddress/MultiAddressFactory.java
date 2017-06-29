package info.blockchain.wallet.multiaddress;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.blockexplorer.FilterType;
import info.blockchain.api.data.AddressSummary;
import info.blockchain.api.data.Input;
import info.blockchain.api.data.MultiAddress;
import info.blockchain.api.data.Output;
import info.blockchain.api.data.Transaction;
import info.blockchain.api.data.Xpub;
import info.blockchain.wallet.bip44.HDChain;
import info.blockchain.wallet.exceptions.ApiException;
import info.blockchain.wallet.multiaddress.TransactionSummary.Direction;
import info.blockchain.wallet.payload.data.AddressLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import retrofit2.Response;

import static info.blockchain.wallet.payload.PayloadManager.MULTI_ADDRESS_ALL;

public class MultiAddressFactory {

    private static Logger log = LoggerFactory.getLogger(MultiAddressFactory.class);

    private BlockExplorer blockExplorer;

    private HashMap<String, Integer> nextReceiveAddressMap;
    private HashMap<String, Integer> nextChangeAddressMap;

    //Field for testing if address belongs to us - Quicker than derivation
    private HashMap<String, String> addressToXpubMap;

    public MultiAddressFactory(BlockExplorer blockExplorer) {
        log.info("Initializing MultiAddressFactory");
        this.blockExplorer = blockExplorer;
        this.addressToXpubMap = new HashMap<>();
        this.nextReceiveAddressMap = new HashMap<>();
        this.nextChangeAddressMap = new HashMap<>();
    }

    public String getXpubFromAddress(String address) {
        return addressToXpubMap.get(address);
    }

    private MultiAddress getMultiAddress(List<String> allActive, String onlyShow, int limit, int offset) throws IOException, ApiException{

        log.info("Fetching multiaddress for {} accounts/addresses", allActive.size());

        if (onlyShow!=null && onlyShow.equals(MULTI_ADDRESS_ALL)) {

            Response<MultiAddress> call = blockExplorer.getMultiAddress(allActive, null, FilterType.RemoveUnspendable.getFilterInt(), limit, offset).execute();

            if(call.isSuccessful()) {
                return call.body();
            } else {
                throw new ApiException(call.errorBody().string());
            }

        } else {
            Response<MultiAddress> call = blockExplorer.getMultiAddress(allActive, onlyShow, FilterType.RemoveUnspendable.getFilterInt(), limit, offset).execute();

            if(call.isSuccessful()) {
                return call.body();
            } else {
                throw new ApiException(call.errorBody().string());
            }
        }
    }

    /**
     *
     * @param all A list of all xpubs and legacy addresses
     * @param watchOnly A list of watch-only legacy addresses
     * @param activeLegacy A list of active legacy addresses. Used to flag transactions as 'watch only'
     * @param onlyShow Xpub or legacy address. Used to fetch transaction only relating to this address.
     * @param limit Maximum amount of transactions fetched
     * @param offset Page offset
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public List<TransactionSummary> getAccountTransactions(ArrayList<String> all, List<String> watchOnly, List<String> activeLegacy, String onlyShow, int limit, int offset)
        throws IOException, ApiException {

        log.info("Get transactions. limit {}, offset {}", limit, offset);

        MultiAddress multiAddress = getMultiAddress(all, onlyShow, limit, offset);
        if(multiAddress == null || multiAddress.getTxs() == null) {
            return new ArrayList<>();
        }

        return summarize(all, watchOnly, multiAddress, activeLegacy);
    }

    public int getNextChangeAddressIndex(String xpub) {

        if(!nextChangeAddressMap.containsKey(xpub)) {
            return 0;
        }

        int index = nextChangeAddressMap.get(xpub);
        log.info("Next change index = {}", index);
        return index;
    }

    public int getNextReceiveAddressIndex(String xpub, List<AddressLabel> reservedAddresses) {

        if(!nextReceiveAddressMap.containsKey(xpub)) {
            return 0;
        }

        Integer receiveIndex = nextReceiveAddressMap.get(xpub);

        //Skip reserved addresses
        for(AddressLabel reservedAddress : reservedAddresses) {
            if(reservedAddress.getIndex() == receiveIndex) {
                receiveIndex++;
            }
        }

        log.info("Next receive index = {}", receiveIndex);
        return receiveIndex;
    }

    public void sort(ArrayList<Transaction> txs) {
        if(txs == null) return;
        Collections.sort(txs, new TxMostRecentDateComparator());
    }

    public boolean isOwnHDAddress(String address) {
        return addressToXpubMap.containsKey(address);
    }

    public void incrementNextReceiveAddress(String xpub, List<AddressLabel> reservedAddresses) {

        int receiveIndex = getNextReceiveAddressIndex(xpub, reservedAddresses);
        receiveIndex++;

        log.info("Increment next receive index to {}", receiveIndex);
        nextReceiveAddressMap.put(xpub, receiveIndex);
    }

    public void incrementNextChangeAddress(String xpub) {

        int index = getNextChangeAddressIndex(xpub);
        index++;

        log.info("Increment next change index to {}", index);
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
        for(AddressSummary address : multiAddress.getAddresses()) {
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

            if (tx.getResult().add(tx.getFee()).signum() == 0) {
                txSummary.setDirection(Direction.TRANSFERRED);
            } else if (tx.getResult().signum() > 0) {
                txSummary.setDirection(Direction.RECEIVED);
            } else {
                txSummary.setDirection(Direction.SENT);
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
                        if(txSummary.inputsMap.containsKey(inputAddr)) {
                            BigInteger prevValue = txSummary.inputsMap.get(inputAddr).add(inputValue);
                            txSummary.inputsMap.put(inputAddr, prevValue);
                        } else {
                            txSummary.inputsMap.put(inputAddr, inputValue);
                        }

                    } else {
                        throw new IllegalStateException("inputAddr is null");
                    }

                } else {
                    //Newly generated coin
                }
            }

            HashMap<String, BigInteger> changeMap = new HashMap<>();
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
                        if (xpubBody.getPath().startsWith("M/" + HDChain.RECEIVE_CHAIN + "/")) {
                            txSummary.outputsMap.put(outputAddr, outputValue);
                            txSummary.outputsXpubMap.put(outputAddr, xpubBody.getM());
                        } else {
                            //Change
                            changeMap.put(outputAddr, outputValue);
                        }

                    } else {
                        //If we own this address and it's not change coming back, it's a transfer
                        if (ownAddressesAndXpubs.contains(outputAddr) && !txSummary.inputsMap.keySet().contains(outputAddr)) {

                            if(txSummary.getDirection() == Direction.SENT) {
                                txSummary.setDirection(Direction.TRANSFERRED);
                            }

                            //Don't add change coming back
                            if (!txSummary.inputsMap.containsKey(outputAddr)) {
                                txSummary.outputsMap.put(outputAddr, outputValue);
                            } else {
                                changeMap.put(outputAddr, outputValue);
                            }

                        } else {
                            //Address does not belong to us
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
                } else {
                    throw new IllegalStateException("outputAddr is null");
                }
            }

            //If we are filtering for legacy and nothing found
            if (legacy != null && !isLegacy) {
                continue;
            }

            //Remove input addresses not ours
            filterOwnedAddresses(
                    ownAddressesAndXpubs,
                    txSummary.inputsMap,
                    txSummary.outputsMap,
                    txSummary.getDirection());

            txSummary.setHash(tx.getHash());
            txSummary.setTime(tx.getTime());
            txSummary.setDoubleSpend(tx.isDoubleSpend());
            txSummary.setFee(tx.getFee());

            if (txSummary.getDirection() == Direction.RECEIVED) {
                BigInteger total = calculateTotalReceived(txSummary.outputsMap);
                txSummary.setTotal(total);
            } else {
                BigInteger total = calculateTotalSent(
                        txSummary.inputsMap,
                        changeMap,
                        tx.getFee(),
                        txSummary.getDirection());
                txSummary.setTotal(total);
            }

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

    private void filterOwnedAddresses(ArrayList<String> ownAddressesAndXpubs,
        HashMap<String, BigInteger> inputsMap,
        HashMap<String, BigInteger> outputsMap, Direction direction) {

        Iterator<Entry<String, BigInteger>> iterator = inputsMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Entry<String, BigInteger> item = iterator.next();
            if(!ownAddressesAndXpubs.contains(item.getKey()) && direction.equals(Direction.SENT)) {
                iterator.remove();
            }
        }

        iterator = outputsMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Entry<String, BigInteger> item = iterator.next();
            if(!ownAddressesAndXpubs.contains(item.getKey()) && direction.equals(Direction.RECEIVED)) {
                iterator.remove();
            }
        }
    }

    private BigInteger calculateTotalReceived(HashMap<String, BigInteger> outputsMap) {

        BigInteger total = BigInteger.ZERO;

        for(BigInteger output : outputsMap.values()) {
            total = total.add(output);
        }

        return total;
    }

    private BigInteger calculateTotalSent(HashMap<String, BigInteger> inputsMap, HashMap<String, BigInteger> changeMap,
        BigInteger fee, Direction direction) {

        BigInteger total = BigInteger.ZERO;

        for(BigInteger input : inputsMap.values()) {
            total = total.add(input);
        }

        for(BigInteger change : changeMap.values()) {
            total = total.subtract(change);
        }

        if(direction == Direction.TRANSFERRED) {
            total = total.subtract(fee);
        }

        return total;
    }

}