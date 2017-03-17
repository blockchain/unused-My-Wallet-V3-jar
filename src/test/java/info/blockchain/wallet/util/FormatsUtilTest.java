package info.blockchain.wallet.util;

import org.junit.Assert;
import org.junit.Test;

public class FormatsUtilTest {

    @Test
    public void isEncrypted() throws Exception {

        Assert.assertTrue(!FormatsUtil.isKeyEncrypted(null));
        Assert.assertTrue(!FormatsUtil.isKeyEncrypted("3tcnfpTzY6G6oL4NujXkXJfpkEJr69fDSRESuA76izac"));

        Assert.assertTrue(FormatsUtil.isKeyEncrypted("51jTHC6+phVaDTqZOyldKRRqrZQiXm/IhTMAjM/G9eCVQJt6POLTsKQT29RlFH9vH2tbJaowM5firNiSiNNIPw=="));
        Assert.assertTrue(FormatsUtil.isKeyEncrypted("QQBIDa4SO84Uow1AlWo/1STqO2n5OXN6seU2eULjK/4ydHYW/LRTmBQT3eyIgdYCnNtJ1QBSatZ/9d4oNbkH0pmPeZEd+4Sekz9zoqfJs35k0kt7R3De+L6cqYymLpQJLELZwlP78SmWnlC31pCAB/lklBXwlv9xcSRq9qO9sLk="));
    }

    @Test
    public void isUnencrypted() throws Exception {

        Assert.assertTrue(!FormatsUtil.isKeyUnencrypted(null));
        Assert.assertTrue(!FormatsUtil.isKeyUnencrypted("51jTHC6+phVaDTqZOyldKRRqrZQiXm/IhTMAjM/G9eCVQJt6POLTsKQT29RlFH9vH2tbJaowM5firNiSiNNIPw=="));
        Assert.assertTrue(!FormatsUtil.isKeyUnencrypted("QQBIDa4SO84Uow1AlWo/1STqO2n5OXN6seU2eULjK/4ydHYW/LRTmBQT3eyIgdYCnNtJ1QBSatZ/9d4oNbkH0pmPeZEd+4Sekz9zoqfJs35k0kt7R3De+L6cqYymLpQJLELZwlP78SmWnlC31pCAB/lklBXwlv9xcSRq9qO9sLk="));
        Assert.assertTrue(FormatsUtil.isKeyUnencrypted("3tcnfpTzY6G6oL4NujXkXJfpkEJr69fDSRESuA76izac"));
    }
}