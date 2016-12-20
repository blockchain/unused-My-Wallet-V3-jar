package info.blockchain.wallet.contacts;

import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.util.RestClient;
import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.exceptions.MetadataException;
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
    OkHttpClient okHttpClient;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {

        mockInterceptor = new MockInterceptor();

        //Set environment
        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {

                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                okHttpClient = new OkHttpClient.Builder()
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
            mockInterceptor.setIOException(false);
            return;
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
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
    public void wipe() throws Exception {
        Contacts contacts = init();
        contacts.wipe();
        Assert.assertTrue(true);
    }

    @Test
    public void invalidateToken() throws Exception {
        Contacts contacts = init();
        contacts.invalidateToken();
    }

    @Test
    public void authorize() throws Exception {

    }

    @Test
    public void getContactList() throws Exception {

    }

    @Test
    public void setContactList() throws Exception {

    }

    @Test
    public void addContact() throws Exception {

    }

    @Test
    public void publishXpub() throws Exception {

    }

    @Test
    public void fetchXpub() throws Exception {

    }

    @Test
    public void createInvitation() throws Exception {

    }

    @Test
    public void readInvitationLink() throws Exception {

    }

    @Test
    public void acceptInvitationLink() throws Exception {

    }

    @Test
    public void readInvitationSent() throws Exception {

    }

    @Test
    public void addTrusted() throws Exception {

    }

    @Test
    public void deleteTrusted() throws Exception {

    }

    @Test
    public void sendMessage() throws Exception {

    }

    @Test
    public void getMessages() throws Exception {

    }

    @Test
    public void readMessage() throws Exception {

    }

    @Test
    public void markMessageAsRead() throws Exception {

    }

    @Test
    public void decryptMessageFrom() throws Exception {

    }

    @Test
    public void sendPaymentRequest() throws Exception {

    }

    @Test
    public void getPaymentRequests() throws Exception {

    }

    @Test
    public void getPaymentRequestResponses() throws Exception {

    }

    @Test
    public void acceptPaymentRequest() throws Exception {

    }

}