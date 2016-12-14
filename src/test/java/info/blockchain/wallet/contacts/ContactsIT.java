package info.blockchain.wallet.contacts;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.api.PersistentUrls;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.util.RestClient;
import info.blockchain.wallet.contacts.data.Contact;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Integration Test
 */
@Ignore
public class ContactsIT {

    @Before
    public void setup() throws Exception {

        //Set environment
        PersistentUrls.getInstance().setCurrentEnvironment(PersistentUrls.Environment.DEV);
        PersistentUrls.getInstance().setCurrentApiUrl("https://api.dev.blockchain.info/");
        PersistentUrls.getInstance().setCurrentServerUrl("https://explorer.dev.blockchain.info/");

        BlockchainFramework.init(new FrameworkInterface() {
            @Override
            public Retrofit getRetrofitApiInstance() {

                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                        .addInterceptor(loggingInterceptor)//Extensive logging
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

    @Test
    public void testIntegration() throws Exception {

        Contacts contacts = new Contacts(getWallet().getMasterKey());
        contacts.wipe();
        contacts.fetch();
        Assert.assertTrue(contacts.getContacts().size() == 0);

        List<Contact> contactList = new ArrayList<>();
        Contact contact = new Contact();
        contact.setSurname("Doe");
        contact.setName("John");
        contact.setCompany("Blockchain");
        contactList.add(contact);

        contact = new Contact();
        contact.setSurname("Clark");
        contact.setName("Bill");
        contact.setCompany("Blockchain");
        contactList.add(contact);

        contacts.setContacts(contactList);
        Assert.assertTrue(contacts.getContacts().size() == 2);

        contacts.save();

        contacts = new Contacts(getWallet().getMasterKey());
        contacts.fetch();
        Assert.assertTrue(contacts.getContacts().size() == 2);

        Assert.assertEquals(contacts.getContacts().get(0).getName(), "John");
        Assert.assertEquals(contacts.getContacts().get(0).getSurname(), "Doe");
        Assert.assertEquals(contacts.getContacts().get(0).getCompany(), "Blockchain");
        Assert.assertEquals(contacts.getContacts().get(1).getName(), "Bill");
        Assert.assertEquals(contacts.getContacts().get(1).getSurname(), "Clark");
        Assert.assertEquals(contacts.getContacts().get(1).getCompany(), "Blockchain");

        contacts.wipe();

        contacts = new Contacts(getWallet().getMasterKey());
        contacts.fetch();
        Assert.assertTrue(contacts.getContacts().size() == 0);

    }
}