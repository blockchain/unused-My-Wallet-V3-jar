package info.blockchain.wallet.payload;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by riaanvos on 27/07/16.
 */
public class LegacyAddressTest {

    @Test
    public void generateLegacyAddress_address_shouldMatchECKeyAddress() throws Exception {

        String device = "device";
        String version = "version";

        LegacyAddress legacyAddress = PayloadManager.getInstance().generateLegacyAddress(device, version, null);

        assertThat(legacyAddress.getCreatedDeviceName(), is(device));
        assertThat(legacyAddress.getCreatedDeviceVersion(), is(version));

        ECKey ecKey = legacyAddress.getECKey();
        String address = ecKey.toAddress(MainNetParams.get()).toString();
        assertThat(address, is(legacyAddress.getAddress()));
    }
}
