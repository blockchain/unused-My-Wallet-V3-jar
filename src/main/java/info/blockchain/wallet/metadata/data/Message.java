package info.blockchain.wallet.metadata.data;

public class Message {

    String payload;//base64 encoded
    String signature;
    String recipient;
    String id;
    String sender;
    long sent;
    boolean processed;
    boolean notified;
    int type;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "payload='" + payload + '\'' +
                ", signature='" + signature + '\'' +
                ", recipient='" + recipient + '\'' +
                ", id='" + id + '\'' +
                ", sender='" + sender + '\'' +
                ", sent=" + sent +
                ", processed=" + processed +
                ", notified=" + notified +
                ", type=" + type +
                '}';
    }
}
