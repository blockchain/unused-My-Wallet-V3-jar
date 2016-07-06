package info.blockchain.bip44;

import org.apache.commons.cli.*;
import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.crypto.MnemonicException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;

public class Main {

    private static Logger mLogger = LoggerFactory.getLogger(Main.class);

    private static Wallet hdw = null;

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("a", true, "create/restore with this number of accounts (default = 2 accounts)");
        options.addOption("c", false, "create new wallet (default = 12 words)");
        options.addOption("f", true, "restore wallet from JSON using this password");
        options.addOption("l", true, "use this Locale for BIP 39 nword list");
        options.addOption("p", true, "use this passphrase (BIP 39)");
        options.addOption("r", true, "restore wallet from hex, mnemonic, or ':' separated XPUBs (write-only wallet)");
        options.addOption("t", true, "save wallet to JSON using this password");
        options.addOption("w", true, "create wallet using this number of words for mnemonic");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        }
        catch(ParseException pe) {
            pe.printStackTrace();
        }

        if(cmd.hasOption("l") && cmd.getOptionValue("l").length() == 5) {
			Locale locale = new Locale(cmd.getOptionValue("l").substring(0, 2), cmd.getOptionValue("l").substring(3));
			if(locale != null)	{
                WalletFactory.getInstance().setLocale(locale);
			}
		}

        if(cmd.hasOption("f")) {
            String password = cmd.getOptionValue("f");
            try {
                WalletFactory.getInstance().setJSONFilePath("/tmp/");
                hdw = WalletFactory.getInstance().restoreWalletfromJSON(password.length() == 0 ? null : password);
                mLogger.info(hdw.toJSON().toString());
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                return;
            }
        }

        String passphrase = "";
        if(cmd.hasOption("p")) {
            passphrase = cmd.getOptionValue("p");
        }

        String restore = null;
        boolean isRestore = false;
        if(cmd.hasOption("r")) {
            isRestore = true;
            restore = cmd.getOptionValue("r");
        }
        else {
            isRestore = false;
        }

        int nbAccounts = 1;
        if(cmd.hasOption("a")) {
            try {
                nbAccounts = Integer.parseInt(cmd.getOptionValue("a"));
            }
            catch(Exception e) {
                ;
            }
        }

        if(isRestore) {

            try {
                hdw = WalletFactory.getInstance().restoreWallet(restore, passphrase, nbAccounts);
            }
            catch(MnemonicException.MnemonicLengthException mle) {
                mle.printStackTrace();
            }
            catch(MnemonicException.MnemonicWordException mwe) {
                mwe.printStackTrace();
            }
            catch(MnemonicException.MnemonicChecksumException mce) {
                mce.printStackTrace();
            }
            catch(DecoderException de) {
                de.printStackTrace();
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
            catch(AddressFormatException afe) {
                afe.printStackTrace();
            }

        }
        else {
            int nbWords = 12;

            if(cmd.hasOption("w")) {
                try {
                    nbWords = Integer.parseInt(cmd.getOptionValue("w"));
                    hdw = WalletFactory.getInstance().newWallet(nbWords, passphrase, nbAccounts);
                }
                catch(Exception e) {
                    ;
                }
            }
            else {
                try {
                    hdw = WalletFactory.getInstance().get();
                }
                catch(IOException ioe) {
                    ioe.printStackTrace();
                    mLogger.info(ioe.getLocalizedMessage());
                }
                catch(MnemonicException.MnemonicLengthException mle) {
                    mle.printStackTrace();
                    mLogger.info(mle.getLocalizedMessage());
                }
            }

        }

        if(cmd.hasOption("t")) {
            String password = cmd.getOptionValue("t");
            try {
                WalletFactory.getInstance().setJSONFilePath("/tmp/");
                WalletFactory.getInstance().saveWalletToJSON(password.length() == 0 ? null : password);
            }
            catch(MnemonicException.MnemonicLengthException mle) {
                mle.printStackTrace();
            }
            catch(JSONException je) {
                je.printStackTrace();
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }

        if(hdw != null) {
            mLogger.info(hdw.toJSON().toString());
        }
        else {
            mLogger.info("error producing wallet");
        }

    }

}
