package info.blockchain.wallet.multiaddr;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import info.blockchain.wallet.payload.PayloadFactory;
import info.blockchain.wallet.payload.Tx;
import info.blockchain.wallet.payload.TxMostRecentDateComparator;
import info.blockchain.wallet.util.WebUtil;

public class MultiAddrFactory	{

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

//    private static Logger mLogger = LoggerFactory.getLogger(MultiAddrFactory.class);

    private MultiAddrFactory()	{ ; }

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

    public JSONObject getXPUB(String[] xpubs) {

        JSONObject jsonObject  = null;

        try {
            StringBuilder url = new StringBuilder(WebUtil.MULTIADDR_URL);
            url.append(StringUtils.join(xpubs, "|"));
            String response = WebUtil.getInstance().getURL(url.toString());
            try {
                jsonObject = new JSONObject(response);
                parseXPUB(jsonObject);
            }
            catch(JSONException je) {
                je.printStackTrace();
                jsonObject = null;
            }
        }
        catch(Exception e) {
            jsonObject = null;
            e.printStackTrace();
        }

        return jsonObject;
    }

    public JSONObject getLegacy(String[] addresses, boolean simple) {

        JSONObject jsonObject  = null;

        StringBuilder url = new StringBuilder(WebUtil.MULTIADDR_URL);
        url.append(StringUtils.join(addresses, "|"));
        if(simple) {
            url.append("&simple=true&format=json");
        }
        else {
            url.append("&symbol_btc="+ "BTC" + "&symbol_local=" + "USD");
        }

        try {
            String response = WebUtil.getInstance().getURL(url.toString());
            jsonObject = new JSONObject(response);
            parseLegacy(jsonObject);
        }
        catch(Exception e) {
            jsonObject = null;
            e.printStackTrace();
        }

        return jsonObject;
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
                    String mt_addr = null;
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
                            if(prevOutObj.has("addr") && !own_hd_addresses.contains(prevOutObj.has("addr")))  {
                                own_hd_addresses.add((String)prevOutObj.get("addr"));
                                address_2_xpub.put((String)prevOutObj.get("addr"), addr);
                            }
                        }
                        else  {
                            o_addr = (String)prevOutObj.get("addr");
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
                                move_amount = outObj.getLong("value");
                                mt_addr = addr;
                            }
                            if(outObj.has("addr") && !own_hd_addresses.contains(outObj.has("addr")))  {
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
                            if(xpub_txs.containsKey(mf_addr))  {
                                xpub_txs.get(mf_addr).add(tx);
                            }
                            else  {
                                xpub_txs.put(mf_addr, new ArrayList<Tx>());
                                xpub_txs.get(mf_addr).add(tx);
                            }
                            if(xpub_txs.containsKey(mt_addr))  {
                                xpub_txs.get(mt_addr).add(tx);
                            }
                            else  {
                                xpub_txs.put(mt_addr, new ArrayList<Tx>());
                                xpub_txs.get(mt_addr).add(tx);
                            }
                        }
                        else  {
                            if(xpub_txs.containsKey(addr))  {
                                xpub_txs.get(addr).add(tx);
                            }
                            else  {
                                xpub_txs.put(addr, new ArrayList<Tx>());
                                xpub_txs.get(addr).add(tx);
                            }
                        }
                    }
                }
            }

        }

    }

    public long nbTxXPUB(String xpub) throws JSONException  {

        String response = null;
        JSONObject jsonObject = null;
        long ret = -1L;

        try {
            StringBuilder url = new StringBuilder(WebUtil.MULTIADDR_URL);
            url.append(xpub);
            response = WebUtil.getInstance().getURL(url.toString());
            jsonObject = new JSONObject(response);
        }
        catch(Exception e) {
            jsonObject = null;
            e.printStackTrace();
        }

        if(jsonObject != null && jsonObject.has("wallet"))  {
            JSONObject walletObj = (JSONObject)jsonObject.get("wallet");
            if(walletObj.has("n_tx"))  {
                ret = walletObj.getLong("n_tx");
            }
        }

        return ret;

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
                    long ts = 0L;
                    String hash = null;
                    String addr = null;
                    boolean isMove = false;
                    String ownInput = null;
                    String ownOutput = null;

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

                    List<String> ownLegacyAddresses = PayloadFactory.getInstance().get().getLegacyAddressStrings();

                    if(txObj.has("inputs"))  {
                        JSONArray inputArray = (JSONArray)txObj.get("inputs");
                        JSONObject inputObj = null;
                        for(int j = 0; j < inputArray.length(); j++)  {
                            inputObj = (JSONObject)inputArray.get(j);
                            if(inputObj.has("prev_out"))  {
                                JSONObject prevOutObj = (JSONObject)inputObj.get("prev_out");
                                addr = (String)prevOutObj.get("addr");
                                if(ownLegacyAddresses.contains(addr))  {
                                    ownInput = addr;
                                }
                            }
                        }
                    }

                    if(txObj.has("out"))  {
                        JSONArray outArray = (JSONArray)txObj.get("out");
                        JSONObject outObj = null;
                        for(int j = 0; j < outArray.length(); j++)  {
                            outObj = (JSONObject)outArray.get(j);
                            addr = (String)outObj.get("addr");
                            if(ownLegacyAddresses.contains(addr) && ownInput != null && !ownInput.equals(addr))  {
                                isMove = true;
                                ownOutput = addr;
                            }
                            else if(ownLegacyAddresses.contains(addr)) {
                              ownOutput = addr;
                            }
                            else  {
                              ;
                            }
                        }
                    }

                    if(ownInput != null || ownOutput != null)  {

                        Tx tx = null;
                        if(isMove)  {
                            tx = new Tx(hash, "", MOVED, amount, ts, new HashMap<Integer,String>());
                            tx.setIsMove(true);
                        }
                        else  {
                            tx = new Tx(hash, "", amount > 0L ? RECEIVED : SENT, amount, ts, new HashMap<Integer,String>());
                        }
                        tx.setConfirmations((latest_block > 0L && height > 0L) ? (latest_block - height) + 1 : 0);
                        legacy_txs.add(tx);

                        List<Tx> containedLegacyTx = null;
                        if(ownInput != null)  {
                          containedLegacyTx = address_legacy_txs.get(ownInput);
                        }
                        else if (ownOutput != null)  {
                          containedLegacyTx = address_legacy_txs.get(ownOutput);
                        }
                        else  {
                          ;
                        }

                        if(containedLegacyTx == null) {
                          containedLegacyTx = new ArrayList<Tx>();
                        }
                        containedLegacyTx.add(tx);

                        if(ownInput != null)  {
                          address_legacy_txs.put(ownInput != null ? ownInput : addr, containedLegacyTx);
                        }
                        if(ownOutput != null)  {
                          address_legacy_txs.put(ownOutput != null ? ownOutput : addr, containedLegacyTx);
                        }
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
