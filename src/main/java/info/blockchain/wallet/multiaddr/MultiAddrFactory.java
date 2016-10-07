package info.blockchain.wallet.multiaddr;

import info.blockchain.api.MultiAddress;
import info.blockchain.wallet.payload.LegacyAddress;
import info.blockchain.wallet.payload.PayloadManager;
import info.blockchain.wallet.payload.Tx;
import info.blockchain.wallet.payload.TxMostRecentDateComparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MultiAddrFactory   {

    private static long legacy_balance = 0L;
    private static long xpub_balance = 0L;
    private static HashMap<String, Long> xpub_amounts = null;
    private static HashMap<String, Long> legacy_amounts = null;
    private static HashMap<String,List<Tx>> xpub_txs = null;
    private static List<Tx> legacy_txs = null;
    private static HashMap<String,List<Tx>> address_legacy_txs = null;
    private static HashMap<String,List<String>> haveUnspentOuts = null;
    private static List<String> own_hd_addresses = null;
    private static HashMap<String,String> address_2_xpub = null;

    private static HashMap<String,Integer> highestTxReceiveIdx = null;
    private static HashMap<String,Integer> highestTxChangeIdx = null;

    private static MultiAddrFactory instance = null;

    private MultiAddrFactory()  { ; }

    public static final String RECEIVED = "RECEIVED";
    public static final String SENT = "SENT";
    public static final String MOVED = "MOVED";

    public static MultiAddrFactory getInstance() {

        if(instance == null) {
            xpub_amounts = new HashMap<String, Long>();
            legacy_amounts = new HashMap<String, Long>();
            xpub_txs = new HashMap<String,List<Tx>>();
            legacy_txs = new ArrayList<Tx>();
            address_legacy_txs = new HashMap<String,List<Tx>>();
            haveUnspentOuts = new HashMap<String,List<String>>();
            highestTxReceiveIdx = new HashMap<String,Integer>();
            highestTxChangeIdx = new HashMap<String,Integer>();
            legacy_balance = 0L;
            xpub_balance = 0L;
            own_hd_addresses = new ArrayList<String>();
            address_2_xpub = new HashMap<String,String>();
            instance = new MultiAddrFactory();
        }

        return instance;
    }

    public void wipe() {
        instance = null;
    }

    public void refreshXPUBData(String[] xpubs) throws Exception {

        MultiAddress api = new MultiAddress();

        JSONObject jsonObject  = api.getXPUB(xpubs);

        if(jsonObject != null){
            parseXPUB(jsonObject);
        }
    }

    public void refreshLegacyAddressData(String[] addresses, boolean simple) throws Exception{

        MultiAddress api = new MultiAddress();

        JSONObject jsonObject  = api.getLegacy(addresses, simple);

        if(jsonObject != null){
            parseLegacy(jsonObject);
        }
    }

    private void parseXPUB(JSONObject jsonObject) throws JSONException  {

        if(jsonObject != null)  {
            if(jsonObject.has("wallet"))  {
                JSONObject walletObj = (JSONObject)jsonObject.get("wallet");
                if(walletObj.has("final_balance"))  {
                    xpub_balance = walletObj.getLong("final_balance");
                }
            }

            long latest_block = 0L;

            if(jsonObject.has("info"))  {
                JSONObject infoObj = (JSONObject)jsonObject.get("info");
                if(infoObj.has("latest_block"))  {
                    JSONObject blockObj = (JSONObject)infoObj.get("latest_block");
                    if(blockObj.has("height"))  {
                        latest_block = blockObj.getLong("height");
                    }
                }
            }

            if(jsonObject.has("addresses"))  {

                xpub_amounts = new HashMap<String, Long>();

                JSONArray addressesArray = (JSONArray)jsonObject.get("addresses");
                JSONObject addrObj = null;
                for(int i = 0; i < addressesArray.length(); i++)  {
                    addrObj = (JSONObject)addressesArray.get(i);
                    if(addrObj.has("final_balance") && addrObj.has("address"))  {
                        xpub_amounts.put((String)addrObj.get("address"), addrObj.getLong("final_balance"));
                    }
                    if(addrObj.has("account_index"))  {
                        highestTxReceiveIdx.put((String)addrObj.get("address"), addrObj.getInt("account_index"));
                    }
                    if(addrObj.has("change_index"))  {
                        highestTxChangeIdx.put((String)addrObj.get("address"), addrObj.getInt("change_index"));
                    }
                }
            }

            List<String> ownLegacyAddresses = PayloadManager.getInstance().getPayload().getLegacyAddressStrings(LegacyAddress.NORMAL_ADDRESS);

            if(jsonObject.has("txs"))  {

                xpub_txs = new HashMap<String,List<Tx>>();
                own_hd_addresses = new ArrayList<String>();
                address_2_xpub = new HashMap<String,String>();
                haveUnspentOuts = new HashMap<String,List<String>>();

                JSONArray txArray = (JSONArray)jsonObject.get("txs");
                JSONObject txObj = null;
                for(int i = 0; i < txArray.length(); i++)  {

                    txObj = (JSONObject)txArray.get(i);
                    long height = 0L;
                    long amount = 0L;
                    long inputs_amount = 0L;
                    long outputs_amount = 0L;
                    long move_amount = 0L;
                    long ts = 0L;
                    String hash = null;
                    String addr = null;
                    String mf_addr = null;
                    List<String> moveToAddrArray = new ArrayList<String>();
                    String o_addr = null;
                    boolean isMove = false;

                    if(txObj.has("block_height"))  {
                        height = txObj.getLong("block_height");
                    }
                    else  {
                        height = -1L;  // 0 confirmations
                    }

                    hash = (String)txObj.get("hash");
                    amount = txObj.getLong("result");
                    ts = txObj.getLong("time");

                    JSONArray inputArray = (JSONArray)txObj.get("inputs");
                    JSONObject inputObj = null;
                    for(int j = 0; j < inputArray.length(); j++)  {
                        inputObj = (JSONObject)inputArray.get(j);
                        JSONObject prevOutObj = (JSONObject)inputObj.get("prev_out");
                        if(prevOutObj.has("xpub"))  {
                            JSONObject xpubObj = (JSONObject)prevOutObj.get("xpub");
                            addr = (String)xpubObj.get("m");
                            mf_addr = addr;
                            if(prevOutObj.has("addr") && !own_hd_addresses.contains(prevOutObj.get("addr")))  {
                                own_hd_addresses.add((String)prevOutObj.get("addr"));
                                address_2_xpub.put((String)prevOutObj.get("addr"), addr);
                            }
                        }
                        else  {
                            o_addr = (String)prevOutObj.get("addr");

                            //(Legacy to HD transfer check)
                            //If contained in our own legacy addresses - it is a move
                            //We still need to calculate the move amount below
                            if(ownLegacyAddresses.contains(o_addr)){
                                mf_addr = o_addr;
                                isMove = true;
                            }
                        }

                        inputs_amount += prevOutObj.getLong("value");
                    }

                    JSONArray outArray = (JSONArray)txObj.get("out");
                    JSONObject outObj = null;
                    String path = null;
                    for(int j = 0; j < outArray.length(); j++)  {
                        outObj = (JSONObject)outArray.get(j);
                        if(outObj.has("xpub"))  {
                            JSONObject xpubObj = (JSONObject)outObj.get("xpub");
                            addr = (String)xpubObj.get("m");
                            path = (String)xpubObj.get("path");
                            if(path.startsWith("M/0/"))  {
                                move_amount += outObj.getLong("value");
                                moveToAddrArray.add(addr);
                            }
                            if(outObj.has("addr") && !own_hd_addresses.contains(outObj.get("addr")))  {
                                own_hd_addresses.add((String)outObj.get("addr"));
                                address_2_xpub.put((String)outObj.get("addr"), addr);
                            }

                            //
                            // collect unspent outputs for each xpub
                            // store path info in order to generate private key later on
                            //
                            if(outObj.has("spent"))  {
                                if(outObj.getBoolean("spent") == false && outObj.has("addr"))  {
                                    if(!haveUnspentOuts.containsKey(addr))  {
                                        List<String> addrs = new ArrayList<String>();
                                        haveUnspentOuts.put(addr, addrs);
                                    }
                                    String data = path + "," + (String)outObj.get("addr");
                                    if(!haveUnspentOuts.get(addr).contains(data))  {
                                        haveUnspentOuts.get(addr).add(data);
                                    }
                                }
                            }
                        }
                        else  {
                            o_addr = (String)outObj.get("addr");
                            //(HD to Legacy transfer check)
                            //if !outObj.has("xpub") - this means we are not receiving to own HD
                            //If contained in our own legacy addresses (Sent from HD to Legacy)
                            if(ownLegacyAddresses.contains(o_addr)){
                                isMove = true;
                                moveToAddrArray.add(o_addr);
                                move_amount += outObj.getLong("value");
                            }
                        }

                        outputs_amount += outObj.getLong("value");
                    }

                    if(Math.abs(inputs_amount - outputs_amount) == Math.abs(amount))  {
                        isMove = true;
                    }

                    if(addr != null)  {
                        Tx tx = null;
                        if(isMove)  {
                            tx = new Tx(hash, "", MOVED, move_amount, ts, new HashMap<Integer,String>());
                            tx.setIsMove(true);
                        }
                        else  {
                            tx = new Tx(hash, "", amount > 0L ? RECEIVED : SENT, amount, ts, new HashMap<Integer,String>());
                        }

                        tx.setConfirmations((latest_block > 0L && height > 0L) ? (latest_block - height) + 1 : 0);

                        if(isMove)  {
                            if(!xpub_txs.containsKey(mf_addr))  {
                                xpub_txs.put(mf_addr, new ArrayList<Tx>());
                            }
                            xpub_txs.get(mf_addr).add(tx);

                            for (String moveToAddr : moveToAddrArray) {
                                if(!xpub_txs.containsKey(moveToAddr))  {
                                    xpub_txs.put(moveToAddr, new ArrayList<Tx>());
                                }
                                xpub_txs.get(moveToAddr).add(tx);
                            }
                        }
                        else  {
                            if(!xpub_txs.containsKey(addr))  {
                                xpub_txs.put(addr, new ArrayList<Tx>());
                            }
                            xpub_txs.get(addr).add(tx);

                        }
                    }
                }
            }

        }

    }

    private void parseLegacy(JSONObject jsonObject) throws JSONException  {

        if(jsonObject != null)  {

            legacy_balance = 0L;

            if(jsonObject.has("wallet"))  {
                JSONObject walletObj = (JSONObject)jsonObject.get("wallet");
                if(walletObj.has("final_balance"))  {
                    legacy_balance = walletObj.getLong("final_balance");
                }
            }

            long latest_block = 0L;

            if(jsonObject.has("info"))  {
                JSONObject infoObj = (JSONObject)jsonObject.get("info");
                if(infoObj.has("latest_block"))  {
                    JSONObject blockObj = (JSONObject)infoObj.get("latest_block");
                    if(blockObj.has("height"))  {
                        latest_block = blockObj.getLong("height");
                    }
                }
            }

            if(jsonObject.has("addresses"))  {
                JSONArray addressArray = (JSONArray)jsonObject.get("addresses");
                JSONObject addrObj = null;
                for(int i = 0; i < addressArray.length(); i++)  {
                    addrObj = (JSONObject)addressArray.get(i);
                    long amount = 0L;
                    String addr = null;
                    if(addrObj.has("address"))  {
                        addr = (String)addrObj.get("address");
                    }
                    if(addrObj.has("final_balance"))  {
                        amount = addrObj.getLong("final_balance");
                    }
                    if(addr != null)  {
                        legacy_amounts.put(addr, amount);
                    }
                }
            }

            if(jsonObject.has("txs"))  {

                legacy_txs = new ArrayList<Tx>();
                address_legacy_txs = new HashMap<String,List<Tx>>();

                JSONArray txArray = (JSONArray)jsonObject.get("txs");
                JSONObject txObj = null;
                for(int i = 0; i < txArray.length(); i++)  {

                    txObj = (JSONObject)txArray.get(i);
                    long height = 0L;
                    long amount = 0L;
                    long move_amount = 0l;
                    long ts = 0L;
                    String hash = null;
                    String inputAddr = null;
                    String outputAddr = null;
                    boolean isMove = false;
                    boolean isWatchOnly = false;
                    ArrayList<String> ownInput = new ArrayList<String>();
                    ArrayList<String> ownOutput = new ArrayList<String>();

                    ArrayList<Long> amountListOut = new ArrayList<Long>();
                    ArrayList<Long> amountListIn = new ArrayList<Long>();

                    if(txObj.has("block_height"))  {
                        height = txObj.getLong("block_height");
                    }
                    else  {
                        height = -1L;  // 0 confirmations
                    }

                    if(txObj.has("hash"))  {
                        hash = (String)txObj.get("hash");
                    }
                    if(txObj.has("result"))  {
                        amount = txObj.getLong("result");
                    }
                    if(txObj.has("time"))  {
                        ts = txObj.getLong("time");
                    }

                    List<String> ownLegacyAddresses = PayloadManager.getInstance().getPayload().getLegacyAddressStrings(LegacyAddress.NORMAL_ADDRESS);
                    List<String> watchOnlyLegacyAddresses = PayloadManager.getInstance().getPayload().getWatchOnlyAddressStrings();

                    if(txObj.has("inputs"))  {
                        JSONArray inputArray = (JSONArray)txObj.get("inputs");
                        JSONObject inputObj = null;
                        for(int j = 0; j < inputArray.length(); j++)  {
                            inputObj = (JSONObject)inputArray.get(j);
                            if(inputObj.has("prev_out"))  {
                                JSONObject prevOutObj = (JSONObject)inputObj.get("prev_out");
                                inputAddr = (String)prevOutObj.get("addr");

                                //If input is own legacy or hd address = move
                                if(ownLegacyAddresses.contains(inputAddr) || own_hd_addresses.contains(inputAddr))  {
                                    isMove = true;

                                    long amountInput = prevOutObj.getLong("value");
                                    if(ownInput.contains(inputAddr)) {
                                        int index=ownInput.indexOf(inputAddr);
                                        amountListIn.set(index, amountListIn.get(index)+amountInput);
                                    } else {
                                        ownInput.add(inputAddr);
                                        amountListIn.add(amountInput);
                                    }
                                }

                                if(watchOnlyLegacyAddresses.contains(inputAddr)){
                                    isWatchOnly = true;
                                }
                            }
                        }
                    }

                    if(txObj.has("out"))  {
                        JSONArray outArray = (JSONArray)txObj.get("out");
                        JSONObject outObj = null;
                        for(int j = 0; j < outArray.length(); j++)  {
                            outObj = (JSONObject)outArray.get(j);
                            outputAddr = (String)outObj.get("addr");

                            //If output is own legacy or hd address = move
                            if(ownLegacyAddresses.contains(outputAddr) || own_hd_addresses.contains(outputAddr)) {

                                long amountOutput = outObj.getLong("value");

                                //Don't add change coming back
                                if(inputAddr != null && !inputAddr.equals(outputAddr))
                                    move_amount += amountOutput;

                                if (ownOutput.contains(outputAddr)) {
                                    int index = ownOutput.indexOf(outputAddr);
                                    amountListOut.set(index, amountListOut.get(index) + amountOutput);
                                } else {
                                    ownOutput.add(outputAddr);
                                    amountListOut.add(amountOutput);
                                }

                            } else {
                                isMove = false; //one foreign address is enough to not call it move anymore
                            }

                            if(watchOnlyLegacyAddresses.contains(outputAddr)){
                                isWatchOnly = true;
                            }
                        }
                    }

                    //Check back all the transactions where one of our addresses is listed as output
                    //If that address is also listed as an input, we get the output back as change
                    Tx tx = null;
                    for(String address : ownOutput) {

                        int index = ownOutput.indexOf(address);
                        long outputAmount = amountListOut.get(index);
                        String mode = RECEIVED;

                        //Check if this is just the change we get back
                        if(ownInput.contains(address)) {

                            long inputAmount = amountListIn.get(ownInput.indexOf(address));
                            if(inputAmount < outputAmount) {
                                mode = RECEIVED;
                                outputAmount = Math.abs(inputAmount-outputAmount);
                            } else {
                                mode = SENT;
                                outputAmount = (-1)*(amountListIn.get(ownInput.indexOf(address))-outputAmount);
                            }
                        }
                        if(isMove)
                            mode = MOVED;

                        tx = new Tx(hash, "", mode, outputAmount, ts, new HashMap<Integer,String>());
                        tx.setIsMove(isMove);
                        tx.setDirection(mode);

                        tx.setConfirmations((latest_block > 0L && height > 0L) ? (latest_block - height) + 1 : 0);

                        List<Tx> containedLegacyTx = address_legacy_txs.get(address);

                        if(containedLegacyTx == null) {
                            containedLegacyTx = new ArrayList<Tx>();
                        }

                        if(isWatchOnly) {
                            tx.setIsWatchOnly(true);
                        }

                        containedLegacyTx.add(tx);
                        address_legacy_txs.put(address, containedLegacyTx);
                    }

                    //Check all cases where a address is listed as an input
                    //Exclude cases  where it is in the output array, as these were covered above
                    for(String address : ownInput) {

                        if(ownOutput.contains(address))
                            continue;

                        int index = ownInput.indexOf(address);
                        long inputAmount = (-1)*Math.abs(amountListIn.get(index));

                        String mode = SENT;
                        if(isMove)
                            mode = MOVED;

                        tx = new Tx(hash, "", mode, inputAmount, ts, new HashMap<Integer,String>());
                        tx.setIsMove(isMove);

                        tx.setConfirmations((latest_block > 0L && height > 0L) ? (latest_block - height) + 1 : 0);

                        List<Tx> containedLegacyTx = address_legacy_txs.get(address);
                        if(containedLegacyTx == null) {
                            containedLegacyTx = new ArrayList<Tx>();
                        }

                        if(watchOnlyLegacyAddresses.contains(address)){
                            tx.setIsWatchOnly(true);
                        }

                        containedLegacyTx.add(tx);
                        address_legacy_txs.put(address, containedLegacyTx);
                    }

                    //Also make sure to state the wallet impact of this transaction in the general view
                    if(ownInput.size() > 0 || ownOutput.size() > 0) {
                        if (isMove) {
                            tx = new Tx(hash, "", MOVED, move_amount, ts, new HashMap<Integer, String>());
                            tx.setIsMove(true);
                        } else {
                            tx = new Tx(hash, "", amount > 0L ? RECEIVED : SENT, amount, ts, new HashMap<Integer, String>());
                        }

                        if(isWatchOnly) {
                            tx.setIsWatchOnly(true);
                        }

                        tx.setConfirmations((latest_block > 0L && height > 0L) ? (latest_block - height) + 1 : 0);
                        legacy_txs.add(tx);
                    }
                }
            }

        }

    }

    public long getLegacyBalance(String addr)  {
        if(legacy_amounts.containsKey(addr))  {
            return legacy_amounts.get(addr);
        }
        else  {
            return 0L;
        }
    }

    public void setLegacyBalance(String addr, long value)  {
        legacy_amounts.put(addr, value);
    }

    public long getLegacyBalance()  {
        return legacy_balance;
    }

    public long getLegacyBalance(long address_type)  {

        if(PayloadManager.getInstance().getPayload() != null)  {
            List<String> addrs = PayloadManager.getInstance().getPayload().getLegacyAddressStrings(address_type);
            long value = 0L;

            for(String addr : addrs) {
                if(legacy_amounts.containsKey(addr))  {
                    value += legacy_amounts.get(addr);
                }
            }

            return value;
        }
        else {
            return 0L;
        }
    }

    public long getLegacyActiveBalance()  {

        if(PayloadManager.getInstance().getPayload() != null)  {
            List<String> addrs = PayloadManager.getInstance().getPayload().getActiveLegacyAddressStrings();
            long value = 0L;

            for(String addr : addrs) {
                if(legacy_amounts.containsKey(addr))  {
                    value += legacy_amounts.get(addr);
                }
            }

            return value;
        }
        else {
            return 0L;
        }
    }

    public void setLegacyBalance(long value)  {
        legacy_balance = value;
    }

    public long getXpubBalance()  {
        return xpub_balance;
    }

    public void setXpubBalance(long amount)  {
        xpub_balance = amount;
    }

    public long getTotalBalance()  {
        return xpub_balance + legacy_balance;
    }

    public boolean isOwnHDAddress(String addr)  {
        return own_hd_addresses.contains(addr);
    }

    public HashMap<String,String> getAddress2Xpub()  {
        return address_2_xpub;
    }

    public HashMap<String,Long> getXpubAmounts()  {
        return xpub_amounts;
    }

    public void setXpubAmount(String xpub, long amount)  {
        xpub_amounts.put(xpub, amount);
    }

    public HashMap<String,List<Tx>> getXpubTxs()  {
        return xpub_txs;
    }

    public List<Tx> getAllXpubTxs()  {

        List<String> seen_moves = new ArrayList<String>();
        List<Tx> ret = new ArrayList<Tx>();
        for(String key : xpub_txs.keySet())  {
            List<Tx> txs = xpub_txs.get(key);
            for(Tx tx : txs)  {
                if(tx.isMove())  {
                    if(seen_moves.contains(tx.getHash()))  {
                        continue;
                    }
                    else  {
                        ret.add(tx);
                        seen_moves.add(tx.getHash());
                    }
                }
                else  {
                    ret.add(tx);
                }
            }
        }

        Collections.sort(ret, new TxMostRecentDateComparator());

        return ret;
    }

    public List<Tx> getLegacyTxs()  {
        return legacy_txs;
    }

    public List<Tx> getAddressLegacyTxs(String addr)  {
        return address_legacy_txs.get(addr);
    }

    public HashMap<String,List<String>> getUnspentOuts()  {
        return haveUnspentOuts;
    }

    public int getHighestTxReceiveIdx(String xpub) {
        if(highestTxReceiveIdx.get(xpub) != null) {
            return highestTxReceiveIdx.get(xpub);
        }
        else {
            return 0;
        }
    }

    public void setHighestTxReceiveIdx(String xpub, int idx) {
        highestTxReceiveIdx.put(xpub, idx);
    }

    public int getHighestTxChangeIdx(String xpub) {
        if(highestTxChangeIdx.get(xpub) != null) {
            return highestTxChangeIdx.get(xpub);
        }
        else {
            return 0;
        }
    }

    public void setHighestTxChangeIdx(String xpub, int idx) {
        highestTxChangeIdx.put(xpub, idx);
    }

}
