package info.blockchain.wallet.util;

import info.blockchain.wallet.api.PersistentUrls;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.CashAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

public class FormatsUtil {

    private static final Pattern emailPattern = Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");
    private static final Pattern phonePattern = Pattern.compile("(\\+[1-9]{1}[0-9]{1,2}+|00[1-9]{1}[0-9]{1,2}+)[\\(\\)\\.\\-\\s\\d]{6,16}");

    private static final Pattern ignoreCaseEthAddrPattern = Pattern.compile("(?i)^(0x)?[0-9a-f]{40}$");
    private static final Pattern lowerCaseEthAddrPattern = Pattern.compile("^(0x)?[0-9a-f]{40}$");
    private static final Pattern upperCaseEthAddrPattern = Pattern.compile("^(0x)?[0-9A-F]{40}$");

    public static String validateBitcoinAddress(final String address) {

        if (isValidBitcoinAddress(address)) {
            return address;
        } else {
            String addr = uri2BitcoinAddress(address);
            if (addr != null) {
                return addr;
            } else {
                return null;
            }
        }
    }

    public static boolean isBitcoinUri(final String s) {

        if(s == null)return false;

        boolean ret;

        try {
            new BitcoinURI(s);
            ret = true;
        } catch (BitcoinURIParseException bupe) {
            ret = false;
        }

        return ret;
    }

    public static String getBitcoinUri(final String s) {

        String ret;
        BitcoinURI uri;

        try {
            uri = new BitcoinURI(s);
            ret = uri.toString();
        } catch (BitcoinURIParseException bupe) {
            ret = null;
        }

        return ret;
    }

    public static String getBitcoinAddress(final String s) {

        if(s == null)return "";

        String ret;
        BitcoinURI uri;

        try {
            uri = new BitcoinURI(s);
            Address address = uri.getAddress();
            if(address == null) throw new BitcoinURIParseException(s+" is not valid bitcoin uri.");
            ret = address.toString();
        } catch (BitcoinURIParseException bupe) {
            ret = "";
        }

        return ret;
    }

    public static String getBitcoinAmount(final String s) {

        if(s == null)return "0.0000";

        String ret;
        BitcoinURI uri;

        try {
            uri = new BitcoinURI(s);
            if (uri.getAmount() != null) {
                ret = uri.getAmount().toString();
            } else {
                ret = "0.0000";
            }
        } catch (BitcoinURIParseException bupe) {
            ret = "0.0000";
        }

        return ret;
    }

    public static boolean isValidBitcoinAddress(final String address) {

        boolean ret;

        if(address == null)
            return false;

        try {
            Address.fromBase58(PersistentUrls.getInstance().getBitcoinParams(), address);
            ret = true;
        } catch (WrongNetworkException wne) {
            ret = false;
        } catch (AddressFormatException afe) {
            ret = false;
        }

        return ret;
    }

    public static boolean isValidEmailAddress(final String address) {
        return emailPattern.matcher(address).matches();
    }

    public static boolean isValidMobileNumber(final String mobile) {
        return phonePattern.matcher(mobile).matches();
    }

    private static String uri2BitcoinAddress(final String address) {

        String ret;
        BitcoinURI uri;

        try {
            uri = new BitcoinURI(address);
            Address address1 = uri.getAddress();
            assert address1 != null;
            ret = address1.toString();
        } catch (BitcoinURIParseException bupe) {
            ret = null;
        }

        return ret;
    }

