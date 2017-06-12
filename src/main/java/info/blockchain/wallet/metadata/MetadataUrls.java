package info.blockchain.wallet.metadata;

final class MetadataUrls {

    private MetadataUrls() {
        throw new UnsupportedOperationException("You can't implement this class");
    }

    /* Base endpoint for Contacts/Shared Metadata/CryptoMatrix */
    private static final String IWCS = "iwcs";

    /* Base endpoint for generic Metadata */
    static final String METADATA = "metadata";

    /* Complete paths */
    static final String AUTH = IWCS + "/auth";
    static final String TRUSTED = IWCS + "/trusted";
    static final String MESSAGE = IWCS + "/message";
    static final String MESSAGES = IWCS + "/messages";
    static final String SHARE = IWCS + "/share";

}
