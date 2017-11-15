package info.blockchain.wallet.hd;

import info.blockchain.wallet.coin.BitcoinWallet;
import java.security.SecureRandom;
import org.bitcoinj.params.MainNetParams;
import org.junit.Before;
import org.junit.Test;

public class BitcoinWalletTest {

    BitcoinWallet subject;
    MainNetParams params = MainNetParams.get();
    String passphrase = "";

    @Test
    public void constructor() throws Exception{
        subject = new BitcoinWallet(params, passphrase);
        System.out.println(subject.getPubB58(0));
        System.out.println(subject.getPrivB58(0));
        System.out.println(subject.getMnemonic());
    }

    @Test
    public void constructor2() throws Exception{
    }
}
