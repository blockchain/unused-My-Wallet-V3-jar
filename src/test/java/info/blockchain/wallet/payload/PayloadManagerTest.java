package info.blockchain.wallet.payload;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by riaanvos on 07/07/16.
 */
public class PayloadManagerTest {

    @Test
    public void newWallet_shouldPass() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();

        String label = "Account 1";
        String passphrase = "passphrase";

        Payload payload = payloadManager.createHDWallet(passphrase, label);

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getAccounts().get(0).getLabel(), is(label));
        assertThat(payload, is(payloadManager.getPayload()));
        assertThat("Checksum should not be null", payloadManager.getCheckSum() != null);

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void restoreWallet_withMnemonic_shouldPass() throws Exception {

        PayloadManager payloadManager = PayloadManager.getInstance();

        String mnemonic = "all all all all all all all all all all all all";

        String seedHex = "0660cc198330660cc198330660cc1983";//All all ...
        String xpub1 = "xpub6BiVtCpG9fQPxnPmHXG8PhtzQdWC2Su4qWu6XW9tpWFYhxydCLJGrWBJZ5H6qTAHdPQ7pQhtpjiYZVZARo14qHiay2fvrX996oEP42u8wZy";
        String xpub2 = "xpub6BiVtCpG9fQQ1EW99bMSYwySbPWvzTFRQZCFgTmV3samLSZAYU7C3f4Je9vkNh7h1GAWi5Fn93BwoGBy9EAXbWTTgTnVKAbthHpxM1fXVRL";
        String xpub3 = "xpub6BiVtCpG9fQQ4xJHzNkdmqspAeMdBTDFZ2kYM39RzDYMAcb4wtkWZNSu7k3BbJgoPgTzx62G69mBiUjDnD3EJrTA5ZYZg4vfz1YWcGBnX2x";
        String xpub4 = "xpub6BiVtCpG9fQQ77Qr7WArXSG3yWYm2bkRYpoSYtRkVEAk5nrcULBG8AeRYMMKVUXAsNeXdR7TGuL6SkUc4RF2YC7X4afLyZrT9NrrUFyotkH";
        String xpub5 = "xpub6BiVtCpG9fQQ8pVjVF7jm3kLahkNbQRkWGUvzsKQpXWYvhYD4d4UDADxZUL4xp9UwsDT5YgwNKofTWRtwJgnHkbNxuzLDho4mxfS9KLesGP";

        Payload payload = payloadManager.restoreHDWallet(mnemonic, "", "");

        assertThat(payload.getGuid().length(), is(36));//GUIDs are 36 in length
        assertThat(payload.getHdWallet().getSeedHex(), is(seedHex));

        assertThat(payload.getHdWallet().getAccounts().get(0).getXpub(), is(xpub1));
        assertThat(payload.getHdWallet().getAccounts().get(0).getXpriv().substring(4), is("9xj9UhHNKHr6kJKJBVj82ZxFrbfhczBDUHyVj7kHGAiZqAeUenz2JhrphnMMYVKcWcVPFJESngtKsVa4FYEvFfWUTtZThCoZdwDeS9qQnqm"));

        payloadManager.addAccount("",null);
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpub(), is(xpub2));
        assertThat(payload.getHdWallet().getAccounts().get(1).getXpriv().substring(4), is("9xj9UhHNKHr6nkRg3ZpSBp2i3MgSazXa3LGet5MsVY3nTeE1zvnwVrjpnsJGEtEvvcm8fwoUBVpnHcioJfFqRUaZ6ijXEuwUuv2Q5RM6dGR"));

        payloadManager.addAccount("",null);
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpub(), is(xpub3));
        assertThat(payload.getHdWallet().getAccounts().get(2).getXpriv().substring(4), is("9xj9UhHNKHr6rUDptMDdQhw5ccX8mzVQBopwYejpRt1NHpFvQMSG1a8RGRJjZRE8rRJJ6N9g1GcB6yWEgkXCzGBweq934jS9LfBuViQRxRw"));

        payloadManager.addAccount("",null);
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpub(), is(xpub4));
        assertThat(payload.getHdWallet().getAccounts().get(3).getXpriv().substring(4), is("9xj9UhHNKHr6tdLP1UdrAJKKRUiGd92aBbsqkW28vtdmCzXTvns1aNKwh5uM1nSbdD8Y4x9VBnTLrDDEbREnu9KYnDyvt8QRPtPWQ78UgAG"));

        payloadManager.addAccount("",null);
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpub(), is(xpub5));
        assertThat(payload.getHdWallet().getAccounts().get(4).getXpriv().substring(4), is("9xj9UhHNKHr6vLRGPDajPuoc2futBwhu93ZLCUuoGBya3uD4X5kDfMuUiEHz7HPWPpkgCHiwNbLWjxa6QrqfjmPmVr146GUt8D5shiXkQpC"));

        PayloadManager.getInstance().wipe();
    }

    @Test
    public void restoreWallet_withMnemonic_shouldContainCorrectReceiveAddresses() throws Exception{

        PayloadManager payloadManager = PayloadManager.getInstance();

        String mnemonic = "pen news pluck gaze suit moon pride potato senior keep patient ensure";
        payloadManager.restoreHDWallet(mnemonic, "", "");

        assertThat(payloadManager.getReceiveAddress(0), is("11LjwroZHsKvWiJqbJezQpnTjY7g7oup3"));
        assertThat(payloadManager.getChangeAddress(0), is("167WqTeExjQJH54k3ZpjCnUaf1fuGzbk5r"));

        payloadManager.addAccount("",null);
        assertThat(payloadManager.getReceiveAddress(1), is("17b3y6mxmU7ZTNAWx33384ytQr3jH9tV2n"));
        assertThat(payloadManager.getChangeAddress(1), is("1Kk4MiERVf9ZL2uAKbvpPN1Fa5hrSPiXy4"));

        payloadManager.addAccount("",null);
        assertThat(payloadManager.getReceiveAddress(2), is("18pEjskhpR7pZn1Rh4UrUWHMFusvfMsymX"));
        assertThat(payloadManager.getChangeAddress(2), is("1BW6kawa8EEKpyFeN6QGiXTzxs2Mi7h137"));

        payloadManager.addAccount("",null);
        assertThat(payloadManager.getReceiveAddress(3), is("17te2ij1Lhm8YuMJoSsVFVDkSpQYzCyRF6"));
        assertThat(payloadManager.getChangeAddress(3), is("1Fzsc6beQYu6qXrRXbrhR3zpFruhcygsxU"));
    }
}
