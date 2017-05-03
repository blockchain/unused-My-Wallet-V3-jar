package info.blockchain.wallet.payload;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.data.Balance;
import info.blockchain.wallet.exceptions.ServerConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static info.blockchain.wallet.payload.PayloadManager.MULTI_ADDRESS_ALL;
import static info.blockchain.wallet.payload.PayloadManager.MULTI_ADDRESS_ALL_LEGACY;

public class BalanceManager {

    private static Logger log = LoggerFactory.getLogger(BalanceManager.class);

    private BlockExplorer blockExplorer;

    private HashMap<String, BigInteger> balanceMap;

    public BalanceManager(BlockExplorer blockExplorer) {
        log.info("Initializing BalanceManager");
        this.blockExplorer = blockExplorer;
        this.balanceMap = new HashMap<>();
    }

    public void subtractAmountFromAddressBalance(String address, BigInteger amount)
        throws Exception {

        log.info("Updating internal balance of address "+address);

        //Update individual address
        BigInteger currentBalance = balanceMap.get(address);
        if(currentBalance == null) {
            throw new Exception("No info for this address. updateAllBalances should be called first.");
        }
        BigInteger newBalance = currentBalance.subtract(amount);
        balanceMap.put(address, newBalance);

        //Update wallet balance
        currentBalance = balanceMap.get(MULTI_ADDRESS_ALL);
        if(currentBalance == null) {
            throw new Exception("No info for this address. updateAllBalances should be called first.");
        }
        newBalance = currentBalance.subtract(amount);
        balanceMap.put(MULTI_ADDRESS_ALL, newBalance);
    }

    public BigInteger getAddressBalance(String address) {
        return balanceMap.get(address);
    }

    public BigInteger getWalletBalance() {
        return balanceMap.get(MULTI_ADDRESS_ALL);
    }

    public BigInteger getImportedAddressesBalance() {
        return balanceMap.get(PayloadManager.MULTI_ADDRESS_ALL_LEGACY);
    }

    public void updateAllBalances(List<String> legacyAddressList, List<String> allAccountsAndAddresses) throws ServerConnectionException, IOException {
        Call<HashMap<String, Balance>> call = blockExplorer.getBalance(allAccountsAndAddresses,
            BlockExplorer.TX_FILTER_REMOVE_UNSPENDABLE);


        log.info("Fetching wallet balances");

        BigInteger walletFinalBalance = BigInteger.ZERO;
        BigInteger importedFinalBalance = BigInteger.ZERO;

        Response<HashMap<String, Balance>> exe = call.execute();
        if(exe.isSuccessful()) {

            Set<Entry<String, Balance>> set = exe.body().entrySet();
            for(Entry<String, Balance> item : set) {
                String address = item.getKey();
                Balance balance = item.getValue();

                balanceMap.put(address, balance.getFinalBalance());

                //Consolidate 'All'
                walletFinalBalance = walletFinalBalance.add(balance.getFinalBalance());

                //Consolidate 'Imported'
                if(legacyAddressList.contains(address)) {
                    importedFinalBalance = importedFinalBalance.add(balance.getFinalBalance());
                }
            }

            balanceMap.put(MULTI_ADDRESS_ALL, walletFinalBalance);
            balanceMap.put(MULTI_ADDRESS_ALL_LEGACY, importedFinalBalance);

        } else {
            throw new ServerConnectionException(exe.errorBody().string());
        }
    }

    public Call<HashMap<String, Balance>> getBalanceOfAddresses(List<String> addresses) {
        return blockExplorer.getBalance(addresses, BlockExplorer.TX_FILTER_REMOVE_UNSPENDABLE);
    }
}
