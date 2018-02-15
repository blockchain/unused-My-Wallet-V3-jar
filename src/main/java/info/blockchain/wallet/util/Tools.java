package info.blockchain.wallet.util;

import info.blockchain.wallet.api.PersistentUrls;
import info.blockchain.wallet.bip44.HDAccount;
import info.blockchain.wallet.payload.data.LegacyAddress;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;

public class Tools {

    public static Transaction applyBip69(Transaction transaction) {
        //This will render an already signed transaction invalid, as the signature covers the ordering of the in/outputs.

        List<TransactionInput> inputList = new ArrayList<>(transaction.getInputs());
        List<TransactionOutput> outputList = new ArrayList<>(transaction.getOutputs());

        Collections.sort(inputList, new Comparator<TransactionInput>() {
            @Override
            public int compare(TransactionInput o1, TransactionInput o2) {
                byte[] hash1 = o1.getOutpoint().getHash().getBytes();
                byte[] hash2 = o2.getOutpoint().getHash().getBytes();
                int hashCompare = LexicographicalComparator.getComparator().compare(hash1, hash2);
                if (hashCompare != 0) {
                    return hashCompare;
                } else {
                    return (int) (o1.getOutpoint().getIndex() - o2.getOutpoint().getIndex());
                }
            }
        });

        Collections.sort(outputList, new Comparator<TransactionOutput>() {
            @Override
            public int compare(TransactionOutput o1, TransactionOutput o2) {
                long amountDiff = o1.getValue().getValue() - o2.getValue().value;
                if (amountDiff != 0) {
                    return (int) amountDiff;
                } else {
                    byte[] hash1 = o1.getScriptBytes();
                    byte[] hash2 = o2.getScriptBytes();
                    return LexicographicalComparator.getComparator().compare(hash1, hash2);
                }
            }
        });

        Transaction sortedTransaction = new Transaction(transaction.getParams());
        for (TransactionInput input : inputList) {
            sortedTransaction.addInput(input);
        }
        for (TransactionOutput output : outputList) {
            sortedTransaction.addOutput(output);
        }
        return sortedTransaction;
    }

    public static byte[] hexStringToByteArray(String s) {
        s = s.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        int len = s.length();

        if (len % 2 != 0) {
            throw new IllegalArgumentException("Uneven hexadecimal string");
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static ECKey getECKeyFromKeyAndAddress(@Nonnull String decryptedKey, @Nonnull String address) throws
            AddressFormatException {

        byte[] privBytes = Base58.decode(decryptedKey);
        ECKey ecKey;

        ECKey keyCompressed;
        ECKey keyUnCompressed;
        BigInteger priv = new BigInteger(privBytes);
        if (priv.compareTo(BigInteger.ZERO) >= 0) {
            keyCompressed = ECKey.fromPrivate(priv, true);
            keyUnCompressed = ECKey.fromPrivate(priv, false);
        } else {
            byte[] appendZeroByte = ArrayUtils.addAll(new byte[1], privBytes);
            BigInteger priv2 = new BigInteger(appendZeroByte);
            keyCompressed = ECKey.fromPrivate(priv2, true);
            keyUnCompressed = ECKey.fromPrivate(priv2, false);
        }

        if (keyCompressed.toAddress(PersistentUrls.getInstance().getBitcoinParams())
                .toString().equals(address)) {
            ecKey = keyCompressed;
        } else if (keyUnCompressed.toAddress(PersistentUrls.getInstance().getBitcoinParams())
                .toString().equals(address)) {
            ecKey = keyUnCompressed;
        } else {
            ecKey = null;
        }

        return ecKey;
    }

    public static List<String> filterLegacyAddress(int filter, @Nonnull List<LegacyAddress> keys) {

        List<String> addressList = new ArrayList<>();

        for (LegacyAddress key : keys) {
            if (key.getTag() == filter) {
                addressList.add(key.getAddress());
            }
        }

        return addressList;
    }

    public static List<String> getAddressList(int chain, String xpub, int startIndex, int endIndex) {
        HDAccount hdAccount = new HDAccount(PersistentUrls.getInstance().getBitcoinParams(),
                xpub);

        List<String> list = new ArrayList<>();

        for (int i = startIndex; i < endIndex; i++) {
            list.add(hdAccount.getChain(chain).getAddressAt(i).getAddressString());
        }

        return list;
    }

    /**
     * Returns a list of receive addresses between two points on the chain.
     *
     * @param account    The {@link HDAccount} that you wish to derive addresses from
     * @param startIndex The starting index, probably the next available index
     * @param endIndex   The finishing index, an arbitrary number away from the starting point
     * @return A non-null List of addresses as Strings
     */
    public static List<String> getReceiveAddressList(HDAccount account, int startIndex, int endIndex) {
        List<String> list = new ArrayList<>();

        for (int i = startIndex; i < endIndex; i++) {
            list.add(account.getReceive().getAddressAt(i).getAddressString());
        }

        return list;
    }

}
