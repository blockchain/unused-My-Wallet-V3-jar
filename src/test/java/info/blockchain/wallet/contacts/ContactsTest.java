package info.blockchain.wallet.contacts;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.util.RestClient;
import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.exceptions.MetadataException;
import info.blockchain.wallet.exceptions.SharedMetadataException;
import info.blockchain.wallet.metadata.MockInterceptor;
import info.blockchain.wallet.util.MetadataUtil;

import org.bitcoinj.crypto.DeterministicKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class ContactsTest {

    MockInterceptor mockInterceptor;

    @Before
    public void setup() throws Exception {

        mockInterceptor = new MockInterceptor();

        //Set environment
        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {

                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(mockInterceptor)//Mock responses
                        .addInterceptor(loggingInterceptor)//Extensive logging
                        .build();
                return RestClient.getRetrofitInstance(okHttpClient);
            }

            @Override
            public Retrofit getRetrofitServerInstance() {
                return null;
            }
        });
    }

    private Wallet getWallet() throws Exception {

        return new WalletFactory().restoreWallet("15e23aa73d25994f1921a1256f93f72c",
                "",
                1);
    }

    private Contacts init() throws Exception{
        Wallet b_wallet = getWallet();
        DeterministicKey sharedMetaDataHDNode = MetadataUtil.deriveSharedMetadataNode(b_wallet.getMasterKey());
        DeterministicKey metaDataHDNode = MetadataUtil.deriveMetadataNode(b_wallet.getMasterKey());
        mockInterceptor.setResponseString("{\"version\":1,\"payload\":\"9SnGfbzzKWljjsI6KBpnWt2vnVFqdj5j60qdsBjI1MaQZgzLghgjYEKkoEw+wqN6rdkZARgX9/d9aLPEEKgFdrWAN6OzAVlQnYIePbdmpDBuretq4iol66veV9xAOtqegA/FVYSVeMVFLvdb455L5QWoNNDUQq6Yo11stbHc5eSfPIMp5x/ekG9tFIK4yvKGsluVkL8y3+fR9riY+NXBg0kVqyY4KLEfYPVUXmZG4f2+BXFZoSBx57HwVq0Ay8AWc1rNtghmWf05xJU1h368Xw==\",\"signature\":\"H0tvbP8JYD6bj5y6f/eRXxSE40wyZoziRRImC+U3EzeQElGsUSRKaHEGC/L5cp/BbuJWM7o+a9MNVStQGHG87jg=\",\"type_id\":4,\"created_at\":1482153702000,\"updated_at\":1482153702000,\"address\":\"1LF1QvtK6gnxJ3f8tZx9hamWS9jytKZJ6C\"}");
        return new Contacts(metaDataHDNode, sharedMetaDataHDNode);
    }

    @Test
    public void fetch() throws Exception {

        Contacts contacts = init();
        mockInterceptor.setResponseString("{\"payload\":\"9SnGfbzzKWljjsI6KBpnWt2vnVFqdj5j60qdsBjI1MaQZgzLghgjYEKkoEw+wqN6rdkZARgX9/d9aLPEEKgFdrWAN6OzAVlQnYIePbdmpDBuretq4iol66veV9xAOtqegA/FVYSVeMVFLvdb455L5QWoNNDUQq6Yo11stbHc5eSfPIMp5x/ekG9tFIK4yvKGsluVkL8y3+fR9riY+NXBg0kVqyY4KLEfYPVUXmZG4f2+BXFZoSBx57HwVq0Ay8AWc1rNtghmWf05xJU1h368Xw==\",\"version\":1,\"type_id\":4,\"signature\":\"H0tvbP8JYD6bj5y6f/eRXxSE40wyZoziRRImC+U3EzeQElGsUSRKaHEGC/L5cp/BbuJWM7o+a9MNVStQGHG87jg=\",\"created_at\":1482153702000,\"updated_at\":1482153702000,\"address\":\"1LF1QvtK6gnxJ3f8tZx9hamWS9jytKZJ6C\"}");
        contacts.fetch();
        Assert.assertEquals(contacts.getContactList().size(), 2);
    }

    @Test
    public void fetch_IOException() throws Exception {

        Contacts contacts = init();
        try {
            mockInterceptor.setIOException(true);
            contacts.fetch();
        } catch (MetadataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Assert.assertTrue(true);
            return;
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }finally {
            mockInterceptor.setIOException(false);
        }
        Assert.assertTrue("IOException not caught", false);
    }

    @Test
    public void save() throws Exception {
        Contacts contacts = init();
        Contact contact = new Contact();
        contact.setName("John");
        contacts.addContact(contact);

        contacts.save();
        Assert.assertTrue(true);
    }

    @Test
    public void save_IOException() throws Exception {
        Contacts contacts = init();
        Contact contact = new Contact();
        contact.setName("John");
        contacts.addContact(contact);

        mockInterceptor.setIOException(true);
        try {
            contacts.save();
        } catch (IOException e) {
            Assert.assertTrue(true);
            return;
        } catch (MetadataException e) {
            e.printStackTrace();
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }finally {
            mockInterceptor.setIOException(false);
        }
        Assert.assertTrue(false);
    }

    @Test
    public void wipe() throws Exception {
        Contacts contacts = init();
        Contact contact = new Contact();
        contact.setName("John");
        contacts.addContact(contact);
        contacts.wipe();
        Assert.assertTrue(contacts.getContactList().size() == 0);
    }

    @Test
    public void wipe_IOException() throws Exception {
        Contacts contacts = init();
        Contact contact = new Contact();
        contact.setName("John");
        contacts.addContact(contact);

        mockInterceptor.setIOException(true);
        try {
            contacts.wipe();
        } catch (IOException e) {
            Assert.assertTrue(true);
            return;
        } catch (MetadataException e) {
            e.printStackTrace();
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }finally {
            mockInterceptor.setIOException(false);
        }
        Assert.assertTrue(false);
    }

    @Test
    public void publishXpub() throws Exception {
        Contacts contacts = init();
        try {
            contacts.publishXpub();
        } catch (MetadataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(true);
    }

    @Test
    public void publishXpub_IOException() throws Exception {
        Contacts contacts = init();
        try {
            mockInterceptor.setIOException(true);
            contacts.publishXpub();
        } catch (MetadataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Assert.assertTrue(true);
            return;
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }finally {
            mockInterceptor.setIOException(false);
        }
        Assert.assertTrue(false);
    }

    @Test
    public void fetchXpub() throws Exception {
        Contacts contacts = init();
        mockInterceptor.setResponseString("{\"payload\":\"eyJ4cHViIjoieHB1YjY4aGpMM01rdmZ6S1pSdXZmQUFBZWFiYUFuWmpnWXFVM0ZTbTFMRUNxRGNhVHI1N013YzREY2lHcTJKQnJyVG9zOHNuUHg3OG1MdEt4dGFQSzJMcWJoVTVnMW9QRk1hR29uRTI3a0g4S0dBIn0=\",\"version\":1,\"type_id\":4,\"signature\":\"IE2zczTK0sRPLRu/vfbM3v6S7gAIh2o+UQxkn1P4uUT+KqCU+P8kVEt7SLjixIQqSb4UzlKNKirXUBiNWGU4Ygg=\",\"prev_magic_hash\":\"8f122f88cad5faedcc3433dbf0618cea17a5682da2c3dfdf36d03d63f88a90c6\",\"created_at\":1482242589000,\"updated_at\":1482244810000,\"address\":\"16uJDcPbvegnJUhgXr5TW9nd9wbJYNWBAd\"}");
        String xpub = contacts.fetchXpub("16uJDcPbvegnJUhgXr5TW9nd9wbJYNWBAd");
        Assert.assertTrue(xpub.equals("xpub68hjL3MkvfzKZRuvfAAAeabaAnZjgYqU3FSm1LECqDcaTr57Mwc4DciGq2JBrrTos8snPx78mLtKxtaPK2LqbhU5g1oPFMaGonE27kH8KGA"));
    }

    @Test
    public void fetchXpub_IOException() throws Exception {
        Contacts contacts = init();
        mockInterceptor.setResponseString("{\"payload\":\"eyJ4cHViIjoieHB1YjY4aGpMM01rdmZ6S1pSdXZmQUFBZWFiYUFuWmpnWXFVM0ZTbTFMRUNxRGNhVHI1N013YzREY2lHcTJKQnJyVG9zOHNuUHg3OG1MdEt4dGFQSzJMcWJoVTVnMW9QRk1hR29uRTI3a0g4S0dBIn0=\",\"version\":1,\"type_id\":4,\"signature\":\"IE2zczTK0sRPLRu/vfbM3v6S7gAIh2o+UQxkn1P4uUT+KqCU+P8kVEt7SLjixIQqSb4UzlKNKirXUBiNWGU4Ygg=\",\"prev_magic_hash\":\"8f122f88cad5faedcc3433dbf0618cea17a5682da2c3dfdf36d03d63f88a90c6\",\"created_at\":1482242589000,\"updated_at\":1482244810000,\"address\":\"16uJDcPbvegnJUhgXr5TW9nd9wbJYNWBAd\"}");
        String xpub = null;
        try {
            mockInterceptor.setIOException(true);
            xpub = contacts.fetchXpub("16uJDcPbvegnJUhgXr5TW9nd9wbJYNWBAd");
        } catch (MetadataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Assert.assertTrue(true);
            return;
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }finally {
            mockInterceptor.setIOException(false);
        }
        Assert.assertTrue(false);
    }

    @Test
    public void createInvitation() throws Exception {
        Contacts contacts = init();
        Contact me = new Contact();
        me.setName("Me");
        Contact him = new Contact();
        him.setName("Him");
        mockInterceptor.setResponseString("{\"nonce\":\"yzlbDB+JbpUr7nJuGc9Y4C70C6Y=\",\"mdid\":\"1borrXJLeFgwF1aKS3io9c3rQ1uXHf1s5\",\"signature\":\"H00kjYWQ5rsT0+693EoL5r7DZObJxrxng3O7hs6bLL3HFGzPWEwBRs6Gy0Wi5aE8FRwW8HAQSwtZ7Zygwelih54=\"}");
        Contact myInvite = contacts.createInvitation(me, him);

        Assert.assertTrue(contacts.getContactList().get(0).getName().equals("Him"));
        Assert.assertTrue(myInvite.getName().equals("Me"));
        Assert.assertTrue(myInvite.getOutgoingInvitation().getMdid().equals("1borrXJLeFgwF1aKS3io9c3rQ1uXHf1s5"));
    }

    @Test
    public void createInvitation_IOException() throws Exception {
        Contacts contacts = init();
        Contact me = new Contact();
        me.setName("Me");
        Contact him = new Contact();
        him.setName("Him");
        mockInterceptor.setResponseString("{\"nonce\":\"yzlbDB+JbpUr7nJuGc9Y4C70C6Y=\",\"mdid\":\"1borrXJLeFgwF1aKS3io9c3rQ1uXHf1s5\",\"signature\":\"H00kjYWQ5rsT0+693EoL5r7DZObJxrxng3O7hs6bLL3HFGzPWEwBRs6Gy0Wi5aE8FRwW8HAQSwtZ7Zygwelih54=\"}");
        Contact myInvite = null;
        try {
            mockInterceptor.setIOException(true);
            myInvite = contacts.createInvitation(me, him);
        } catch (IOException e) {
            Assert.assertTrue(true);
            return;
        } catch (SharedMetadataException e) {
            e.printStackTrace();
        }finally {
            mockInterceptor.setIOException(false);
        }

        Assert.assertTrue(contacts.getContactList().get(0).getName().equals("Him"));
        Assert.assertTrue(myInvite.getName().equals("Me"));
        Assert.assertTrue(myInvite.getOutgoingInvitation().getMdid().equals("1borrXJLeFgwF1aKS3io9c3rQ1uXHf1s5"));
    }

    @Test
    public void readInvitationLink() throws Exception {
        Contacts contacts = init();
        Contact receivedInvite = contacts.readInvitationLink("http://blockchain.info/invite?id=852bb6796c2aefb7ea96131b785da397dca9cb3bee5df4ea7c937493613e9c37&name=Me");

        Assert.assertTrue(receivedInvite.getName().equals("Me"));
        Assert.assertTrue(receivedInvite.getId().equals("852bb6796c2aefb7ea96131b785da397dca9cb3bee5df4ea7c937493613e9c37"));
    }
}