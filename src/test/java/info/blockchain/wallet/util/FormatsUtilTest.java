package info.blockchain.wallet.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bitcoinj.params.BitcoinCashMainNetParams;
import org.bitcoinj.params.BitcoinCashTestNet3Params;
import org.junit.Test;

public class FormatsUtilTest {

    @Test
    public void isEncrypted() throws Exception {

        assertFalse(FormatsUtil.isKeyEncrypted(null));
        assertFalse(FormatsUtil.isKeyEncrypted("3tcnfpTzY6G6oL4NujXkXJfpkEJr69fDSRESuA76izac"));

        assertTrue(FormatsUtil.isKeyEncrypted("51jTHC6+phVaDTqZOyldKRRqrZQiXm/IhTMAjM/G9eCVQJt6POLTsKQT29RlFH9vH2tbJaowM5firNiSiNNIPw=="));
        assertTrue(FormatsUtil.isKeyEncrypted("QQBIDa4SO84Uow1AlWo/1STqO2n5OXN6seU2eULjK/4ydHYW/LRTmBQT3eyIgdYCnNtJ1QBSatZ/9d4oNbkH0pmPeZEd+4Sekz9zoqfJs35k0kt7R3De+L6cqYymLpQJLELZwlP78SmWnlC31pCAB/lklBXwlv9xcSRq9qO9sLk="));
    }

    @Test
    public void isUnencrypted() throws Exception {

        assertFalse(FormatsUtil.isKeyUnencrypted(null));
        assertFalse(FormatsUtil.isKeyUnencrypted("51jTHC6+phVaDTqZOyldKRRqrZQiXm/IhTMAjM/G9eCVQJt6POLTsKQT29RlFH9vH2tbJaowM5firNiSiNNIPw=="));
        assertFalse(FormatsUtil.isKeyUnencrypted("QQBIDa4SO84Uow1AlWo/1STqO2n5OXN6seU2eULjK/4ydHYW/LRTmBQT3eyIgdYCnNtJ1QBSatZ/9d4oNbkH0pmPeZEd+4Sekz9zoqfJs35k0kt7R3De+L6cqYymLpQJLELZwlP78SmWnlC31pCAB/lklBXwlv9xcSRq9qO9sLk="));
        assertTrue(FormatsUtil.isKeyUnencrypted("3tcnfpTzY6G6oL4NujXkXJfpkEJr69fDSRESuA76izac"));
    }

    @Test
    public void isValidBitcoinCashAddress() {
        assertFalse(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashMainNetParams.get(), null));
        assertFalse(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashMainNetParams.get(), ""));
        assertFalse(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashMainNetParams.get(), "test string"));
        // Standard BTC address
        assertFalse(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashMainNetParams.get(), "19dPodLBKT4Fpym4PJ3UfkoMBDiTGkHw2V"));
        // BECH32 Segwit BTC address
        assertFalse(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashMainNetParams.get(), "3MG8XBSphrQg8HLkz51Y6vJVgtXV1R8qS6"));
        // Valid BECH32 BCH address
        assertTrue(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashMainNetParams.get(), "bitcoincash:qp02xpzz9qq0u7mtefw028mtlkszshxxdv0xsgv8pc"));
        // Valid BECH32 BCH address but with single digit changed
        assertFalse(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashMainNetParams.get(), "bitcoincash:qp02xpzz9qq0u7mtefw028mtlkszshxxdv0xsgv8pd"));
        // Valid BECH32 BCH address but with single digit missing
        assertFalse(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashMainNetParams.get(), "bitcoincash:qp02xpzz9qq0u7mtefw028mtlkszshxxdv0xsgv8p"));
        // Valid Testnet cash address
        assertTrue(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashTestNet3Params.get(), "bchtest:ppm2qsznhks23z7629mms6s4cwef74vcwvhanqgjxu"));
        // Valid BECH32 BCH address - no prefix
        assertTrue(FormatsUtil.isValidBitcoinCashAddress(BitcoinCashMainNetParams.get(), "qp02xpzz9qq0u7mtefw028mtlkszshxxdv0xsgv8pc"));
    }

    @Test
    public void getBitcoinAddress() throws Exception {

        assertEquals("", FormatsUtil.getBitcoinAddress(null));
        assertEquals("", FormatsUtil.getBitcoinAddress(""));
        assertEquals("", FormatsUtil.getBitcoinAddress("Tobi Kadachi"));
        assertEquals("", FormatsUtil.getBitcoinAddress("bitcoin:?r=https://bitpay.com/i/AX146S9yK1ftUPmZGoNr9B"));
        assertEquals("12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu", FormatsUtil.getBitcoinAddress("bitcoin:12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu"));
        assertEquals("12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu", FormatsUtil.getBitcoinAddress("bitcoin:12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu?amount=1.2"));
        assertEquals("12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu", FormatsUtil.getBitcoinAddress("bitcoin:12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu?amount=1.2&message=Payment&label=Satoshi&extra=other-param"));
    }

    @Test
    public void getBitcoinAmount() throws Exception {

        assertEquals("0.0000", FormatsUtil.getBitcoinAmount(null));
        assertEquals("0.0000", FormatsUtil.getBitcoinAmount(""));
        assertEquals("0.0000", FormatsUtil.getBitcoinAmount("Tobi Kadachi"));
        assertEquals("0.0000", FormatsUtil.getBitcoinAmount("bitcoin:?r=https://bitpay.com/i/AX146S9yK1ftUPmZGoNr9B"));
        assertEquals("0.0000", FormatsUtil.getBitcoinAmount("bitcoin:12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu"));
        assertEquals("120000000", FormatsUtil.getBitcoinAmount("bitcoin:12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu?amount=1.2"));
        assertEquals("120000000", FormatsUtil.getBitcoinAmount("bitcoin:12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu?amount=1.2&message=Payment&label=Satoshi&extra=other-param"));
    }

    @Test
    public void isBitcoinUri() throws Exception {

        assertFalse(FormatsUtil.isBitcoinUri(null));
        assertFalse(FormatsUtil.isBitcoinUri(""));
        assertFalse(FormatsUtil.isBitcoinUri("Tobi Kadachi"));
        assertTrue(FormatsUtil.isBitcoinUri("bitcoin:?r=https://bitpay.com/i/AX146S9yK1ftUPmZGoNr9B"));
        assertTrue(FormatsUtil.isBitcoinUri("bitcoin:12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu"));
        assertTrue(FormatsUtil.isBitcoinUri("bitcoin:12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu?amount=1.2"));
        assertTrue(FormatsUtil.isBitcoinUri("bitcoin:12A1MyfXbW6RhdRAZEqofac5jCQQjwEPBu?amount=1.2&message=Payment&label=Satoshi&extra=other-param"));
    }
}