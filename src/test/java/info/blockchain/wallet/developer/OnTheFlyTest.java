package info.blockchain.wallet.developer;

import info.blockchain.api.Unspent;
import info.blockchain.util.FeeUtil;
import info.blockchain.wallet.payment.Payment;
import info.blockchain.wallet.payment.data.UnspentOutputs;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigInteger;

public class OnTheFlyTest {

    @Test
    public void getCoins() throws Exception {

        String address = "xpub6DHgYHmEhNxMrZP3rvfSnnPHVi5X2H4h4HLmBnRPHyZ2krP9SKrhEGNFUFRvDwGEAX3KTdD27TQFsTfyjAiCqBvnqWcRwUKZBe5X9MsKkZT";

        JSONObject unspentJson = Unspent.getUnspentOutputs(address);

        Payment transaction = new Payment();
        UnspentOutputs unspentOutputs = transaction.getCoins(unspentJson);
        System.out.println("unspentOutputs: "+unspentOutputs);

        BigInteger fee = FeeUtil.estimatedFee(1, unspentOutputs.getOutputs().size(), FeeUtil.AVERAGE_FEE_PER_KB);
        System.out.println("fee: "+fee);
    }
}
