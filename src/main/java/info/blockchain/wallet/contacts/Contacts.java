package info.blockchain.wallet.contacts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.blockchain.wallet.contacts.data.Contact;
import info.blockchain.wallet.metadata.Metadata;

import org.bitcoinj.crypto.DeterministicKey;

import java.util.ArrayList;
import java.util.List;

public class Contacts {

    private final static int METADATA_TYPE_EXTERNAL = 4;
    private Metadata metadata;
    private List<Contact> contacts;
    private ObjectMapper mapper = new ObjectMapper();

    public Contacts(DeterministicKey masterKey) throws Exception {

        metadata = new Metadata.Builder(masterKey, METADATA_TYPE_EXTERNAL)
                .build();

        contacts = new ArrayList<>();
    }

    public void fetch() throws Exception {

        String data = metadata.getMetadata();
        if(data != null) {
            contacts = mapper.readValue(data, new TypeReference<List<Contact>>(){});
        } else {
            contacts = new ArrayList<>();
        }
    }

    public void save() throws Exception {

        if(contacts != null) {
            metadata.putMetadata(mapper.writeValueAsString(contacts));
        }
    }

    public void wipe() throws Exception {
        metadata.putMetadata(mapper.writeValueAsString(new ArrayList<Contact>()));
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts){
        this.contacts = contacts;
    }

    public void add(Contact contact){
        contacts.add(contact);
    }
}
