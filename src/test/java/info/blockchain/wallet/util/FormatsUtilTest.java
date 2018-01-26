package info.blockchain.wallet.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FormatsUtilTest {

    @Test
    public void isEncrypted() throws Exception {

        assertTrue(!FormatsUtil.isKeyEncrypted(null));
        assertTrue(!FormatsUtil.isKeyEncrypted("3tcnfpTzY6G6oL4NujXkXJfpkEJr69fDSRESuA76izac"));

        assertTrue(FormatsUtil.isKeyEncrypted("51jTHC6+phVaDTqZOyldKRRqrZQiXm/IhTMAjM/G9eCVQJt6POLTsKQT29RlFH9vH2tbJaowM5firNiSiNNIPw=="));
        assertTrue(FormatsUtil.isKeyEncrypted("QQBIDa4SO84Uow1AlWo/1STqO2n5OXN6seU2eULjK/4ydHYW/LRTmBQT3eyIgdYCnNtJ1QBSatZ/9d4oNbkH0pmPeZEd+4Sekz9zoqfJs35k0kt7R3De+L6cqYymLpQJLELZwlP78SmWnlC31pCAB/lklBXwlv9xcSRq9qO9sLk="));
    }

    @Test
    public void isUnencrypted() throws Exception {

        assertTrue(!FormatsUtil.isKeyUnencrypted(null));
        assertTrue(!FormatsUtil.isKeyUnencrypted("51jTHC6+phVaDTqZOyldKRRqrZQiXm/IhTMAjM/G9eCVQJt6POLTsKQT29RlFH9vH2tbJaowM5firNiSiNNIPw=="));
        assertTrue(!FormatsUtil.isKeyUnencrypted("QQBIDa4SO84Uow1AlWo/1STqO2n5OXN6seU2eULjK/4ydHYW/LRTmBQT3eyIgdYCnNtJ1QBSatZ/9d4oNbkH0pmPeZEd+4Sekz9zoqfJs35k0kt7R3De+L6cqYymLpQJLELZwlP78SmWnlC31pCAB/lklBXwlv9xcSRq9qO9sLk="));
        assertTrue(FormatsUtil.isKeyUnencrypted("3tcnfpTzY6G6oL4NujXkXJfpkEJr69fDSRESuA76izac"));
    }

    @Test
    public void isValidBitcoinCashAddress() {
        assertFalse(FormatsUtil.isValidBitcoinCashAddress(null));
        assertFalse(FormatsUtil.isValidBitcoinCashAddress(""));
        assertFalse(FormatsUtil.isValidBitcoinCashAddress("test string"));
        // Standard BTC address
        assertFalse(FormatsUtil.isValidBitcoinCashAddress("19dPodLBKT4Fpym4PJ3UfkoMBDiTGkHw2V"));
        // BECH32 Segwit BTC address
        assertFalse(FormatsUtil.isValidBitcoinCashAddress("3MG8XBSphrQg8HLkz51Y6vJVgtXV1R8qS6"));
        // Valid BECH32 BCH address
        assertTrue(FormatsUtil.isValidBitcoinCashAddress("bitcoincash:qp02xpzz9qq0u7mtefw028mtlkszshxxdv0xsgv8pc"));
        // Valid BECH32 BCH address but with single digit changed
        assertFalse(FormatsUtil.isValidBitcoinCashAddress("bitcoincash:qp02xpzz9qq0u7mtefw028mtlkszshxxdv0xsgv8pd"));
        // Valid BECH32 BCH address but with single digit missing
        assertFalse(FormatsUtil.isValidBitcoinCashAddress("bitcoincash:qp02xpzz9qq0u7mtefw028mtlkszshxxdv0xsgv8p"));
    }

}