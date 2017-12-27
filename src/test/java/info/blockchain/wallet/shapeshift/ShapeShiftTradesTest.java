package info.blockchain.wallet.shapeshift;

import info.blockchain.wallet.MockedResponseTest;
import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDWallet;
import info.blockchain.wallet.bip44.HDWalletFactory;
import info.blockchain.wallet.bip44.HDWalletFactory.Language;
import info.blockchain.wallet.shapeshift.data.Quote;
import info.blockchain.wallet.shapeshift.data.Trade;
import info.blockchain.wallet.shapeshift.data.Trade.STATUS;
import info.blockchain.wallet.util.MetadataUtil;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;

public class ShapeShiftTradesTest extends MockedResponseTest {

    private ShapeShiftTrades subject = new ShapeShiftTrades();

    private HDWallet getWallet(String seedHex) throws Exception {
        return HDWalletFactory
            .restoreWallet(PersistentUrls.getInstance().getCurrentNetworkParams(), Language.US,
                seedHex, "", 1);
    }

    private HDWallet getWallet1() throws Exception {
        //bicycle balcony prefer kid flower pole goose crouch century lady worry flavor
        return getWallet("15e23aa73d25994f1921a1256f93f72c");//e085c52b-fa20-4e4c-b558-9ed9ac8c8b95
    }

    private HDWallet getWallet2() throws Exception {
        //radar blur cabbage chef fix engine embark joy scheme fiction master release
        return getWallet("b0a30c7e93a58094d213c4c0aaba22da");
    }

    private HDWallet getWallet3() throws Exception {
        //search present horn clip convince wash motion want sea desert admit increase =jaxx
        return getWallet("c21541b615a2f9eee417b7c1e7740eb9");
    }

    @Test
    public void constructor1() throws Exception {

        HDWallet wallet = getWallet1();

        //Arrange
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = new ShapeShiftTrades(wallet.getMasterKey());

        //Assert
        Assert.assertEquals(0, subject.getTrades().size());
    }

    @Test
    public void load_trade_status_received() throws Exception {

        HDWallet wallet = getWallet1();

        //Arrange
        String response = "{\"payload\":\"uasmFuo40Q4PrAozy+Uswx1oBry2IjUtzmuCkKRS+VUtAFGp9EIL5Rejf88wqXwKQ2h7Mf2X9YF9g0+vwvwQKb5f+r/QIbIHjMnGFkQheRueljYHwYjTDOC7GmqU2ClCdC4nWjrnbbvMa8Mbgms2UgePHO4ncgr/yRVHWTRO43OZOF7N6pjakXuS0Lq1cZKL6pb0B+Mi1tWii15P8Q6FMIDqALPpl75pRpz3mMQlOA9UmZtLwOZQJa4BKGkV3OD3IKU+S5F5Zph9L/KtZyJZK8pjbTmCmu13VsXazRO3SRIA7HEUya2cS9dmyys/xn078bJEM7MDrYS50+IOwLK1Onb1Dgcmb79faPvY3J29Y+Uq9b6JGKhxpzcr/JBKKcMULSrNAIR1SSkX6ilc3Ntg1fNyKR17KznW8z+jqwy24vKAKRinpl1n6qvIVhaG/QU8ikCLbSdJLHlR1wFF9kXaXwVy0E7RryxqJB9slxe0v9JA59qHeNrZLA66s9/GHXazOScy9iKv3GIMpoiZNaOvtGxYbW6PN2ayjlvqIpOxFE0Ie8AGsoQNdFZb1pKYKTZDWdb/Wj3s8ppG6Ffxz8BfdNTBDR9kabMDU1l4pZ4lXoM=\",\"version\":1,\"type_id\":6,\"signature\":\"H1w0MzoOlvkBl3vU+ROqpBEDJbt56QsKKmgy1TMOaAgTSjlD/BRnWfuwC07cmkRI66HyIqcncXhFkjIxADudeWg=\",\"prev_magic_hash\":\"54f17f58823da7db98c7c01dace66ade6d1792c20c612f16cc36c8abe53c0872\",\"address\":\"16frGZdJNT1APT9ht6m91FKXQSgGUr91jy\",\"created_at\":1507724873000,\"updated_at\":1507724896000}";
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(200, response));
        responseList.add(Pair.of(200, response));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = ShapeShiftTrades.load(MetadataUtil.deriveMetadataNode(wallet.getMasterKey()));

