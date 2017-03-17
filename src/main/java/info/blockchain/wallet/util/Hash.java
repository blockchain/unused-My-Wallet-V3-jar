package info.blockchain.wallet.util;

import java.io.Serializable;
import java.util.Arrays;
import org.spongycastle.util.encoders.Hex;

public class Hash implements Serializable {

    private final byte[] hash;

    public Hash() {
        this.hash = null;
    }

    public Hash(String hex) {
        this.hash = Hex.decode(hex);
    }

    public Hash(byte[] bytes) {
        this.hash = bytes;
    }

    public void reverse() {
        if (hash == null) {
            return;
        }
        int i = 0;
        int j = hash.length - 1;
        byte tmp;
        while (j > i) {
            tmp = hash[j];
            hash[j] = hash[i];
            hash[i] = tmp;
            j--;
            i++;
        }
    }

    public byte[] getBytes() {
        return hash;
    }

    public int nLeadingZeros() {
        int n = 0;

        for (byte b : hash) {
            if (b == 0)
                n += 8;
            else {
                n += Math.max(0, Integer.numberOfLeadingZeros(b) - (3 * 8));
                break;
            }
        }

        return n;
    }

    public boolean isNull() {
        if (hash == null || hash.length == 0)
            return true;

        for (byte b : hash) {
            if (b != 0)
                return false;
        }

        return true;
    }

    @Override
    public String toString() {

        if (hash == null)
            return null;

        return new String(Hex.encode(hash));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Hash other = (Hash) obj;
        return Arrays.equals(hash, other.hash);
    }
}