package info.blockchain.wallet.contacts;

import info.blockchain.BlockchainFramework;
import info.blockchain.FrameworkInterface;
import info.blockchain.api.PersistentUrls;
import info.blockchain.bip44.Wallet;
import info.blockchain.bip44.WalletFactory;
import info.blockchain.util.RestClient;
import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.metadata.data.Message;
import info.blockchain.wallet.util.MetadataUtil;

import org.bitcoinj.crypto.DeterministicKey;
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

    //dev wallets
    private String wallet_A_guid = "014fb9fc-64f9-4cf5-b76b-d927d7619717";
    private String wallet_A_sharedKey = "bc73239b-d3d9-4bee-a1f9-80248e179486";
    private String wallet_A_seedHex = "20e3939d08ddf727f34a130704cd925e";

    private String wallet_B_guid = "6fbe154a-35e0-46fb-a22b-699dc7cba87c";
    private String wallet_B_sharedKey = "49e58bdb-5a66-4353-923a-3b49054603d6";
    private String wallet_B_seedHex = "b88d0d894c19ad1d8e7f1563b7455f7c";

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
    public void testMetadataIntegration() throws Exception {

        DeterministicKey sharedMetaDataHDNode = MetadataUtil.deriveSharedMetadataNode(getWallet().getMasterKey());
        DeterministicKey metaDataHDNode = MetadataUtil.deriveMetadataNode(getWallet().getMasterKey());

        Contacts contacts = new Contacts(metaDataHDNode, sharedMetaDataHDNode);
        contacts.wipe();
        contacts.fetch();
        Assert.assertTrue(contacts.getContactList().size() == 0);

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

        contacts.setContactList(contactList);
        Assert.assertTrue(contacts.getContactList().size() == 2);

        contacts.save();

        contacts = new Contacts(metaDataHDNode, sharedMetaDataHDNode);
        contacts.fetch();
        Assert.assertTrue(contacts.getContactList().size() == 2);

        Assert.assertEquals(contacts.getContactList().get(0).getName(), "John");
        Assert.assertEquals(contacts.getContactList().get(0).getSurname(), "Doe");
        Assert.assertEquals(contacts.getContactList().get(0).getCompany(), "Blockchain");
        Assert.assertEquals(contacts.getContactList().get(1).getName(), "Bill");
        Assert.assertEquals(contacts.getContactList().get(1).getSurname(), "Clark");
        Assert.assertEquals(contacts.getContactList().get(1).getCompany(), "Blockchain");

        contacts.wipe();

        contacts = new Contacts(metaDataHDNode, sharedMetaDataHDNode);
        contacts.fetch();
        Assert.assertTrue(contacts.getContactList().size() == 0);

    }

    @Test
    public void testSharedMetadataIntegration() throws Exception {

        /*
        Create wallets
         */
        Wallet a_wallet = setupWallet(wallet_A_seedHex, wallet_A_guid, wallet_A_sharedKey);
        DeterministicKey sharedMetaDataHDNode = MetadataUtil.deriveSharedMetadataNode(a_wallet.getMasterKey());
        DeterministicKey metaDataHDNode = MetadataUtil.deriveMetadataNode(a_wallet.getMasterKey());
        Contacts a_contacts = new Contacts(metaDataHDNode, sharedMetaDataHDNode);
        a_contacts.fetch();

        Wallet b_wallet = setupWallet(wallet_B_seedHex, wallet_B_guid, wallet_B_sharedKey);
        sharedMetaDataHDNode = MetadataUtil.deriveSharedMetadataNode(b_wallet.getMasterKey());
        metaDataHDNode = MetadataUtil.deriveMetadataNode(b_wallet.getMasterKey());
        Contacts b_contacts = new Contacts(metaDataHDNode, sharedMetaDataHDNode);
        b_contacts.fetch();

        /*
        Pair
         */
        System.out.println("\n--Sender--");
        Contact me = new Contact();
        me.setName("Riaan");
        Contact him = new Contact();
        him.setName("Jaume");
        Contact myInvite = a_contacts.createInvitation(me, him);
        String oneTimeUri = myInvite.createURI();
        System.out.println("createInvitation: " + oneTimeUri);

        System.out.println("\n--Recipient--");
        Contact senderDetails = b_contacts.acceptInvitationLink(oneTimeUri);
        System.out.println("accept Invitation: " + senderDetails.toJson());

        System.out.println("\n--Sender--");
        boolean accepted = a_contacts.readInvitationSent(myInvite);
        System.out.println("Accepted: " + accepted);
        a_contacts.sendMessage(myInvite.getMdid(), "Hey hey", 66, true);

        /*
        Already in trusted contact list
         */
//      Contact senderDetails = a_contacts.getContactList().get...


        /*
        Send messages
         */
        System.out.println("\n--Recipient--");
        List<Message> messages = b_contacts.getMessages(true);
        System.out.println("Received messages: '" + messages.size() + "'");

        Message message = messages.get(messages.size() - 1);
        b_contacts.readMessage(message.getId());
        System.out.println("ECDH Encrypted: " + message.getPayload());
        System.out.println("Decrypted: " + b_contacts.decryptMessageFrom(message, senderDetails.getMdid()).getPayload());
        b_contacts.markMessageAsRead(message.getId(), true);
    }

    private Wallet setupWallet(String hex, String guid, String sharedKey) throws Exception {
        System.out.println("\n--Start Wallet " + guid + "--");
        Wallet wallet = new WalletFactory().restoreWallet(hex, "", 1);
        //If you want notifications to fire register mdid
        //PayloadManager.getInstance().registerMdid(guid, sharedKey, sharedMetadata.getNode());
        return wallet;
    }

    @Test
    public void testProc() throws Exception {

        Wallet b_wallet = setupWallet(wallet_B_seedHex, wallet_B_guid, wallet_B_sharedKey);
        DeterministicKey sharedMetaDataHDNode = MetadataUtil.deriveSharedMetadataNode(b_wallet.getMasterKey());
        DeterministicKey metaDataHDNode = MetadataUtil.deriveMetadataNode(b_wallet.getMasterKey());
        Contacts b_contacts = new Contacts(metaDataHDNode, sharedMetaDataHDNode);
        b_contacts.fetch();

        List<Message> messages = b_contacts.getMessages(true);

        for(int i = 0; i < messages.size(); i++){
            Message message = messages.get(i);
            b_contacts.markMessageAsRead(message.getId(), true);
        }

        messages = b_contacts.getMessages(true);
        Assert.assertTrue(messages.size() == 0);
    }
}