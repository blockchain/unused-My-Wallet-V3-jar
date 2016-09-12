package info.blockchain.api;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class AccessTest {

    final String guid = "a09910d9-1906-4ea1-a956-2508c3fe0661";

    @Test
    public void getEncryptedPayload_whenBadString_shouldPass() throws Exception {

        WalletPayload access = new WalletPayload();
        String sessionId = access.getSessionId(guid);
        String payload = access.getEncryptedPayload(guid, sessionId);
        assertThat("Encrypted payload should not be null", payload != null);
    }
}
