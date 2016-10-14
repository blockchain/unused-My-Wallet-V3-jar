package info.blockchain.wallet.util;

import java.security.SecureRandom;
import java.util.Arrays;

public class CharSequenceX implements CharSequence {

    private final char[] chars;
    private int rounds = 100;

    private CharSequenceX(CharSequence charSequence, int start, int end) {
        zap();
        int len = end - start;
        chars = new char[len];
        for (int i = start; i < end; i++) {
            chars[i - start] = charSequence.charAt(i);
        }
    }

    public CharSequenceX(int len) {
        chars = new char[len];
    }

    public CharSequenceX(CharSequence charSequence) {
        this(charSequence, 0, charSequence.length());
    }

    public CharSequenceX(char[] chars) {
        zap();
        this.chars = chars;
    }

    private void zap() {
        if (chars != null) {
            for (int i = 0; i < rounds; i++) {
                fill('0');
                rfill();
                fill('0');
            }
        }
    }

    public void setRounds(int rounds) {
        if (rounds < 100) {
            this.rounds = 100;
        } else {
            this.rounds = rounds;
        }
    }

    public char charAt(int index) {
        if (chars != null) {
            return chars[index];
        } else {
            return 0;
        }
    }

    public int length() {
        if (chars != null) {
            return chars.length;
        } else {
            return 0;
        }
    }

    public String toString() {
        return new String(chars);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CharSequenceX && Arrays.equals(chars, ((CharSequenceX) o).chars);
    }

    public CharSequence subSequence(int start, int end) {
        return new CharSequenceX(this, start, end);
    }

    @SuppressWarnings("FinalizeDoesntCallSuperFinalize")
    protected void finalize() {
        zap();
    }

    private void fill(char c) {
        for (int i = 0; i < chars.length; i++) {
            chars[i] = c;
        }
    }

    private void rfill() {
        SecureRandom r = new SecureRandom();
        byte[] b = new byte[chars.length];
        r.nextBytes(b);
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) b[i];
        }
    }

}