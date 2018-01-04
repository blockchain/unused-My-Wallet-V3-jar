package info.blockchain.wallet.util;

import com.google.common.primitives.UnsignedBytes;
import java.util.Comparator;

/**
 * This class is a slower but Android safe replacement for {@link UnsignedBytes#lexicographicalComparator()},
 * which uses {@link sun.misc.Unsafe}. This seems to cause crashes of the JVM process on Android, so
 * here we'll have to sacrifice speed for safety.
 *
 * The implementation is lifted from {@link UnsignedBytes} PureJavaComparator, which is what {@link
 * UnsignedBytes#lexicographicalComparator()} would ideally default to but does not on Android
 * processes.
 */
class LexicographicalComparator {

    static Comparator<byte[]> getComparator() {
        return LexicographicalComparatorImpl.INSTANCE;
    }

    private enum LexicographicalComparatorImpl implements Comparator<byte[]> {
        INSTANCE;

        @Override
        public int compare(byte[] left, byte[] right) {
            int minLength = Math.min(left.length, right.length);
            for (int i = 0; i < minLength; i++) {
                int result = UnsignedBytes.compare(left[i], right[i]);
                if (result != 0) {
                    return result;
                }
            }
            return left.length - right.length;
        }
    }

}
