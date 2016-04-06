package info.blockchain.wallet.developer;

import info.blockchain.api.Unspent;
import info.blockchain.util.FeeUtil;
import info.blockchain.wallet.transaction.BaseTransaction;
import info.blockchain.wallet.transaction.SimpleTransaction;
import info.blockchain.wallet.transaction.data.UnspentOutputs;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigInteger;

public class OnTheFlyTest {

    @Test
    public void getCoins() throws Exception {

        String address = "xpub6DHgYHmEhNxMrZP3rvfSnnPHVi5X2H4h4HLmBnRPHyZ2krP9SKrhEGNFUFRvDwGEAX3KTdD27TQFsTfyjAiCqBvnqWcRwUKZBe5X9MsKkZT";

        JSONObject unspentJson = Unspent.getUnspentOutputs(address);

        SimpleTransaction transaction = new BaseTransaction();
        UnspentOutputs unspentOutputs = transaction.getCoins(address, unspentJson);
        System.out.println("unspentOutputs: "+unspentOutputs);

        BigInteger fee = FeeUtil.estimatedFee(1, unspentOutputs.getOutputs().size(), FeeUtil.AVERAGE_FEE_PER_KB);
        System.out.println("fee: "+fee);
    }
}