    public static boolean isValidXpub(String xpub) {

        try {
            byte[] xpubBytes = Base58.decodeChecked(xpub);

            ByteBuffer byteBuffer = ByteBuffer.wrap(xpubBytes);
            if (byteBuffer.getInt() != 0x0488B21E) {
                throw new AddressFormatException("invalid version: " + xpub);
            } else {

                byte[] chain = new byte[32];
                byte[] pub = new byte[33];
                // depth:
                byteBuffer.get();
                // parent fingerprint:
                byteBuffer.getInt();
                // child no.
                byteBuffer.getInt();
                byteBuffer.get(chain);
                byteBuffer.get(pub);

                ByteBuffer pubBytes = ByteBuffer.wrap(pub);
                int firstByte = pubBytes.get();
                if (firstByte == 0x02 || firstByte == 0x03) {
                    return true;
                } else {
                    throw new AddressFormatException("invalid format: " + xpub);
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ex) {
            try {
                new JSONArray(json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static boolean isKeyEncrypted(String data) {

        if (data != null && isBase64(data)) {
            try {
                Base58.decode(data);
                return false;
            } catch (AddressFormatException e) {
                return true;
            }

        } else {
            return false;
        }
    }

    public static boolean isKeyUnencrypted(String data) {

        if (data == null)
            return false;

        try {
            Base58.decode(data);
            return true;
        } catch (AddressFormatException e) {
            return false;
        }
    }

    private static boolean isBase64(String data) {
        String regex = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return (data.matches(regex) && !containsSpaces(data));
    }

    private static boolean containsSpaces(String data) {
        return data.contains(" ") ||
                data.contains("\n") ||
                data.contains("\r") ||
                data.contains("\t");
    }

    /**
     * Check for poorly formed BIP21 URIs
     */
    public static String getURIFromPoorlyFormedBIP21(String uri) {
        if (uri.startsWith("bitcoin://") && uri.length() > 10) {
            return "bitcoin:" + uri.substring(10);
        } else {
            return uri;
        }
    }

    /**
     * Verify that a hex account string is a valid Ethereum address.
     *
     * @param address given address in HEX
     * @return is this a valid address
     */
    public static Boolean isValidEthereumAddress(String address) {
        /*
         * check basic address requirements, i.e. is not empty and contains
         * the valid number and type of characters
         */
        if (address == null || address.isEmpty() || !ignoreCaseEthAddrPattern.matcher(address).find()) {
            return false;
        } else if (lowerCaseEthAddrPattern.matcher(address).find()
                || upperCaseEthAddrPattern.matcher(address).find()) {
            // if it's all small caps or caps return true
            return true;
        } else {
            // if it is mixed caps it is a checksum address and needs to be validated
            return validateChecksumEthereumAddress(address);
        }
    }

    /**
     * Verify that a String is a valid BECH32 Bitcoin Cash address.
     *
     * @param address    The String you wish to test
     * @return Is this a valid BECH32 format BCH address
     */
    public static Boolean isValidBitcoinCashAddress(String address) {
        /*
         * Check basic address requirements, i.e. is not empty
         */
        NetworkParameters networkParameters = PersistentUrls.getInstance().getBitcoinCashParams();

        if (address == null || address.isEmpty()) {
            return false;
        } else {
            try {
                CashAddress.decode(address);
                return true;
            } catch (AddressFormatException e) {

                if (address.startsWith(networkParameters.getBech32AddressPrefix())) {
                    return false;
                } else {
                    return isValidBitcoinCashAddress(
                        networkParameters.getBech32AddressPrefix() +
                            (char)(networkParameters.getBech32AddressSeparator()) +
                            address);
                }
            }
        }
    }

    private static Boolean validateChecksumEthereumAddress(String address) {
        address = address.replace("0x", "");
        String hash = Numeric.toHexStringNoPrefix(Hash.sha3(address.toLowerCase().getBytes()));
        for (int i = 0; i < 40; i++) {
            if (Character.isLetter(address.charAt(i))) {
                // each uppercase letter should correlate with a first bit of 1 in the hash
                // char with the same index, and each lowercase letter with a 0 bit
                int charInt = Integer.parseInt(Character.toString(hash.charAt(i)), 16);
                if ((Character.isUpperCase(address.charAt(i)) && charInt <= 7)
                        || (Character.isLowerCase(address.charAt(i)) && charInt > 7)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Accepts bech32 cash address or base58 legacy address
     * @param address
     * @return Short cash address (Example: qpmtetdtqpy5yhflnmmv8s35gkqfdnfdtywdqvue4p)
     */
    public static String toShortCashAddress(String address) {

        if (address == null && address.isEmpty()) {
            throw new AddressFormatException("Invalid address format - "+address);
        }

        NetworkParameters networkParameters = PersistentUrls.getInstance().getBitcoinCashParams();
        String result = address;

        if (isValidBitcoinAddress(address)) {
            address = Address.fromBase58(networkParameters, address).toCashAddress();
        }

        if (isValidBitcoinCashAddress(address)) {
            result = address.replace(
                networkParameters.getBech32AddressPrefix() + (char)networkParameters
                    .getBech32AddressSeparator(), "");
        } else {
            throw new AddressFormatException("Invalid address format - "+address);
        }

        return result;
    }
}