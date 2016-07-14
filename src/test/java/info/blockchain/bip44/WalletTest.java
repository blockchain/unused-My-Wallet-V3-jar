package info.blockchain.bip44;

import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by riaanvos on 06/07/16.
 */
public class WalletTest {

    @Test
    public void createWalletTest() throws Exception {
        Wallet wallet = null;

        Locale locale = new Locale("en", "US");
        int wordCount = 12;
        String passphrase = "myPassPhrase";
        int accountCount = 4;
        MnemonicCode mc;

        NetworkParameters params = MainNetParams.get();

        SecureRandom random = new SecureRandom();
        byte seed[] = new byte[wordCount];
        random.nextBytes(seed);

        InputStream wis = this.getClass()
                .getClassLoader()
                .getResourceAsStream("wordlist/" + locale.toString() + ".txt");
        if(wis != null) {
            mc = new MnemonicCode(wis, null);
            wallet = new Wallet(mc, params, seed, passphrase, accountCount);
            wis.close();
        }else {
            throw new Exception("Cannot read BIP39 word list");
        }

        assertThat(wallet.getAccounts().size(), is(accountCount));
        assertThat(wallet.getPassphrase(), is(passphrase));
    }

    @Test
    public void createWalletFromXpubsTest() throws AddressFormatException {

        String xpub1 = "xpub6CFgfYG9chNp7rzZ7ByXyAJruku5JSVhtGmGqR9tmeLRwu3jtioyBZpXC6GAnpMQPBQg5rviqTwMN4EwgMCZNVT3N22sSnM1yEfBQzjHXJt";
        String xpub2 = "xpub6C2grzAkm4ikWsjqffKKqLPbVEyhZmBY9nKcW1QrSZexNe9ynfisSLwyoWs94yHqGnfX3VgCeCmW38x4GxHzVSoYNcT9KVq6SH9P3VpgbFs";

        String[] xpubs = {xpub1, xpub2};

        //Generate watch-only wallet
        Wallet wallet = new Wallet(MainNetParams.get(), xpubs);

        assertThat(wallet.getAccounts().get(0).xpubstr(), is(xpub1));
        assertThat(wallet.getAccounts().get(1).xpubstr(), is(xpub2));
    }

    @Test
    public void createWalletFromJsonTest() throws MnemonicException.MnemonicLengthException, IOException, DecoderException {

        JSONObject json = new JSONObject();
        String seedHex = "0660cc198330660cc198330660cc1983";
        String passphrase = "myPassPhrase";
        json.put("hex_seed",seedHex);
        json.put("passphrase",passphrase);

        ArrayList<String> acc = new ArrayList<String>();
        acc.add("1");
        acc.add("2");
        json.put("accounts",new JSONArray(acc));

        Wallet wallet = new Wallet(json, MainNetParams.get(), new Locale("en", "US"));
        assertThat(wallet.getAccounts().size(), is(2));
        assertThat(wallet.getSeedHex(), is(seedHex));
        assertThat(wallet.getPassphrase(), is(passphrase));
    }
}
