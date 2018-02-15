package info.blockchain.wallet.util;

import org.junit.Test;
import org.spongycastle.pqc.math.linearalgebra.ByteUtils;
import org.spongycastle.pqc.math.linearalgebra.CharUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HexUtilsTest {

    /**
     * Test Hex Strings/Byte Arrays provided by <a href="https://gchq.github.io/CyberChef/">CyberChef</a>
     */
    private static final String TEST_INPUT = "this is a test string";
    private static final String TEST_INPUT_HEX = "746869732069732061207465737420737472696e67";
    private static final char[] TEST_INPUT_CHAR_ARRAY_LOWER_CASE = {'7', '4', '6', '8', '6', '9', '7', '3', '2', '0', '6', '9', '7', '3', '2', '0', '6', '1', '2', '0', '7', '4', '6', '5', '7', '3', '7', '4', '2', '0', '7', '3', '7', '4', '7', '2', '6', '9', '6', 'e', '6', '7'};
    private static final char[] TEST_INPUT_CHAR_ARRAY_UPPER_CASE = {'7', '4', '6', '8', '6', '9', '7', '3', '2', '0', '6', '9', '7', '3', '2', '0', '6', '1', '2', '0', '7', '4', '6', '5', '7', '3', '7', '4', '2', '0', '7', '3', '7', '4', '7', '2', '6', '9', '6', 'E', '6', '7'};
    private static final byte[] TEST_INPUT_BYTES = {116, 104, 105, 115, 32, 105, 115, 32, 97, 32, 116, 101, 115, 116, 32, 115, 116, 114, 105, 110, 103,};

    @Test
    public void encodeHexString() {
        // Arrange

        // Act
        final String result = HexUtils.encodeHexString(TEST_INPUT.getBytes());
        // Assert
        assertEquals(TEST_INPUT_HEX, result);
    }

    @Test
    public void encodeHexCharArray() {
        // Arrange

        // Act
        final char[] result = HexUtils.encodeHex(TEST_INPUT.getBytes());
        // Assert
        assertTrue(CharUtils.equals(result, TEST_INPUT_CHAR_ARRAY_LOWER_CASE));
    }

    @Test
    public void encodeHexCharArray_toLowerCaseTrue() {
        // Arrange

        // Act
        final char[] result = HexUtils.encodeHex(TEST_INPUT.getBytes(), true);
        // Assert
        assertTrue(CharUtils.equals(result, TEST_INPUT_CHAR_ARRAY_LOWER_CASE));
    }

    @Test
    public void encodeHexCharArray_toLowerCaseFalse() {
        // Arrange

        // Act
        final char[] result = HexUtils.encodeHex(TEST_INPUT.getBytes(), false);
        // Assert
        assertTrue(CharUtils.equals(result, TEST_INPUT_CHAR_ARRAY_UPPER_CASE));
    }

    @Test
    public void decodeHex() throws Exception {
        // Arrange

        // Act
        final byte[] result = HexUtils.decodeHex(TEST_INPUT_CHAR_ARRAY_LOWER_CASE);
        // Assert
        assertTrue(ByteUtils.equals(TEST_INPUT_BYTES, result));
    }

}