        //Assert
        Assert.assertEquals(1, subject.getTrades().size());
        Trade trade = subject.getTrades().get(0);
        Assert.assertEquals(STATUS.RECEIVED, trade.getStatus());
        Assert.assertEquals("34bb9ddc58584697edaf6b1158657d6db28799b86e01af3f86bb2954b11c2d7f", trade.getHashIn());
        Assert.assertEquals(0, trade.getTimestamp());
        Assert.assertEquals("76c2ce1e-7638-4848-84d2-1bc3e7e35003", trade.getQuote().getOrderId());
        Assert.assertEquals("38etuiWCZmURkQQ5SZaB3bA77rpLejZRCy", trade.getQuote().getDeposit());
        Assert.assertEquals(0, trade.getQuote().getExpiration());
        Assert.assertEquals(BigDecimal.valueOf(15.86248012), trade.getQuote().getQuotedRate());
        Assert.assertEquals(BigDecimal.valueOf(0.001), trade.getQuote().getMinerFee());
        Assert.assertEquals("0x14f2bd143692b14d170c34b2ee25ee5fc61e8570", trade.getQuote().getWithdrawal());
        Assert.assertEquals(BigDecimal.valueOf(0.02697539), trade.getQuote().getWithdrawalAmount());
    }

    @Test
    public void load_trade_status_complete() throws Exception {

        HDWallet wallet = getWallet1();

        //Arrange
        String response = "{\"payload\":\"2KjHxQESeAuExH+aIUJIoU9H1QWdN6GlUbIj/OTf9TYJwni82p8sVZoY1P9PHbs9HAeS0UfZXH0uYmkJZtY3rAGCsYQPp8BlC0yQx6DbCeUwvd3NgTwB1ANj73L3NXXDLa7a7ND3ubxc0xcBqekobQlSpbyM7QMfxyU2AYvFSO6vqz++5H9Mn484Uu6Naq+hVgmvD6yNxJbWTpudxKzJRDCoR/X0uNmqkRMxLI4W/GGpBo/1DxBBazIBIVKavHdYmbdWuEfG3nOrQonquHIxbpPQw9jV1/AXa91I30Dts8sMpvKATivZYZghFP1LpNO+/AM6H/BchjGpeqv2qOfVmvJgUp6eJ8WoVxbdBeOyApeCy4DAyjU52nq7p0XyWuhBK7htRd/XK66jvT/QOrbZGPX23Jk9BX7R73lkjVhl30gRqyq6ivL6+Q1Vehg/xTC7+rCETOOjootR0V+WjmBy/NIGp6ubZxv2VaDNkTFe+Rhiza0lVXnZ68+QGwp2C+sIaysFETmPQC0qM5YzlD1dOb7IzLvBspOt/c2zPFUEY5w=\",\"version\":1,\"type_id\":6,\"signature\":\"IHGxHSt3cI6PhliyVyBXDDsxh/ornIZTyoIMicaaATi5Ibaj9xNhlhrFU8J/DjIBVrJ85tClfsTStYbfWtqeLWM=\",\"prev_magic_hash\":\"bbe4acba2dbfa04f7e726e225948ca0050846ec5a6e8d9b386156b46343cb5bc\",\"address\":\"16frGZdJNT1APT9ht6m91FKXQSgGUr91jy\",\"created_at\":1507724873000,\"updated_at\":1507725315000}";
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(200, response));
        responseList.add(Pair.of(200, response));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = ShapeShiftTrades.load(MetadataUtil.deriveMetadataNode(wallet.getMasterKey()));

        //Assert
        Assert.assertEquals(1, subject.getTrades().size());
        Trade trade = subject.getTrades().get(0);
        Assert.assertEquals(STATUS.COMPLETE, trade.getStatus());
        Assert.assertEquals("34bb9ddc58584697edaf6b1158657d6db28799b86e01af3f86bb2954b11c2d7f", trade.getHashIn());
        Assert.assertEquals(0, trade.getTimestamp());
        Assert.assertEquals("76c2ce1e-7638-4848-84d2-1bc3e7e35003", trade.getQuote().getOrderId());
        Assert.assertEquals(BigDecimal.valueOf(15.86248012), trade.getQuote().getQuotedRate());
        Assert.assertEquals(BigDecimal.valueOf(0.001), trade.getQuote().getMinerFee());
        Assert.assertEquals("38etuiWCZmURkQQ5SZaB3bA77rpLejZRCy", trade.getQuote().getDeposit());
    }

    @Test
    public void load_empty() throws Exception {

        HDWallet wallet = getWallet2();

        //Arrange
        String response = "{\"message\":\"Not Found\"}";
        LinkedList<Pair> responseList = new LinkedList<>();
        responseList.add(Pair.of(404, response));
        responseList.add(Pair.of(404, response));
        mockInterceptor.setResponseList(responseList);

        //Act
        subject = ShapeShiftTrades.load(MetadataUtil.deriveMetadataNode(wallet.getMasterKey()));

        //Assert
        Assert.assertNull(subject);
    }

    @Test
    public void load_save() throws Exception {

        HDWallet wallet = getWallet3();

        //Arrange
        LinkedList<Pair> responseList = new LinkedList<>();
        //load empty
        responseList.add(Pair.of(404, "{\"message\":\"Not Found\"}"));
        //save
        responseList.add(Pair.of(200, "{\"payload\":\"f2GjFSz05KwSFBEXC7u7RFPJvI5qjKEyB+Eg+6cWSXJoWVQ4e99LKI3AeZYzKnml2SVnTvIh+EVIcxKWdz5J22gY5c5BKXJHwYtqSVu0VO75U0QtMiIW+j2tMH2yzKcBm+EBC/GlhdvlhSnJlt+yB6lcbQ8RFp0/9IsZnxUpkjXiLbJSLMOxefIbFwbWaN3fqNxQ2Dat4pDu4gerldkElaXWKQ9OPkc0xif1EdvnbNOUHPmbt7Tg8f0e83DUUYjO\",\"version\":1,\"type_id\":6,\"signature\":\"HybdOlPHQmlHSPkqJs+DqoOL9QIjrtXOKaGrrWtngwGqDVWejI2/d7U6y0/TDYzhqHBdfT7xy48EMiGETQrmesQ=\",\"address\":\"1CCokrP6SA4wVJa4dTrmZkabXh9Rzh1PiB\",\"created_at\":1507731253397,\"updated_at\":1507731253397}"));
        //load non empty
        responseList.add(Pair.of(200, "{\"payload\":\"EE7qzcrEaVG11y/SJHvrO84LFAXS2h8y2UrH9FPZbUbxTwNdLBr9Af2jvosR+kEb+qVw2BF9fOsOOSUYQCNYnXniAjKpJIIfEce4PwYZp1wpjb/uqgupUMeWHD83OY1R7j5QEH36gUdT9T9yPLy66H1nk1yJNZbxnJZmTNPtnlKtyV0RCRhjZU7Vf1SxN6qVPMb/yHyr0VEtf0K8qBCMHhtwA0yLfEJyKcJicr0Y2A1swtuL8yDWZduCuNoZTdFw\",\"version\":1,\"type_id\":6,\"signature\":\"IBDZB3Lyu1JXpAile+OZGJNL5vcsNc/2Qdx/888j90z1UawbO3lmVMULkpnefst+IW4mOduHkOP91XBkE/NQFTQ=\",\"prev_magic_hash\":\"e9922795975b37b333d64680273f37ce89a7333b84e0a823f8141db6e1ffdbc9\",\"address\":\"1CCokrP6SA4wVJa4dTrmZkabXh9Rzh1PiB\",\"created_at\":1507731253000,\"updated_at\":1507731419440}"));
        responseList.add(Pair.of(200, "{\"payload\":\"EE7qzcrEaVG11y/SJHvrO84LFAXS2h8y2UrH9FPZbUbxTwNdLBr9Af2jvosR+kEb+qVw2BF9fOsOOSUYQCNYnXniAjKpJIIfEce4PwYZp1wpjb/uqgupUMeWHD83OY1R7j5QEH36gUdT9T9yPLy66H1nk1yJNZbxnJZmTNPtnlKtyV0RCRhjZU7Vf1SxN6qVPMb/yHyr0VEtf0K8qBCMHhtwA0yLfEJyKcJicr0Y2A1swtuL8yDWZduCuNoZTdFw\",\"version\":1,\"type_id\":6,\"signature\":\"IBDZB3Lyu1JXpAile+OZGJNL5vcsNc/2Qdx/888j90z1UawbO3lmVMULkpnefst+IW4mOduHkOP91XBkE/NQFTQ=\",\"prev_magic_hash\":\"e9922795975b37b333d64680273f37ce89a7333b84e0a823f8141db6e1ffdbc9\",\"address\":\"1CCokrP6SA4wVJa4dTrmZkabXh9Rzh1PiB\",\"created_at\":1507731253000,\"updated_at\":1507731419440}"));
        mockInterceptor.setResponseList(responseList);

        subject = new ShapeShiftTrades(wallet.getMasterKey());

        ArrayList<Trade> trades = new ArrayList<>();

        Quote quote = new Quote();
        quote.setOrderId("some order id");

        Trade trade = new Trade();
        trade.setHashIn("some hash in");
        trade.setHashOut("some hash out");
        trade.setQuote(quote);
        trade.setStatus(STATUS.RECEIVED);

        trades.add(trade);

        subject.setTrades(trades);

        //Act
        subject.save();
        subject = ShapeShiftTrades.load(MetadataUtil.deriveMetadataNode(wallet.getMasterKey()));

        //Assert
        Assert.assertEquals(1, subject.getTrades().size());
        Trade t = subject.getTrades().get(0);
        Assert.assertEquals(STATUS.RECEIVED, t.getStatus());
        Assert.assertEquals("some hash in", t.getHashIn());
        Assert.assertEquals("some hash out", t.getHashOut());
        Assert.assertEquals("some order id", t.getQuote().getOrderId());
    }
}