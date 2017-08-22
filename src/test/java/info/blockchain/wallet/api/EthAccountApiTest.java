package info.blockchain.wallet.api;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.api.data.EthAccount;
import info.blockchain.wallet.api.data.EthTransaction;

import org.junit.Test;

import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertEquals;

public class EthAccountApiTest extends MockedResponseTest {

    private EthAccountApi subject = new EthAccountApi();

    @Test
    public void getEthAccount() throws Exception {
        // Arrange
        mockInterceptor.setResponseString(EXAMPLE_RESPONSE);
        // Act
        final TestObserver<EthAccount> response = subject.getEthAccount("address").test();
        // Assert
        response.assertComplete();
        response.assertNoErrors();
        final EthAccount ethAccount = response.values().get(0);
        assertEquals(8878260, (int) ethAccount.getId());
        assertEquals("0x879dbfde84b0239feb355f55f81fb29f898c778c", ethAccount.getAccount());
        assertEquals(2, ethAccount.getTransactions().size());
        final EthTransaction transaction = ethAccount.getTransactions().get(1);
        assertEquals(1503064376L, ((long) transaction.getTimeStamp()));
        assertEquals("0x74d62f5ac3423c6a28aaff458be3ef807137696b", transaction.getFrom());
        assertEquals("0x879dbfde84b0239feb355f55f81fb29f898c778c", transaction.getTo());
    }

    private static final String EXAMPLE_RESPONSE = "{\n" +
            "  \"id\": 8878260,\n" +
            "  \"txn_count\": 2,\n" +
            "  \"account\": \"0x879dbfde84b0239feb355f55f81fb29f898c778c\",\n" +
            "  \"accountType\": 0,\n" +
            "  \"balance\": \"0\",\n" +
            "  \"nonce\": 1,\n" +
            "  \"firstTime\": 1503064376,\n" +
            "  \"numNormalTxns\": 2,\n" +
            "  \"numInternalTxns\": 0,\n" +
            "  \"totalReceived\": \"663843982550680064\",\n" +
            "  \"totalSent\": \"663402982550680064\",\n" +
            "  \"totalFee\": \"441000000000000\",\n" +
            "  \"createdBy\": null,\n" +
            "  \"createdIn\": null,\n" +
            "  \"txns\": [\n" +
            "    {\n" +
            "      \"blockNumber\": 4176736,\n" +
            "      \"timeStamp\": 1503130094,\n" +
            "      \"hash\": \"0xcc6952c8f5c6e90d1addcaf3717b6df251982637f0cafc32c7f6348018dd2a7b\",\n" +
            "      \"failFlag\": false,\n" +
            "      \"errorDescription\": null,\n" +
            "      \"nonce\": \"0x0\",\n" +
            "      \"blockHash\": \"0x33a980e70dd3951f85db42cfdddac4d320284d259c6e684ffb179d21586b666f\",\n" +
            "      \"transactionIndex\": 66,\n" +
            "      \"from\": \"0x879dbfde84b0239feb355f55f81fb29f898c778c\",\n" +
            "      \"to\": \"0x0297a2a4cf8117a27b4ad684e43c34e21e600753\",\n" +
            "      \"value\": \"663402982550680064\",\n" +
            "      \"gas\": 21000,\n" +
            "      \"gasPrice\": 21000000000,\n" +
            "      \"gasUsed\": 21000,\n" +
            "      \"input\": \"0x\",\n" +
            "      \"internalFlag\": false,\n" +
            "      \"contractAddress\": null\n" +
            "    },\n" +
            "    {\n" +
            "      \"blockNumber\": 4173545,\n" +
            "      \"timeStamp\": 1503064376,\n" +
            "      \"hash\": \"0x982a1349b446b2613b911e8c0247cb209bfa6b5746581dc9025efbedc7b67996\",\n" +
            "      \"failFlag\": false,\n" +
            "      \"errorDescription\": null,\n" +
            "      \"nonce\": \"0x0\",\n" +
            "      \"blockHash\": \"0x4f15dd1e327130121b9900d6ec99f28623a459478abe28161dafb61eb314dbf7\",\n" +
            "      \"transactionIndex\": 58,\n" +
            "      \"from\": \"0x74d62f5ac3423c6a28aaff458be3ef807137696b\",\n" +
            "      \"to\": \"0x879dbfde84b0239feb355f55f81fb29f898c778c\",\n" +
            "      \"value\": \"663843982550680064\",\n" +
            "      \"gas\": 21000,\n" +
            "      \"gasPrice\": 21000000000,\n" +
            "      \"gasUsed\": 21000,\n" +
            "      \"input\": \"0x\",\n" +
            "      \"internalFlag\": false,\n" +
            "      \"contractAddress\": null\n" +
            "    }\n" +
            "  ],\n" +
            "  \"txnOffset\": 0\n" +
            "}";

}