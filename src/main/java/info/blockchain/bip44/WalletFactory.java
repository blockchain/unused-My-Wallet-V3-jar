package info.blockchain.bip44;

import info.blockchain.api.PersistentUrls;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.util.CharSequenceX;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * WalletFactory.java : Class for creating/restoring/reading BIP44 HD wallet
 *
 * BIP44 extension of Bitcoinj
 */
public class WalletFactory {

    public static final String BIP39_ENGLISH_SHA256 = "ad90bf3beb7b0eb7e5acd74727dc0da96e0a280a258354e7293fb7e211ac03db";

    private final Logger mLogger = LoggerFactory.getLogger(WalletFactory.class);

    private Locale locale = null;

    public String strJSONFilePath = null;

    public WalletFactory() {
        locale = new Locale("en", "US");
    }

    public void setJSONFilePath(String path) {
        strJSONFilePath = path;
    }

    /**
     * Set Locale. Defaults to 'en_US'
     *
     * @param loc to be used.
     */
    public void setLocale(Locale loc) {
        if (loc != null) {
            locale = loc;
        } else {
            locale = new Locale("en", "US");
        }
    }

    /**
     * Create new wallet.
     *
     * @param nbWords    number of words in menmonic
     * @param passphrase optional BIP39 passphrase
     * @param nbAccounts create this number of accounts
     * @return Wallet
     */
    public Wallet newWallet(int nbWords, String passphrase, int nbAccounts) throws IOException, MnemonicException.MnemonicLengthException {

        Wallet hdw;

        if ((nbWords % 3 != 0) || (nbWords < 12 || nbWords > 24)) {
            nbWords = 12;
        }

        // len == 16 (12 words), len == 24 (18 words), len == 32 (24 words)
        int len = (nbWords / 3) * 4;

        if (passphrase == null) {
            passphrase = "";
        }

        NetworkParameters params = PersistentUrls.getInstance().getCurrentNetworkParams();

        SecureRandom random = new SecureRandom();
        byte seed[] = new byte[len];
        random.nextBytes(seed);

        InputStream wis = this.getClass()
                .getClassLoader()
                .getResourceAsStream("wordlist/" + locale.toString() + ".txt");
        if (wis != null) {
            MnemonicCode mc = new MnemonicCode(wis, null);
            hdw = new Wallet(mc, params, seed, passphrase, nbAccounts);
            wis.close();
        } else {
            mLogger.info("cannot read BIP39 word list");
            return null;
        }

        return hdw;
    }

    /**
     * Restore wallet.
     *
     * @param data:      either BIP39 mnemonic or hex seed
     * @param passphrase optional BIP39 passphrase
     * @param nbAccounts create this number of accounts
     * @return Wallet
     */
    public Wallet restoreWallet(String data, String passphrase, int nbAccounts) throws AddressFormatException, IOException, DecoderException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicWordException, MnemonicException.MnemonicChecksumException {

        Wallet hdw;

        if (passphrase == null) {
            passphrase = "";
        }

        NetworkParameters params = PersistentUrls.getInstance().getCurrentNetworkParams();

        InputStream wis = this.getClass().getClassLoader().getResourceAsStream("wordlist/" + locale.toString() + ".txt");
        if (wis != null) {
            List<String> words;

            MnemonicCode mc;
            mc = new MnemonicCode(wis, null);

            byte[] seed;
            if (data.startsWith("xpub")) {
                String[] xpub = data.split(":");
                hdw = new Wallet(params, xpub);
            } else if (data.length() % 4 == 0 && !data.contains(" ")) {
                seed = Hex.decodeHex(data.toCharArray());
                hdw = new Wallet(mc, params, seed, passphrase, nbAccounts);
            } else if (locale.toString().equals("en_US")) {
                data = data.replaceAll("[^a-z]+", " ");             // only use for BIP39 English
                words = Arrays.asList(data.trim().split("\\s+"));
                seed = mc.toEntropy(words);
                hdw = new Wallet(mc, params, seed, passphrase, nbAccounts);
            } else {
                words = Arrays.asList(data.trim().split("\\s+"));
                seed = mc.toEntropy(words);
                hdw = new Wallet(mc, params, seed, passphrase, nbAccounts);
            }

            wis.close();

        } else {
            mLogger.info("cannot read BIP39 word list");
            return null;
        }

        return hdw;
    }

    public void saveWalletToJSON(Wallet wallet, String password) throws Exception {
        serialize(wallet.toJSON(), password);
    }

    public Wallet restoreWalletfromJSON(String password) throws Exception {

        Wallet hdw = null;

        NetworkParameters params = PersistentUrls.getInstance().getCurrentNetworkParams();

        JSONObject obj;
        try {
            obj = deserialize(password);
            if (obj != null) {
                hdw = new Wallet(obj, params, locale);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return hdw;
    }

    private void serialize(JSONObject jsonobj, String password) throws Exception {

        File newfile = new File(strJSONFilePath + "bip44_wallet.dat");
        File tmpfile = new File(strJSONFilePath + "bip44_wallet.tmp");

        // serialize to byte array.
        String jsonstr = jsonobj.toString(4);
        byte[] cleartextBytes = jsonstr.getBytes(Charset.forName("UTF-8"));

        // prepare tmp file.
        if (tmpfile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            tmpfile.delete();
        }

        String data;
        if (password != null) {
            data = AESUtil.encrypt(jsonstr, new CharSequenceX(password), AESUtil.QR_CODE_PBKDF_2ITERATIONS);
        } else {
            data = jsonstr;
        }

        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpfile), "UTF-8"));
        try {
            out.write(data);
        } finally {
            out.close();
        }

        // rename tmp file
        if (tmpfile.renameTo(newfile)) {
            mLogger.info("file saved to  " + newfile.getPath());
        } else {
            mLogger.warn("rename to " + newfile.getPath() + " failed");
        }
    }

    private JSONObject deserialize(String password) throws Exception {

        File file = new File(strJSONFilePath + "bip44_wallet.dat");
        StringBuilder sb = new StringBuilder();

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String str;

        while ((str = in.readLine()) != null) {
            sb.append(str);
        }

        JSONObject node;
        if (password == null) {
            node = new JSONObject(sb.toString());
        } else {
            node = new JSONObject(AESUtil.decrypt(sb.toString(), new CharSequenceX(password), AESUtil.QR_CODE_PBKDF_2ITERATIONS));
        }

        return node;
    }
}