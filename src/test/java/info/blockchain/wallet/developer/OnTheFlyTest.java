package info.blockchain.wallet.developer;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OnTheFlyTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void onTheFly() throws Exception {
        assertThat("test", is("test"));
    }
}