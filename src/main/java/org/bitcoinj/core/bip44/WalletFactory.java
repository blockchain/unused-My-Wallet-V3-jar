package org.bitcoinj.core.bip44;

import com.bip44.crypto.AESUtil;
import com.bip44.util.CharSequenceX;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class WalletFactory {

    public static final String BIP39_ENGLISH_SHA256 = "ad90bf3beb7b0eb7e5acd74727dc0da96e0a280a258354e7293fb7e211ac03db";

    private static WalletFactory instance = null;
    private static List<Wallet> wallets = null;

    private static Logger mLogger = LoggerFactory.getLogger(WalletFactory.class);
	
	private static Locale locale = null;

    public static String strJSONFilePath = null;

    private WalletFactory()	{ ; }

    public static WalletFactory getInstance() {

        if (instance == null) {
			locale = new Locale("en", "US");
            wallets = new ArrayList<Wallet>();
            instance = new WalletFactory();
        }

        return instance;
    }

	public void setJSONFilePath(String path)	{
        strJSONFilePath = path;
	}

	public void setLocale(Locale loc)	{
		if(loc != null)	{
	        locale = loc;
		}
		else	{
			locale = new Locale("en", "US");
		}
	}

    public Wallet newWallet(int nbWords, String passphrase, int nbAccounts) throws IOException, MnemonicException.MnemonicLengthException   {

        Wallet hdw = null;

        if((nbWords % 3 != 0) || (nbWords < 12 || nbWords > 24)) {
            nbWords = 12;
        }

        // len == 16 (12 words), len == 24 (18 words), len == 32 (24 words)
        int len = (nbWords / 3) * 4;

        if(passphrase == null) {
            passphrase = "";
        }

        NetworkParameters params = MainNetParams.get();

        SecureRandom random = new SecureRandom();
        byte seed[] = new byte[len];
        random.nextBytes(seed);

        InputStream wis = WalletFactory.class.getResourceAsStream("/" + locale.toString() + ".txt");
        if(wis != null) {
            MnemonicCode mc = new MnemonicCode(wis, null);
            hdw = new Wallet(mc, params, seed, passphrase, nbAccounts);
            wis.close();
        }
        else {
            mLogger.info("cannot read BIP39 word list");
			return null;
        }

        wallets.clear();
        wallets.add(hdw);

        return hdw;
    }

    public Wallet restoreWallet(String data, String passphrase, int nbAccounts) throws AddressFormatException, IOException, DecoderException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicWordException, MnemonicException.MnemonicChecksumException  {

        Wallet hdw = null;

        if(passphrase == null) {
            passphrase = "";
        }

        NetworkParameters params = MainNetParams.get();

        InputStream wis = WalletFactory.class.getResourceAsStream("/" + locale.toString() + ".txt");
        if(wis != null) {
            List<String> words = null;

            MnemonicCode mc = null;
            mc = new MnemonicCode(wis, null);

            byte[] seed = null;
            if(data.startsWith("xpub")) {
                String[] xpub = data.split(":");
                hdw = new Wallet(params, xpub);
            }
            else if(data.length() % 4 == 0 && !data.contains(" ")) {
                seed = Hex.decodeHex(data.toCharArray());
                hdw = new Wallet(mc, params, seed, passphrase, nbAccounts);
            }
            else if(locale.toString().equals("en_US")) {
                data = data.replaceAll("[^a-z]+", " ");             // only use for BIP39 English
                words = Arrays.asList(data.trim().split("\\s+"));
                seed = mc.toEntropy(words);
                hdw = new Wallet(mc, params, seed, passphrase, nbAccounts);
            }
            else {
//                data = data.replaceAll("[^a-z]+", " ");
                words = Arrays.asList(data.trim().split("\\s+"));
                seed = mc.toEntropy(words);
                hdw = new Wallet(mc, params, seed, passphrase, nbAccounts);
            }

            wis.close();

        }
        else {
            mLogger.info("cannot read BIP39 word list");
			return null;
        }

        wallets.clear();
        wallets.add(hdw);

        return hdw;
    }

    public Wallet get() throws IOException, MnemonicException.MnemonicLengthException {

        if(wallets.size() < 1) {
            wallets.clear();
            wallets.add(newWallet(12, "", 1));
        }

        return wallets.get(0);
    }

    public void set(Wallet wallet)	{

        if(wallet != null)	{
            wallets.clear();
            wallets.add(wallet);
        }

    }

    public void saveWalletToJSON(String password) throws MnemonicException.MnemonicLengthException, IOException, JSONException {
        serialize(get().toJSON(), password);
    }

    public Wallet restoreWalletfromJSON(String password) throws DecoderException, MnemonicException.MnemonicLengthException {

        Wallet hdw = null;

        NetworkParameters params = MainNetParams.get();

        JSONObject obj = null;
        try {
            obj = deserialize(password);
            if(obj != null) {
                hdw = new Wallet(obj, params);
            }
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(JSONException je) {
            je.printStackTrace();
        }

        wallets.clear();
        wallets.add(hdw);

        return hdw;
    }

    private void serialize(JSONObject jsonobj, String password) throws IOException, JSONException {

        File newfile = new File(strJSONFilePath + "bip44_wallet.dat");
        File tmpfile = new File(strJSONFilePath + "bip44_wallet.tmp");

        // serialize to byte array.
        String jsonstr = jsonobj.toString(4);
        byte[] cleartextBytes = jsonstr.getBytes(Charset.forName("UTF-8"));

        // prepare tmp file.
        if(tmpfile.exists()) {
            tmpfile.delete();
        }

        String data = null;
        if(password != null) {
            data = AESUtil.encrypt(jsonstr, new CharSequenceX(password), AESUtil.DefaultPBKDF2Iterations);
        }
        else {
            data = jsonstr;
        }

        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpfile), "UTF-8"));
        try {
            out.write(data);
        } finally {
            out.close();
        }

        // rename tmp file
        if(tmpfile.renameTo(newfile)) {
            mLogger.info("file saved to  " + newfile.getPath());
        }
        else {
            mLogger.warn("rename to " + newfile.getPath() + " failed");
        }
    }

    private JSONObject deserialize(String password) throws IOException, JSONException {

        File file = new File(strJSONFilePath + "bip44_wallet.dat");
        StringBuilder sb = new StringBuilder();

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String str = null;

        while((str = in.readLine()) != null) {
            sb.append(str);
        }

        JSONObject node = null;
        if(password == null) {
            node = new JSONObject(sb.toString());
        }
        else {
            node = new JSONObject(AESUtil.decrypt(sb.toString(), new CharSequenceX(password), AESUtil.DefaultPBKDF2Iterations));
        }

        return node;
    }

}
