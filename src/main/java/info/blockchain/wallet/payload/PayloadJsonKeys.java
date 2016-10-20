package info.blockchain.wallet.payload;

public interface PayloadJsonKeys {

    //Wallet - Stored remotely
    String KEY_WALLET_VERSION = "version";
    String KEY_WALLET_PAYLOAD = "payload";//encrypted payload
    String KEY_WALLET_PBKDF2_ITERATIONS = "pbkdf2_iterations";

    //Payload - unencrypted payload
    String KEY_PAYLOAD__GUID = "guid";
    String KEY_PAYLOAD__SHAREDKEY = "sharedKey";
    String KEY_PAYLOAD__DOUBLE_ENCRYPTION = "double_encryption";
    String KEY_PAYLOAD__DPASSWORDHASH = "dpasswordhash";
    String KEY_PAYLOAD__PBKDF2_ITERATIONS = "pbkdf2_iterations";
    String KEY_PAYLOAD__TX_NOTES = "tx_notes";
    String KEY_PAYLOAD__TX_TAGS = "tx_tags";
    String KEY_PAYLOAD__TAG_NAMES = "tag_names";
    String KEY_PAYLOAD__OPTION = "options";
    String KEY_PAYLOAD__WALLET_OPTIONS = "wallet_options";//some wallets might have this key in stead of 'options'
    String KEY_PAYLOAD__PAIDTO = "paidTo";
    String KEY_PAYLOAD__HD_WALLET = "hd_wallets";
    String KEY_PAYLOAD__LEGACY_KEYS = "keys";
    String KEY_PAYLOAD__ADDRESS_BOOK = "address_book";

    //Paid to - Currently parsed but not used
    String KEY_PAIDTO__EMAIL = "email";
    String KEY_PAIDTO__MOBILE = "mobile";
    String KEY_PAIDTO__REDEEMED_AT = "redeemedAt";
    String KEY_PAIDTO__ADDRESS = "address";

    //HD wallet
    String KEY_HD_WALLET__SEED_HEX = "seed_hex";
    String KEY_HD_WALLET__PASSPHRASE = "passphrase";
    String KEY_HD_WALLET__MNEMONIC_VERIFIED = "mnemonic_verified";
    String KEY_HD_WALLET__DEFAULT_ACCOUNT_INDEX = "default_account_idx";
    String KEY_HD_WALLET__ACCOUNTS = "accounts";
    String KEY_HD_WALLET__ARCHIVED = "archived";
    String KEY_HD_WALLET__LABEL = "label";
    String KEY_HD_WALLET__XPUB = "xpub";
    String KEY_HD_WALLET__XPRIV = "xpriv";
    String KEY_HD_WALLET__RECEIVE_ADDRESSES = "receive_addresses";
    String KEY_HD_WALLET__INDEX = "index";
    String KEY_HD_WALLET__AMOUNT = "amount";
    String KEY_HD_WALLET__PAID = "paid";
    String KEY_HD_WALLET__CANCELLED = "cancelled";
    String KEY_HD_WALLET__TAGS = "tags";
    String KEY_HD_WALLET__ADDRESS_LABELS = "address_labels";
    String KEY_HD_WALLET__CACHE = "cache";
    String KEY_HD_WALLET__RECEIVE_ACCOUNT = "receiveAccount";
    String KEY_HD_WALLET__CHANGE_ACCOUNT = "changeAccount";

    //Legacy Addresses
    String KEY_LEGACY_KEYS__ADDR = "addr";
    String KEY_LEGACY_KEYS__PRIV = "priv";
    String KEY_LEGACY_KEYS__LABEL = "label";
    String KEY_LEGACY_KEYS__CREATED_TIME = "created_time";
    String KEY_LEGACY_KEYS__TAG = "tag";
    String KEY_LEGACY_KEYS__CREATED_DEVICE_NAME = "created_device_name";
    String KEY_LEGACY_KEYS__CREATED_DEVICE_VERSION = "created_device_version";
    String KEY_LEGACY_KEYS__CHANGE_ADDRESS = "change_addresses";//?
    String KEY_LEGACY_KEYS__RECEIVE_ADDRESS = "receive_addresses";
    String KEY_LEGACY_KEYS__TAGS = "tags";//?
    String KEY_LEGACY_KEYS__AMOUNT = "amount";

    //Address Book
    String KEY_ADDRESS_BOOK__LABEL = "label";
    String KEY_ADDRESS_BOOK__ADDR = "addr";
}
